package gkmo.mediasynchronizer.businesslogic.flickr;

import java.io.BufferedOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import gkmo.mediasynchronizer.businesslogic.ILogger;
import gkmo.mediasynchronizer.model.MediaItem;

public class FlickrDownloadMediaItemTask implements Runnable 
{
	private MediaItem mediaItem;
	private Map<MediaItem, File> downloadedFiles;
	private ILogger logger;

	public FlickrDownloadMediaItemTask(MediaItem item, Map<MediaItem, File> downloadedFiles, ILogger logger) 
	{
		this.mediaItem = item;
		this.downloadedFiles = downloadedFiles;
		this.logger = logger;
	}

	@Override
	public void run() 
	{
		this.logger.info(getClass().getSimpleName(), "Downloading '" + this.mediaItem.getTitle() + "' from '" + this.mediaItem.getDownloadUrl() + "'");	
		
		try 
		{
			File tempFile = File.createTempFile("flickr", this.mediaItem.getId());
			URL url = new URL(this.mediaItem.getDownloadUrl());
	        HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
	
	        java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
	        java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
	        java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 2048);
	        
	        byte[] data = new byte[2048];
	        int x = 0;
	        
	        while ((x = in.read(data, 0, data.length)) >= 0) 
	        {
	            bout.write(data, 0, x);
	        }
	        
	        bout.close();
	        in.close();
	        
	        this.downloadedFiles.put(this.mediaItem, tempFile);
	    } 
		catch (Exception e) 
		{
			this.logger.error(getClass().getSimpleName(), e.getMessage(), e);
	    }
	}

}
