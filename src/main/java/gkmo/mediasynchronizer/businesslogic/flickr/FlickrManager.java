/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gkmo.mediasynchronizer.businesslogic.flickr;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;

import gkmo.mediasynchronizer.businesslogic.ILogger;
import gkmo.mediasynchronizer.businesslogic.IMediaManager;
import gkmo.mediasynchronizer.model.Album;
import gkmo.mediasynchronizer.model.MediaItem;

public class FlickrManager implements IMediaManager 
{    
    private final Flickr flickr;
    private final ILogger logger;
    
    public FlickrManager(String authToken, String tokenSecret, ILogger logger) throws FlickrException
    {    
        String apikey = "619d9125ae4acab780bd6eae1ef9b242";
        String secret = "e93e5d52fe5e7bc5";

        this.logger = logger;
        this.flickr = new Flickr(apikey, secret, new REST());
                
        AuthInterface authInterface = flickr.getAuthInterface();       
        Auth auth = authInterface.checkToken(authToken, tokenSecret);
        
        RequestContext.getRequestContext().setAuth(auth);
    }
    
    @Override
    public Map<String, Album> getAlbums() 
    {
        try 
        {
        	String userId = RequestContext.getRequestContext().getAuth().getUser().getId();
        	
        	this.logger.info(getClass().getSimpleName(), "Getting albums for user id = " + userId);
        	
            Photosets photosets = flickr.getPhotosetsInterface().getList(userId);
            
            HashMap<String, Album> result = new HashMap<>();
            
            photosets.getPhotosets().forEach((set) -> 
            {
                result.put(set.getTitle(), buildAlbum(set));
            });
            
            return result;            
        } 
        catch (FlickrException ex) 
        {
        	this.logger.error(getClass().getSimpleName(), ex.getMessage());
        }
        
        return null;
    }
    
    public Map<String, MediaItem> getMediaItems(Album album) 
    {
    	HashMap<String, MediaItem> result = new HashMap<>();
    	
    	if (album.getPhotosCount() == 0) {
    		return result;
    	}
    	
    	HashSet<String> extras = new HashSet<>();
    	extras.add("url_o");
    	
    	try 
    	{
    		int itemsPerPage = 500;
    		int totalPages = (album.getPhotosCount()/itemsPerPage) + 1;
    		int remainingPhotos = album.getPhotosCount();
    		
    		for (int page = 1; page <= totalPages; page++) 
    		{		
		    	PhotoList<Photo> response = this.flickr.getPhotosetsInterface().getPhotos(album.getId(), extras, 
		    			Flickr.PRIVACY_LEVEL_NO_FILTER, Math.min(itemsPerPage,  remainingPhotos), page);
		
		    	for (int i = 0; i < response.getPerPage(); i++) 
		    	{
		    		Photo item = response.get(i);
		    		
		    	    result.put(item.getTitle(), new MediaItem(item.getId(), item.getTitle(), "", item.getOriginalUrl()));
		    	    
		    	    remainingPhotos--;
		    	}    	
    		}
    	} 
    	catch (FlickrException ex) 
    	{
    		this.logger.error(getClass().getSimpleName(), ex.getMessage());
		}
    	
    	return result;
    }

	@Override
	public Album createAlbum(String name) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void uploadMediaItem(File file, MediaItem item, String destinationAlbumId) 
	{
		this.logger.info(getClass().getSimpleName(), "Updaloding '" + item.getTitle() + "' to '" + destinationAlbumId + "'");	
	}
	
	@Override
	public File downloadMediaItem(MediaItem item) 
	{
		this.logger.info(getClass().getSimpleName(), "Downloading '" + item.getTitle() + "' from '" + item.getDownloadUrl() + "'");	
		
		try 
		{
			File tempFile = File.createTempFile("flickr", item.getId());
			URL url = new URL(item.getDownloadUrl());
	        HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
	        //long completeFileSize = httpConnection.getContentLength();
	
	        java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
	        java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
	        java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 2048);
	        
	        byte[] data = new byte[2048];
	        //long downloadedFileSize = 0;
	        int x = 0;
	        
	        while ((x = in.read(data, 0, data.length)) >= 0) 
	        {
	            //downloadedFileSize += x;
	
	            // calculate progress
	            //final int currentProgress = (int) ((((double)downloadedFileSize) / ((double)completeFileSize)) * 100000d);
	
	            bout.write(data, 0, x);
	        }
	        
	        bout.close();
	        in.close();
	        
	        return tempFile;
	    } 
		catch (FileNotFoundException e) 
		{
			this.logger.error(getClass().getSimpleName(), e.getMessage());
			e.printStackTrace();
			return null;
	    } 
		catch (IOException e) 
		{
			this.logger.error(getClass().getSimpleName(), e.getMessage());
			e.printStackTrace();
			return null;
	    }
	}

	@Override
	public Album getAlbumByName(String name) 
	{
		Map<String, Album> albums = getAlbums();
		
		if (albums.containsKey(name))
		{
			return albums.get(name);
		}
		
		return null;
	}

	@Override
	public Album getAlbumById(String id) 
	{
		try 
		{
			Photoset photoset = this.flickr.getPhotosetsInterface().getInfo(id);
			return buildAlbum(photoset);
		} 
		catch (FlickrException e) 
		{
			this.logger.error(getClass().getSimpleName(), e.getMessage(), e);
		}
		
		return null;
	}
	
	private Album buildAlbum(Photoset photoset)
	{
		return new Album(photoset.getId(), photoset.getTitle(), photoset.getPhotoCount(), true);
	}

	@Override
	public void shutdown() 
	{
		
	}

	@Override
	public void uploadMediaItems(Map<MediaItem, File> files, String destinationAlbumId) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<MediaItem, File> downloadMediaItems(List<MediaItem> mediaItems) 
	{
		Map<MediaItem, File> downloadedFiles = new HashMap<MediaItem, File>(mediaItems.size());
		
		ExecutorService pool = Executors.newFixedThreadPool(mediaItems.size()); 
		
		for (MediaItem item : mediaItems)
		{
			FlickrDownloadMediaItemTask task = new FlickrDownloadMediaItemTask(item, downloadedFiles, logger);
			pool.execute(task);
		}
		
		pool.shutdown();

		try 
		{
			if(!pool.awaitTermination(14, TimeUnit.MINUTES))
			{
				pool.shutdownNow();
			}
		} 
		catch (InterruptedException e) 
		{
			logger.error(getClass().getSimpleName(), "Unable to upload all items in time", e);
		}
		
		return downloadedFiles;
	}
}
