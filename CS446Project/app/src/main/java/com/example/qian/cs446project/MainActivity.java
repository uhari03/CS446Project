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
                    playlistSong.getUri());
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayers.add(mediaPlayer);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = findViewById(R.id.songlist);
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

    // Ke Qiao Chen: I based this method on the createTimeLabel(int time) method shown in
    // https://www.youtube.com/watch?v=zCYQBIcePaw at 13:47
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
            String elapsedTimeValue = formatTime(currentPosition);
            elapsedTime = customMusicAdapter.getElapsedTimes().get(currentSong);
            elapsedTime.setText(elapsedTimeValue);
            remainingTime = customMusicAdapter.getRemainingTimes().get(currentSong);
            String remainingTimeValue = formatTime(songLength - currentPosition);
            remainingTime.setText("-" + remainingTimeValue);
        }
    };

    // Ke Qiao Chen: I based this method on viewHolder.ivStop's OnClickListener in
    // https://github.com/quocnguyenvan/media-player-demo/blob/master/app/src/main/java/com/quocnguyen/mediaplayerdemo/CustomMusicAdapter.java
    // except that
    // - I clean up a MediaPlayer instance for each song in the playlist, instead of just the
    // current song. I need to do so because I need to show the duration of each song before the
    // user starts the playlist. To find out the duration of a song, I need to call the
    // getDuration() method on the MediaPlayer instance for that song. Therefore, I needed to
    // create a MediaPlayer instance for each song in the playlist before the user starts the
    // playlist.
    // - The tutorial does not set a MediaPlayer instance to null after releasing it, but I do this
    // because Java's garbage collector cannot detect shortage of media-related resources
    // (https://developer.android.com/guide/topics/media/mediaplayer.html#mpandservices).
    // - I also reset each song's progress bar, elapsed time, and remaining time. The tutorial does
    // not include these widgets.
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

    // Ke Qiao Chen: I based this method on Valentina Chumak's answer in
    // https://stackoverflow.com/questions/10916129/playing-multiple-files-in-mediaplayer-one-after-other-in-android
    // except for the following changes:
    // - I set a MediaPlayer instance to null after I release it.
    // - I increment currentSong before checking if it is less than the number of songs in the
    // playlist. The answer increments currentSong after the check, but this results in off-by-1
    // errors.
    // - I do not create a new MediaPlayer instance and set its onCompletionListener since I need to
    // create a MediaPlayer instance for every song in the playlist before the user starts the
    // playlist. Instead, I implement these actions in the method createMediaPlayers().
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
