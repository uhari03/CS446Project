package com.synchronicity.APBdev.connectivity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class WifiSocketManager implements SocketManager {


    /*
     FIELDS
     */


    private Context context;
    private ServerSocket serverSocket;
    private Set<Socket> activeConnectionsSet;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;


    /*
     CONSTANTS
     */


    private final String classTag = "SocketManager> ";


    /*
     CONSTRUCTORS
     */


    WifiSocketManager(Context context) {

        this.context = context;
        this.serverSocket = new ServerSocket(0);
        this.activeConnectionsSet = new HashSet<Socket>();

        // BroadcastReceive creation and registration.
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
        this.intentFilter = new IntentFilter();
        this.intentFilter.addAction();
        this.intentFilter.addAction();
        this.intentFilter.addAction();
        this.intentFilter.addAction();
        context.registerReceiver(this.broadcastReceiver, this.intentFilter);

    }


    /*
     PUBLIC METHODS
     */


    public void connectToServer(final String address, final int port) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        Socket remoteSocket = new Socket();
                        try {
                            remoteSocket.bind(null);
                            remoteSocket.connect(
                                    new InetSocketAddress(address, port),
                                    500
                            );
                        } catch(IOException ioException) {
                            ioException.printStackTrace();
                        }
                        while(!remoteSocket.isClosed()) {
                            // Handle stuff here.
                        }
                    }
                }
        );
    }

    @Override
    public void sendData(final Parcelable parcelable) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        byte[] data = ParcelableUtil.marshall(parcelable);
                    }
                }
        );
    }

    @Override
    public void sendDataByPath(String pathToData) {

    }

    @Override
    public void sendSignal(int signal) {

    }

    @Override
    public void setOnReceiveDataListener(SocketManager.ActionListener actionListener) {

    }

    @Override
    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    @Override
    public void cleanUp() {
        this.context.unregisterReceiver(this.broadcastReceiver);
    }

}
