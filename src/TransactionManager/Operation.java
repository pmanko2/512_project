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
import ResImpl.Hotel;
import ResImpl.MiddlewareImpl;
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
	
	/**
	 * Method used to execute functions that return a boolean value. The result of these
	 * executions are not persistent until they are committed.
	 * @return
	 */
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
						
				case ADD_ROOMS:
					
					//acquire write lock
					lm.Lock(transaction_id, (String)arguments.get("key"), TrxnObj.WRITE);
					
					//call rm to create a non-committed RMItem
					return rm.addRooms(OP_ID, (String)arguments.get("location"), (Integer)arguments.get("numRooms"), (Integer)arguments.get("price"));
				
				case NEW_CUSTOMER_ID:
					
					//acquire write lock
					lm.Lock(transaction_id, (String)arguments.get("key"), TrxnObj.WRITE);
					
					//call rm to create a non-committed RMItem
					if ((rm.newCustomerExecute(OP_ID, (Integer)arguments.get("cid")))!=-1)
					{
						return true;
					}
					else
					{
						return false;
					}
					
				case DELETE_FLIGHT:
					
					//acquire write lock
					lm.Lock(transaction_id, (String)arguments.get("key"), TrxnObj.WRITE);
					
					//call rm to delete flight (not in a persistent way until committed
					return rm.deleteFlight(OP_ID, (Integer)arguments.get("flightNum"));

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
	 * Method used to execute functions that return a boolean value. The result of these
	 * executions are not persistent until they are committed.
	 * @return
	 */
	public int executeIntReturn()
	{
		try 
		{
			//string used to hold key for whatever item is being worked with
			String key;

			switch(operation)
			{
				case NEW_CUSTOMER:
				
					//acquire write lock
					lm.Lock(transaction_id, (String)arguments.get("key"), TrxnObj.WRITE);
					
					//call rm to create a non-committed RMItem
					return rm.newCustomerExecute(OP_ID, (Integer)arguments.get("cid"));

				default:
					
					return -1;
			}
		}
		catch (DeadlockException e)
		{
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Commits this operation
	 * @return True if the operation was successfully committed; otherwise, returns false
	 */
	public boolean commit()
	{
		try {			
			boolean result;
			if (rm instanceof MiddlewareImpl)
			{
				result = ((MiddlewareImpl) rm).commitOperation(OP_ID);
			}
			else
			{
				result = rm.commit(OP_ID);
			}
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
			if (rm instanceof MiddlewareImpl)
			{
				((MiddlewareImpl) rm).abortOperation(OP_ID);
			}
			else
			{
				rm.abort(OP_ID);
			}
		} catch (InvalidTransactionException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
