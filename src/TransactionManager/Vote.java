package TransactionManager;

public class Vote 
{
	private String vote;
	private String rmName;
	
	public Vote(String vote, String rmName)
	{
		this.vote = vote;
		this.rmName = rmName;
	}
	
	public String getVote()
	{
		return this.vote;
	}
	
	public String getRMName()
	{
		return this.rmName;
	}
}
