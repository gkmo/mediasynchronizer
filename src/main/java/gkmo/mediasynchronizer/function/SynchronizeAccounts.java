package gkmo.mediasynchronizer.function;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.util.json.Jackson;

import gkmo.mediasynchronizer.businesslogic.AwsLogger;
import gkmo.mediasynchronizer.businesslogic.IMediaManager;
import gkmo.mediasynchronizer.businesslogic.MediaManagerFactory;
import gkmo.mediasynchronizer.model.Album;
import gkmo.mediasynchronizer.model.SynchronizeAccountsRequest;
import gkmo.mediasynchronizer.model.SynchronizeAlbumsRequest;

public class SynchronizeAccounts implements RequestHandler<SynchronizeAccountsRequest, Void> 
{

    @Override
    public Void handleRequest(SynchronizeAccountsRequest input, Context context) 
    {
    	AwsLogger logger = new AwsLogger(context.getLogger());
        
    	logger.info(getClass().getSimpleName(), "Source: " + input.getSourceService());
    	logger.info(getClass().getSimpleName(), "Destination: " + input.getDestinationService());
    	
        IMediaManager sourceManager = MediaManagerFactory.createMediaManager(input.getSourceService(), input.getSourceCredentials(), logger);
        IMediaManager destinationManager = MediaManagerFactory.createMediaManager(input.getDestinationService(), input.getDestinationCredentials(), logger);
        
        Map<String, Album> sourceAlbums = sourceManager.getAlbums();
    	Map<String, Album> destinationAlbums = destinationManager.getAlbums();
    	
    	String synchronizeAlbumsQueueArn = System.getenv("OUTPUT_QUEUE");
    	
    	logger.info(getClass().getSimpleName(), "Output queue: " + synchronizeAlbumsQueueArn);
    	
    	AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
    	
    	String queueUrl = sqs.getQueueUrl(synchronizeAlbumsQueueArn).getQueueUrl();
    	
    	for(String sourceAlbumName : sourceAlbums.keySet())
    	{
    		String destinationAlbumId = null;
    		int destinationAlbumMediaCount = 0;
    		
    		if (destinationAlbums.containsKey(sourceAlbumName)) 
    		{
    			Album destinationAlbum = destinationAlbums.get(sourceAlbumName);
    			destinationAlbumId = destinationAlbum.getId();
    			destinationAlbumMediaCount = destinationAlbum.getPhotosCount();
    		}
    		
    		Album sourceAlbum = sourceAlbums.get(sourceAlbumName);
    		
    		if (sourceAlbum.getPhotosCount() <= destinationAlbumMediaCount)
    		{
    			logger.info(SynchronizeAccounts.class.getName(), String.format("Skipping synchronization of '%s' because the source and destination contains the same amount of items", sourceAlbumName));
    			continue;
    		}
    		
    		SynchronizeAlbumsRequest request = new SynchronizeAlbumsRequest();
    		request.setSourceAlbumId(sourceAlbum.getId());
    		request.setDestinationAlbumId(destinationAlbumId);
    		request.setSourceService(input.getSourceService());
    		request.setSourceCredentials(input.getSourceCredentials());
    		request.setDestinationService(input.getDestinationService());
    		request.setDestinationCredentials(input.getDestinationCredentials());
    		
    		sqs.sendMessage(queueUrl, Jackson.toJsonString(request));
    		
    		logger.info(SynchronizeAccounts.class.getName(), String.format("Requested synchronization of '%s'", sourceAlbumName));
    	}
        	
    	sourceManager.shutdown();
    	destinationManager.shutdown();
    	
        return null;
    }
}
