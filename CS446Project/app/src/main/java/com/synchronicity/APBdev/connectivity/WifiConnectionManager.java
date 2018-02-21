package com.synchronicity.APBdev.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;

/**
 * Created by Andrew on 2018-02-15.
 */

public class WifiConnectionManager extends BaseConnectionManager {

    //Fields
    Context context;
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;


    // Constructors.
    public WifiConnectionManager(Context context) {
        this.context = context;
        this.manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = manager.initialize(context, context.getMainLooper(), null)
    }



    // Methods
    public void discoverPeers () {
        try {
            this.manager.discoverPeers(channel, new WifiP2pManager.ActionListener(){
                @Override
                public void onSuccess() {
                    // Stub
                }
                @Override
                public void onFailure (int reasonCode) {
                    throw new Exception("Could not find peers!");
                }
            });
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void requestPeers () {
            try {
                this.manager.requestPeers(this.channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        // Do stuff with available peers.
                    }
                });
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
    }


    // BroadcastReceiver stuff.
    @Override
    public BroadcastReceiver getBroadcastReceiver() {
        if (this.broadcastReceiver == null) {
            broadcastReceiver = new WcmBroadcastReceiver();
        }
        return broadcastReceiver;
    }

    public IntentFilter getIntentFilter() {
        if (this.intentFilter == null) {
            intentFilter = new WcmIntentFilter();
            // WiFi P2P filters.
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
            // Synchronicity filters.
        }
        return intentFilter;
    }

    private class WcmBroadcastReceiver extends BroadcastReceiver {
        private WcmBroadcastReceiver() {
            super();
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            /* Monolithic if statement here. */
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                /*
                    If wifi is enabled, everything is good, continue as normal ?
                    If wifi is disabled, notify user and cleanup any outstanding connections ?
                 */
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                /*
                    Do as the above comment says.
                 */
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
                /*
                    Cleanup resources associated with lost connection.
                 */
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
                /*
                    Similar to WIFI_P2P_STATE_CHANGED_ACTION ?
                 */
            }
        }
    }

    private class WcmIntentFilter extends IntentFilter {
        private WcmIntentFilter () {
            super();
        }
    }
}

