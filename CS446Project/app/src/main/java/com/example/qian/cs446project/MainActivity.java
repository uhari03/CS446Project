package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
        connectionManager.initiateSession();
        TextView textView = findViewById(R.id.logBox);
        textView.setText("Started broadcasting service!\n");
    }

    public void findSession (View view) {
        connectionManager.joinSession();
        TextView textView = findViewById(R.id.logBox);
        textView.setText("Started searching for service!\n");
    }
}
