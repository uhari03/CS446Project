package com.example.qian.cs446project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
<<<<<<< HEAD
import android.view.View;
import android.widget.TextView;

import com.synchronicity.APBdev.connectivity.BaseConnectionManager;
import com.synchronicity.APBdev.connectivity.WifiConnectionManager;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    BaseConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Regular house keeping.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectionManager = new WifiConnectionManager(this, this);
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


=======

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
>>>>>>> origin/APBdev-Workspace
