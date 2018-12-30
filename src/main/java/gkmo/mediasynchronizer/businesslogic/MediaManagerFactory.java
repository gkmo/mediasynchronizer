package gkmo.mediasynchronizer.businesslogic;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.flickr4java.flickr.FlickrException;

import gkmo.mediasynchronizer.businesslogic.flickr.FlickrManager;
import gkmo.mediasynchronizer.businesslogic.googlephotos.GooglePhotosManager;
import gkmo.mediasynchronizer.model.MediaSynchronizerCredentials;

public class MediaManagerFactory 
{
	public static IMediaManager createMediaManager(String serviceName, MediaSynchronizerCredentials credentials, ILogger logger) 
	{
		if (serviceName.equalsIgnoreCase("Flickr"))
		{
			logger.info(MediaManagerFactory.class.getSimpleName(), "Instantiating Flickr Manager");
			
			try 
			{
				return new FlickrManager(credentials.getFlickrCredentials().getAuthToken(), credentials.getFlickrCredentials().getTokenSecret(), logger);
			} 
			catch (FlickrException e) 
			{
				logger.error(MediaManagerFactory.class.getSimpleName(), e.getMessage(), e);
			}
		}
		else if (serviceName.equalsIgnoreCase("GooglePhotos"))
		{
			logger.info(MediaManagerFactory.class.getSimpleName(), "Instantiating GooglePhotos Manager");
			
			try 
			{
				return new GooglePhotosManager(credentials.getGooglePhotosCredentials().getUserToken(), logger);
			} 
			catch (IOException e) 
			{
				logger.error(MediaManagerFactory.class.getSimpleName(), e.getMessage(), e);
			} 
			catch (GeneralSecurityException e) 
			{
				logger.error(MediaManagerFactory.class.getSimpleName(), e.getMessage(), e);
			}
		}
		
		logger.warning(MediaManagerFactory.class.getSimpleName(), "Coundn't find a media manager for '" + serviceName + "'");
		
		return null;
	}
}
