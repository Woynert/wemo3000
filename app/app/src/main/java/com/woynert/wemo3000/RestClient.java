package com.woynert.wemo3000;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class RestClient {
    public static boolean ping (String address, int port, int timeout) {
        try {
            URL url = new URL (String.format("http://%s:%d/ping", address, port));
            //System.out.println(String.format("http://%s:%d/ping", address, port));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(timeout);
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            }
        } catch (Exception e) {
            System.err.println (e);
        }
        return false;
    }
}
