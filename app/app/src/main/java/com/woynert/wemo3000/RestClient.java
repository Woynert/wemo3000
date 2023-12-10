package com.woynert.wemo3000;

import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class RestClient {
    public static boolean ping (String address, int port, int timeout) {
        try {
            URL url = new URL (String.format("http://%s:%d/ping", address, port));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(timeout);
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            }
        } catch (Exception e) {
            Log.e ("REST", e.getMessage());
        }
        return false;
    }

    public static boolean shutdown (String address, int port, int timeout) {
        try {
            URL url = new URL (String.format("http://%s:%d/shutdown", address, port));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(timeout);
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            }
        } catch (Exception e) {
            Log.e ("REST", e.getMessage());
        }
        return false;
    }
}
