package gkmo.mediasynchronizer.model;

public abstract class MediaSynchronizerRequest 
{
	private String sourceService;
	private String destinationService;
	private MediaSynchronizerCredentials sourceCredentials;
	private MediaSynchronizerCredentials destinationCredentials;
	
	public MediaSynchronizerCredentials getSourceCredentials() {
		return sourceCredentials;
	}

	public void setSourceCredentials(MediaSynchronizerCredentials sourceCredentials) {
		this.sourceCredentials = sourceCredentials;
	}

	public MediaSynchronizerCredentials getDestinationCredentials() {
		return destinationCredentials;
	}

	public void setDestinationCredentials(MediaSynchronizerCredentials destinationCredentials) {
		this.destinationCredentials = destinationCredentials;
	}

	public String getSourceService() {
		return sourceService;
	}
	
	public void setSourceService(String sourceService) {
		this.sourceService = sourceService;
	}
	
	public String getDestinationService() {
		return destinationService;
	}
	
	public void setDestinationService(String destinationService) {
		this.destinationService = destinationService;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destinationCredentials == null) ? 0 : destinationCredentials.hashCode());
		result = prime * result + ((destinationService == null) ? 0 : destinationService.hashCode());
		result = prime * result + ((sourceCredentials == null) ? 0 : sourceCredentials.hashCode());
		result = prime * result + ((sourceService == null) ? 0 : sourceService.hashCode());
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
		MediaSynchronizerRequest other = (MediaSynchronizerRequest) obj;
		if (destinationCredentials == null) {
			if (other.destinationCredentials != null) {
				return false;
			}
		} else if (!destinationCredentials.equals(other.destinationCredentials)) {
			return false;
		}
		if (destinationService == null) {
			if (other.destinationService != null) {
				return false;
			}
		} else if (!destinationService.equals(other.destinationService)) {
			return false;
		}
		if (sourceCredentials == null) {
			if (other.sourceCredentials != null) {
				return false;
			}
		} else if (!sourceCredentials.equals(other.sourceCredentials)) {
			return false;
		}
		if (sourceService == null) {
			if (other.sourceService != null) {
				return false;
			}
		} else if (!sourceService.equals(other.sourceService)) {
			return false;
		}
		return true;
	}

	
}
