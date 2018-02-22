package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.synchronicity.APBdev.connectivity.BaseConnectionManager;
import com.synchronicity.APBdev.connectivity.WifiConnectionManager;

public class MainActivity extends AppCompatActivity {
    BaseConnectionManager connectionManager;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Regular house keeping.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectionManager = new WifiConnectionManager(this, this);
        broadcastReceiver = connectionManager.getBroadcastReceiver();
    }

    public void initSession (View view) {
        TextView textView = findViewById(R.id.logBox);
        textView.append("Started broadcasting service!\n");
        connectionManager.initiateSession();
    }

    public void findSession (View view) {
        TextView textView = findViewById(R.id.logBox);
        textView.append("Started searching for service!\n");
        connectionManager.joinSession();
    }

    public void cleanUpSession (View view) {
        TextView textView = findViewById(R.id.logBox);
        textView.append("Starting clean up!\n");
        connectionManager.cleanUp();
    }
}
