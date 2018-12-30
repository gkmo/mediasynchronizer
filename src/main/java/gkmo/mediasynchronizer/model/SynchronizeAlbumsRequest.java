package gkmo.mediasynchronizer.model;

public class SynchronizeAlbumsRequest extends MediaSynchronizerRequest
{
	private String sourceAlbumId;	
	private String destinationAlbumId;
	
	public String getSourceAlbumId() {
		return sourceAlbumId;
	}
	
	public void setSourceAlbumId(String sourceAlbumId) {
		this.sourceAlbumId = sourceAlbumId;
	}
	
	public String getDestinationAlbumId() {
		return destinationAlbumId;
	}
	
	public void setDestinationAlbumId(String destinationAlbumId) {
		this.destinationAlbumId = destinationAlbumId;
	}
}
