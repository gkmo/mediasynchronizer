package gkmo.mediasynchronizer.function;

import java.io.File;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.amazonaws.util.json.Jackson;

import gkmo.mediasynchronizer.businesslogic.AwsLogger;
import gkmo.mediasynchronizer.businesslogic.IMediaManager;
import gkmo.mediasynchronizer.businesslogic.MediaManagerFactory;
import gkmo.mediasynchronizer.model.DownloadPhotoRequest;
import gkmo.mediasynchronizer.model.MediaItem;

public class SynchronizeMediaItems implements RequestHandler<SQSEvent, Void> 
{
	@Override
    public Void handleRequest(SQSEvent event, Context context) 
    {
    	AwsLogger logger = new AwsLogger(context.getLogger());
        
    	for (SQSMessage message : event.getRecords())
    	{
    		try 
    		{
		    	DownloadPhotoRequest input = Jackson.fromJsonString(message.getBody(), DownloadPhotoRequest.class);
		    	
		    	logger.info(getClass().getSimpleName(), "Source: " + input.getSourceService());
		    	logger.info(getClass().getSimpleName(), "Destination: " + input.getDestinationService());
		    	logger.info(getClass().getSimpleName(), "Destination Album Id: " + input.getDestinationAlbumId());
		    
		    	IMediaManager sourceManager = MediaManagerFactory.createMediaManager(input.getSourceService(), input.getSourceCredentials(), logger);
		        IMediaManager destinationManager = MediaManagerFactory.createMediaManager(input.getDestinationService(), input.getDestinationCredentials(), logger);
		    
		        Map<MediaItem, File> tempFiles = sourceManager.downloadMediaItems(input.getMediaItems());
		        
		        destinationManager.uploadMediaItems(tempFiles, input.getDestinationAlbumId());
		        
		        for (File file : tempFiles.values())
		        {
					file.delete();
				}
		        
		        sourceManager.shutdown();
		        destinationManager.shutdown();
    		}
    		catch (Exception e) 
    		{
				logger.error(getClass().getSimpleName(), e.getMessage(), e);
			}
    	}
        
        return null;
    }
}
