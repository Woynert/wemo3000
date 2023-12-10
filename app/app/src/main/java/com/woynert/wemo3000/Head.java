package com.woynert.wemo3000;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
        final int delay = 10000;

        // ping loop
        handler.postDelayed(new Runnable() {
            public void run() {
                loop();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public void loop () {
        Thread thread = new Thread(() -> {
            // TODO: ping service
            //RestClient.ping(peer);
            //MDNSDiscovery.start();
        });
        thread.start();
    }
}
