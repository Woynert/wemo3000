package com.woynert.wemo3000;

import android.app.Activity;
import android.util.Log;

import com.github.druk.dnssd.BrowseListener;
import com.github.druk.dnssd.DNSSD;
import com.github.druk.dnssd.DNSSDBindable;
import com.github.druk.dnssd.DNSSDException;
import com.github.druk.dnssd.DNSSDService;

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

    private static class Listener implements BrowseListener {
        @Override
        public void serviceFound(DNSSDService browser, int flags, int ifIndex,
        final String serviceName, String regType, String domain) {
            Log.i("TAG", "Found " + serviceName);
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
}
