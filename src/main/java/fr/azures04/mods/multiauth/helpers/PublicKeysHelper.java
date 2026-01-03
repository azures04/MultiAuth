package fr.azures04.mods.multiauth.helpers;

import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Level;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import fr.azures04.mods.multiauth.Constants;
import fr.azures04.mods.multiauth.Constants.Endpoints;
import fr.azures04.mods.multiauth.MultiAuth;

public class PublicKeysHelper {
    
    public static HashMap<String, PublicKey> loadedPublicKeys = new HashMap<>();
    
    public static void fetchPublicKeys() {
        for (Map.Entry<String, URL> server : MultiAuth.config.getServers().entrySet()) {
            URL url = server.getValue();
            String name = server.getKey();
            
            if (url.toString().contains(Constants.BLOCKED_ENDPOINT)) {
                continue;
            }
            
            MultiAuth.logger.log(Level.INFO, "[MultiAuth] Getting key for : " + url);
            try {
                String publicKeysEndpoint = url.toString() + Endpoints.PUBLIC_KEYS;
                String response = RequestHelper.get(publicKeysEndpoint);
                
                if (response == null || response.trim().isEmpty()) {
                    MultiAuth.logger.error("[MultiAuth] Failed to get key from : " + url);
                    continue;
                }

                JSONObject json = JsonHelper.parseObject(response);
                JSONArray keysArray = JsonHelper.getArray(json, "profilePropertyKeys");
                
                if (keysArray != null && !keysArray.isEmpty()) {
                    JSONObject keyObj = JsonHelper.getObject(keysArray, 0);
                    String publicKeyStr = JsonHelper.getString(keyObj, "publicKey", "");
                    
                    if (!publicKeyStr.isEmpty()) {
                        PublicKey key = decode(publicKeyStr);
                        loadedPublicKeys.put(name, key);
                        MultiAuth.logger.info("[MultiAuth] PublicKey successfully loaded for: " + name);
                    }
                } else {
                    MultiAuth.logger.error("[MultiAuth] No valid 'profilePropertyKeys' found for: " + name);
                }
                
            } catch (Exception e) {
                MultiAuth.logger.error("[MultiAuth] Failed to connect or parse key for : " + url);
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
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        
        return keyFactory.generatePublic(keySpec);
    }
    
    public static boolean verifySignature(String data, String signature, PublicKey key) {
        if (key == null) return false;
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(key);
            sig.update(data.getBytes("UTF-8"));
            return sig.verify(Base64.decodeBase64(signature));
        } catch (Exception e) {
            MultiAuth.logger.error("[MultiAuth] Signature verification failed", e);
            return false;
        }
    }
}