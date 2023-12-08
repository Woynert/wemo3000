package com.woynert.wemo3000;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RestClient {
    Peer peer;

    public static boolean ping (Peer peer) {
        try {
            URL url = new URL (String.format("http://%s:%d/ping", peer.ip, peer.port));
            System.out.println(String.format("http://%s:%d/ping", peer.ip, peer.port));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject res = new JSONObject(response.toString());
                String hostname = res.get("hostname").toString();
                if (hostname != ""){
                    peer.hostname = hostname;
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println (e);
        }
        return false;
    }
}
