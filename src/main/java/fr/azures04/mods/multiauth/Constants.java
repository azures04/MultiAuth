package fr.azures04.mods.multiauth;

public class Constants {

    public static final String MODID = "multiauth";
    public static final String NAME = "MultiAuth";
    public static final String VERSION = "0.0.1";
	
    public static final String BLOCKED_ENDPOINT = "sessionserver.mojang.com";
    
    public static class Endpoints {
    	public static final String PUBLIC_KEYS = System.getProperty("endpoints.publickeys", "/minecraftservices/publickeys");
    	public static final String HAS_JOINED = System.getProperty("endpoints.hasjoined", "/sessionserver/session/minecraft/hasJoined");
    }
    
}
