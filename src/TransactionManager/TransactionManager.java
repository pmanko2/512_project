package TransactionManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.transaction.InvalidTransactionException;

import LockManager.LockManager;
import ResImpl.Trace;
import ResInterface.ResourceManager;

/**
 * This class is responsible for organizing all transactions in the system. It handles starting, committing, and aborting
 * transactions.
 * @author nic
 *
 */
public class TransactionManager {

	//this table manages all transaction objects, mapped by id
	private Hashtable<String, Transaction> transaction_table;
	//TODO Write transaction_table to disk
	private static int transaction_id_counter;
	private static LockManager lm;
	private static ScheduledExecutorService scheduler;
	private static Hashtable<String, ScheduledFuture<Boolean>> scheduledFutures;
	private long secondsToLive;
	//TODO will this need to be reloaded when MW/TM are recovered since it won't be read in?
	private static ResourceManager middleware;
	//private static ResourceManager
	private static ResourceManager flights;
	private static ResourceManager cars;
	private static ResourceManager hotels;

	public TransactionManager(ResourceManager f, ResourceManager c,
			ResourceManager h, ResourceManager mw)
	{
		transaction_table = new Hashtable<String, Transaction>();
		lm = new LockManager();
		
		//set up scheduler object
		//TODO need to shut this down on exit
		scheduler = Executors.newScheduledThreadPool(1000);
		scheduledFutures = new Hashtable<String, ScheduledFuture<Boolean>>();
		secondsToLive = 300;
		flights = f;
		cars = c;
		hotels = h;
		middleware = mw;
		transaction_id_counter = 0;
	}
	
	/**
	 * Synchronized method that starts a new transaction
	 * @return Transaction ID
	 * @throws RemoteException 
	 */
	public synchronized int start() throws RemoteException
	{
		Transaction t = new Transaction(transaction_id_counter, lm);
		int to_return = t.getID();
		transaction_id_counter++;
		transaction_table.put("" + to_return, t);
		Trace.info("Transaction.start(): creating new transaction with ID: " + to_return);
		
		//start TIL timer for this transaction
		TransactionTimer tt = new TransactionTimer(to_return, this);
		ScheduledFuture<Boolean> scheduledFuture = scheduler.schedule(tt, secondsToLive, TimeUnit.SECONDS);
		scheduledFutures.put("" + to_return, scheduledFuture);
		
		//flush transactions to disk
		middleware.flushToDisk();
		
		return to_return;
	}
	
	public synchronized boolean commit(int transaction_id) throws TransactionAbortedException, RemoteException
	{
		Transaction t = transaction_table.get("" + transaction_id);
		if (t == null)
		{
			throw new TransactionAbortedException();
		}
		boolean return_value = t.commit();
		transaction_table.remove("" + transaction_id);
		scheduledFutures.get("" + transaction_id).cancel(false);
		scheduledFutures.remove("" + transaction_id);
		middleware.flushToDisk();
		return return_value;
	}
	
	public void abort(int transaction_id) throws TransactionAbortedException, RemoteException
	{
		Transaction t = transaction_table.get("" + transaction_id);
		if (t == null)
		{
			throw new TransactionAbortedException();
		}
		t.abort();
		transaction_table.remove("" + transaction_id);
		scheduledFutures.get("" + transaction_id).cancel(false);
		scheduledFutures.remove("" + transaction_id);
		middleware.flushToDisk();
	}
	
	/**
	 * Method to initiate voting phase of two phase commit
	 * @param transactionID
	 * @return
	 */
	public boolean prepare(int transactionID) throws RemoteException, TransactionAbortedException, InvalidTransactionException
	{
		boolean allYes = transaction_table.get("" + transactionID).startVotingProcess();
		
		if(allYes)
		{
			Trace.info("Voting process returned all YES. Committing transaction");
			//crash("flights", transaction_table.get("" + transactionID));
			return this.commit(transactionID);
		}
		else
		{
			Trace.info("Voting process returned at least one NO. Aborting transaction");
			this.abort(transactionID);
			return false;
		}
	}
	
	/**
	 * Method used to add an operation to a transaction
	 * @param transaction_id
	 * @return
	 * @throws RemoteException 
	 */
	public boolean addOperation(int transaction_id, ResourceManager r, OP_CODE op, Hashtable<String, Object> args, ArrayList<String> keys) throws RemoteException
	{
		boolean to_return = transaction_table.get("" + transaction_id).addOperation(r, op, args, keys);
		middleware.flushToDisk();
		//TODO should we flush to disk here? 
		Trace.info("TransactionTable size: " + transaction_table.size());
		return to_return;
	}
	
	public int addOperationIntReturn(int transaction_id, ResourceManager r, OP_CODE op, Hashtable<String, Object> args, ArrayList<String> keys) throws RemoteException
	{
		int to_return = transaction_table.get("" + transaction_id).addOperationIntReturn(r, op, args, keys);
		middleware.flushToDisk();
		return to_return;
	}

	public String addOperationStringReturn(int transaction_id, ResourceManager r, OP_CODE op, Hashtable<String, Object> args, ArrayList<String> keys) throws RemoteException
	{
		String to_return = transaction_table.get("" + transaction_id).addOperationStringReturn(r, op, args, keys);
		middleware.flushToDisk();
		return to_return;
	}
	
	private void crash(String which, Transaction transaction) throws RemoteException
	{
		transaction.crash(which);
	}
	
	/**
	 * Method called from MW when writing transaction data to disk
	 * @param file File path to transactionmanager data
	 * @throws IOException 
	 */
	public void flushToDisk(String file) throws IOException
	{
		//write TM data to disk
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		//write transaction table
		oos.writeObject(transaction_table);
		fos.close();
		oos.close();
	}
	
	/**
	 * Method called from MW when booting up in order to read transaction data from main memory
	 * @param file File path to transaction manager data
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public void readFromDisk(String file) throws ClassNotFoundException, IOException
	{
		Trace.info("Reading TransactionManager data back into main memory..."
				+ "\n" + file);

		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		transaction_table = (Hashtable<String, Transaction>) ois.readObject();
		Trace.info("transaction_table size: " + transaction_table.size());
		
		//find max key (transaction ID) and use to reset transaction_id_counter
		//at the same time, reset timers on transactions
		int max = 0; 
		Set<String> keys = transaction_table.keySet();
		for (String key : keys)
		{			
			int trxnID = transaction_table.get(key).getID();
			Trace.info("transaction ID: " + trxnID);
			if (trxnID > max)
			{
				max = trxnID;
			}
			TransactionTimer tt = new TransactionTimer(trxnID, this);
			ScheduledFuture<Boolean> scheduledFuture = scheduler.schedule(tt, secondsToLive, TimeUnit.SECONDS);
			scheduledFutures.put("" + trxnID, scheduledFuture);
		}
		transaction_id_counter = max + 1;
		
		//reset all locks
		//reexecute all operations on RMs
		keys = transaction_table.keySet();
		int maxOPID = 0;
		for (String key : keys)
		{
			Transaction t = transaction_table.get(key);
			int thisTrxnOpMax = t.reexecute(lm, flights, cars, hotels, middleware);
			if (thisTrxnOpMax > maxOPID)
			{
				maxOPID = thisTrxnOpMax;
			}
		}
		
		//reset operation counter?
		Trace.info("Setting new starting max op count to: " + (maxOPID + 1));
		Operation.setOpCount(maxOPID + 1);
		
		fis.close();
		ois.close();
	}
	
}
