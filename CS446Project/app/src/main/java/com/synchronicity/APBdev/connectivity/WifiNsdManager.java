package com.synchronicity.APBdev.connectivity;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class WifiNsdManager implements NsdManager {

    /*
    FIELDS
     */


    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel wifiP2pChannel;
    private WifiP2pServiceInfo wifiP2pServiceInfo;
    private WifiP2pServiceRequest wifiP2pServiceRequest;
    private HashMap<String,HashSet<WifiP2pDevice>> sessionMap;
    private WifiNsdManagerState wifiNsdManagerState;
    private String currentSession;
    /*
    Constant used for logging. classTag, combined with funcTag (found in functions) defines the
    tagging convention used for logging.
    */
    private final String classTag = "NsdManager>";
    /*
    Constants related to network service discovery using WifiP2p.
    */
    public static final String NSD_RECORD_APP_ID = "sName";
    public static final String NSD_RECORD_PROTOCOL_ID = "sProtocol";
    public static final String NSD_RECORD_SESSION_ID = "sName";
    public static final String NSD_RECORD_PORT_ID = "sPort";
    public static final String NSD_RECORD_USERTAG_ID = "sUser";
    public static final String NSD_RECORD_APP_VAL ="Synchronicity";
    public static final String NSD_RECORD_PROTOCOL_VAL ="_presence._tcp";


    /*
    CONSTRUCTORS
     */


    WifiNsdManager(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel wifiP2pChannel) {
        this.wifiP2pManager = wifiP2pManager;
        this.wifiP2pChannel = wifiP2pChannel;
        this.wifiP2pServiceInfo = null;
        this.wifiP2pServiceRequest = null;
        this.sessionMap = new HashMap<String,HashSet<WifiP2pDevice>>();
        this.wifiNsdManagerState = new PreSessionState();
        this.currentSession = "";
    }


    /*
    PUBLIC METHODS
     */


    /*
    Takes an NsdServiceInfo object, and then based on the information within the object, starts
    advertising a service that can be discovered by other users.
     */
    public void advertiseService(NsdServiceInfo info) {

        final String funcTag = "Advertise> ";

        /*
        If we try to start another instance of service discovery while we are already advertising,
        then we are in an error state and throw a new RuntimeException. Whatever calls this should
        check to make sure that we are not already running a service.
         */
        if (this.wifiP2pServiceInfo != null) {
            throw new RuntimeException(classTag+funcTag+"wifiP2pServiceRequest already has a value.");
        }

        NsdServiceInfo<Map<String,String>> specificInfo = info;
        Map<String,String> record = specificInfo.getInfo();

        String serviceName = record.get(WifiNsdManager.NSD_RECORD_APP_ID);
        String serviceProtocol = record.get(WifiNsdManager.NSD_RECORD_PROTOCOL_ID);

        this.wifiP2pServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(
                serviceName,
                serviceProtocol,
                record
        );

        this.wifiP2pManager.addLocalService(
                this.wifiP2pChannel,
                this.wifiP2pServiceInfo,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(classTag +funcTag,"addLocalService success.");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(classTag +funcTag,"addLocalService failure.");
                    }
                }
        );

    }

    public void findService(NsdServiceInfo info) {

        final String funcTag = "Find> ";

        NsdServiceInfo<Map<String,String>> specificInfo = info;
        Map<String,String> record = specificInfo.getInfo();

        final String serviceName = record.get(WifiNsdManager.NSD_RECORD_APP_ID);

        this.wifiP2pManager.setDnsSdResponseListeners(
                this.wifiP2pChannel,
                new WifiP2pManager.DnsSdServiceResponseListener() {
                    @Override
                    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                        Log.d(classTag+funcTag,"findService - service found.");
                    }
                },
                new WifiP2pManager.DnsSdTxtRecordListener() {
                    @Override
                    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                        Log.d(classTag+funcTag,"findService - text record available.");
                        String otherServiceName = txtRecordMap.get(WifiNsdManager.NSD_RECORD_APP_ID);
                        /*
                        Check if the other service is our app. If so, then get the name of the
                        session being advertised, and add it to our set of available sessions.
                        If the session already exists, then add the new peer to the set of peers
                        associated with the session.
                        */
                        if (serviceName == otherServiceName) {
                            String otherServiceInstance = txtRecordMap.get(WifiNsdManager.NSD_RECORD_SESSION_ID);
                            updateSessionMap(otherServiceInstance, srcDevice);
                        }
                    }
                }
        );

    }

    public void connectToService(NsdServiceInfo info) {

        NsdServiceInfo<Map<String,String>> specificInfo = info;
        Map<String,String> record = specificInfo.getInfo();

        String instanceName = record.get(WifiNsdManager.NSD_RECORD_APP_ID);
        this.currentSession = instanceName;

        Iterator<WifiP2pDevice> it = this.sessionMap.get(instanceName).iterator();
        while (it.hasNext()) {
            WifiP2pDevice remoteDevice = it.next();
            this.establishConnection(remoteDevice);
        }

        this.wifiNsdManagerState = new OngoingSessionState();

        // Still need to get the old nsd service info and re-advertise it (after updating).

    }


    /*
    PRIVATE CLASSES
     */


    private void updateSessionMap(String instanceName, WifiP2pDevice device) {
        this.wifiNsdManagerState.updateServiceMap(instanceName, device);
    }

    private void establishConnection(WifiP2pDevice remoteDevice) {

        final String funcTag = "eConn> ";

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = remoteDevice.deviceAddress;

        this.wifiP2pManager.connect(
                this.wifiP2pChannel,
                config,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(classTag+funcTag,"establishConnection success.");
                    }
                    @Override
                    public void onFailure(int reason) {
                        Log.d(classTag+funcTag,"establishConnection failure.");
                    }
                }
        );

    }


    /*
    INNER CLASSES
     */


    private interface WifiNsdManagerState {
        void updateServiceMap(String instanceName, WifiP2pDevice device);
    }

    private class PreSessionState implements WifiNsdManagerState {
        @Override
        public void updateServiceMap(String instanceName, WifiP2pDevice device) {
            if (!WifiNsdManager.this.sessionMap.containsKey(instanceName)) {
                WifiNsdManager.this.sessionMap.put(
                        instanceName,
                        new HashSet<WifiP2pDevice>()
                );
            }
            WifiNsdManager.this.sessionMap.get(instanceName).add(device);
        }
    }

    private class OngoingSessionState implements WifiNsdManagerState {
        @Override
        public void updateServiceMap(String instanceName, WifiP2pDevice device) {
            if (instanceName == WifiNsdManager.this.currentSession) {
                WifiNsdManager.this.sessionMap.get(instanceName).add(device);
                WifiNsdManager.this.establishConnection(device);
            }
        }
    }

}
