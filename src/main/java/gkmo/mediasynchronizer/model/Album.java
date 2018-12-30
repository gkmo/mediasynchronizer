package gkmo.mediasynchronizer.model;

public class Album {

    private final String id;
    private final String title;
    private final int photosCount;
    private final boolean isWriteable;

    public Album(String id, String title, int photoCounts, boolean isWriteable) {
        this.id = id;
        this.title = title;
        this.photosCount = photoCounts;
        this.isWriteable = isWriteable;
    }
    
    public String getId() {
        return id;
    }

    public boolean isWriteable() {
		return isWriteable;
	}

	public String getTitle() {
        return title;
    }

    public int getPhotosCount() {
        return photosCount;
    }
    
    @Override
    public int hashCode() {
    	return getTitle().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (!(obj instanceof Album)) {
    		return false;
    	}
    	return getTitle().equals(((Album)obj).getTitle());
    }
}
