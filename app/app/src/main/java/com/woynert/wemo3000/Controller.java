package com.woynert.wemo3000;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.util.Date;

public class Controller {
    public Peer peer;
    private MdnsDiscovery discovery;
    Handler handlerPingLoop;

    public void setup (View view) {

        discovery = new MdnsDiscovery();
        discovery.startDiscovery(view, (Peer peer) -> {
            this.peer = peer;
            Log.d("TAG", "Peer Found " + peer.ip + " : " + peer.port);
        });

        handlerPingLoop = new Handler();
        final int delay = 2000;

        // ping loop
        handlerPingLoop.postDelayed(new Runnable() {
            public void run() {
                servicePingLoop();
                if (handlerPingLoop != null) {
                    handlerPingLoop.postDelayed(this, delay);
                }
            }
        }, delay);
    }

    public void servicePingLoop () {
        Thread thread = new Thread(() -> {
            if (peer == null) return;

            // TODO: ping service
            boolean res = RestClient.ping(peer.ip, peer.port, 2000);
            if (res) {
                peer.lastTimeActive = new Date();
            }

            // TODO: delete after too much time has passed without an answer
            // then start MDNS discovery again
        });
        thread.start();
    }

    public void stop () {
        handlerPingLoop.removeCallbacksAndMessages(null);
        handlerPingLoop = null;
        discovery.stop();
    }
}
