package gkmo.mediasynchronizer.businesslogic;

public interface ILogger 
{
	void info(String category, String message);
	
	void warning(String category, String message);
	
	void error(String category, String message);
	
	void error(String category, String message, Exception ex);
}
