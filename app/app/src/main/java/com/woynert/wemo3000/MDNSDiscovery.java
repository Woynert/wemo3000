package com.woynert.wemo3000;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.github.druk.dnssd.BrowseListener;
import com.github.druk.dnssd.DNSSD;
import com.github.druk.dnssd.DNSSDBindable;
import com.github.druk.dnssd.DNSSDException;
import com.github.druk.dnssd.DNSSDService;
import com.github.druk.dnssd.NSType;
import com.github.druk.dnssd.QueryListener;
import com.github.druk.dnssd.ResolveListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;

public class MDNSDiscovery {

    private DNSSD dnssd;
    private String addressFound; // TODO: Use a list or collection of Peers
    private PeerUpdateCallback peerUpdateCallback;

    private interface PeerUpdateCallback {
        void onCallback();
    }

    public void startDiscovery (Activity activity) {
        System.out.println("listening I guess1");

        peerUpdateCallback = () -> {
            Toast.makeText(activity, "Found " + addressFound, Toast.LENGTH_LONG).show();
            System.out.println("Found " + addressFound);
        };

        try {
            dnssd = new DNSSDBindable(activity);
            DNSSDService browseService = dnssd.browse("_wemo3000._tcp", new Listener());
        } catch (DNSSDException e) {
            Log.e("TAG", "error", e);
        }
    }

    private class Listener implements BrowseListener {
        @Override
        public void serviceFound(DNSSDService browser, int flags, int ifIndex,
                                 final String serviceName, String regType, String domain) {
            Log.i("TAG", "Found " + serviceName + " " + regType + " " + domain + " " + flags + " " + ifIndex);
            startResolve(flags, ifIndex, serviceName, regType, domain);
        }
        @Override
        public void serviceLost(DNSSDService browser, int flags, int ifIndex,
                                String serviceName, String regType, String domain) {
            Log.i("TAG", "Lost " + serviceName);
        }
        @Override
        public void operationFailed(DNSSDService service, int errorCode) {
            Log.e("TAG", "error: " + errorCode);
        }
    }

    private void startResolve(int flags, int ifIndex, final String serviceName, final String regType, final String domain) {
        try {
            dnssd.resolve(flags, ifIndex, serviceName, regType, domain, new ResolveListener() {
                @Override
                public void serviceResolved(DNSSDService resolver, int flags, int ifIndex, String fullName, String hostName, int port, Map<String, String> txtRecord) {
                    Log.d("TAG", "Resolved " + flags + " " + ifIndex + " " + hostName + " " + port + "\n" + txtRecord);

                    startQueryRecords(ifIndex, serviceName, regType, domain, hostName, port, txtRecord);
                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {}
            });
        } catch (DNSSDException e) {
            e.printStackTrace();
        }
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

                        try {
                            InetAddress address = InetAddress.getByAddress(rdata);
                            Log.d("TAG", address.getHostAddress() + " " + ifIndex + " " + fullName);

                            PingRunnable runnable = new PingRunnable(address.getHostAddress(), port);
                            Thread thread = new Thread(runnable);
                            thread.start();
                            thread.join();
                            boolean reachable = runnable.getResult();

                            if (reachable) {
                                Log.d("TAG", "reachable " + address.getHostAddress() + " " + ifIndex + " " + fullName);
                                addressFound = address.getHostAddress();
                                peerUpdateCallback.onCallback();
                            }
                            else {
                                Log.d("TAG", "NOT reachable " + address.getHostAddress() + " " + ifIndex + " " + fullName);
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }


                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {
                    Log.d("TAG", "Unable to find the address : " + errorCode);
                }

            };

            dnssd.queryRecord(0, 0, hostName, NSType.A, 1, listener);
            // TODO: add IPv6 support (NSType.AAAA)
        } catch (DNSSDException e) {
            e.printStackTrace();
        }
    }

    private static InetAddress getDeviceIpAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress address = interfaceAddress.getAddress();
                    if (address != null && !address.isLoopbackAddress()) {
                        return address;
                    }
                }
            }
        }

        return null;
    }
}
