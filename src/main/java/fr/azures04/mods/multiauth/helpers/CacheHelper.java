package fr.azures04.mods.multiauth.helpers;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import org.apache.logging.log4j.Level;
import org.json.simple.JSONObject;

import fr.azures04.mods.multiauth.Constants;
import fr.azures04.mods.multiauth.MultiAuth;


public class CacheHelper {

	private static HashMap<String, String> cacheMap = new HashMap<>();
	private static File cacheFile;
	
	public CacheHelper(File configDir) {
	    cacheFile = new File(configDir, Constants.MODID + "_cache.json");
	}
	
	public static void createDefault() {
		try {
			JSONObject root = new JSONObject();
			root.put("azures04:1ab5f218-a6fa-4e69-be44-8b505a48d279", "thealfigame");

			try (FileWriter writer = new FileWriter(cacheFile)) {
				writer.write(root.toJSONString());
				writer.flush();
			}
			MultiAuth.logger.log(Level.INFO, "[MultiAuth] Cache created");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void load() {
		if (!MultiAuth.config.isCacheAuthServerEnabled()) return;
	    try {
	        if (!cacheFile.exists()) {
	            createDefault();
	        }
	        JSONObject root = JsonHelper.parseObject(cacheFile);
            cacheMap.clear();

            for (Object key : root.keySet()) {
                String playerKey = (String) key;
                String serverName = (String) root.get(playerKey);
                cacheMap.put(playerKey, serverName);
            }
            
	        MultiAuth.logger.log(Level.INFO, "[MultiAuth] Cache loaded");
	    } catch (Exception e) {
	        MultiAuth.logger.error("[MultiAuth] Error occurred while loading config");
	        e.printStackTrace();
	    }
	}
	
	public void save(String username, String uuid, String serverName) {
        if (!MultiAuth.config.isCacheAuthServerEnabled()) return;
        cacheMap.put(username + ":" + uuid, serverName);
        try (FileWriter writer = new FileWriter(cacheFile)) {
            JSONObject root = new JSONObject();
            root.putAll(cacheMap);
            writer.write(root.toJSONString());
            writer.flush();
        } catch (Exception e) {
            MultiAuth.logger.error("[MultiAuth] Failed to save cache");
        }
    }

    public HashMap<String, String> getCacheMap() {
		return cacheMap;
	}
	
}
