package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.synchronicity.APBdev.connectivity.BaseConnectionManager;
import com.synchronicity.APBdev.connectivity.WifiConnectionManager;

public class MainActivity extends AppCompatActivity {
    BaseConnectionManager connectionManager;
    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;

    String sessionName = "Andrew";
    int dummy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Regular house keeping.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectionManager = new WifiConnectionManager(this, this);
        broadcastReceiver = connectionManager.getBroadcastReceiver();
        intentFilter = connectionManager.getIntentFilter();
        this.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void initSession (View view) {
        TextView textView = findViewById(R.id.logBox);
        textView.append("Started broadcasting service!\n");
        dummy = 0;
        connectionManager.initiateSession(sessionName);
        Log.d("Hari test", "test");
    }

    public void findSession (View view) {
        TextView textView = findViewById(R.id.logBox);
        textView.append("Started searching for service!\n");
        connectionManager.joinSession(sessionName);
    }

    public void cleanUpSession (View view) {
        TextView textView = findViewById(R.id.logBox);
        textView.append("Starting clean up!\n");
        connectionManager.cleanUp();
    }

    public void sendSignals (View view) {
        TextView textView = findViewById(R.id.logBox);
        textView.append("Sending some signals!\n");
        connectionManager.sendSig(WifiConnectionManager.SIG_PLAY_CODE);
        connectionManager.sendSig(WifiConnectionManager.SIG_PAUSE_CODE);
        connectionManager.sendSig(WifiConnectionManager.SIG_STOP_CODE);
    }
}
