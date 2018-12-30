package gkmo.mediasynchronizer.businesslogic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class AwsLogger implements ILogger
{
	private LambdaLogger logger;
	
	public AwsLogger(LambdaLogger logger)
	{
		this.logger = logger;
	}
	
	@Override
	public void info(String category, String message) 
	{
		log("INFO", category, message);
	}

	@Override
	public void warning(String category, String message) 
	{
		log("WARNING", category, message);
	}

	@Override
	public void error(String category, String message) 
	{
		log("ERROR", category, message);
	}

	@Override
	public void error(String category, String message, Exception ex) 
	{
		log("ERROR", category, message);	
		
		StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));        
        this.logger.log(sw.toString());
	}
	
	private void log(String level, String category, String message)
	{
		@SuppressWarnings("deprecation")
		String formattedMessage = String.format("[%s] %s - %s - %s", Calendar.getInstance().getTime().toGMTString(), level, category, message);
		this.logger.log(formattedMessage);
	}

}
