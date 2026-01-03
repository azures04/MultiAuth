package fr.azures04.mods.multiauth.helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.azures04.mods.multiauth.Constants;

public class RequestHelper {

	public static String get(String path) throws Exception {
		URL url = new URL(path);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		connection.setRequestProperty("User-Agent", Constants.NAME + "/" + Constants.MODID);
		
		BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
		String inputLine;
		StringBuilder content = new StringBuilder();
		
		if (connection.getResponseCode() != 200) {
			return null;
		}
		
		while ((inputLine = input.readLine()) != null) {
			content.append(inputLine);
		}
		
		input.close();
		return content.toString();
	}
	
}