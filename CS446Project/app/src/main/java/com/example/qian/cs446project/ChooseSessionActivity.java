package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Qian on 2018-02-23.
 */

public class ChooseSessionActivity extends AppCompatActivity {

    private Context applicationContext = getApplicationContext();
    private ListView sessionList;
    private TextView waitMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_session);
        sessionList = findViewById(R.id.listViewSessionsList);
        waitMessage = findViewById(R.id.textViewWaitForSessionNames);
        IntentFilter chooseSessionIntentFilter = new IntentFilter();
        // When the list of names of all sessions the user can join becomes available,
        // ChooseSessionActivity populates its ListView with those session names.
        chooseSessionIntentFilter
                .addAction(applicationContext.getString(R.string.session_list_ready));
        LocalBroadcastManager.getInstance(this).registerReceiver(chooseSessionActivityReceiver,
                chooseSessionIntentFilter);
    }

    private BroadcastReceiver chooseSessionActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(applicationContext.getString(R.string.session_list_ready))) {
                ArrayList<String> sessionNames = intent.getParcelableExtra(applicationContext
                                .getString(R.string.session_names_list));
                SessionListAdapter sessionListAdapter =
                        new SessionListAdapter(ChooseSessionActivity.this,
                                R.layout.session_in_list, sessionNames);
                sessionList.setAdapter(sessionListAdapter);
                waitMessage.setVisibility(View.INVISIBLE);
            }
        }
    };

    public void onExitSessionsList(View v) {
        finish();
    }

}
