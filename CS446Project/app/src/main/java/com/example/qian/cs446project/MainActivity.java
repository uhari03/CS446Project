package com.example.qian.cs446project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonCreateSession = findViewById(R.id.create_session);
        buttonCreateSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createSessionIntent = new Intent(MainActivity.this, CreateSessionActivity.class);
                startActivity(createSessionIntent);
            }
        });

        Button buttonManagePlaylist = findViewById(R.id.manage_playlist);
        buttonManagePlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent managePlaylistIntent = new Intent(MainActivity.this, ManagePlaylistActivity.class);
                startActivity(managePlaylistIntent);
            }
        });
    }

}
