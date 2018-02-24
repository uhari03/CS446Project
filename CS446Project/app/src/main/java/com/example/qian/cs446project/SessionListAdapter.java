package com.example.qian.cs446project;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Qian on 2018-02-23.
 */

public class SessionListAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private ArrayList<String> sessionNames = new ArrayList<>();
    private HashMap<Integer, View> displayedSessions = new HashMap<>();
    private Context applicationContext;

    public SessionListAdapter(Context context, int layout, ArrayList<String> sessionNames) {
        this.context = context;
        applicationContext = context.getApplicationContext();
        this.layout = layout;
        this.sessionNames = sessionNames;
    }

    @Override
    public int getCount() {
        return sessionNames.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class SessionListViewHolder {

        private TextView sessionName;
        private Button joinButton;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SessionListViewHolder sessionListViewHolder;
        if (!displayedSessions.containsKey(position)) {
            sessionListViewHolder = new SessionListViewHolder();
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(layout, null);
            sessionListViewHolder.sessionName = convertView.findViewById(R.id.textViewSessionName);
            sessionListViewHolder.joinButton = convertView.findViewById(R.id.buttonJoinThisSession);
            sessionListViewHolder.sessionName.setText(sessionNames.get(position));

            sessionListViewHolder.joinButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent participantJoinedSessionIntent =
                            new Intent(applicationContext.getString(R.string.domain_name) +
                                    applicationContext.getString(R.string.user_chose_session));
                    participantJoinedSessionIntent
                            .putExtra(applicationContext.getString(R.string.name_of_chosen_session),
                                    sessionListViewHolder.sessionName.getText());
                    LocalBroadcastManager.getInstance(context)
                            .sendBroadcast(participantJoinedSessionIntent);
                }

            });

            convertView.setTag(sessionListViewHolder);
            displayedSessions.put(position, convertView);
        } else {
            convertView = displayedSessions.get(position);
        }
        return convertView;
    }

}
