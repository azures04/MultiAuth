package fr.azures04.mods.multiauth.services.objects;

import java.util.List;
import java.util.UUID;

import com.mojang.util.UUIDTypeAdapter;

public class ResponseProfile {
    
    public String id; 
    public String name;
    public List<ResponseProperty> properties;

    public UUID getUUID() {
        try {
            return UUIDTypeAdapter.fromString(this.id);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static class ResponseProperty {
        public String name;
        public String value;
        public String signature;
    }
}