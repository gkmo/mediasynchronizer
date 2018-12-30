/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gkmo.mediasynchronizer.businesslogic.googlephotos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ApiException;
import com.google.auth.Credentials;
import com.google.auth.oauth2.UserCredentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient.SearchMediaItemsPagedResponse;
import com.google.photos.library.v1.proto.BatchCreateMediaItemsResponse;
import com.google.photos.library.v1.proto.NewMediaItem;
import com.google.photos.library.v1.proto.NewMediaItemResult;
import com.google.rpc.Code;

import gkmo.mediasynchronizer.businesslogic.ILogger;
import gkmo.mediasynchronizer.businesslogic.IMediaManager;
import gkmo.mediasynchronizer.model.Album;
import gkmo.mediasynchronizer.model.MediaItem;

public class GooglePhotosManager implements IMediaManager 
{	
	private static final java.io.File DATA_STORE_DIR = new java.io.File(GooglePhotosManager.class.getResource("/").getPath(), "credentials");
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final int LOCAL_RECEIVER_PORT = 61984;
    
    private final PhotosLibraryClient googlePhotos;
    private final ILogger logger;
    
    public GooglePhotosManager(ILogger logger) throws IOException, GeneralSecurityException 
    {
    	List<String> selectedScopes = new ArrayList<>();
        selectedScopes.add("https://www.googleapis.com/auth/photoslibrary"); 
        selectedScopes.add("https://www.googleapis.com/auth/photoslibrary.sharing");
        
    	PhotosLibrarySettings settings = PhotosLibrarySettings.newBuilder().setCredentialsProvider(
				FixedCredentialsProvider.create(getUserCredentials(selectedScopes))).build();
		
    	this.googlePhotos = PhotosLibraryClient.initialize(settings);
    	this.logger = logger;
    }
    
    public GooglePhotosManager(String userToken, ILogger logger) throws IOException, GeneralSecurityException 
    {
    	PhotosLibrarySettings settings = PhotosLibrarySettings.newBuilder().setCredentialsProvider(
				FixedCredentialsProvider.create(getUserCredentials(userToken))).build();
		
    	this.googlePhotos = PhotosLibraryClient.initialize(settings);
    	this.logger = logger;
    }
    
    @Override
    public Map<String, Album> getAlbums() 
    {
        try 
        {
            HashMap<String, Album> result = new HashMap<>();
        
            InternalPhotosLibraryClient.ListAlbumsPagedResponse albums = googlePhotos.listAlbums();

            for(com.google.photos.library.v1.proto.Album album : albums.iterateAll())
            {
                result.put(album.getTitle(), buildAlbum(album));
            }
                    
            return result;
        } 
        catch (ApiException e) 
        {
        	this.logger.error(getClass().getSimpleName(), e.getMessage());
        } 
        
        return null;
    }

	private Album buildAlbum(com.google.photos.library.v1.proto.Album album) {
		return new Album(album.getId(), album.getTitle(), (int)album.getMediaItemsCount(), album.getIsWriteable());
	}
    
    public Map<String, MediaItem> getMediaItems(Album album) 
    {
    	HashMap<String, MediaItem> result = new HashMap<>();
    	
    	if (album.getPhotosCount() == 0)
    	{
    		return result;
    	}
    	
    	SearchMediaItemsPagedResponse response = this.googlePhotos.searchMediaItems(album.getId());

    	for (com.google.photos.library.v1.proto.MediaItem item : response.iterateAll()) 
    	{
    	    result.put(item.getFilename(), new MediaItem(item.getId(), item.getFilename(), item.getDescription(), item.getBaseUrl()));
    	}
    	
    	return result;
    }
   
	@Override
	public Album createAlbum(String name) 
	{
		com.google.photos.library.v1.proto.Album album = this.googlePhotos.createAlbum(name);
		
		if (album == null)
		{
			return null;
		}
		
		return new Album(album.getId(), album.getTitle(), 0, album.getIsWriteable());
	}

	@Override
	public void uploadMediaItem(File file, MediaItem item, String destinationAlbumId) 
	{	
		HashMap<MediaItem, File> files = new HashMap<MediaItem, File>(1);
		files.put(item,file);
		this.uploadMediaItems(files, destinationAlbumId);
	}
	
	@Override
	public void uploadMediaItems(Map<MediaItem, File> files, String destinationAlbumId) 
	{
		this.logger.info(getClass().getSimpleName(), String.format("Updaloding %d items to album '%s'", files.size(), destinationAlbumId));
		
		List<NewMediaItem> uploadedMediaItems = new ArrayList<NewMediaItem>(files.size());
		
		ExecutorService pool = Executors.newFixedThreadPool(files.size()); 
		
		for (Entry<MediaItem, File> item : files.entrySet())
		{
			UploadMediaItemTask task = new UploadMediaItemTask(item.getKey(), item.getValue(), this.googlePhotos, uploadedMediaItems, logger);
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
		
		if (uploadedMediaItems.size() > 0)
		{
			BatchCreateMediaItemsResponse response = googlePhotos.batchCreateMediaItems(destinationAlbumId, uploadedMediaItems);
			
			for (NewMediaItemResult itemsResponse : response.getNewMediaItemResultsList()) 
			{
			    com.google.rpc.Status status = itemsResponse.getStatus();
			    
			    if (status.getCode() == Code.OK_VALUE) 
			    {
			      com.google.photos.library.v1.proto.MediaItem createdItem = itemsResponse.getMediaItem();
			      this.logger.info(getClass().getSimpleName(), createdItem.getFilename() + "' uploaded.");
			    }
			    else 
			    {
			    	this.logger.warning(getClass().getSimpleName(), "Unable to create MediaItem. Error code: " + status.getCode());
			    }
		    }
		}
	}

	@Override
	public Map<MediaItem, File> downloadMediaItems(List<MediaItem> mediaItems) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File downloadMediaItem(MediaItem item) {
		// TODO Auto-generated method stub
		return null;
	}
    
	private static Credentials getUserCredentials(String userToken) throws IOException, GeneralSecurityException 
	{	
		return UserCredentials.newBuilder()
				.setClientId("789177060700-ji66ek3j252qt500nq839m2npskbagjf.apps.googleusercontent.com")
				.setClientSecret("CRcBvZu9hEDBeSFeNRSFnmfC")
				.setRefreshToken(userToken).build();
	}
	
	private Credentials getUserCredentials(List<String> selectedScopes) throws IOException, GeneralSecurityException 
	{
		InputStream in = getClass().getResourceAsStream("/clientCredentials.json"); 
	    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
	    String clientId = clientSecrets.getDetails().getClientId();
	    String clientSecret = clientSecrets.getDetails().getClientSecret();

	    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, selectedScopes)
	            .setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR))
	            .setAccessType("offline")
	            .build();
	    
	    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(LOCAL_RECEIVER_PORT).build();
	    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	    
	    String token = credential.getRefreshToken();
	    
	    return UserCredentials.newBuilder()
	        .setClientId(clientId)
	        .setClientSecret(clientSecret)
	        .setRefreshToken(token)
	        .build();
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
		com.google.photos.library.v1.proto.Album album = this.googlePhotos.getAlbum(id);
		return buildAlbum(album);
	}

	@Override
	public void shutdown() 
	{
		try 
		{
			this.googlePhotos.shutdown();
			
			if (!this.googlePhotos.awaitTermination(5, TimeUnit.MINUTES))
			{
				this.googlePhotos.shutdownNow();
			}
		} 
		catch (InterruptedException e) 
		{
			this.logger.error(getClass().getSimpleName(), e.getMessage());
		}
	}
}
