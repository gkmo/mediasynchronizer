package gkmo.mediasynchronizer.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.util.json.Jackson;

import gkmo.mediasynchronizer.businesslogic.AwsLogger;
import gkmo.mediasynchronizer.businesslogic.ILogger;
import gkmo.mediasynchronizer.businesslogic.IMediaManager;
import gkmo.mediasynchronizer.businesslogic.MediaManagerFactory;
import gkmo.mediasynchronizer.model.Album;
import gkmo.mediasynchronizer.model.DownloadPhotoRequest;
import gkmo.mediasynchronizer.model.MediaItem;
import gkmo.mediasynchronizer.model.SynchronizeAlbumsRequest;

public class SynchronizeAlbums implements RequestHandler<SQSEvent, Void> {

    @Override
    public Void handleRequest(SQSEvent event, Context context) 
    {
        AwsLogger logger = new AwsLogger(context.getLogger());
        
        for(SQSMessage message : event.getRecords())
        {
         	SynchronizeAlbumsRequest input = Jackson.fromJsonString(message.getBody(), SynchronizeAlbumsRequest.class);
         	
         	logger.info(getClass().getSimpleName(), "Source: " + input.getSourceService());
        	logger.info(getClass().getSimpleName(), "Destination: " + input.getDestinationService());
        	logger.info(getClass().getSimpleName(), "Source Album Id: " + input.getSourceAlbumId());
        	logger.info(getClass().getSimpleName(), "Destination Album Id: " + input.getDestinationAlbumId());
        	
            IMediaManager sourceManager = MediaManagerFactory.createMediaManager(input.getSourceService(), input.getSourceCredentials(), logger);
            IMediaManager destinationManager = MediaManagerFactory.createMediaManager(input.getDestinationService(), input.getDestinationCredentials(), logger);
            
            synchronize(sourceManager, destinationManager, input.getSourceAlbumId(), input.getDestinationAlbumId(), input, logger);
            
            sourceManager.shutdown();
            destinationManager.shutdown();
        }
    	
        
        return null;
    }
    
    private static void synchronize(IMediaManager sourcePhotoManager, IMediaManager destinationPhotoManager, 
    		String sourceAlbumId, String destinationAlbumId, SynchronizeAlbumsRequest synchronizeRequest, ILogger logger)
	{
		Album sourceAlbum = sourcePhotoManager.getAlbumById(sourceAlbumId);
		Album destinationAlbum;
		
		if (destinationAlbumId == null || "".equals(destinationAlbumId))
		{
			destinationAlbum = destinationPhotoManager.createAlbum(sourceAlbum.getTitle());
		}
		else 
		{
			destinationAlbum = destinationPhotoManager.getAlbumById(destinationAlbumId);
		}
		
		logger.info(SynchronizeAlbums.class.getSimpleName(), "Synchronizing album '"+ sourceAlbum.getTitle() +"' with " + sourceAlbum.getPhotosCount() + " photos");
		
		if (sourceAlbum.getPhotosCount() == destinationAlbum.getPhotosCount())
		{
			logger.info(SynchronizeAlbums.class.getSimpleName(), "Skiping synchronization. Both source and destination have the same amount of photos");
			return;
		}
		
		if (!destinationAlbum.isWriteable())
		{
			logger.warning(SynchronizeAlbums.class.getSimpleName(), "Skiping synchronization. Destination album is not writeable");
			return;
		}
		
		Map<String, MediaItem> sourcePhotos = sourcePhotoManager.getMediaItems(sourceAlbum);
		Map<String, MediaItem> destinationPhotos = destinationPhotoManager.getMediaItems(destinationAlbum);
		
		String queueUrl = System.getenv("OUTPUT_QUEUE");
		
		AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		
		List<MediaItem> itemsToSynchronize = new ArrayList<MediaItem>();
		
		for (MediaItem item : sourcePhotos.values())
		{
			if (!destinationPhotos.containsKey(item.getTitle()))
			{
				itemsToSynchronize.add(item);
				
				if (itemsToSynchronize.size() == 25)
				{
					PubishDownloadRequest(destinationAlbumId, synchronizeRequest, logger, queueUrl, sqs, itemsToSynchronize);
					itemsToSynchronize.clear();
				}
			}
		}
		
		
		PubishDownloadRequest(destinationAlbumId, synchronizeRequest, logger, queueUrl, sqs, itemsToSynchronize);
	}

	private static void PubishDownloadRequest(String destinationAlbumId, SynchronizeAlbumsRequest synchronizeRequest,
			ILogger logger, String queueUrl, AmazonSQS sqs, List<MediaItem> itemsToSynchronize) 
	{
		if (itemsToSynchronize.size() == 0)
		{
			return;
		}
		
		DownloadPhotoRequest downloadPhotoRequest = new DownloadPhotoRequest(itemsToSynchronize, destinationAlbumId, synchronizeRequest);
		
		String jsonMessage = Jackson.toJsonString(downloadPhotoRequest);
		
		sqs.sendMessage(queueUrl, jsonMessage);
		
		logger.info(SynchronizeAlbums.class.getSimpleName(), String.format("Published download photos request with %d to %s", itemsToSynchronize.size(), queueUrl));
	}
}
