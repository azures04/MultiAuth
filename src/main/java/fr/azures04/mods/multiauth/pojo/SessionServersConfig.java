package fr.azures04.mods.multiauth.pojo;

import java.security.PublicKey;

public class SessionServersConfig {
	private String name;
    private String url;
    public transient PublicKey loadedPublicKey;
    
    public SessionServersConfig() { }
    
	public SessionServersConfig(String name, String url) {
		this.name = name;
		this.url = url;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
}
