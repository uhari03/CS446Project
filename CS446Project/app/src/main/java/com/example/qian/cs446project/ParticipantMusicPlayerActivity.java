package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Qian on 2018-02-21.
 */

public class ParticipantMusicPlayerActivity extends SynchronicityMusicPlayerActivity {

    private Playlist playlist;
    private Context applicationContext;
    private TextView waitMessage;
    private IntentFilter participantIntentFilter;
    private BroadcastReceiver participantMusicPlayerReceiver;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_music_player);
        muteTogglingButton = findViewById(R.id.imageViewMuteTogglingButton);
        waitMessage = findViewById(R.id.textViewWaitForSongs);
        applicationContext = getApplicationContext();
        ArrayList<Playlist> allPlaylists = PlaylistManager.listAllAppPlaylists(applicationContext);
        if (allPlaylists == null || allPlaylists.size() == 0) {
            Toast errorToast = Toast.makeText(applicationContext, "Participant failed to retrieve session playlist.",
                    Toast.LENGTH_SHORT);
            errorToast.setMargin(50, 50);
            errorToast.show();
            baseConnectionManager.cleanUp();
            System.exit(1);
        } else {
            playlist = allPlaylists.get(0);
            PlaylistManager.listAllPlaylistSongs(applicationContext, playlist);

            if (playlist.songs == null || playlist.songs.size() == 0) {
                Toast errorToast = Toast.makeText(applicationContext, "Participant failed to retrieve session playlist songs.",
                        Toast.LENGTH_SHORT);
                errorToast.setMargin(50, 50);
                errorToast.show();
                LocalBroadcastManager.getInstance(this).unregisterReceiver(participantMusicPlayerReceiver);
                participantIntentFilter = null;
                participantMusicPlayerReceiver = null;
                baseConnectionManager.cleanUp();
                System.exit(1);
            }

            setPlaylist(playlist);
            waitMessage.setVisibility(View.INVISIBLE);
            // Andrew's code
            baseConnectionManager.joinSession("Demo");
        }
    }

    private ServiceConnection participantMusicPlayerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ParticipantMusicPlayerService.ParticipantMusicPlayerBinder participantMusicPlayerBinder
                    = (ParticipantMusicPlayerService.ParticipantMusicPlayerBinder) iBinder;
            synchronicityMusicPlayerService = participantMusicPlayerBinder.getBinder();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }

    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, ParticipantMusicPlayerService.class);
        serviceIntent.putExtra(applicationContext.getString(R.string.session_playlist),
                playlist);
        bindService(serviceIntent, participantMusicPlayerServiceConnection,
                Context.BIND_AUTO_CREATE);
        participantIntentFilter = new IntentFilter();
        // When the host presses Play for the session playlist, the session playlist should play on
        // the participant's device.
        participantIntentFilter.addAction(applicationContext.getString(R.string.receive_play));
        // When the host pauses the session playlist, the session playlist should pause on the
        // participant's device.
        participantIntentFilter.addAction(applicationContext.getString(R.string.receive_pause));
        // When the host stops the session playlist, the session playlist should stop on the
        // the participant's device.
        participantIntentFilter.addAction(applicationContext.getString(R.string.receive_stop));
        participantMusicPlayerReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                MusicPlayerState musicPlayerState =
                        synchronicityMusicPlayerService.musicPlayerState;
                if (action.equals(applicationContext.getString(R.string.receive_play))) {
                    if (!musicPlayerState.isPlaying()) {
                        playUpdateGUINotifyService(null);
                    }
                } else if (action.equals(applicationContext.getString(R.string.receive_pause))) {
                    if (!musicPlayerState.isPaused()) {
                        pauseUpdateGUINotifyService(null);
                    }
                } else if (action.equals(applicationContext.getString(R.string.receive_stop))) {
                    if (!musicPlayerState.isStopped()) {
                        stopUpdateGUINotifyService(null);
                    }
                } else if (action.equals(applicationContext.getString(R.string.playlist_ready))) {
                    playlist = intent.getParcelableExtra(applicationContext
                            .getString(R.string.session_playlist));
                    setPlaylist(playlist);
                    waitMessage.setVisibility(View.INVISIBLE);
                }
            }

        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                participantMusicPlayerReceiver, participantIntentFilter
        );
        // Broadcast an Intent to tell other components that the user has joined a certain
        // session and is requesting the session playlist. This Intent contains the session name.
        Intent participantJoinedSessionIntent =
                new Intent(applicationContext.getString(R.string.user_chose_session));
        String sessionName =
                getIntent().getStringExtra(applicationContext.getString(R.string.session_name));
        participantJoinedSessionIntent
                .putExtra(applicationContext.getString(R.string.name_of_chosen_session),
                        sessionName);
        LocalBroadcastManager.getInstance(this).sendBroadcast(participantJoinedSessionIntent);
    }

    @Override
    public void muteUpdateGUINotifyService(View view) {
        super.muteUpdateGUINotifyService(view);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(participantMusicPlayerReceiver);
        participantIntentFilter = null;
        participantMusicPlayerReceiver = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            unbindService(participantMusicPlayerServiceConnection);
            bound = false;
        }
    }

}
