package com.example.qian.cs446project;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private ArrayList<MediaPlayer> mediaPlayers;
    private ArrayList<PlaylistSong> playlist;
    private int currentSong = 0;
    private Boolean stopped = true;
    private Boolean movingToNextSong = false;
    private ImageView playPauseButtons;
    private SeekBar songProgressBar;
    private TextView elapsedTime;
    private TextView remainingTime;
    private int songLength;
    private CustomMusicAdapter customMusicAdapter;

    private void createMediaPlayers() {
        mediaPlayers = new ArrayList<>();
        for (PlaylistSong playlistSong : playlist) {
            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),
                    playlistSong.getIdentifyingNumber());
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayers.add(mediaPlayer);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.songlist);
        playlist = new ArrayList<>();
        playlist.add(new PlaylistSong("The New Black Gold", R.raw.the_new_black_gold));
        playlist.add(new PlaylistSong("Sovngarde Song", R.raw.sovngarde_song));
        playlist.add(new PlaylistSong("Commander Shepard", R.raw.commander_shepard));
        playlist.add(new PlaylistSong("Big Ten", R.raw.big_ten));
        createMediaPlayers();
        customMusicAdapter =
                new CustomMusicAdapter(this, R.layout.song, playlist, mediaPlayers);
        listView.setAdapter(customMusicAdapter);
        currentSong = 0;
        playPauseButtons = findViewById(R.id.playPauseButtons);
    }

    public void onTogglePlay(View v) {
        if (stopped) {
            createMediaPlayers();
            songLength = mediaPlayers.get(currentSong).getDuration();
            // Thread to update the song progress bar, elapsed time, and remaining time
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MediaPlayer currentMediaPlayer = null;
                    while (!stopped && currentSong < mediaPlayers.size() &&
                            (currentMediaPlayer = mediaPlayers.get(currentSong)) != null) {
                        try {
                            if (!movingToNextSong) {
                                Message message = new Message();
                                message.what = currentMediaPlayer.getCurrentPosition();
                                handler.sendMessage(message);
                                Thread.sleep(1000);
                            }
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                }
            }).start();
            stopped = false;
        }
        if (mediaPlayers.get(currentSong).isPlaying()) {
            mediaPlayers.get(currentSong).pause();
            playPauseButtons.setImageResource(R.drawable.play);
        } else {
            mediaPlayers.get(currentSong).start();
            playPauseButtons.setImageResource(R.drawable.pause);
        }
    }

    public String formatTime(int timeInMilliseconds) {
        int minutes = timeInMilliseconds / 1000 / 60;
        int seconds = timeInMilliseconds / 1000 % 60;
        String formattedTime = "" + minutes + ":";
        if (seconds < 10) {
            formattedTime += "0";
        }
        formattedTime += seconds;
        return formattedTime;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update the song progress bar.
            songProgressBar = customMusicAdapter.getSongProgressBars().get(currentSong);
            songProgressBar.setProgress(currentPosition);
            // Update elapsed time and remaining time.
            String elapsedTimeValue = formatTime(currentPosition);
            elapsedTime = customMusicAdapter.getElapsedTimes().get(currentSong);
            elapsedTime.setText(elapsedTimeValue);
            remainingTime = customMusicAdapter.getRemainingTimes().get(currentSong);
            String remainingTimeValue = formatTime(songLength - currentPosition);
            remainingTime.setText("-" + remainingTimeValue);
        }
    };

    public void onStop(View v) {
        if (!stopped) {
            stopped = true;
            mediaPlayers.get(currentSong).stop();
            for (int i = 0; i < mediaPlayers.size(); ++i) {
                customMusicAdapter.getSongProgressBars().get(i).setProgress(0);
                customMusicAdapter.getElapsedTimes().get(i).setText(formatTime(0));
                customMusicAdapter.getRemainingTimes().get(i).
                        setText("-" + formatTime(mediaPlayers.get(i).getDuration()));
                mediaPlayers.get(i).release();
                mediaPlayers.set(i, null);
            }
            playPauseButtons.setImageResource(R.drawable.play);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        movingToNextSong = true;
        mediaPlayers.get(currentSong).release();
        mediaPlayers.set(currentSong, null);
        ++currentSong;
        if (currentSong < playlist.size()) {
            songProgressBar = customMusicAdapter.getSongProgressBars().get(currentSong);
            songLength = mediaPlayers.get(currentSong).getDuration();
            mediaPlayers.get(currentSong).start();
        } else {
            stopped = true;
        }
        movingToNextSong = false;
    }
}
