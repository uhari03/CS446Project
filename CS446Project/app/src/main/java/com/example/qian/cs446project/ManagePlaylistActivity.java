package com.example.qian.cs446project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class ManagePlaylistActivity extends AppCompatActivity {
    private ArrayList<Playlist> allAppPlaylists;
    ListView listViewManagePlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_playlist);

        // Retrieve all app playlists and display them in listview.
        allAppPlaylists = PlaylistManager.listAllAppPlaylists(getApplicationContext());
        ArrayAdapter<Playlist> allAppPlaylistAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allAppPlaylists);
        listViewManagePlaylist = findViewById(R.id.listViewManagePlaylist);
        listViewManagePlaylist.setAdapter(allAppPlaylistAdapter);

        // Button should launch activity to create a new playlist.
        Button buttonNewPlaylist = findViewById(R.id.buttonNewPlaylist);
        buttonNewPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createPlaylistIntent = new Intent(ManagePlaylistActivity.this, CreatePlaylistActivity.class);
                startActivity(createPlaylistIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Retrieve all app playlists and refresh display in listview.
        allAppPlaylists = PlaylistManager.listAllAppPlaylists(getApplicationContext());
        ArrayAdapter<Playlist> allAppPlaylistAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allAppPlaylists);
        listViewManagePlaylist.setAdapter(allAppPlaylistAdapter);
    }
}
