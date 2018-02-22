package com.example.qian.cs446project;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Qian on 2018-02-21.
 */

public class ParticipantMusicPlayer extends AppCompatActivity {

    private ArrayList<MediaPlayer> mediaPlayers;
    private ArrayList<PlaylistSong> playlist;
    private int currentSong = 0;
    private Boolean stopped = true;
    private Boolean muted = false;
    private Boolean movingToNextSong = false;
    private Button muteTogglingButton;
    private TextView waitMessage;
    private SeekBar songProgressBar;
    private int songLength;
    private CustomMusicAdapter customMusicAdapter;
    private static final TimeFormatter timeFormatter = new TimeFormatter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.participant_music_player);
        ListView listView = findViewById(R.id.songlist);
        // The ParticipantMusicPlayer activity represents screen 3 in the mockup.
        currentSong = 0;
        muteTogglingButton = findViewById(R.id.muteTogglingButton);
        customMusicAdapter =
                new CustomMusicAdapter(this, R.layout.song, playlist, null);
        listView.setAdapter(customMusicAdapter);
    }

}
