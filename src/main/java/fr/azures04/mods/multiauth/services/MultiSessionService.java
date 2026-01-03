package fr.azures04.mods.multiauth.services;

import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;

import fr.azures04.mods.multiauth.Constants;
import fr.azures04.mods.multiauth.Constants.Endpoints;
import fr.azures04.mods.multiauth.MultiAuth;
import fr.azures04.mods.multiauth.exceptions.MissingTexturePropertiesException;
import fr.azures04.mods.multiauth.exceptions.UUIDTakenException;
import fr.azures04.mods.multiauth.exceptions.UsernameTakenException;
import fr.azures04.mods.multiauth.helpers.JsonHelper;
import fr.azures04.mods.multiauth.helpers.PlayerHelper;
import fr.azures04.mods.multiauth.helpers.PublicKeysHelper;
import fr.azures04.mods.multiauth.helpers.RequestHelper;

public class MultiSessionService extends YggdrasilMinecraftSessionService {

	public MultiSessionService(YggdrasilAuthenticationService authenticationService) {
		super(authenticationService);
	}
	
	@Override
	public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) throws AuthenticationUnavailableException {
	    String cacheKey = user.getName().toLowerCase() + ":" + user.getId();
	    Object cachedServerName = MultiAuth.cache.getCacheMap().get(cacheKey);

	    if (cachedServerName != null) {
	        String serverName = cachedServerName.toString();
	        MultiAuth.logger.info("[MultiAuth] Cache hit for " + user.getName() + ": " + serverName);

	        URL url = MultiAuth.config.getServers().get(serverName);
	        if (url != null && url.toString().contains(Constants.BLOCKED_ENDPOINT)) {
	            return super.hasJoinedServer(user, serverId, address);
	        }
	        
	    }

	    return bulkHasJoined(user, serverId);
	}
	
	private GameProfile bulkHasJoined(GameProfile user, String serverId) {
	    String username = user.getName();
	    
	    if (MultiAuth.config.isCacheAuthServerEnabled()) {
	        for (Map.Entry<String, String> entry : MultiAuth.cache.getCacheMap().entrySet()) {
	            if (entry.getKey().startsWith(username + ":")) {
	                String cachedServerName = entry.getValue();
	                URL url = MultiAuth.config.getServers().get(cachedServerName);
	                
	                if (url != null) {
	                    try {
	                        MultiAuth.logger.info("[MultiAuth] Testing cached server for " + username + ": " + cachedServerName);
	                        GameProfile profile = hasJoined(user, serverId, url, cachedServerName);
	                        if (profile != null) {
	                            return profile;
	                        }
	                    } catch (Exception e) {
	                        MultiAuth.logger.error("[MultiAuth] Cache hit failed for " + cachedServerName);
	                    }
	                }
	            }
	        }
	    }

	    for (Map.Entry<String, URL> server : MultiAuth.config.getServers().entrySet()) {
	        String name = server.getKey();
	        URL url = server.getValue();
	        
	        MultiAuth.logger.log(Level.INFO, "[MultiAuth] Verifying on: " + name + " (" + url + ")");
	        
	        try {
	            GameProfile profile = hasJoined(user, serverId, url, name);
	            if (profile != null) {
	                MultiAuth.logger.info("[MultiAuth] Successfully authenticated " + username + " on " + name);
	                if (MultiAuth.config.isCacheAuthServerEnabled()) {
	                    MultiAuth.cache.save(username, profile.getId().toString(), name);
	                }
	                
	                return profile;
	            }
	        } catch (Exception e) {
	            MultiAuth.logger.error("[MultiAuth] Error during auth on " + name + ": " + e.getMessage());
	        }
	    }

	    MultiAuth.logger.warn("[MultiAuth] No authentication server recognized " + username);
	    return null;
	}
	
	private GameProfile hasJoined(GameProfile user, String serverId, URL url, String name) throws Exception {
	    if (url.toString().contains(Constants.BLOCKED_ENDPOINT)) {
			return super.hasJoinedServer(user, serverId, null);
		}
		
		String hasJoinedEndpoint = url + Endpoints.HAS_JOINED + "?username=" + user.getName() + "&serverId=" + serverId;
	    String response = RequestHelper.get(hasJoinedEndpoint);
	    
	    if (response == null || response.trim().isEmpty()) {
	        return null;
	    }
	    
	    JSONObject json = JsonHelper.parseObject(response);
	    
	    String idStr = JsonHelper.getString(json, "id", null);
	    String nameStr = JsonHelper.getString(json, "name", null);
	    JSONArray properties = JsonHelper.getArray(json, "properties");

	    if (idStr == null || nameStr == null || properties == null) {
	        return null;
	    }
	    
	    UUID realUUID = PlayerHelper.parseUUID(idStr); 
	    
	    GameProfile authProfile = new GameProfile(realUUID, nameStr);

	    JSONObject textureObj = JsonHelper.getObjectByFieldValue(properties, "name", "textures");
	    if (textureObj == null) {
	        throw new MissingTexturePropertiesException("Missing texture property");
	    }

	    String signature = JsonHelper.getString(textureObj, "signature", null);
	    String textureValue = JsonHelper.getString(textureObj, "value", null);
	    
	    if (signature != null && !signature.isEmpty()) {
	        PublicKey key = PublicKeysHelper.loadedPublicKeys.get(name);
	        boolean isTextureValid = PublicKeysHelper.verifySignature(textureValue, signature, key);
	        
	        if (!isTextureValid) {
	            MultiAuth.logger.error("[MultiAuth] Signature invalid for " + nameStr);
	            return null;
	        }
	    }

	    for (int i = 0; i < properties.size(); i++) {
	        JSONObject prop = JsonHelper.getObject(properties, i);
	        String pName = JsonHelper.getString(prop, "name", "");
	        String pValue = JsonHelper.getString(prop, "value", "");
	        String pSig = JsonHelper.getString(prop, "signature", null);
	        authProfile.getProperties().put(pName, new Property(pName, pValue, pSig));
	    }

	    return createPlayer(authProfile, name);
	}
	
	private GameProfile createPlayer(GameProfile user, String serverName) throws AuthenticationException {
	    String originalName = user.getName();
	    UUID originalUUID = user.getId();
	    
	    int serverIndex = MultiAuth.config.getServerIndex(serverName);
	    
	    boolean nameTaken = PlayerHelper.isUsernameTakenOnServer(originalName);
	    
	    if (nameTaken && !MultiAuth.config.isAllowDuplicateNames()) {
	        throw new UsernameTakenException("Username already taken on this server.");
	    }

	    String finalName = originalName;
	    UUID finalUUID = originalUUID;

	    if (nameTaken || PlayerHelper.isUUIDTakenOnServer(originalUUID)) {
	        finalName = originalName + "@" + serverIndex;
	        String uuidSeed = serverName + originalUUID.toString();
	        finalUUID = UUID.nameUUIDFromBytes(uuidSeed.getBytes(StandardCharsets.UTF_8));
	        
	        if (PlayerHelper.isUUIDTakenOnServer(finalUUID)) {
	        	throw new UUIDTakenException("Are you already logged in? UUID already used.");
	        }
	    }

	    GameProfile player = new GameProfile(finalUUID, finalName);
	    player.getProperties().putAll(user.getProperties());
	    return player;
	}
}
