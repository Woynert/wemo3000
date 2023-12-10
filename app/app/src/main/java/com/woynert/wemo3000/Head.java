package com.woynert.wemo3000;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Date;

public class Head {
    public Peer peer;
    private MDNSDiscovery discovery;

    public void setup (View view) {
        discovery = new MDNSDiscovery();

        Thread thread = new Thread(() -> {
            discovery.startDiscovery(view, (Peer peer) -> {
                this.peer = peer;
                Toast.makeText(view.getContext(), "Peer Found " + peer.ip + " : " + peer.port, Toast.LENGTH_LONG).show();
                Log.d("TAG", "Peer Found " + peer.ip + " : " + peer.port);
            });
        });
        thread.start();

        final Handler handler = new Handler();
        final int delay = 2000;

        // ping loop
        handler.postDelayed(new Runnable() {
            public void run() {
                servicePingLoog();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public void servicePingLoog () {
        Thread thread = new Thread(() -> {
            if (peer == null) return;

            // TODO: ping service
            boolean res = RestClient.ping(peer.ip, peer.port, 2000);
            if (res) {
                peer.lastTimeActive = new Date();
            }

            // TODO: delete after too much time has passed without an answer
        });
        thread.start();
    }
}
