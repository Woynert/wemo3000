package com.woynert.wemo3000;

import android.util.Log;
import android.view.View;

import com.github.druk.dnssd.BrowseListener;
import com.github.druk.dnssd.DNSSD;
import com.github.druk.dnssd.DNSSDBindable;
import com.github.druk.dnssd.DNSSDException;
import com.github.druk.dnssd.DNSSDService;
import com.github.druk.dnssd.NSType;
import com.github.druk.dnssd.QueryListener;
import com.github.druk.dnssd.ResolveListener;

import java.net.InetAddress;
import java.util.Date;
import java.util.Map;

public class MdnsDiscovery {

    private DNSSD dnssd;
    private DNSSDService browseService;
    private PeerUpdateCallback peerUpdateCallback;

    public interface PeerUpdateCallback {
        void onCallback(Peer peer);
    }

    public void stop () {
        browseService.stop();
    }

    public void startDiscovery (View view, PeerUpdateCallback callback) {
        Log.d("MDNS", "Starting discovery");
        peerUpdateCallback = callback;

        try {
            // TODO: use one if the other isn't available
            dnssd = new DNSSDBindable(view.getContext());
            //dnssd = new DNSSDEmbedded(view.getContext());
            browseService = dnssd.browse("_wemo3000._tcp", new Listener());
        } catch (DNSSDException e) {
            Log.e("MDNS", "error", e);
        }
    }

    private class Listener implements BrowseListener {
        @Override
        public void serviceFound(DNSSDService browser, int flags, int ifIndex,
                                 final String serviceName, String regType, String domain) {
            Log.i("MDNS", "Found " + serviceName + " " + regType + " " + domain + " " + flags + " " + ifIndex);
            startResolve(flags, ifIndex, serviceName, regType, domain);
        }
        @Override
        public void serviceLost(DNSSDService browser, int flags, int ifIndex,
                                String serviceName, String regType, String domain) {
            Log.i("MDNS", "Lost " + serviceName);
        }
        @Override
        public void operationFailed(DNSSDService service, int errorCode) {
            Log.e("MDNS", "error: " + errorCode);
        }
    }

    private void startResolve(int flags, int ifIndex, final String serviceName, final String regType, final String domain) {
        new Thread(() -> {
            try {
                dnssd.resolve(flags, ifIndex, serviceName, regType, domain, new ResolveListener() {
                    @Override
                    public void serviceResolved(DNSSDService resolver, int flags, int ifIndex, String fullName, String hostName, int port, Map<String, String> txtRecord) {
                        Log.d("MDNS", "Resolved " + flags + " " + ifIndex + " " + hostName + " " + port + "\n" + txtRecord);
                        startQueryRecords(ifIndex, serviceName, regType, domain, hostName, port, txtRecord);
                    }

                    @Override
                    public void operationFailed(DNSSDService service, int errorCode) {}
                });
            } catch (DNSSDException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private class PingRunnable implements Runnable {
        private boolean result;
        private String ip;
        private int port;

        public PingRunnable (String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result = RestClient.ping(ip, port, 5000);
        }

        public boolean getResult() {
            return result;
        }
    }

    private void startQueryRecords(int ifIndex, final String serviceName, final String regType, final String domain, final String hostName, final int port, final Map<String, String> txtRecord) {
        try {

            QueryListener listener = new QueryListener() {
                @Override
                public void queryAnswered(DNSSDService query, int flags, int ifIndex, String fullName, int rrtype, int rrclass, byte[] rdata, int ttl) {

                    new Thread(() -> {
                        try {
                            InetAddress address = InetAddress.getByAddress(rdata);

                            PingRunnable runnable = new PingRunnable(address.getHostAddress(), port);
                            Thread thread = new Thread(runnable);
                            thread.start();
                            thread.join();
                            boolean reachable = runnable.getResult();

                            if (reachable) {
                                Log.d("MDNS", "reachable " + address.getHostAddress() + " " + ifIndex + " " + fullName);

                                Peer peer = new Peer();
                                peer.hostname = hostName;
                                peer.port = port;
                                peer.ip = address.getHostAddress();
                                peer.lastTimeActive = new Date();
                                peerUpdateCallback.onCallback(peer);

                                // TODO: start again when this peer is lost
                                browseService.stop();
                            } else {
                                Log.d("MDNS", "NOT reachable " + address.getHostAddress() + " " + ifIndex + " " + fullName);
                            }
                        } catch (Exception e) {
                            Log.e("MDNS", e.getMessage());
                        }
                    }).start();
                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {
                    Log.d("MDNS", "Unable to find the address : " + errorCode);
                }
            };

            dnssd.queryRecord(0, 0, hostName, NSType.A, 1, listener);
            // TODO: add IPv6 support (NSType.AAAA)
        } catch (DNSSDException e) {
            e.printStackTrace();
        }
    }
}
