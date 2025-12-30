package fr.azures04.mods.multiauth.publickeys;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Level;

import fr.azures04.mods.multiauth.MultiAuth;
import fr.azures04.mods.multiauth.Constants;
import fr.azures04.mods.multiauth.Constants.Endpoints;
import fr.azures04.mods.multiauth.config.SessionServersConfig;
import fr.azures04.mods.multiauth.helpers.RequestHelper;

public class PublicKeys {
	
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
				
				server.loadedPublicKey = decode(publicKeysJsonResponse);
				
				if (server.loadedPublicKey != null) {
					MultiAuth.logger.log(Level.INFO, "[MultiAuth] Loaded key for : " + server.getName());
				} else {
					MultiAuth.logger.error("[MultiAuth] No valid key found in the response for : " + server.getUrl());
				}
			} catch (Exception e) {
				MultiAuth.logger.error("[MultiAuth] Failed to connect to : " + server.getUrl());
				e.printStackTrace();
			}
		}
	}

	private static PublicKey decode(String key) throws Exception {
		String header = "-----BEGIN PUBLIC KEY-----";
        String footer = "-----END PUBLIC KEY-----";

        int start = key.indexOf(header);
        int end = key.indexOf(footer);
        
        if (start == -1 || end == -1) {
			return null;
		}
        
        String pem = key.substring(start + header.length(), end).replaceAll("\\s+", "");
        byte[] keyBytes = Base64.decodeBase64(pem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFectory = KeyFactory.getInstance("RSA");
        
		return keyFectory.generatePublic(keySpec);
	}
	
}
