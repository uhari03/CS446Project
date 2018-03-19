package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.synchronicity.APBdev.connectivity.BaseConnectionManager;
import com.synchronicity.APBdev.connectivity.WifiConnectionManager;

import static com.example.qian.cs446project.CS446Utils.formatTime;

public class SynchronicityMusicPlayerActivity extends AppCompatActivity
        implements MusicPlayerActivity {

    private CustomMusicAdapter customMusicAdapter;
    Context applicationContext;
    ImageView muteTogglingButton;
    private Playlist playlist;
    private SeekBar songProgressBar;
    private int songLength;
    SynchronicityMusicPlayerService synchronicityMusicPlayerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_music_player);
        applicationContext = getApplicationContext();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        customMusicAdapter = new CustomMusicAdapter(this, R.layout.song_in_gui, playlist);
        ListView listView = findViewById(R.id.listViewSonglist);
        listView.setAdapter(customMusicAdapter);
    }

    private void unboldPreviousSongMetadata(int previousSong) {
        if (previousSong >= 0) {
            customMusicAdapter.getSongsInGUI().get(previousSong).getTitle()
                    .setTypeface(null, Typeface.NORMAL);
            customMusicAdapter.getSongsInGUI().get(previousSong).getArtist()
                    .setTypeface(null, Typeface.NORMAL);
            customMusicAdapter.getSongsInGUI().get(previousSong).getAlbum()
                    .setTypeface(null, Typeface.NORMAL);
        }
    }

    private void boldCurrentSongMetadata(int currentSong) {
        customMusicAdapter.getSongsInGUI().get(currentSong).getTitle()
                .setTypeface(null, Typeface.BOLD);
        customMusicAdapter.getSongsInGUI().get(currentSong).getArtist()
                .setTypeface(null, Typeface.BOLD);
        customMusicAdapter.getSongsInGUI().get(currentSong).getAlbum()
                .setTypeface(null, Typeface.BOLD);
    }

    void resetPlaylistGUI(boolean playlistPlayedUntilEnd) {
        if (playlistPlayedUntilEnd) {
            unboldPreviousSongMetadata(playlist.songs.size() - 1);
        }
        for (int i = 0; i < playlist.songs.size(); ++i) {
            customMusicAdapter.getSongsInGUI().get(i).getSongProgressBar().setProgress(0);
            customMusicAdapter.getSongsInGUI().get(i).getElapsedTime()
                    .setText(formatTime(0));
            customMusicAdapter.getSongsInGUI().get(i).getRemainingTime()
                    .setText("-" + formatTime(playlist.songs.get(i).getDuration()));
        }
    }

    void songCompleted(int currentSong) {
        if (currentSong != 0) {
            unboldPreviousSongMetadata(currentSong - 1);
            boldCurrentSongMetadata(currentSong);
        } else {
            resetPlaylistGUI(true);
        }
        songProgressBar = customMusicAdapter.getSongsInGUI().get(currentSong).getSongProgressBar();
        songLength = playlist.songs.get(currentSong).getDuration();
    }

    // Ke Qiao Chen: I based this method on https://www.youtube.com/watch?v=zCYQBIcePaw at 14:14
    // except that tutorial only plays a single song instead of a playlist. To update the progress
    // bar, elapsed time, and remaining time for specific songs, I use the currentSong variable to
    // specify a song in the playlist.
    private Handler timeUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            int currentSong = synchronicityMusicPlayerService.getCurrentSong();
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

    @Override
    public void playUpdateGUINotifyService(View view) {
        if (synchronicityMusicPlayerService.getStopped()) {
            synchronicityMusicPlayerService.setStopped(false);
            int currentSong = synchronicityMusicPlayerService.getCurrentSong();
            songLength = playlist.songs.get(currentSong).getDuration();
            boldCurrentSongMetadata(currentSong);
            // Thread to update the song progress bar, elapsed time, and remaining time
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!synchronicityMusicPlayerService.getStopped() &&
                            synchronicityMusicPlayerService.getCurrentSong() < playlist.songs.size()
                            && synchronicityMusicPlayerService.getMediaPlayer() != null) {
                        try {
                            if (!synchronicityMusicPlayerService.getMovingToNextSong()) {
                                Message message = new Message();
                                message.what = synchronicityMusicPlayerService.getMediaPlayer()
                                        .getCurrentPosition();
                                timeUpdateHandler.sendMessage(message);
                                Thread.sleep(1000);
                            }
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                }
            }).start();
        }
        synchronicityMusicPlayerService.play();
    }

    @Override
    public void pauseUpdateGUINotifyService(View view) {
        synchronicityMusicPlayerService.pause();
    }

    @Override
    public void stopUpdateGUINotifyService(View view) {
        unboldPreviousSongMetadata(synchronicityMusicPlayerService.getCurrentSong());
        synchronicityMusicPlayerService.stop();
        resetPlaylistGUI(false);
    }

    @Override
    public void muteUpdateGUINotifyService(View view) {
        if (!synchronicityMusicPlayerService.getMuted()) {
            synchronicityMusicPlayerService.mute();
            muteTogglingButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
            muteTogglingButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    unmuteUpdateGUINotifyService(view);
                }

            });
        }
    }

    @Override
    public void unmuteUpdateGUINotifyService(View view) {
        if (synchronicityMusicPlayerService.getMuted()) {
            synchronicityMusicPlayerService.unmute();
            muteTogglingButton.setImageResource(android.R.drawable.ic_lock_silent_mode);
            muteTogglingButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    muteUpdateGUINotifyService(view);
                }

            });
        }
    }

}
