package gkmo.mediasynchronizer.model;

import java.util.List;

public class DownloadPhotoRequest extends MediaSynchronizerRequest 
{
	private List<MediaItem> mediaItems;
	private String destinationAlbumId;

	public DownloadPhotoRequest()
	{
		
	}
	
	public DownloadPhotoRequest(List<MediaItem> mediaItems, String destinationAlbumId, MediaSynchronizerRequest request)
	{
		this.mediaItems = mediaItems;
		this.destinationAlbumId = destinationAlbumId;
		this.setSourceService(request.getSourceService());	
		this.setDestinationService(request.getDestinationService());
		this.setSourceCredentials(request.getSourceCredentials());
		this.setDestinationCredentials(request.getDestinationCredentials());
	}
	
	public List<MediaItem> getMediaItems() {
		return mediaItems;
	}

	public void setMediaItem(List<MediaItem> mediaItems) {
		this.mediaItems = mediaItems;
	}

	public String getDestinationAlbumId() {
		return destinationAlbumId;
	}

	public void setDestinationAlbumId(String destinationAlbumId) {
		this.destinationAlbumId = destinationAlbumId;
	}
}
