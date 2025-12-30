package fr.azures04.mods.multiauth.services;

import java.net.InetAddress;
import java.util.UUID;

import org.apache.logging.log4j.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.util.UUIDTypeAdapter;

import fr.azures04.mods.multiauth.Constants;
import fr.azures04.mods.multiauth.Constants.Endpoints;
import fr.azures04.mods.multiauth.MultiAuth;
import fr.azures04.mods.multiauth.helpers.RequestHelper;
import fr.azures04.mods.multiauth.pojo.ResponseProfile;
import fr.azures04.mods.multiauth.pojo.SessionServersConfig;
import fr.azures04.mods.multiauth.pojo.ResponseProfile.ResponseProperty;

public class MultiSessionService extends YggdrasilMinecraftSessionService {

	private static final Gson GSON = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
	
	public MultiSessionService(YggdrasilAuthenticationService authenticationService) {
		super(authenticationService);
	}

	@Override
	public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) throws AuthenticationUnavailableException {
		for(SessionServersConfig sessionServers : MultiAuth.config.getServers()) {
			MultiAuth.logger.log(Level.INFO, "[MultiAuth] Verifying session for " + user.getName() + "@" + sessionServers.getName());
			GameProfile player = null;
			if (sessionServers.getUrl().contains(Constants.BLOCKED_ENDPOINT)) {
				try {
					player = super.hasJoinedServer(user, serverId, address);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				player = hasJoinedServerOn(sessionServers, user.getName(), serverId);
				MultiAuth.logger.log(Level.INFO, "[MultiAuth] Succesfully auth session on : " + user.getName() + "@" + sessionServers.getName());
			}
			if (player != null) {
				MultiAuth.logger.log(Level.INFO, "[MultiAuth] " + user.getName() + "@" + sessionServers.getName() + " authenticated succesfully");
				return player;
			}
		}
		MultiAuth.logger.log(Level.WARN, "[MultiAuth] Could not authenticate " + user.getName() + " on any server.");
		return null;
	}

	private GameProfile hasJoinedServerOn(SessionServersConfig server, String username, String serverId) {
		try {
			String hasJoinedEndpoint = server.getUrl() + Endpoints.HAS_JOINED + "?username=" + username + "&serverId=" + serverId;
			String hasJoinedJsonResponse = RequestHelper.get(hasJoinedEndpoint);
			
			if (hasJoinedJsonResponse == null || hasJoinedJsonResponse.trim().isEmpty()) {
				return null;
			}
			
			ResponseProfile responseProfile = GSON.fromJson(hasJoinedJsonResponse, ResponseProfile.class);
			if (responseProfile != null && responseProfile.id != null && responseProfile.name != null) {
				GameProfile profile = new GameProfile(responseProfile.getUUID(), responseProfile.name);
				if (responseProfile.properties != null) {
				    for (ResponseProperty prop : responseProfile.properties) {
				        profile.getProperties().put(prop.name, new Property(prop.name, prop.value, prop.signature));
				    }
				}
				
			    return profile;
			}
		} catch (java.io.FileNotFoundException e) {
	        return null;

	    } catch (java.net.SocketTimeoutException | java.net.ConnectException e) {
	    	MultiAuth.logger.warn("[MultiAuth] Unable to connect to " + server.getName() + " (" + e.getMessage() + ")");
	        return null;

	    } catch (com.google.gson.JsonSyntaxException e) {
	    	MultiAuth.logger.error("[MultiAuth] Received invalid data from : " + server.getName());
	        return null;

	    } catch (Exception e) {
	    	MultiAuth.logger.error("[MultiAuth] Unkown error on " + server.getName() + " : " + e.getClass().getSimpleName());
	        return null;
	    }
		return null;
	}
		
}
