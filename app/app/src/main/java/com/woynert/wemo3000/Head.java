package com.woynert.wemo3000;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;

public class Head {
    private String name = "ewa";
    private Peer peer;
    private MDNSDiscovery discovery;

    public void setup (Activity activity) {
        discovery = new MDNSDiscovery();

        // TODO: wrap in try catch

        Thread thread = new Thread(() -> {
            discovery.startDiscovery(activity);
        });
        thread.start();

        // dummy peer
        peer = new Peer();
        peer.ip = "10.42.0.1";
        peer.port = 2333;

        // start loop
        final Handler handler = new Handler();
        final int delay = 10000;

        handler.postDelayed(new Runnable() {
            public void run() {
                loop();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public void loop () {
        System.out.println(name);
        Thread thread = new Thread(() -> {
            // ping peer
            //RestClient.ping(peer);
            //System.out.println(peer.lastTimeActive);

            //MDNSDiscovery.start();
        });
        thread.start();
    }

}
