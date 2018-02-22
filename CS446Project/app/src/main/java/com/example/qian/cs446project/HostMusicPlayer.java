package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Qian on 2018-02-20.
 */

public class HostMusicPlayer extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mediaPlayer;
    private ArrayList<PlaylistSong> playlist;
    private int currentSong = 0;
    private Boolean stopped = true;
    private Boolean muted = false;
    private Boolean movingToNextSong = false;
    private ImageView playPauseButtons;
    private ImageView stopButton;
    private Button muteTogglingButton;
    private TextView waitMessage;
    private SeekBar songProgressBar;
    private int songLength;
    private CustomMusicAdapter customMusicAdapter;
    private static final TimeFormatter timeFormatter = new TimeFormatter();

    private void createMediaPlayer() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(),
                    Uri.parse(Environment.getExternalStorageDirectory().getPath() +
                    playlist.get(currentSong).getFilePath()));
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_music_player);
        ListView listView = findViewById(R.id.songlist);
        // The HostMusicPlayer activity represents screen 6 in the mockup. A playlist is passed to
        // the HostMusicPlayer activity to represent the session playlist. Each song in the
        // playlist has the following metadata:
        // - Path to the music file
        // - Title
        // - Artist
        // - Album
        // The object passed in the parcel is some sort of list where each element represents a
        // song.
        playlist = getIntent().getParcelableExtra("playlist");
        currentSong = 0;
        muteTogglingButton = findViewById(R.id.muteTogglingButton);
        createMediaPlayer();
        customMusicAdapter =
                new CustomMusicAdapter(this, R.layout.song, playlist, mediaPlayer);
        listView.setAdapter(customMusicAdapter);
        playPauseButtons = findViewById(R.id.playPauseButtons);
        stopButton = findViewById(R.id.stopButton);
        waitMessage = findViewById(R.id.waitMessage);
        IntentFilter waitForDownload = new IntentFilter();
        // When a user joins a session, the play, pause, and stop buttons should be disabled for the
        // host because the new participant needs time to receive and download at least the 1st
        // song in the playlist.
        waitForDownload.addAction("new_participant_joined");
        // When all participants have finished downloading at least the 1st song in the playlist,
        // the host's play, pause, and stop buttons should be enabled.
        waitForDownload.addAction("all_participants_ready");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                hostMusicPlayerReceiver, waitForDownload
        );
    }

    private BroadcastReceiver hostMusicPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("new_participant_joined")) {
                playPauseButtons.setEnabled(false);
                stopButton.setEnabled(false);
                waitMessage.setText("Please wait for new participants to download files.");
            } else {
                playPauseButtons.setEnabled(true);
                stopButton.setEnabled(true);
                waitMessage.setText("Ready to play");
            }
        }
    };

    private void broadcastIntent(String intentName) {
        Intent intentToBroadcast = new Intent(intentName);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentToBroadcast);
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
            currentSong = 0;
            createMediaPlayer();
            songLength = mediaPlayer.getDuration();
            // Thread to update the song progress bar, elapsed time, and remaining time
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MediaPlayer currentMediaPlayer;
                    while (!stopped && currentSong < playlist.size() && mediaPlayer != null) {
                        try {
                            if (!movingToNextSong) {
                                Message message = new Message();
                                message.what = mediaPlayer.getCurrentPosition();
                                handler.sendMessage(message);
                                Thread.sleep(1000);
                            }
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                }
            }).start();
            // At least in the prototype, we might want to prevent users from joining a session
            // while a session playlist is playing or paused. MusicPlayer broadcasts an intent when
            // the host starts the session playlist and another when the host stops the session
            // playlist or the playlist finishes. As a result, other components know when they
            // should prevent users from joining a session.
            broadcastIntent("playlist-not-stopped");
            stopped = false;
        }
        if (mediaPlayer.isPlaying()) {
            // Broadcast an intent for all participants to pause the playlist.
            broadcastIntent("pause");
            mediaPlayer.pause();
            playPauseButtons.setImageResource(R.drawable.play);
        } else {
            // Broadcast an intent for all participants to play the playlist.
            broadcastIntent("play");
            mediaPlayer.start();
            playPauseButtons.setImageResource(R.drawable.pause);
        }
    }

    // Ke Qiao Chen: I based this method on https://www.youtube.com/watch?v=zCYQBIcePaw at 14:14
    // except that tutorial only plays a single song instead of a playlist. To update the progress
    // bar, elapsed time, and remaining time for specific songs, I use the currentSong variable to
    // specify a song in the playlist.
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update the song progress bar.
            songProgressBar = customMusicAdapter.getSongProgressBars().get(currentSong);
            songProgressBar.setProgress(currentPosition);
            // Update elapsed time and remaining time.
            String elapsedTimeValue = timeFormatter.formatTime(currentPosition);
            TextView elapsedTime = customMusicAdapter.getElapsedTimes().get(currentSong);
            elapsedTime.setText(elapsedTimeValue);
            TextView remainingTime = customMusicAdapter.getRemainingTimes().get(currentSong);
            String remainingTimeValue =
                    timeFormatter.formatTime(songLength - currentPosition);
            remainingTime.setText("-" + remainingTimeValue);
        }
    };

    private void resetPlaylist() {
        broadcastIntent("playlist-stopped");
        stopped = true;
        for (int i = 0; i < playlist.size(); ++i) {
            customMusicAdapter.getSongProgressBars().get(i).setProgress(0);
            customMusicAdapter.getElapsedTimes().get(i)
                    .setText(timeFormatter.formatTime(0));
            customMusicAdapter.getRemainingTimes().get(i).
                    setText("-" + timeFormatter.formatTime(playlist.get(i).getDuration()));
        }
        playPauseButtons.setImageResource(R.drawable.play);
    }

    // Ke Qiao Chen: I based this method on viewHolder.ivStop's OnClickListener in
    // https://github.com/quocnguyenvan/media-player-demo/blob/master/app/src/main/java/com/quocnguyen/mediaplayerdemo/CustomMusicAdapter.java
    // except that
    // - I do not release the MediaPlayers because it is likely that the user will replay the
    // playlist after stopping it.
    // - I reset each song's progress bar, elapsed time, and remaining time. The tutorial does not
    // not include these widgets.
    public void onStop(View v) {
        if (!stopped) {
            mediaPlayer.stop();
            resetPlaylist();
        }
    }

    public void onToggleMute(View v) {
        if (muted) {
            mediaPlayer.setVolume(1, 1);
            muteTogglingButton.setText("Mute");
            muted = false;
        } else {
            mediaPlayer.setVolume(0, 0);
            muteTogglingButton.setText("Unmute");
            muted = true;
        }
    }

    // Ke Qiao Chen: I based this method on Valentina Chumak's answer in
    // https://stackoverflow.com/questions/10916129/playing-multiple-files-in-mediaplayer-one-after-other-in-android
    // except for the following changes:
    // - I do not release the MediaPlayer because it is likely that the user will replay the
    // playlist after it finishes.
    // - I increment currentSong before checking if it is less than the number of songs in the
    // playlist. The answer increments currentSong after the check, but this results in off-by-1
    // errors.
    // - I do not create a new MediaPlayer instance and set its onCompletionListener since I need to
    // create a MediaPlayer instance for every song in the playlist before the user starts the
    // playlist. Instead, I implement these actions in the method createMediaPlayers().
    @Override
    public void onCompletion(MediaPlayer mp) {
        movingToNextSong = true;
        ++currentSong;
        if (currentSong < playlist.size()) {
            songProgressBar = customMusicAdapter.getSongProgressBars().get(currentSong);
            songLength = mediaPlayer.getDuration();
            mediaPlayer.start();
        } else {
            resetPlaylist();
        }
        movingToNextSong = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopped = true;
        mediaPlayer.release();
        mediaPlayer = null;
    }

}
