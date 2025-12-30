package fr.azures04.mods.multiauth;

import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import fr.azures04.mods.multiauth.config.SessionServersConfigManager;
import fr.azures04.mods.multiauth.publickeys.PublicKeys;
import fr.azures04.mods.multiauth.services.MultiSessionService;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;

@Mod(modid = Constants.MODID, name =Constants.NAME, version = Constants.VERSION, serverSideOnly = true, acceptableRemoteVersions = "*")
public class MultiAuth {

    public static Logger logger;
    public static SessionServersConfigManager config;

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        config = new SessionServersConfigManager(event.getModConfigurationDirectory());
        config.load();
        
        new Thread(() -> {
        	PublicKeys.fetchPublicKeys(config.getServers());
        }).start();
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
    }
    
    @EventHandler
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
    	MinecraftServer server = event.getServer();
    	logger.log(Level.INFO, "[MultiAuth] Injecting custom session server service");
    	
    	try {
    		YggdrasilAuthenticationService baseAuthService = new YggdrasilAuthenticationService(server.getServerProxy(), UUID.randomUUID().toString());
    		MultiSessionService service = new MultiSessionService(baseAuthService);
    		ObfuscationReflectionHelper.setPrivateValue(MinecraftServer.class, server, service, "sessionService", "field_147143_S");
        	logger.log(Level.INFO, "[MultiAuth] Injection succeed");
    	} catch (Exception e) {
			logger.error("[MultiAuth] Error occured while trying to inject custom session server service");
			e.printStackTrace();
		}
    }
}
