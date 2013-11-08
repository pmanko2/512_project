package TransactionManager;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.transaction.InvalidTransactionException;

import LockManager.DeadlockException;
import LockManager.LockManager;
import LockManager.TrxnObj;
import ResImpl.Car;
import ResImpl.Flight;
import ResImpl.Trace;
import ResInterface.ResourceManager;

public class Operation {

	private ResourceManager rm;
	private OP_CODE operation;
	private HashMap<String, Object> arguments;
	private LockManager lm;
	private int transaction_id;
	private static int operation_count = 0;
	private final int OP_ID;
	
	public Operation(int id, ResourceManager r, OP_CODE op, HashMap<String,Object> args, LockManager l)
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
				
					//acquire write lock
					lm.Lock(transaction_id, (String)arguments.get("key"), TrxnObj.WRITE);
					
					//call rm to create a non-committed RMItem
					return rm.addFlight(OP_ID, (Integer)arguments.get("flightNum"), (Integer)arguments.get("flightSeats"), (Integer)arguments.get("flightPrice"));

				case ADD_CARS:
					
					//acquire write lock
					lm.Lock(transaction_id, (String)arguments.get("key"), TrxnObj.WRITE);
					
					//call rm to create a non-committed RMItem
					return rm.addCars(OP_ID, (String)arguments.get("location"), (Integer)arguments.get("numCars"), (Integer)arguments.get("price"));
										
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
