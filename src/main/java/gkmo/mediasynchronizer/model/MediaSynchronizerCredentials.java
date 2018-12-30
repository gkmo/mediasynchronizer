package gkmo.mediasynchronizer.model;

public class MediaSynchronizerCredentials 
{
	private FickrCredentials flickrCredentials;
	private GooglePhotosCredentials googlePhotosCredentials;
	private AmazonDriveCredentils amazonDriveCredentials;

	public FickrCredentials getFlickrCredentials() {
		return flickrCredentials;
	}
	
	public void setFlickrCredentials(FickrCredentials flickrCredentials) {
		this.flickrCredentials = flickrCredentials;
	}
	
	public GooglePhotosCredentials getGooglePhotosCredentials() {
		return googlePhotosCredentials;
	}
	
	public void setGooglePhotosCredentials(GooglePhotosCredentials googlePhotosCredentials) {
		this.googlePhotosCredentials = googlePhotosCredentials;
	}
	
	public AmazonDriveCredentils getAmazonDriveCredentials() {
		return amazonDriveCredentials;
	}
	
	public void setAmazonDriveCredentials(AmazonDriveCredentils amazonDriveCredentials) {
		this.amazonDriveCredentials = amazonDriveCredentials;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amazonDriveCredentials == null) ? 0 : amazonDriveCredentials.hashCode());
		result = prime * result + ((flickrCredentials == null) ? 0 : flickrCredentials.hashCode());
		result = prime * result + ((googlePhotosCredentials == null) ? 0 : googlePhotosCredentials.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MediaSynchronizerCredentials other = (MediaSynchronizerCredentials) obj;
		if (amazonDriveCredentials == null) {
			if (other.amazonDriveCredentials != null) {
				return false;
			}
		} else if (!amazonDriveCredentials.equals(other.amazonDriveCredentials)) {
			return false;
		}
		if (flickrCredentials == null) {
			if (other.flickrCredentials != null) {
				return false;
			}
		} else if (!flickrCredentials.equals(other.flickrCredentials)) {
			return false;
		}
		if (googlePhotosCredentials == null) {
			if (other.googlePhotosCredentials != null) {
				return false;
			}
		} else if (!googlePhotosCredentials.equals(other.googlePhotosCredentials)) {
			return false;
		}
		return true;
	}
	
	
	
	
}
