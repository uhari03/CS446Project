package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.synchronicity.APBdev.connectivity.BaseConnectionManager;
import com.synchronicity.APBdev.connectivity.WifiConnectionManager;

import java.io.IOException;

import static com.example.qian.cs446project.CS446Utils.broadcastIntentWithoutExtras;
import static com.example.qian.cs446project.CS446Utils.formatTime;

/**
 * Created by Qian on 2018-02-20.
 */

public class HostMusicPlayerActivity extends AppCompatActivity
        implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mediaPlayer;
    private Playlist playlist;
    private int previousSong = -1;
    private int currentSong = 0;
    private Boolean stopped = true;
    private Boolean muted = false;
    private Boolean movingToNextSong = false;
    private ImageView playPauseButtons;
    private ImageView stopButton;
    private ImageView muteTogglingButton;
    private TextView waitMessage;
    private SeekBar songProgressBar;
    private int songLength;
    private CustomMusicAdapter customMusicAdapter;
    private Context applicationContext;
    private IntentFilter waitForDownload;
    private BroadcastReceiver hostMusicPlayerReceiver;
    // Andrew's stuff
    private BaseConnectionManager baseConnectionManager;
    private BroadcastReceiver wifiBroadcastReceiver;


    private void createMediaPlayer() {
        mediaPlayer = MediaPlayer.create(applicationContext,
                    Uri.parse(playlist.songs.get(currentSong).getFilePath()));
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_music_player);
        ListView listView = findViewById(R.id.listViewSonglist);
        applicationContext = getApplicationContext();
        // The HostMusicPlayerActivity activity represents screen 6 in the mockup. A Playlist is
        // passed to the HostMusicPlayerActivity activity to represent the session playlist.
        playlist = getIntent().getParcelableExtra(applicationContext
                        .getString(R.string.session_playlist));
        currentSong = 0;
        muteTogglingButton = findViewById(R.id.imageViewMuteTogglingButton);
        createMediaPlayer();
        customMusicAdapter = new CustomMusicAdapter(this, R.layout.song_in_gui, playlist);
        listView.setAdapter(customMusicAdapter);
        playPauseButtons = findViewById(R.id.imageViewPlayPauseButtons);
        stopButton = findViewById(R.id.imageViewStopButton);
        waitMessage = findViewById(R.id.textViewWaitMessage);
        // Broadcast an Intent to tell the app that the user has created a session.
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.session_created),
                this);
        // Andrew's code
        baseConnectionManager = new WifiConnectionManager(this, this);
        wifiBroadcastReceiver = baseConnectionManager.getBroadcastReceiver();
        registerReceiver(wifiBroadcastReceiver, baseConnectionManager.getIntentFilter());
        baseConnectionManager.initiateSession("Demo");
    }

    @Override
    protected void onStart() {
        super.onStart();
        waitForDownload = new IntentFilter();
        waitForDownload.addAction(applicationContext.getString(R.string.receive_stop));
        waitForDownload.addAction(applicationContext.getString(R.string.receive_play));
        waitForDownload.addAction(applicationContext.getString(R.string.receive_pause));
        // When a user joins a session, the Play, Pause, and Stop buttons should be disabled for the
        // host because the new participant needs time to receive and download at least the 1st
        // song in the playlist.
        waitForDownload.addAction(applicationContext.getString(R.string.participant_joined));
        // When all participants have finished downloading at least the 1st song in the playlist,
        // the host's Play, Pause, and Stop buttons should be enabled.
        waitForDownload.addAction(applicationContext.getString(R.string.all_participants_ready));
        // When the 1st participant joins the session, the connection manager requests the session
        // playlist from the host.
        waitForDownload.addAction(applicationContext.getString(R.string.request_session_playlist));
        waitForDownload.addAction((applicationContext.getString(R.string.user_chose_session)));
        hostMusicPlayerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(applicationContext.getString(R.string.participant_joined))) {
                    playPauseButtons.setEnabled(false);
                    stopButton.setEnabled(false);
                    waitMessage.setText(applicationContext.getString(R.string.wait_message));
                } else if (action
                        .equals(applicationContext.getString(R.string.all_participants_ready))) {
                    playPauseButtons.setEnabled(true);
                    stopButton.setEnabled(true);
                    waitMessage.setText(applicationContext.getString(R.string.ready_to_play));
                } else if (action
                        .equals(applicationContext.getString(R.string.request_session_playlist))) {
                    Intent returnPlaylistIntent = new Intent(applicationContext
                            .getString(R.string.return_session_playlist));
                    returnPlaylistIntent.putExtra(applicationContext
                            .getString(R.string.session_playlist), playlist);
                    LocalBroadcastManager.getInstance(HostMusicPlayerActivity.this)
                            .sendBroadcast(returnPlaylistIntent);
                } else if (action.equals(applicationContext.getString(R.string.receive_stop))) {
                    mediaPlayer.stop();
                } else if (action.equals(applicationContext.getString(R.string.receive_play))) {
                    mediaPlayer.start();
                } else if (action.equals(applicationContext.getString(R.string.receive_pause))) {
                    mediaPlayer.pause();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                hostMusicPlayerReceiver, waitForDownload
        );
    }

    private void unboldPreviousSongMetadata() {
        if (previousSong >= 0) {
            customMusicAdapter.getSongsInGUI().get(previousSong).getTitle()
                    .setTypeface(null, Typeface.NORMAL);
            customMusicAdapter.getSongsInGUI().get(previousSong).getArtist()
                    .setTypeface(null, Typeface.NORMAL);
            customMusicAdapter.getSongsInGUI().get(previousSong).getAlbum()
                    .setTypeface(null, Typeface.NORMAL);
        }
    }

    private void boldCurrentSongMetadata() {
        customMusicAdapter.getSongsInGUI().get(currentSong).getTitle()
                .setTypeface(null, Typeface.BOLD);
        customMusicAdapter.getSongsInGUI().get(currentSong).getArtist()
                .setTypeface(null, Typeface.BOLD);
        customMusicAdapter.getSongsInGUI().get(currentSong).getAlbum()
                .setTypeface(null, Typeface.BOLD);
    }

    // Ke Qiao Chen: I based this method on viewHolder.ivPlay's OnClickListener in
    // https://github.com/quocnguyenvan/media-player-demo/blob/master/app/src/main/java/com/quocnguyen/mediaplayerdemo/CustomMusicAdapter.java
    // except for the following changes:
    // - I named the variable that indicates whether or not the playlist is stopped "stopped"
    // instead of "flag" for clarity.
    // - I based the Thread that updates the progress bar, elapsed time, and remaining time on
    // https://www.youtube.com/watch?v=zCYQBIcePaw at 11:57 except for the following changes:
    //  - I stop trying to update these widgets when the user has stopped the playlist or when the
    //  playlist has finished.
    //  - I do not try to update these widgets when a song in the playlist has ended but the next
    //  has not yet begun.
    public void onTogglePlay(View v) {
        if (stopped) {
            stopped = false;
            songLength = playlist.songs.get(currentSong).getDuration();
            unboldPreviousSongMetadata();
            boldCurrentSongMetadata();
            // Thread to update the song progress bar, elapsed time, and remaining time
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!stopped && currentSong < playlist.songs.size() && mediaPlayer != null) {
                        try {
                            if (!movingToNextSong) {
                                Message message = new Message();
                                message.what = mediaPlayer.getCurrentPosition();
                                timeUpdateHandler.sendMessage(message);
                                Thread.sleep(1000);
                            }
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                }
            }).start();
            // At least in the prototype, we might want to prevent users from joining a session
            // while a session playlist is playing or paused. HostMusicPlayerActivity broadcasts an
            // intent when the host starts the session playlist and another when the host stops the
            // session playlist or the playlist finishes. As a result, other components know when
            // they should prevent users from joining a session.
            broadcastIntentWithoutExtras(
                    applicationContext.getString(R.string.playlist_not_stopped),
                    this);
        }
        if (mediaPlayer.isPlaying()) {
            // Broadcast an intent for all participants to pause the playlist.
//            broadcastIntentWithoutExtras(applicationContext.getString(R.string.send_pause),
//                    this);
            baseConnectionManager.sendSig(WifiConnectionManager.SIG_PAUSE_CODE);
            // mediaPlayer.pause();
            playPauseButtons.setImageResource(android.R.drawable.ic_media_play);
        } else {
            // Broadcast an intent for all participants to play the playlist.
//            broadcastIntentWithoutExtras(applicationContext.getString(R.string.send_play),
//                    this);
            baseConnectionManager.sendSig(WifiConnectionManager.SIG_PLAY_CODE);
            // mediaPlayer.start();
            playPauseButtons.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    // Ke Qiao Chen: I based this method on https://www.youtube.com/watch?v=zCYQBIcePaw at 14:14
    // except that tutorial only plays a single song instead of a playlist. To update the progress
    // bar, elapsed time, and remaining time for specific songs, I use the currentSong variable to
    // specify a song in the playlist.
    private Handler timeUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update the song progress bar.
            songProgressBar =
                    customMusicAdapter.getSongsInGUI().get(currentSong).getSongProgressBar();
            songProgressBar.setProgress(currentPosition);
            // Update elapsed time and remaining time.
            String elapsedTimeValue = formatTime(currentPosition);
            TextView elapsedTime =
                    customMusicAdapter.getSongsInGUI().get(currentSong).getElapsedTime();
            elapsedTime.setText(elapsedTimeValue);
            TextView remainingTime =
                    customMusicAdapter.getSongsInGUI().get(currentSong).getRemainingTime();
            String remainingTimeValue =
                    formatTime(songLength - currentPosition);
            remainingTime.setText("-" + remainingTimeValue);
        }
    };

    private void setMediaPlayerToCurrentSong() {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(playlist.songs.get(currentSong).getFilePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetPlaylist() {
        stopped = true;
        // At least in the prototype, we might want to prevent users from joining a session
        // while a session playlist is playing or paused. MusicPlayer broadcasts an intent when
        // the host starts the session playlist and another when the host stops the session
        // playlist or the playlist finishes. As a result, other components know when they
        // should prevent users from joining a session.
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.playlist_stopped),
                this);
        if (currentSong < playlist.songs.size()) {
            previousSong = currentSong;
        } else {
            previousSong = playlist.songs.size() - 1;
        }
        unboldPreviousSongMetadata();
        currentSong = 0;
        setMediaPlayerToCurrentSong();
        for (int i = 0; i < playlist.songs.size(); ++i) {
            customMusicAdapter.getSongsInGUI().get(i).getSongProgressBar().setProgress(0);
            customMusicAdapter.getSongsInGUI().get(i).getElapsedTime()
                    .setText(formatTime(0));
            customMusicAdapter.getSongsInGUI().get(i).getRemainingTime()
                    .setText("-" + formatTime(playlist.songs.get(i).getDuration()));
        }
        playPauseButtons.setImageResource(android.R.drawable.ic_media_play);
    }

    // Ke Qiao Chen: I based this method on viewHolder.ivStop's OnClickListener in
    // https://github.com/quocnguyenvan/media-player-demo/blob/master/app/src/main/java/com/quocnguyen/mediaplayerdemo/CustomMusicAdapter.java
    // except that
    // - I do not release the MediaPlayer because it is likely that the user will replay the
    // playlist after stopping it.
    // - I reset each song's progress bar, elapsed time, and remaining time. The tutorial does not
    // include these widgets.
    public void onStop(View v) {
        if (!stopped) {
            // Broadcast an Intent for all participants to stop the playlist.
//            broadcastIntentWithoutExtras(applicationContext.getString(R.string.send_stop),
//                    this);
            baseConnectionManager.sendSig(WifiConnectionManager.SIG_STOP_CODE);
            // mediaPlayer.stop();
            resetPlaylist();
        }
    }

    public void onToggleMute(View v) {
        if (muted) {
            mediaPlayer.setVolume(1, 1);
            muteTogglingButton.setImageResource(android.R.drawable.ic_lock_silent_mode);
            muted = false;
        } else {
            mediaPlayer.setVolume(0, 0);
            muteTogglingButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
            muted = true;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        movingToNextSong = true;
        previousSong = currentSong;
        unboldPreviousSongMetadata();
        ++currentSong;
        if (currentSong < playlist.songs.size()) {
            setMediaPlayerToCurrentSong();
            songProgressBar =
                    customMusicAdapter.getSongsInGUI().get(currentSong).getSongProgressBar();
            songLength = playlist.songs.get(currentSong).getDuration();
            boldCurrentSongMetadata();
            mediaPlayer.start();
        } else {
            resetPlaylist();
        }
        movingToNextSong = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(hostMusicPlayerReceiver);
        waitForDownload = null;
        hostMusicPlayerReceiver = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopped = true;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        baseConnectionManager.cleanUp();
    }

}
