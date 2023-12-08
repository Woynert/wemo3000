package com.woynert.wemo3000;

import android.os.Handler;

public class Head {
    private String name = "ewa";
    private Peer peer;

    public void setup () {
        // dummy peer
        peer = new Peer();
        peer.ip = "10.42.0.1";
        peer.port = 2333;

        // start loop
        final Handler handler = new Handler();
        final int delay = 1000;

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
            RestClient.ping(peer);
        });
        thread.start();
    }

}
