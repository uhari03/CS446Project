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
import android.widget.TextView;

import com.example.qian.cs446project.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrew on 2018-02-15.
 */

public class WifiConnectionManager extends BaseConnectionManager {
    //Fields
    Context context;
    Activity activity;
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;


    // Constructors.
    public WifiConnectionManager(Context context, Activity activity) {
        this.activity = activity;
        this.context = context;
        this.manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = manager.initialize(context, context.getMainLooper(), null);
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

    @Override
    public void initiateSession() {
        String serviceName = "SynchronicityCS446";
        String serviceProtocol = "_presence._tcp";
        String sessionName = "testSession";
        this.registerForNSD(serviceName, serviceProtocol, sessionName);
    }


    @Override
    public void joinSession() {
        this.findServices();
    }


    // Methods for Network Service Discovery (NSD).
    private void registerForNSD(String serviceName, String serviceProtocol, String sessionName) {
        Map record = new HashMap();
        /*
            Uses hardcoded String values. Consider moving these to a more central point of control.
            This describes our service and lets other potential peers know information about the
            session that they can join. Once they have selected a session, they should use this
            information to connect to the peer advertising this service.
         */
        record.put("sessionName", sessionName);
        record.put("listenPort", "foo"); // Add a port here
        record.put("available", "visible");
        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceName, serviceProtocol, record);
        WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Use for actions when the service is successfully registered.
                screenDebug("Hooray, we registered for NSD!\n");
            }

            @Override
            public void onFailure(int reason) {
                // Use for actions when the service is NOT successfully registered.
                screenDebug("Oh no, we failed to register for NSD!\n");
            }
        };
        this.manager.addLocalService(channel, serviceInfo, actionListener);
    }


    private void findServices () {
        WifiP2pManager.DnsSdServiceResponseListener serviceListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                // Check for our app here. Could this discovery be an echo of our own broadcast?
                // If not our own broadcast, get some info about the broadcast and give it the user to select a room?
                screenDebug("Checking for services: Service found!\n");
                screenDebug("Instance Name: " + instanceName);
            }
        };
        WifiP2pManager.DnsSdTxtRecordListener textListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                /*
                    Get information about the service from the text record. Probably store the name
                    of the rooms and the person that you can connect with.
                 */
                screenDebug("Check for text records: Text record found!\n");
                screenDebug("Session name: " + txtRecordMap.get("sessionName"));
            }
        };
        this.manager.setDnsSdResponseListeners(this.channel, serviceListener, textListener);

        // Use this in this.manager.addServiceRequest.
        WifiP2pManager.ActionListener addServiceActionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                screenDebug("Check if service request added: Service added!\n");
            }

            @Override
            public void onFailure(int reason) {
                screenDebug("Check if service request added: Service NOT added!\n");
             }
        };
        WifiP2pServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        this.manager.addServiceRequest(this.channel, serviceRequest, addServiceActionListener);

        // Use this in this.manager.discoverServices.
        WifiP2pManager.ActionListener discoverServiceActionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                screenDebug("Service discovery initiated!\n");
            }

            @Override
            public void onFailure(int reason) {
                screenDebug("Failed to initiate service discovery!\n");

            }
        };
        this.manager.discoverServices(this.channel, discoverServiceActionListener);
    }


    private void screenDebug(String message) {
        TextView textView = WifiConnectionManager.this.activity.findViewById(R.id.logBox);
        textView.append(message);
    }



    // --- Methods + Class info for BroadcastReceiver specific to this object.



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
                 int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                 if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                     screenDebug("Wifi p2p enabled...\n");
                 } else {
                     screenDebug("Wifi p2p NOT enabled!");
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
                screenDebug("Something about the device changed...");
            }
        }
    }


    private class WcmIntentFilter extends IntentFilter {
        private WcmIntentFilter () {
            super();
        }
    }
}