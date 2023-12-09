package com.woynert.wemo3000;

import android.app.Activity;
import android.util.Log;

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

    public void startDiscovery (Activity activity) {
        System.out.println("listening I guess1");
        try {
            dnssd = new DNSSDBindable(activity);
            DNSSDService browseService = dnssd.browse("_wemo3000._tcp", new Listener());
        } catch (DNSSDException e) {
            Log.e("TAG", "error", e);
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

    private void startQueryRecords(int ifIndex, final String serviceName, final String regType, final String domain, final String hostName, final int port, final Map<String, String> txtRecord) {
        try {

            QueryListener listener = new QueryListener() {
                @Override
                public void queryAnswered(DNSSDService query, int flags, int ifIndex, String fullName, int rrtype, int rrclass, byte[] rdata, int ttl) {

                    try {
                        InetAddress address = InetAddress.getByAddress(rdata);
                        Log.d("TAG", address.getHostAddress() + " " + ifIndex + " " + fullName);
                    } catch (UnknownHostException e) {
                        System.out.println(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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
