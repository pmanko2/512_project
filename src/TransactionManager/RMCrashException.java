package TransactionManager;

public class RMCrashException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7449445534857492193L;

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
