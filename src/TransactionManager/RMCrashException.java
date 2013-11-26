package TransactionManager;

public class RMCrashException extends Exception
{
	public RMCrashException()
	{
		super();
	}
	
	public RMCrashException(String message) 
	{ 
		super(message); 
	}
	
	public RMCrashException(String message, Throwable cause) 
	{ 
		super(message, cause); 
	}
	  
	public RMCrashException(Throwable cause) 
	{ 
		super(cause); 
	}
	
}
