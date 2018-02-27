package com.synchronicity.APBdev.connectivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.qian.cs446project.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    private HashSet<Socket> openSockets;
    private LocalBroadcastManager localBroadcastManager;
    private int serverPort;
    // Constants for NSD. These should go in an enum later?
    private static final String NSD_SERVICE_INSTANCE_VALUE = "SynchronicityCS446";
    private static final String NSD_SERVICE_PROTOCOL_VALUE = "_presence._tcp";
    private static final String NSD_RECORD_APP_ID = "AppName";
    private static final String NSD_RECORD_SESSION_ID = "SessionName";
    private static final String NSD_RECORD_PORT_ID = "PortNum";
    // Constants for signals. These should go in an enum later?
    private static final int BUFFER_SIZE = 1024;
    public static final byte SIG_PLAY_CODE = 1;
    public static final byte SIG_PAUSE_CODE = 2;
    public static final byte SIG_STOP_CODE = 3;
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
        this.openSockets = new HashSet<Socket>();
        this.serverPort = 0;
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }


    // --- Methods adding to fields that need to be synced. --- //

    public void addOpenSocket(Socket socket) {
       // synchronized (this.openSockets) {
            Log.d("AddSocket", "start");
            openSockets.add(socket);
            Log.d("AddSocket", "end");

       // }
    }


    // --- Methods for common data transfer of ConnectionManager. --- //


    private void serverSocketAcceptThreadInit() {
        /*
            In a new thread, listen for incoming connections and create a client socket.
            We want the data to be received asynchronously as well, so we put that in its own.
            This should only be called once after the server socket is set up.
        */
        try {
            // This needs to be outside the thread so it can update the port value synchronously.
            final ServerSocket serverSocket = new ServerSocket(WifiConnectionManager.this.serverPort);
            WifiConnectionManager.this.serverPort = serverSocket.getLocalPort();
            Log.d("LocalPort", Integer.toString(WifiConnectionManager.this.serverPort));
            new Thread(new Runnable () {
                @Override
                public void run() {
                    Log.d("ServerInit", "Start");
                    // Toast.makeText(WifiConnectionManager.this.context, "Server Socket OK!", Toast.LENGTH_SHORT).show();
                    while (!serverSocket.isClosed()) {
                        Log.d("ServerInitWhile", "Start");
                        Socket clientSocket = null;
                        try {
                            clientSocket = serverSocket.accept();
                            Log.d("ServerInitWhile", "after accept");
                            // Add the socket to our set of open sockets. We can call these later in
                            // threaded methods to send data. In the mean time we should listen for incoming
                            // data.
                            WifiConnectionManager.this.addOpenSocket(clientSocket);
                            Log.d("ServerInitWhile", "after addOpenSocket");
                            // Listen for incoming data. Use serverInDataListenThreadInit.
                        } catch(IOException e) {
                            Log.d("ClientSocketFailure", e.getMessage());
                        }
                        Log.d("ServerInitWhile", "End");
                    }
                    Log.d("ServerInit", "End");
                }
            }).start();
        } catch (IOException e) {
            Log.d(TAG, "SERVERSOCKET initializing a thread ... FAILURE.");
            Log.d(TAG, e.getMessage());
        }
    }

    /*
    private void serverInDataListenThreadInit(final Socket clientSocket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Starts a thread that listens for incoming data.
                // Implement this when we need to send data to the server.
                // Should use serverInDataHandler.
            }
        });
    }
    private void serverInDataHandler(InputStream inputStream) {
        // Check for bytes to read.
        // Implement this for when the server needs to receive info.
    }
    */

    /*
        Implementation influenced by code example on stackoverflow:
        URL: https://stackoverflow.com/questions/18000093/how-to-marshall-and-unmarshall-a-parcelable-to-a-byte-array-with-help-of-parcel
    */


    private void clientSocketInDataListenThreadInit(final String hostAddress, final int hostPort) {
        Log.d("ClientListener", "start");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String host = hostAddress;
                int port = hostPort;
                Socket remoteSocket = new Socket();
                try {
                    remoteSocket.bind(null);
                    remoteSocket.connect(new InetSocketAddress(host, port), 500);
                    InputStream inputStream = remoteSocket.getInputStream();
                    while (!remoteSocket.isClosed()) {
                        Log.d("ClientListener", "Listening Start");
                        clientSocketInDataHandler(inputStream);
                        Log.d("ClientListener", "Listening End");
                    }
                } catch(IOException e) {
                    Log.d(TAG, "COMMUNICATION client data listen thread init ... FAILURE");
                }
            }
        }).start();
        Log.d("ClientListener","end");
    }

    private void clientSocketInDataHandler(InputStream inputStream) {
        // While there is data to be received, receive it.
        byte[] resultHeader = new byte[1];
        try {
            Log.d("ClientListener", "Sub start.");
            while (inputStream.read(resultHeader, 0, resultHeader.length) != -1) {
                Log.d("ClientListener", "Inner Listening");
                switch (resultHeader[0]) {
                    case WifiConnectionManager.SIG_PLAY_CODE:
                        Log.d("SigReceived!", "Play sig received!");
                        Intent intent1 = new Intent(this.context.getString(R.string.receive_play));
                        localBroadcastManager.sendBroadcast(intent1);
                        break;
                    case WifiConnectionManager.SIG_PAUSE_CODE:
                        Log.d("SigReceived!", "Pause sig received");
                        Intent intent2 = new Intent(this.context.getString(R.string.receive_pause));
                        localBroadcastManager.sendBroadcast(intent2);
                        break;
                    case WifiConnectionManager.SIG_STOP_CODE:
                        Log.d("SigReceived!", "Stop sig received");
                        Intent intent3 = new Intent(this.context.getString(R.string.receive_stop));
                        localBroadcastManager.sendBroadcast(intent3);
                        break;
                    default:
                        Log.d("SigReceived!", "Oh snape, an unknown signal!");
                }
            }
            Log.d("ClientListener", "Sub end.");
        } catch (IOException e) {
            Log.d("ClientListenException", "COMMUNICATION receiving signal ... FAILED.");
        }
    }

    @Override
    public void sendSig(byte signal) {
        Log.d("SendSig", "Start");
        // Get this set of sockets, iterating over each to send a signal.
        // Uses sendSigAsyncHelper.
        synchronized (this.openSockets) {
            Iterator<Socket> i = this.openSockets.iterator();
            Log.d("SendSig", "Has elements?: "+Boolean.toString(i.hasNext()));
            while(i.hasNext()) {
                sendSigAsyncHelper(i.next(), signal);
            }
        }
        Log.d("SendSig", "end");
    }

    private void sendSigAsyncHelper (final Socket remoteSocket, final byte signal) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("AsyncHelper", "Start");
                Socket socket = remoteSocket;
                try {
                    OutputStream outputStream = socket.getOutputStream();
                    Log.d("AsyncHelper", "got ostream");
                    outputStream.write(signal);
                    Log.d("AsyncHelper", "Wrote to stream");
                } catch (IOException e) {
                    Log.d(TAG, "DATA XFER getting / using output stream ... ERROR");
                    Log.d(TAG, e.getMessage());
                }
                Log.d("AsyncHelper", "End");
            }
        }).start();
    }

    // --- Methods for common connectivity of ConnectionManager. --- //


    @Override
    public void initiateSession(String sessionName) {
        // Start a WifiP2pGroup. This should let us init the server socket.
        this.createSessionGroup();
        // Init the ServerSocket.
        this.serverSocketAcceptThreadInit();
        // Register our service for discovery.
        Log.d("LocalPort", Integer.toString(this.serverPort));
        this.registerForNSD(WifiConnectionManager.NSD_SERVICE_INSTANCE_VALUE, WifiConnectionManager.NSD_SERVICE_PROTOCOL_VALUE, sessionName, this.serverPort);
    }
    @Override
    public void joinSession(String sessionName) {
        this.discoverForNSD(sessionName);
    }
    @Override
    public void cleanUp() {
        this.cleanUpForNSD();
    }


    // --- Methods for Network Service Discovery (NSD) + establishing p2p connections. --- //


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


    private void registerForNSD(String serviceName, String serviceProtocol, String sessionName, int sessionPort) {
        /*
            Set up a record with information about our service. We should include a session name,
            the app name (or other ID unique to the app), and the connection information.

            Use the following in this.manager.addLocalService.
         */
        Map record = new HashMap();
        record.put(this.NSD_RECORD_APP_ID, serviceName);
        record.put(this.NSD_RECORD_SESSION_ID, sessionName);
        record.put(this.NSD_RECORD_PORT_ID, Integer.toString(sessionPort));
        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceName, serviceProtocol, record);
        WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "NSD local service added ... SUCCESS. (Server)");
                Toast.makeText(WifiConnectionManager.this.context, "Service Added!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "NSD local service added ... FAILURE. (Server)\nReason code: " + reason);
                Toast.makeText(WifiConnectionManager.this.context, "Service not added!", Toast.LENGTH_SHORT).show();
            }
        };
        this.manager.addLocalService(channel, serviceInfo, actionListener);
    }

    private void discoverForNSD(final String sessionName) {
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
                        It's our app, so if the session name is the one we want to join, then form
                        a connection.
                    */
                    if (txtRecordMap.get(WifiConnectionManager.NSD_RECORD_SESSION_ID).equals(sessionName)) {
                        WifiConnectionManager.this.serverPort = Integer.parseInt(txtRecordMap.get(WifiConnectionManager.NSD_RECORD_PORT_ID));
                        Log.d("ServerPortVal", Integer.toString(WifiConnectionManager.this.serverPort));
                        WifiConnectionManager.this.establishConnection(srcDevice);
                        /*
                            Check the broadcast receiver for changed connections. If the connection
                            has changed then we can get the group owner info and form a socket
                            connection.
                        */
                    }
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

    private void createSessionGroup() {
        WifiP2pManager.ActionListener createGroupActionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG,"GROUP create group ... SUCCESS.");
                Toast.makeText(WifiConnectionManager.this.context, "Group created!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int reason) {
                Log.d(TAG,"GROUP create group ... FAILURE.");
                Toast.makeText(WifiConnectionManager.this.context, "Group not created!", Toast.LENGTH_SHORT).show();
            }
        };
        this.manager.createGroup(this.channel, createGroupActionListener);
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
        this.manager.cancelConnect(this.channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Cancelled Connections.");
                Toast.makeText(WifiConnectionManager.this.context, "Cancelled Connect", Toast.LENGTH_SHORT);
            }
            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Failed Cancel Connections.");
                Toast.makeText(WifiConnectionManager.this.context, "Failed Cancel Connect", Toast.LENGTH_SHORT);
            }
        });
        WifiConnectionManager.this.manager.removeGroup(WifiConnectionManager.this.channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Removed group.");
                Toast.makeText(WifiConnectionManager.this.context, "Removed Group", Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Couldn't Removed group.");
                Toast.makeText(WifiConnectionManager.this.context, "Couldn't Removed Group", Toast.LENGTH_SHORT);
            }

        });
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
                // The following code uses heavily from:
                // https://developer.android.com/training/connect-devices-wirelessly/wifi-direct.html
                Log.d("BCRinner", "Start");
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
                        @Override
                        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
                            InetAddress groupOwnerAddress = info.groupOwnerAddress;

                            // If this device is the group owner, probably want to init a ServerSocket.
                            // Else this device is probably a client, and we should connect to the host.
                            if (info.groupFormed && !info.isGroupOwner) {
                                Log.d("ClientListener", "Port: "+Integer.toString(WifiConnectionManager.this.serverPort));
                                WifiConnectionManager.this.clientSocketInDataListenThreadInit(groupOwnerAddress.getHostAddress(), WifiConnectionManager.this.serverPort);
                            }
                        }
                    };
                    WifiConnectionManager.this.manager.requestConnectionInfo(WifiConnectionManager.this.channel, connectionInfoListener);
                    Log.d("BCRinner", "end");
                }
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