package TransactionManager;

import java.util.concurrent.Callable;

import ResImpl.Trace;

public class TimeoutTimer implements Callable<Boolean>
{
	private String rmName;
	private Operation operation;

	public TimeoutTimer(String rm, Operation op)
	{
		this.rmName = rm;
		this.operation = op;
	}
	
	@Override
	public Boolean call() throws RMCrashException
	{
		Trace.info("TimeOut while waiting for " + rmName + " RM response. RM has crashed, voting NO");
		operation.indicateRMTimeout();
		return new Boolean(true);
	}

}
