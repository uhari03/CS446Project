package com.synchronicity.APBdev.connectivity;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by Andrew on 2018-02-15.
 */

public abstract class BaseConnectionManager implements ConnectionManager {

    protected BroadcastReceiver broadcastReceiver = null;
    protected IntentFilter intentFilter = null;

    // Common connectivity methods for ConnectionManager.
    public abstract void initiateSession();
    public abstract void joinSession();

    // Common data xfer methods for ConnectionManager.
    public abstract void sendData();
    public abstract void receiveData();

    // BroadcastReceiver and IntentFilter methods.
    public abstract BroadcastReceiver getBroadcastReceiver();
    public abstract IntentFilter getIntentFilter();

}
