package TransactionManager;

import java.util.concurrent.Callable;

import ResImpl.Trace;

/**
 * Class used to enforce time to live for transactions
 * @author nic
 *
 */
public class TransactionTimer implements Callable<Boolean> {

	private int transactionID; 
	private TransactionManager transactionManager;
	
	public TransactionTimer(int id, TransactionManager tm)
	{
		transactionID = id;
		transactionManager = tm;
	}

	@Override
	public Boolean call() throws TransactionAbortedException {
		Trace.info("Transaction " + transactionID + " has run out of time. Aborting.");
		transactionManager.abort(transactionID);
		return new Boolean(true);
	}
}
