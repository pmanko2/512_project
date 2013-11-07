package TransactionManager;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.transaction.InvalidTransactionException;

import LockManager.LockManager;
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
	private static int transaction_id_counter;
	private LockManager lm;

	public TransactionManager()
	{
		transaction_table = new Hashtable<String, Transaction>();
		transaction_id_counter = 0;
		lm = new LockManager();
	}
	
	/**
	 * Synchronized method that starts a new transaction
	 * @return Transaction ID
	 */
	public synchronized int start()
	{
		Transaction t = new Transaction(transaction_id_counter, lm);
		int to_return = t.getID();
		transaction_id_counter++;
		transaction_table.put("" + to_return, t);
		return to_return;
	}
	
	public boolean commit(int transaction_id) throws TransactionAbortedException, InvalidTransactionException
	{
		return transaction_table.get("" + transaction_id).commit();
	}
	
	public void abort(int transaction_id)
	{
		transaction_table.get("" + transaction_id).abort();
	}
	
	/**
	 * Method used to add an operation to a transaction
	 * @param transaction_id
	 * @return
	 */
	public boolean addOperation(int transaction_id, ResourceManager r, OP_CODE op, ArrayList<Object> args)
	{
		return transaction_table.get("" + transaction_id).addOperation(r, op, args);
	}

	public void enlist()
	{
		
	}
}