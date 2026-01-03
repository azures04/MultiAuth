package fr.azures04.mods.multiauth.helpers;

import java.io.File;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonHelper {

    private static final JSONParser PARSER = new JSONParser();

    public static JSONObject parseObject(File file) throws Exception {
        JSONParser parser = new JSONParser();
        try (java.io.FileReader reader = new java.io.FileReader(file)) {
            return (JSONObject) parser.parse(reader);
        }
    }
    
    public static JSONObject parseObject(String content) throws ParseException {
        return (JSONObject) PARSER.parse(content);
    }

    public static JSONArray parseArray(String content) throws ParseException {
        return (JSONArray) PARSER.parse(content);
    }

    public static String getString(JSONObject json, String key, String defaultValue) {
        Object res = json.get(key);
        return (res != null) ? res.toString() : defaultValue;
    }

    public static int getInt(JSONObject json, String key, int defaultValue) {
        Object res = json.get(key);
        if (res instanceof Number) {
            return ((Number) res).intValue();
        }
        return defaultValue;
    }

    public static boolean getBoolean(JSONObject json, String key, boolean defaultValue) {
        Object res = json.get(key);
        if (res instanceof Boolean) {
            return (Boolean) res;
        }
        return defaultValue;
    }

    public static JSONObject getObject(JSONObject json, String key) {
        return (JSONObject) json.get(key);
    }

    public static JSONArray getArray(JSONObject json, String key) {
        return (JSONArray) json.get(key);
    }

    public static String getString(JSONArray array, int index) {
        Object res = array.get(index);
        return (res != null) ? res.toString() : null;
    }

    public static JSONObject getObject(JSONArray array, int index) {
        return (JSONObject) array.get(index);
    }

    public static boolean has(JSONObject json, String key) {
        return json.containsKey(key);
    }
    
    public static JSONObject getObjectByFieldValue(JSONArray array, String keyName, String valueToFind) {
        if (array == null) return null;

        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = getObject(array, i);
            if (obj != null) {
                String val = getString(obj, keyName, "");
                if (val.equals(valueToFind)) {
                    return obj;
                }
            }
        }
        return null;
    }
}