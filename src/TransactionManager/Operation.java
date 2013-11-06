package TransactionManager;

import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.transaction.InvalidTransactionException;

import LockManager.DeadlockException;
import LockManager.LockManager;
import LockManager.TrxnObj;
import ResImpl.Flight;
import ResInterface.ResourceManager;

public class Operation {

	private ResourceManager rm;
	private OP_CODE operation;
	private ArrayList<Object> arguments;
	private LockManager lm;
	private int transaction_id;
	private static int operation_count = 0;
	private final int OP_ID;
	
	public Operation(int id, ResourceManager r, OP_CODE op, ArrayList<Object> args, LockManager l)
	{
		transaction_id = id;
		rm = r;
		operation = op;
		arguments = args;
		lm = l;
		OP_ID = operation_count;
		operation_count++;
	}
	
	public boolean execute()
	{
		try {
			switch(operation)
			{
				case ADD_FLIGHT:
					//acquire item key
					String key = Flight.getKey((Integer)arguments.get(1));
					
					//acquire read lock
					lm.Lock(transaction_id, key, TrxnObj.READ);
					
					//call rm to create a non-committed RMItem
					return rm.addFlight(OP_ID, (Integer)arguments.get(1), (Integer)arguments.get(2), (Integer)arguments.get(3));
				
				default:
					return false;
			}
		}
		catch (DeadlockException e)
		{
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * Returns a string key representing the data object necessary for this operation to occur
	 * @return
	 */
	public String getKey()
	{
		return null;
	}
	
	/**
	 * Commits this operation
	 * @return True if the operation was successfully committed; otherwise, returns false
	 */
	public boolean commit()
	{
		try {
		
			return rm.commit(OP_ID);
		
		} catch (InvalidTransactionException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TransactionAbortedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Aborts this operation by committing the undo operation
	 */
	public void abort()
	{
		try {
			rm.abort(OP_ID);
		} catch (InvalidTransactionException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
