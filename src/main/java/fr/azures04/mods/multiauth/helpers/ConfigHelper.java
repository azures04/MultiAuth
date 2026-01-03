package fr.azures04.mods.multiauth.helpers;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import fr.azures04.mods.multiauth.Constants;
import fr.azures04.mods.multiauth.MultiAuth;


public class ConfigHelper {

	private static File configFile;
	private boolean cacheAuthServer;
	private boolean allowDuplicateNames;
	private HashMap<String, URL> servers = new HashMap<String, URL>();
	
	public ConfigHelper(File configDir) {
	    configFile = new File(configDir, Constants.MODID + ".json");
	}
	
	@SuppressWarnings("unchecked")
	public static void createDefault() {
		try {
			JSONObject root = new JSONObject();
			root.put("cache", true);
			root.put("allowDuplicateNames", true);
			
			JSONObject mojangSessionServer = new JSONObject();
			mojangSessionServer.put("name", "Mojang's Session Server");
			mojangSessionServer.put("url", new URL("https://sessionserver.mojang.com").toString());
			
			JSONArray servers = new JSONArray();
			servers.add(mojangSessionServer);
			
			root.put("server", servers);
			
			try (FileWriter writer = new FileWriter(configFile)) {
				writer.write(root.toJSONString());
				writer.flush();
			}
			MultiAuth.logger.log(Level.INFO, "[MultiAuth] Config created");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void load() {
	    try {
	        if (!configFile.exists()) {
	            createDefault();
	        }
	        
	        JSONObject root = JsonHelper.parseObject(configFile);
	        this.cacheAuthServer = JsonHelper.getBoolean(root, "cache", true);
	        this.allowDuplicateNames = JsonHelper.getBoolean(root, "allowDuplicateNames", true);
	        
	        JSONArray sessionServers = JsonHelper.getArray(root, "server");
	        
	        if (sessionServers != null) {
	            this.servers.clear();
	            for (Object object : sessionServers) {
	                JSONObject sessionServer = (JSONObject) object;
	                
	                String name = JsonHelper.getString(sessionServer, "name", "Unknown");
	                String urlStr = JsonHelper.getString(sessionServer, "url", "");
	                
	                try {
	                    if (!urlStr.isEmpty()) {
	                        URL sessionServerUrl = new URL(urlStr);
	                        this.servers.put(name, sessionServerUrl);
	                    }
	                } catch (Exception e) {
	                    MultiAuth.logger.error("[MultiAuth] Invalid URL for " + name);
	                }
	            }
	        }
	        
	        MultiAuth.logger.log(Level.INFO, "[MultiAuth] Config loaded");
	    } catch (Exception e) {
	        MultiAuth.logger.error("[MultiAuth] Error occurred while loading config");
	        e.printStackTrace();
	    }
	}

	public boolean isCacheAuthServerEnabled() {
		return cacheAuthServer;
	}

	public boolean isAllowDuplicateNames() {
		return allowDuplicateNames;
	}

	public HashMap<String, URL> getServers() {
		return servers;
	}
	
	public int getServerIndex(String name) {
	    List<String> keys = new ArrayList<>(this.servers.keySet());
	    return keys.indexOf(name);
	}
	
}
