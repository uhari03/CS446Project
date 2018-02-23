package com.synchronicity.APBdev.connectivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.util.Log;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrew on 2018-02-15.
 */

public class WifiConnectionManager extends BaseConnectionManager {
    //Fields
    private Context context;
    private Activity activity;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pDnsSdServiceInfo serviceInfo;
    private WifiP2pServiceRequest serviceRequest;
    private ServerSocket serverSocket;
    private Map<String, ...>
    // Enumerated Constants for NSD.
    private static final String NSD_SERVICE_INSTANCE_VALUE = "SynchronicityCS446";
    private static final String NSD_SERVICE_PROTOCOL_VALUE = "_presence._tcp";
    private static final String NSD_RECORD_APP_ID = "AppName";
    private static final String NSD_RECORD_SESSION_ID = "SessionName";
    private static final String NSD_RECORD_PORT_ID = "PortNum";



    // Debug constants
    private final String TAG = "WifiConnectionManager";

    // Constructors.
    public WifiConnectionManager(Context context, Activity activity) {
        this.activity = activity;
        this.context = context;
        this.manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = manager.initialize(context, context.getMainLooper(), null);
        this.serviceInfo = null;
        this.serviceRequest = null;
        // Things that throw exceptions go here.
        try {
            this.serverSocket = new ServerSocket(0);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }


    @Override
    public void sendData(){
        return;
    }


    @Override
    public void receiveData() {
        return;
    }


    // --- Methods for common connectivity of ConnectionManager.


    @Override
    public void initiateSession(String sessionName) {
        this.registerForNSD(WifiConnectionManager.NSD_SERVICE_INSTANCE_VALUE, WifiConnectionManager.NSD_SERVICE_PROTOCOL_VALUE, sessionName);
    }


    @Override
    public void joinSession(String sessionName) {
        this.discoverForNSD(sessionName);
    }


    @Override
    public void cleanUp() {
        this.cleanUpForNSD();
    }


    // Methods for Network Service Discovery (NSD).


    /*
        Draws on examples from the Android Developers Guide.
        https://developer.android.com/training/connect-devices-wirelessly/nsd-wifi-direct.html
     */

    /*
        Draws on examples from the following URL:
        https://androiddevsimplified.wordpress.com/2016/09/14/wifi-direct-service-discovery-in-android/

        Most of the examples describe templates for handling NSD and what the various callback
        methods are used for. The examples do not describe explicitly how to use the framework
        with respect to our application and therefore functionality will diverge beyond simple
        set up of the service
     */


    private void registerForNSD(String serviceName, String serviceProtocol, String sessionName) {
        /*
            Set up a record with information about our service. We should include a session name,
            the app name (or other ID unique to the app), and the connection information.

            Use the following in this.manager.addLocalService.
         */
        Map record = new HashMap();
        record.put(this.NSD_RECORD_APP_ID, serviceName);
        record.put(this.NSD_RECORD_SESSION_ID, sessionName);
        record.put(this.NSD_RECORD_PORT_ID, this.serverSocket.getLocalPort());
        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceName, serviceProtocol, record);
        WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "NSD local service added ... SUCCESS. (Server)");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "NSD local service added ... FAILURE. (Server)\nReason code: " + reason);
            }
        };
        this.manager.addLocalService(channel, serviceInfo, actionListener);
    }


    private void discoverForNSD(String sessionName) {
        /*
            Set up listeners for NSD service responses, and for an incoming NSD textRecord.

            When a textRecord is received, check to make sure it's our app, and then save contained
            information about the session and device address in a map we can use when we wish to
            connect with peers.

            Use these in this.manager.setDnsSdResponseListeners.
        */
        WifiP2pManager.DnsSdServiceResponseListener serviceListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                Log.d(TAG, "NSD service(s) available ... SUCCESS. (Client)");
            }
        };
        WifiP2pManager.DnsSdTxtRecordListener textListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                Log.d(TAG, "NSD textRecord received ... SUCCESS. (Client)");
                // Check if this is our app ...
                if (txtRecordMap.get(WifiConnectionManager.NSD_RECORD_APP_ID).equals(WifiConnectionManager.NSD_SERVICE_INSTANCE_VALUE)) {
                    // The other device is running an instance of our app.
                    // Get this session information for this broadcast and update activeSessions.
                }
            }
        };
        this.manager.setDnsSdResponseListeners(this.channel, serviceListener, textListener);

        /*
            Set up listeners that check for success of adding the service request to the framework.
            we are probably only interested in logging information for debugging here.

            Use this in this.manager.addServiceRequest.
         */
        WifiP2pManager.ActionListener addServiceActionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "NSD adding service ... SUCCESS. (Client)");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "NSD adding service ... FAILURE. (Client)\nReason code: " + reason);
             }
        };
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        this.manager.addServiceRequest(this.channel, serviceRequest, addServiceActionListener);

        /*
            Set up listeners that check for success of discovering services.

            If this fails, we most likely want to log the failure and notify the user that the
            action could not be completed.

            Use this in this.manager.discoverServices.
        */
        WifiP2pManager.ActionListener discoverServiceActionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "NSD discovered services ... SUCCESS. (Client)");
            }
            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "NSD discovered services ... FAILURE. (Client)\nReason code: " + reason);
            }
        };
        this.manager.discoverServices(this.channel, discoverServiceActionListener);
    }


    private void cleanUpForNSD() {
        // Make sure that if we added a local service, then clean it up.
        if (this.serviceInfo != null) {
            WifiP2pManager.ActionListener removeServiceActionListener = new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "NSD local service removed ... SUCCESS. (Server)");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "NSD local service removed ... FAILURE. (Server)\nReason code: " + reason);
                }
            };
            this.manager.removeLocalService(this.channel, this.serviceInfo, removeServiceActionListener);
        }
        // Make sure that we if we requested to look for a service that we clean it up.
        if (this.serviceRequest != null) {
            WifiP2pManager.ActionListener removeRequestActionListener = new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "NSD service request removed ... SUCCESS. (Client)");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "NSD service request removed ... FAILURE. (Client)\nReason code: " + reason);
                }
            };
            this.manager.removeServiceRequest(this.channel, this.serviceRequest, removeRequestActionListener);
        }
    }


    // --- Methods + Class info for BroadcastReceiver specific to this object.


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
                 int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                 if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

                 } else {

                 }

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