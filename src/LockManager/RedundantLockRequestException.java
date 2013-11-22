package LockManager;

/*
	The transaction requested a lock that it already had.
*/

@SuppressWarnings("serial")
public class RedundantLockRequestException extends Exception
{
	protected int xid = 0;
	
	public RedundantLockRequestException (int xid, String msg)
	{
		super(msg);
		this.xid = xid;
	}
	
	public int getXId() {
		return this.xid;
	}
}
