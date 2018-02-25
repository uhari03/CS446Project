package com.synchronicity.APBdev.connectivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
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
    private HashMap<String, HashSet<WifiP2pDevice>> activeSessions;
    private ServerSocket serverSocket;
    // Constants for NSD. These should go in an enum later?
    private static final String NSD_SERVICE_INSTANCE_VALUE = "SynchronicityCS446";
    private static final String NSD_SERVICE_PROTOCOL_VALUE = "_presence._tcp";
    private static final String NSD_RECORD_APP_ID = "AppName";
    private static final String NSD_RECORD_SESSION_ID = "SessionName";
    private static final String NSD_RECORD_PORT_ID = "PortNum";
    // Constants for signals. These should go in an enum later?
    private static final int BUFFER_SIZE = 1024;
    private static final byte SIG_PLAY_CODE = 1;
    private static final byte SIG_PAUSE_CODE = 2;
    private static final byte SIG_STOP_CODE = 3;
    private static final byte SIG_HAND_SHAKE = 4;
    // Debug constants
    private final String TAG = "WifiConnectionManager";


    // --- Constructors. --- //


    public WifiConnectionManager(Context context, Activity activity) {
        this.activity = activity;
        this.context = context;
        this.manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = manager.initialize(context, context.getMainLooper(), null);
        this.serviceInfo = null;
        this.serviceRequest = null;
        this.activeSessions = new HashMap<String, HashSet<WifiP2pDevice>>();
        // Things that throw exceptions go here.
        try {
            this.serverSocket = new ServerSocket(0);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }


    // --- Methods for common data transfer of ConnectionManager. --- //


    @Override
    public void sendData(){
        return;
    }
    @Override
    public void receiveData() {
        return;
    }

    private void acceptSocketConnection () {
        /*
            In a new thread, listen for incoming connections and create a client socket.
            We want the data to be received asynchronously as well, so we put that in its own.
            This should only be called once after the server socket is set up.
        */
        new Thread(new Runnable () {
            @Override
            public void run() {
                // Poll for incoming connections.
                ServerSocket serverSocket = WifiConnectionManager.this.serverSocket;
                while (true) {
                    Socket clientSocket = null;
                    try {
                        // Accept incoming connection, which creates a client socket.
                        clientSocket = serverSocket.accept();
                        // Pass the client socket to the handling function.
                        WifiConnectionManager.this.handleSocketConnection(clientSocket);
                    } catch (Exception e) {
                        Log.d(TAG, "SOCKET client socket initialization ... FAILURE.");
                        Log.d(TAG, e.getMessage());
                        if (!clientSocket.isClosed()) {
                            try {
                                clientSocket.close();
                            } catch(Exception f) {
                                Log.d(TAG, "SOCKET client socket close ... FAILURE (This is not good!)");
                                Log.d(TAG, f.getMessage());
                            }
                        }
                    } finally {
                        // Make sure our local reference is nullified.
                        serverSocket = null;
                    }
                }
            }
        }).start();
    }

    private void handleSocketConnection (final Socket clientSocket) {
        /*
            Take a client socket that has connected to us and interpret the first few bytes of
            the signal; Depending on this header information, proceed from there.
        */
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] header = new byte[1];
                    int result;
                    // Get the socket input stream.
                    InputStream inputStream = clientSocket.getInputStream();
                    // Get the header info.
                    result = inputStream.read(header, 0, 1);
                    if (result == -1) {
                        Log.d(TAG, "COMMUNICATION header read ... FAILURE.");
                    } else {
                        switch(header[0]) {
                            case WifiConnectionManager.SIG_HAND_SHAKE:
                                receivedHandshake(inputStream);
                                break;
                            case WifiConnectionManager.SIG_PAUSE_CODE:
                                WifiConnectionManager.this.receivedPauseSig();
                                break;
                            case WifiConnectionManager.SIG_PLAY_CODE:
                                WifiConnectionManager.this.receivedPlaySig();
                                break;
                            case WifiConnectionManager.SIG_STOP_CODE:
                                WifiConnectionManager.this.receivedStopSig();
                                break;
                            default:
                                Log.d(TAG, "COMMUNICATION header read ... ERROR.");
                                throw new Exception("Oh fudge, we got a signal that doesn't make sense");
                        }
                    }
                } catch(Exception e) {
                    Log.d(TAG, e.getMessage());
                } finally {
                    clientSocket.close();
                }
            }
        }).start();
    }

    /*
        Implementation influenced by code example on stackoverflow:
        URL: https://stackoverflow.com/questions/18000093/how-to-marshall-and-unmarshall-a-parcelable-to-a-byte-array-with-help-of-parcel
    */
    private void receivedHandshake(InputStream inputStream) {
        byte[] buffer = new byte[WifiConnectionManager.BUFFER_SIZE];
        byte[] resultArray = null;
        WifiP2pDevice remoteDevice;
        Parcelable.Creator remoteDeviceCreator = WifiP2pDevice.CREATOR;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            int length = inputStream.read(buffer);
            while (length != -1) {
                outputStream.write(buffer, 0, length);
            }
            resultArray = outputStream.toByteArray();
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(resultArray, 0, resultArray.length);
            parcel.setDataPosition(0);
            remoteDevice = (WifiP2pDevice) remoteDeviceCreator.createFromParcel(parcel);
            parcel.recycle();
            this.establishConnection(remoteDevice);
            // Add the remote device to the activeSessions.
            fidfajdlfajkdhsafds;
        } catch (Exception e){
            Log.d(TAG, "COMMUNICATION handshake ... FAILED.");
            Log.d(TAG, e.getMessage());
        } finally {
            inputStream.close();
            outputStream.close();
        }
    }
    private void receivedPlaySig() {}
    private void receivedStopSig() {}
    private void receivedPauseSig() {}

    private void establishConnection(WifiP2pDevice remoteDevice) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = remoteDevice.deviceAddress;
        WifiP2pManager.ActionListener connectActionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "CONNECTION connecting to other device ... SUCCESS.");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "CONNECTION connecting to other device ... FAILURE");
            }
        };
        this.manager.connect(this.channel, config, connectActionListener);
    }

    private void sendHandshake() {
        WifiP2pDevice
    }

    private void sendPlaySig() {}
    private void sendStopSig() {}
    private void sendPauseSig() {}

    // --- Methods for common connectivity of ConnectionManager. --- //


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


    // --- Methods for Network Service Discovery (NSD). --- //


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
                    /*
                        The other device is running an instance of our app; Get this session
                        information (name) for this broadcast and update activeSessions with the
                        device that is a part of this session. If the session has not yet been
                        discovered, make sure to create an entry for it.
                    */
                    String sessionName = txtRecordMap.get(WifiConnectionManager.NSD_RECORD_SESSION_ID);
                    if (!WifiConnectionManager.this.activeSessions.containsKey(sessionName)) {
                        WifiConnectionManager.this.activeSessions.put(sessionName, new HashSet<WifiP2pDevice>());
                    }
                    WifiConnectionManager.this.activeSessions.get(sessionName).add(srcDevice);
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
    @Override
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