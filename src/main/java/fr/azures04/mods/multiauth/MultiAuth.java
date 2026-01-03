package fr.azures04.mods.multiauth;

import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import fr.azures04.mods.multiauth.helpers.CacheHelper;
import fr.azures04.mods.multiauth.helpers.ConfigHelper;
import fr.azures04.mods.multiauth.helpers.PublicKeysHelper;
import fr.azures04.mods.multiauth.services.MultiSessionService;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;

@Mod(modid = Constants.MODID, name = Constants.NAME, version = Constants.VERSION)
public class MultiAuth {

    public static Logger logger;
    public static ConfigHelper config;
    public static CacheHelper cache;
    
    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        config = new ConfigHelper(event.getModConfigurationDirectory());
        config.load();
        cache = new CacheHelper(event.getModConfigurationDirectory());
        cache.load();
        PublicKeysHelper.fetchPublicKeys();
        logger.log(Level.INFO, "[MultiAuth] FMLPreInitializationEvent done");
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
        logger.log(Level.INFO, "[MultiAuth] FMLInitializationEvent done");
    }
    
    @EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        logger.log(Level.INFO, "[MultiAuth] FMLPostInitializationEvent done");
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
