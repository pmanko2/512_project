package TransactionManager;

import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.transaction.InvalidTransactionException;

import LockManager.DeadlockException;
import LockManager.LockManager;
import LockManager.TrxnObj;
import ResImpl.Car;
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
		try 
		{
			//string used to hold key for whatever item is being worked with
			String key;

			switch(operation)
			{
				case ADD_FLIGHT:
					//acquire item key
					key = Flight.getKey((Integer)arguments.get(0));
					
					//acquire write lock
					lm.Lock(transaction_id, key, TrxnObj.WRITE);
					
					//call rm to create a non-committed RMItem
					return rm.addFlight(OP_ID, (Integer)arguments.get(0), (Integer)arguments.get(1), (Integer)arguments.get(2));
				case ADD_CARS:
					//acquire item key
					key = Car.getKey((String)arguments.get(0));
					
					//acquire write lock
					lm.Lock(transaction_id, key, TrxnObj.WRITE);
					
					//call rm to create a non-committed RMItem
					return rm.addCars(OP_ID, (String)arguments.get(0), (Integer)arguments.get(1), (Integer)arguments.get(2));
					
					
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
	 * Commits this operation
	 * @return True if the operation was successfully committed; otherwise, returns false
	 */
	public boolean commit()
	{
		try {			
			boolean result = rm.commit(OP_ID);
			lm.UnlockAll(transaction_id);
			return result;
		
		} catch (InvalidTransactionException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
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
