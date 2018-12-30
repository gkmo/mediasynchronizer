package gkmo.mediasynchronizer.model;

public class MediaItem {

	private String id;
	private String title;
	private String description;
	private String downloadUrl;
	
	public MediaItem()
	{
		
	}
	
	public MediaItem(String id, String title, String description, String downloadUrl) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.downloadUrl = downloadUrl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String filename) {
		this.description = filename;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

}
