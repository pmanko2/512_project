package TransactionManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;

import LockManager.LockManager;
import ResImpl.CrashType;
import ResImpl.Trace;
import ResInterface.ResourceManager;

/**
 * This class used to create a transaction that is passed from the client to the RM (or Middleware)
 * @author nic
 *
 */
public class Transaction implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4994756763174534648L;
	private final int TRANSACTION_ID;
	//array list of operations this transaction is responsible for
	private transient ArrayList<Operation> operations;
	private transient LockManager lm;
	private transient ResourceManager serverToCrash;
	private transient CrashType type;
	
	public Transaction(int id, LockManager l)
	{
		TRANSACTION_ID = id;
		operations = new ArrayList<Operation>();
		lm = l;
		serverToCrash = null;
		type = null;
	}
	
	/**
	 * Returns ID of this transaction
	 * @return int ID for this transaction
	 */
	public int getID()
	{
		return TRANSACTION_ID;
	}
	
	/**
	 * Adds an operation to this transaction
	 * @param o
	 */
	public boolean addOperation(ResourceManager r, OP_CODE op, Hashtable<String, Object> args, ArrayList<String> keys)
	{
		//create operation and add to operation queue
		Operation o = createOperation(TRANSACTION_ID, r, op, args, keys);
		operations.add(o);
		
		//attempt to acquire necessary locks and execute transaction. This returns true 
		//if the operation was able to successfully obtain locks execute (locally!)
		boolean to_return = o.execute();
		Trace.info("Transaction " + TRANSACTION_ID + " contains " + operations.size() + " operations.");
		return to_return;
	}
	
	public int addOperationIntReturn(ResourceManager r, OP_CODE op, Hashtable<String, Object> args, ArrayList<String> keys)
	{
		//create operation and add to operation queue
		Operation o = createOperation(TRANSACTION_ID, r, op, args, keys);
		operations.add(o);
		
		//attempt to acquire necessary locks and execute transaction. This returns true 
		//if the operation was able to successfully obtain locks execute (locally!)
		return o.executeIntReturn();
	}
	
	public String addOperationStringReturn(ResourceManager r, OP_CODE op, Hashtable<String, Object> args, ArrayList<String> keys)
	{
		//create operation and add to operation queue
		Operation o = createOperation(TRANSACTION_ID, r, op, args, keys);
		operations.add(o);
		
		//attempt to acquire necessary locks and execute transaction. This returns true 
		//if the operation was able to successfully obtain locks execute (locally!)
		return o.executeStringReturn();
	}
	
	//this method creates an operation - replaces an operation if the key and the 
	//OP_CODE are the same
	@SuppressWarnings("unused")
	public Operation createOperation(int id, ResourceManager r, OP_CODE op, Hashtable<String, Object> args, ArrayList<String> keys)
	{
		if (operations == null)
		{
			operations = new ArrayList<Operation>();
		}
		//TODO Review logic in this method (should it return an arraylist<Operations>?
		Operation return_value;
		boolean create_new = true;
		for (Operation o: operations)
		{
			boolean same_operation = true;
			//if this is the same type of operation
			//if (o.getOPCODE() == op)
			//{
				ArrayList<String> o_keys = o.getKeys();
				//check if all the keys are the same
				for (String k : keys)
				{
			        String delims = "[-]";
			        String[] tokens = k.split(delims);
			        
					if (!tokens[0].equals("customer"))
					{
						if (!(o_keys.contains(k)))
						{
							same_operation = false;
						}
					}
					if (tokens[0].equals("customer") && keys.size() == 1)
					{
						if (!(o_keys.contains(k)))
						{
							same_operation = false;
						}
					} 
				}
			//}
			//if this is the same operation, create new operation with the same op_id
			if(same_operation)
			{
				create_new = false;
				return new Operation(TRANSACTION_ID, r, op, args, lm, o.getOpID());
			}
		}
		//if this has been reached then this is a new unique operation, and we should
		//create a new one
		return new Operation(TRANSACTION_ID, r, op, args, lm);
	}
	
	/**
	 * Commits this transaction
	 * @return True once the commit succeeds (we assume that once commit is called 
	 * that it will succeed so we don't check if anything failed
	 * @throws RemoteException 
	 */
	public boolean commit()
	{	
		ArrayList<Operation> flightOperations = getOperationsForRM("flights");
		ArrayList<Operation> carOperations = getOperationsForRM("cars");
		ArrayList<Operation> roomOperations = getOperationsForRM("rooms");
		ArrayList<Operation> middlewareOperations = getOperationsForRM("middleware");
		
		ArrayList<Operation> rollBack = new ArrayList<Operation>();
		ArrayList<Operation> abortList = new ArrayList<Operation>();
		
		for(Operation o : operations)
			abortList.add(o);
	
		System.out.println("SIZE OF FLIGHTS LIST: " + flightOperations.size());
		System.out.println("SIZE OF CARS LIST: " + carOperations.size());
		System.out.println("SIZE OF ROOMS LIST: " + roomOperations.size());
		System.out.println("SIZE OF MIDDLEWARE LIST: " + middlewareOperations.size());
		
		// commit all flight operations first. everytime we commit operation remove it from operations to be aborted
		// once all operations committed, add at least one operation of flight type (if there is one) to rollback list in case
		// other RMS have failed.
		// this logic follows for all rms
		for (Operation o : flightOperations)
		{
			if(!o.commit())
			{
				Trace.error("Tried to commit operation " + o.getOpID() + ". Received false from Flight RM. Need to rollback.");
				rollback(rollBack, abortList, "flights");
				return false;
			}
			else
			{
				abortList.remove(o);
			}
		}
		
		if(flightOperations.size() > 0)
		{
			rollBack.add(flightOperations.get(0));
			flushRMToDisk(flightOperations);
		}
		
		for (Operation o : carOperations)
		{
			if(!o.commit())
			{
				Trace.error("Tried to commit operation " + o.getOpID() + ". Received false from Car RM. Need to rollback.");
				
				rollback(rollBack, abortList, "cars");
				return false;
			}
			else
			{
				abortList.remove(o);
			}
		}
		
		if(carOperations.size() > 0)
		{
			rollBack.add(carOperations.get(0));
			flushRMToDisk(carOperations);
		}
		
		for (Operation o : roomOperations)
		{
			if(!o.commit())
			{
				Trace.error("Tried to commit operation " + o.getOpID() + ". Received false from Room RM. Need to rollback.");
				rollback(rollBack, abortList, "rooms");
				return false;
			}
			else
			{
				abortList.remove(o);
			}
		}
		
		if(roomOperations.size() > 0)
		{
			rollBack.add(roomOperations.get(0));
			flushRMToDisk(roomOperations);
		}
		
		for (Operation o : middlewareOperations)
		{
			if(!o.commit())
			{
				Trace.error("Tried to commit operation " + o.getOpID() + ". Received false from Middleware RM. Need to rollback.");
				rollback(rollBack, abortList, "middleware");
				return false;
			}
			else
			{
				abortList.remove(o);
			}
		}
		
		if(middlewareOperations.size() > 0)
			flushRMToDisk(middlewareOperations);
		
		if(type == CrashType.TM_ALL_DECISIONS_SENT)
		{
			try
			{
				serverToCrash.crash("middleware");
			} catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
			
		lm.UnlockAll(TRANSACTION_ID);
		
		return true;
	}
	
	private void flushRMToDisk(ArrayList<Operation> flightOperations) 
	{
		if(flightOperations.size() > 0)
		{
			try
			{
				flightOperations.get(0).getRM().flushToDisk();
			} catch (RemoteException e){
				Trace.info("Trying to flush RM to disk but RM is unavailable");
			}
		}
		
	}

	/**
	 * method prompts each operation(and consequently RM) to submit a vote on whether it wants to commit
	 * If all votes are yes then true otherwise false
	 * @return boolean indicating if voting was successful or not
	 */
	public boolean startVotingProcess()
	{
		int voteCounter = 0;
		
		// go through all operation votes. if we have a no vote, return false
		for(Operation voter : operations)
		{
			Vote currentVote;
			try
			{
				currentVote = voter.requestVoteFromRM();
				voteCounter ++;
				
				if(voteCounter == 2 && operations.size() > 2 && type == CrashType.TM_SOME_REPLIES)
					serverToCrash.crash("middleware");
				
				if(currentVote == Vote.NO)
					return false;
			} catch (RMCrashException e){
				// if we get an exception indicating an rm has crashed, we abort transaction
				Trace.info("RM has crashed. Need to abort transaction. Returning false to TransactionManager");
				return false;
			} catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}
		
		
		return true;

	}
	
	/**
	 * Method to rollback the transaction in case a server crashes after commit has been called
	 */
	public void rollback(ArrayList<Operation> rollBackList, ArrayList<Operation> abortList, String failedRM)
	{
		
		// abort every operation that needs to be aborted
		for(Operation toAbort : abortList)
		{
			try
			{
				if(!toAbort.getRM().getName().equals(failedRM))
					toAbort.abort();
			} catch (RemoteException e)
			{
				Trace.error("Could not abort operation. RM unresponsive");
			}
		}
		
		//rollback all RMS that need to be rolled back
		for(Operation rmOperation : rollBackList)
		{
			try
			{
				rmOperation.getRM().rollback();
			} catch (RemoteException e)
			{
				Trace.error("Could not rollback RM. RM unresponsive");
			}
		}

	}
	
	/**
	 * Aborts this transaction
	 */
	public void abort()
	{
		for (Operation o : operations)
		{
			o.abort();
		}
		lm.UnlockAll(TRANSACTION_ID);
	}
	
	public void crash(String which) throws RemoteException
	{
		for(Operation o : operations)
		{	
			if(o.getRM().getName().equals(which))
			{
				o.getRM().crash(which);
			}
		}
	}
	
	private ArrayList<Operation> getOperationsForRM(String rmName)
	{
		ArrayList<Operation> toReturn = new ArrayList<Operation>();
		
		for(Operation current : operations)
		{
			try
			{
				if(current.getRM().getName().equals(rmName))
					toReturn.add(current);
				
			} catch (RemoteException e)
			{
				Trace.error("Trying to get operation in RM " + rmName + " but RM is unavailable. Need to rollback/abort");
				return null;
			}
		}
		
		return toReturn;
	}
	
	public void setCrashFlags(CrashType type, ResourceManager server)
	{
		this.type = type;
		this.serverToCrash = server;
	}

	/**
	 * Reset the lockmanager pointer in the transaction and then pass it to the operations  
	 * in this transaction along with all the other rms so that they can reacquire
	 * all necessary locks and reexecute. Also returns max op id in this transaction 
	 * (used to reset the op counter)
	 * @param l
	 */
	public int reexecute(LockManager l, ResourceManager flights, ResourceManager cars, 
			ResourceManager hotels, ResourceManager mw)
	{
		lm = l;
		int max = 0;
		Trace.info("Transaction " + TRANSACTION_ID + " contains " + operations.size() + " operations.");
		for (Operation o : operations)
		{
			o.reexecute(lm, flights, cars, hotels, mw);
			if (o.getOpID() > max)
			{
				max = o.getOpID();
			}
		}
		return max;
	}
	
	/**
	 * Used to deserialize a transaction
	 * @param in
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		     
		//always perform the default de-serialization first
	     in.defaultReadObject();
	     operations = new ArrayList<Operation>();
	     int totalOps = in.readInt();
	     //Trace.info("Total ops in transaction read-in: " + totalOps);
	     for (int i = 0; i < totalOps; i++)
	     {
	    	operations.add((Operation)in.readObject());
	     }
		//Trace.info("Reading transaction " + TRANSACTION_ID + ", which has " + operations.size() + " operations");

	}

  	/**
    * This is used to serialize a transaction
    */
    private void writeObject(ObjectOutputStream out) throws IOException {
      
    	//perform the default serialization for all non-transient, non-static fields
		//Trace.info("Writing transaction " + TRANSACTION_ID + ", which has " + operations.size() + " operations");
    	out.defaultWriteObject();
    	out.writeInt(operations.size());
    	for (Operation o : operations)
    	{
           	out.writeObject(o);
    	}
    }
}
