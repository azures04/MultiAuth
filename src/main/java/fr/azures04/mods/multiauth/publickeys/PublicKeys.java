package fr.azures04.mods.multiauth.publickeys;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.azures04.mods.multiauth.Constants;
import fr.azures04.mods.multiauth.Constants.Endpoints;
import fr.azures04.mods.multiauth.MultiAuth;
import fr.azures04.mods.multiauth.helpers.RequestHelper;
import fr.azures04.mods.multiauth.pojo.SessionServersConfig;
import fr.azures04.mods.multiauth.pojo.YggdrasilKeysResponse;

public class PublicKeys {
	
	private static final Gson GSON = new GsonBuilder().create();
	
	public static void fetchPublicKeys(List<SessionServersConfig> sessionServers) {
		for (SessionServersConfig sessionServersConfig : sessionServers) {
			SessionServersConfig server = sessionServersConfig;
			if (server.getUrl().contains(Constants.BLOCKED_ENDPOINT)) {
				continue;
			}
			MultiAuth.logger.log(Level.INFO, "[MultiAuth] Getting key for : " + server.getUrl());
			try {
				String publicKeysEndpoint = server.getUrl() + Endpoints.PUBLIC_KEYS;
				String publicKeysJsonResponse = RequestHelper.get(publicKeysEndpoint);
				YggdrasilKeysResponse publicKeys = GSON.fromJson(publicKeysJsonResponse, YggdrasilKeysResponse.class);
				
				
				
				if (publicKeys != null && publicKeys.profilePropertyKeys != null && !publicKeys.profilePropertyKeys.isEmpty()) {
					String publicKey = publicKeys.profilePropertyKeys.get(0).publicKey;
					server.loadedPublicKey = decode(publicKey);
					if (server.loadedPublicKey != null) {
						MultiAuth.logger.info("[MultiAuth] PublicKey successfully loaded for: " + server.getName());
					} else {
						MultiAuth.logger.error("[MultiAuth] No valid key found in the response for : " + server.getUrl());
					}
				} else {
					MultiAuth.logger.error("[MultiAuth] No valid 'profilePropertyKeys' found for: " + server.getName());
				}
			} catch (Exception e) {
				MultiAuth.logger.error("[MultiAuth] Failed to connect to : " + server.getUrl());
				e.printStackTrace();
			}
		}
	}

	private static PublicKey decode(String key) throws Exception {
		String pem = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        
        byte[] keyBytes = Base64.decodeBase64(pem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFectory = KeyFactory.getInstance("RSA");
        
		return keyFectory.generatePublic(keySpec);
	}
	
}
