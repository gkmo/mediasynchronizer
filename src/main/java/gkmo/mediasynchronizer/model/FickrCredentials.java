package gkmo.mediasynchronizer.model;

public class FickrCredentials 
{
	private String authToken; 
	private String tokenSecret;
	
	public String getAuthToken() {
		return authToken;
	}
	
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
	
	public String getTokenSecret() {
		return tokenSecret;
	}
	
	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}
}
