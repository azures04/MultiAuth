package fr.azures04.mods.multiauth.pojo;

import java.util.List;

public class YggdrasilKeysResponse {
    public List<KeyEntry> profilePropertyKeys;
    
    public static class KeyEntry {
        public String publicKey;
    }
}