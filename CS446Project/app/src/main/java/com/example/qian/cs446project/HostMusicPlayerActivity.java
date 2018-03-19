package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.synchronicity.APBdev.connectivity.BaseConnectionManager;
import com.synchronicity.APBdev.connectivity.WifiConnectionManager;

/**
 * Created by Qian on 2018-02-20.
 */

public class HostMusicPlayerActivity extends SynchronicityMusicPlayerActivity {

    private ImageView playPauseButtons;
    private ImageView stopButton;
    private Playlist playlist;
    private IntentFilter hostMusicPlayerActivityFilter;
    private BroadcastReceiver hostMusicPlayerReceiver;
    private boolean bound = false;
    // Andrew's stuff
    private BaseConnectionManager baseConnectionManager;
    private BroadcastReceiver wifiBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_music_player);
        playPauseButtons = findViewById(R.id.imageViewPlayPauseButtons);
        muteTogglingButton = findViewById(R.id.imageViewMuteTogglingButton);
        playPauseButtons.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                playUpdateGUINotifyService(view);
            }

        });
        stopButton = findViewById(R.id.imageViewStopButton);
        playlist = getIntent().getParcelableExtra(applicationContext
                .getString(R.string.session_playlist));
        // HostMusicPlayerActivity represents screen 6 in the mockup. A Playlist is passed to
        // HostMusicPlayerActivity to represent the session playlist.
        setPlaylist(playlist);
        baseConnectionManager = new WifiConnectionManager(this, this);
        wifiBroadcastReceiver = baseConnectionManager.getBroadcastReceiver();
        registerReceiver(wifiBroadcastReceiver, baseConnectionManager.getIntentFilter());
        baseConnectionManager.initiateSession("Demo");
    }

    private ServiceConnection hostMusicPlayerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            HostMusicPlayerService.HostMusicPlayerBinder hostMusicPlayerBinder =
                    (HostMusicPlayerService.HostMusicPlayerBinder) iBinder;
            synchronicityMusicPlayerService = hostMusicPlayerBinder.getBinder();
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
        Intent serviceIntent = new Intent(this, HostMusicPlayerService.class);
        serviceIntent.putExtra(applicationContext.getString(R.string.session_playlist),
                playlist);
        bindService(serviceIntent, hostMusicPlayerServiceConnection, Context.BIND_AUTO_CREATE);
        hostMusicPlayerActivityFilter = new IntentFilter();
        // When a user joins a session, the Play, Pause, and Stop buttons should be disabled for the
        // host because the new participant needs time to receive and download at least the 1st
        // song in the playlist.
        hostMusicPlayerActivityFilter.addAction(applicationContext.getString(R.string
                .participant_joined));
        // When all participants have finished downloading at least the 1st song in the playlist,
        // the host's Play, Pause, and Stop buttons should be enabled.
        hostMusicPlayerActivityFilter.addAction(applicationContext.getString(R.string
                .all_participants_ready));
        hostMusicPlayerActivityFilter.addAction(applicationContext.getString(R.string
                .user_chose_session));
        // When a song in the session playlist finishes, HostMusicPlayerActivity updates the GUI to
        // indicate that the next song is playing, if applicable. Otherwise,
        // HostMusicPlayerActivity resets the progress bar, elapsed time, and remaining time of each
        // song in the session playlist.
        hostMusicPlayerActivityFilter.addAction(applicationContext.getString(R.string.song_completed));
        hostMusicPlayerReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                TextView waitMessage = findViewById(R.id.textViewWaitMessage);
                if (action.equals(applicationContext.getString(R.string.participant_joined))) {
                    playPauseButtons.setEnabled(false);
                    stopButton.setEnabled(false);
                    waitMessage.setText(applicationContext.getString(R.string.wait_message));
                } else if (action
                        .equals(applicationContext.getString(R.string.all_participants_ready))) {
                    playPauseButtons.setEnabled(true);
                    stopButton.setEnabled(true);
                    waitMessage.setText(applicationContext.getString(R.string.ready_to_play));
                } else if (action.equals(applicationContext.getString(R.string.song_completed))) {
                    songCompleted(intent.getIntExtra(applicationContext.getString(R.string
                            .current_song_index), 0));
                    playPauseButtons.setImageResource(android.R.drawable.ic_media_play);
                    synchronicityMusicPlayerService.setMovingToNextSong(false);
                }
            }

        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                hostMusicPlayerReceiver, hostMusicPlayerActivityFilter
        );
    }

    private void enablePlay() {
        playPauseButtons.setImageResource(android.R.drawable.ic_media_play);
        playPauseButtons.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                playUpdateGUINotifyService(view);
            }

        });
    }

    @Override
    public void playUpdateGUINotifyService(View view) {
        if (!synchronicityMusicPlayerService.getMediaPlayer().isPlaying()) {
            super.playUpdateGUINotifyService(view);
            baseConnectionManager.sendSig(WifiConnectionManager.SIG_PLAY_CODE);
            playPauseButtons.setImageResource(android.R.drawable.ic_media_pause);
            playPauseButtons.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    pauseUpdateGUINotifyService(view);
                }

            });
        }
    }

    @Override
    public void pauseUpdateGUINotifyService(View view) {
        if (synchronicityMusicPlayerService.getMediaPlayer().isPlaying()) {
            super.pauseUpdateGUINotifyService(view);
            // Broadcast an Intent for all participants to pause the playlist.
//        broadcastIntentWithoutExtras(applicationContext.getString(R.string.send_pause),
//                this);
            baseConnectionManager.sendSig(WifiConnectionManager.SIG_PAUSE_CODE);
            enablePlay();
        }
    }

    @Override
    public void stopUpdateGUINotifyService(View view) {
        if (!synchronicityMusicPlayerService.getStopped()) {
            super.stopUpdateGUINotifyService(view);
            // Broadcast an Intent for all participants to stop the playlist.
//            broadcastIntentWithoutExtras(applicationContext.getString(R.string.send_stop),
//                    this);
            baseConnectionManager.sendSig(WifiConnectionManager.SIG_STOP_CODE);
            enablePlay();
        }
    }

    @Override
    public void muteUpdateGUINotifyService(View view) {
        super.muteUpdateGUINotifyService(view);
    }

    @Override
    void resetPlaylistGUI(boolean playlistPlayedUntilEnd) {
        super.resetPlaylistGUI(playlistPlayedUntilEnd);
        enablePlay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        hostMusicPlayerActivityFilter = null;
        hostMusicPlayerReceiver = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            unbindService(hostMusicPlayerServiceConnection);
            bound = false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(hostMusicPlayerReceiver);
        baseConnectionManager.cleanUp();
    }

}
