package TransactionManager;

import java.util.ArrayList;

import LockManager.LockManager;
import ResInterface.ResourceManager;

/**
 * This class used to create a transaction that is passed from the client to the RM (or Middleware)
 * @author nic
 *
 */
public class Transaction {
	
	private final int TRANSACTION_ID;
	//array list of operations this transaction is responsible for
	private ArrayList<Operation> operations;
	private LockManager lm;
	
	public Transaction(int id, LockManager l)
	{
		TRANSACTION_ID = id;
		operations = new ArrayList<Operation>();
		lm = l;
	}
	
	/**
	 * Returns an arraylist of key strings which is used to obtain the locks necessary to carry out this transaction.
	 * @return ArrayList<String> of Keys for the objects that need to be modified for this transaction to occur.
	 */
	public ArrayList<String> getKeys()
	{
		return null;
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
	public boolean addOperation(ResourceManager r, OP_CODE op, ArrayList<Object> args)
	{
		//create operation and add to operation queue
		Operation o = new Operation(TRANSACTION_ID, r, op, args, lm);
		operations.add(o);
		
		//attempt to acquire necessary locks and execute transaction. This returns true 
		//if the operation was able to successfully obtain locks execute (locally!)
		return o.execute();
	}
	
	/**
	 * Commits this transaction
	 * @return True if commit succeeded; otherwise, returns false.
	 */
	public boolean commit()
	{
		boolean transaction_committed = true;
		boolean[] operation_success = new boolean[operations.size()];
		
		for (int i = 0; i < operations.size(); i++)
		{
			Operation o = operations.get(i);
			
			if (o.commit())
			{
				operation_success[i] = true;
			}
			else
			{
				operation_success[i] = false;
				transaction_committed = false;
			}
		}
		//if any of the transactions failed, undo the operations that succeeded
		if (!transaction_committed)
		{
			for (int i = 0; i < operations.size(); i++)
			{
				//undo successful operations
				if (operation_success[i])
				{
					Operation o = operations.get(i);
					o.abort();
				}
			}
		}
		return transaction_committed;
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
	}
}
