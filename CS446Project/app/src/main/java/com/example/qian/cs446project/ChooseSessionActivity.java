package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.qian.cs446project.CS446Utils.broadcastIntentWithoutExtras;

/**
 * Created by Qian on 2018-02-23.
 */

public class ChooseSessionActivity extends AppCompatActivity {

    private Context applicationContext;
    private TextView waitMessage;
    private IntentFilter chooseSessionIntentFilter;
    private BroadcastReceiver chooseSessionActivityReceiver;
    private ArrayAdapter<String> sessionListAdapter;
    private ArrayList<String> sessionNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_session);
        final ListView sessionList = findViewById(R.id.listViewSessionsList);
        waitMessage = findViewById(R.id.textViewWaitForSessionNames);
        applicationContext = getApplicationContext();
        sessionListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sessionNames);
        sessionList.setAdapter(sessionListAdapter);
        Button buttonJoin = findViewById(R.id.buttonJoin);
        buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedSessionName = (String) sessionList.getItemAtPosition(sessionList.getCheckedItemPosition());
                Intent chooseSessionIntent = new Intent(ChooseSessionActivity.this,
                                ParticipantMusicPlayerActivity.class);
                chooseSessionIntent
                        .putExtra(applicationContext.getString(R.string.session_name),
                                selectedSessionName);
                startActivity(chooseSessionIntent);
            }
        });
        // Broadcast an Intent to tell the app that the user is looking for a session to join.
        broadcastIntentWithoutExtras(
                applicationContext.getString(R.string.user_looking_to_join_session),
                this);
    }

    protected void onStart() {
        super.onStart();
        chooseSessionIntentFilter = new IntentFilter();
        // When the list of names of all sessions the user can join becomes available,
        // ChooseSessionActivity populates its ListView with those session names.
        chooseSessionIntentFilter
                .addAction(applicationContext.getString(R.string.session_list_ready));
        chooseSessionActivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(applicationContext.getString(R.string.session_list_ready))) {
                    sessionNames.clear();
                    sessionNames.addAll(intent.getStringArrayListExtra(applicationContext
                            .getString(R.string.session_names_list)));
                    sessionListAdapter.notifyDataSetChanged();
                    waitMessage.setVisibility(View.INVISIBLE);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(chooseSessionActivityReceiver,
                chooseSessionIntentFilter);
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.find_sessions),
                this);
    }

    public void onExitSessionsList(View v) {
        finish();
    }

    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(chooseSessionActivityReceiver);
        chooseSessionIntentFilter = null;
        chooseSessionActivityReceiver = null;
    }

}
