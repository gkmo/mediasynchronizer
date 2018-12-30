package gkmo.mediasynchronizer.businesslogic.googlephotos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.List;

import com.google.api.gax.rpc.ApiException;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.proto.NewMediaItem;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.library.v1.util.NewMediaItemFactory;

import gkmo.mediasynchronizer.businesslogic.ILogger;
import gkmo.mediasynchronizer.model.MediaItem;

public class UploadMediaItemTask implements Runnable 
{
	private MediaItem mediaItem;
	private File file;
	private PhotosLibraryClient googlePhotos;
	private List<NewMediaItem> result;
	private ILogger logger;
	
	public UploadMediaItemTask(MediaItem mediaItem, File file, PhotosLibraryClient googlePhotos, List<NewMediaItem> uploadedMediaItems, ILogger logger) 
	{
		this.mediaItem = mediaItem;
		this.file = file;
		this.googlePhotos = googlePhotos;
		this.result = uploadedMediaItems;
		this.logger = logger;
	}

	@Override
	public void run() 
	{
		try 
		{
			UploadMediaItemRequest uploadRequest = UploadMediaItemRequest.newBuilder()
					.setFileName(this.mediaItem.getTitle())
					.setDataFile(new RandomAccessFile(this.file, "r"))
					.build();
			
			UploadMediaItemResponse uploadResponse = this.googlePhotos.uploadMediaItem(uploadRequest);
  
			if (uploadResponse.getError().isPresent()) 
			{
				com.google.photos.library.v1.upload.UploadMediaItemResponse.Error error = uploadResponse.getError().get();
				this.logger.error(getClass().getSimpleName(), error.getCause().getMessage());
			}
			else 
			{
				String uploadToken = uploadResponse.getUploadToken().get();
				
				NewMediaItem newMediaItem = NewMediaItemFactory.createNewMediaItem(uploadToken, this.mediaItem.getTitle());
				
				this.result.add(newMediaItem);
			}
		} 
		catch (ApiException e) 
		{
			this.logger.error(getClass().getSimpleName(), e.getCause().getMessage());
		} 
		catch (FileNotFoundException e) 
		{
			this.logger.error(getClass().getSimpleName(), e.getMessage());
		}
		
	}

}
