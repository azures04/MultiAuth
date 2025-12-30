package fr.azures04.mods.multiauth.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.azures04.mods.multiauth.Constants;
import fr.azures04.mods.multiauth.MultiAuth;
import fr.azures04.mods.multiauth.pojo.SessionServersConfig;

public class SessionServersConfigManager {
	
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private File configFile;
    
    private List<SessionServersConfig> sessionServers = new ArrayList<>();

    public SessionServersConfigManager(File configDir) {
		File modConfigDir = new File(configDir, Constants.MODID);
		if (!modConfigDir.exists()) {
		    modConfigDir.mkdirs();
		}
		this.configFile = new File(modConfigDir, "auth_servers.json");
    }

    public void load() {
    	if (!configFile.exists()) {
    		createDefaultConfig();
		}
    	try (Reader reader = new FileReader(configFile)) {
			SessionServersConfig[] loadedServer = GSON.fromJson(reader, SessionServersConfig[].class);
			if (loadedServer != null) {
				this.sessionServers = new ArrayList<>(Arrays.asList(loadedServer));
			}
		} catch (Exception e) {
			MultiAuth.logger.error("Error occured while reading json config");
			e.printStackTrace();
		}
	}
    
    private void createDefaultConfig() {
    	List<SessionServersConfig> defaults = new ArrayList<>();
    	defaults.add(new SessionServersConfig("Mojang/Microsoft", "https://sessionserver.mojang.com"));
    	try (Writer writer = new FileWriter(configFile)) {
			GSON.toJson(defaults, writer);
		} catch (Exception e) {
			MultiAuth.logger.error("Error occured while writting default json config");
			e.printStackTrace();
		}
    }
    
    public List<SessionServersConfig> getServers() {
        return sessionServers;
    }
    
}
