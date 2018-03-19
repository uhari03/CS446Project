package com.example.qian.cs446project;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Qian on 2018-03-18.
 */

public abstract class SynchronicityMusicPlayerService extends Service implements MusicPlayerService
{

    MusicPlayerState musicPlayerState = new MusicPlayerStoppedState();
    private boolean muted = false;
    private boolean movingToNextSong = false;
    private int currentSong = 0;
    MediaPlayer mediaPlayer;
    Context applicationContext;
    Playlist playlist;

    public boolean getMuted() {
        return muted;
    }

    public boolean getMovingToNextSong() {
        return movingToNextSong;
    }

    public void setMovingToNextSong(boolean movingToNextSong) {
        this.movingToNextSong = movingToNextSong;
    }

    public int getCurrentSong() {
        return currentSong;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMusicPlayerState(MusicPlayerState musicPlayerState) {
        this.musicPlayerState = musicPlayerState;
    }

    private void setMediaPlayerToCurrentSong() {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(playlist.songs.get(currentSong).getFilePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void resetPlaylist() {
        musicPlayerState = new MusicPlayerStoppedState();
        currentSong = 0;
        setMediaPlayerToCurrentSong();
    }

    public void createMediaPlayer() {
        mediaPlayer = MediaPlayer.create(applicationContext,
                Uri.parse(playlist.songs.get(currentSong).getFilePath()));
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.d("please help", "entered onCompletion");
                movingToNextSong = true;
                ++currentSong;
                if (currentSong < playlist.songs.size()) {
                    setMediaPlayerToCurrentSong();
                    mediaPlayer.start();
                } else {
                    resetPlaylist();
                }
                Intent onCompletionIntent =
                        new Intent(applicationContext.getString(R.string.song_completed));
                onCompletionIntent
                        .putExtra(applicationContext.getString(R.string.current_song_index),
                                currentSong);
                LocalBroadcastManager.getInstance(SynchronicityMusicPlayerService.this)
                        .sendBroadcast(onCompletionIntent);
            }

        });

    }

    public class SynchronicityMusicPlayerBinder extends Binder {

        SynchronicityMusicPlayerService getBinder() {
            return SynchronicityMusicPlayerService.this;
        }

    }

    SynchronicityMusicPlayerService.SynchronicityMusicPlayerBinder synchronicityMusicPlayerBinder =
            new SynchronicityMusicPlayerService.SynchronicityMusicPlayerBinder();

    @Override
    public IBinder onBind(Intent intent) {

        applicationContext = getApplicationContext();
        playlist = intent.getParcelableExtra(applicationContext.getString(R.string
                .session_playlist));
        createMediaPlayer();
        return synchronicityMusicPlayerBinder;
    }

    @Override
    public void play() {
        if (!musicPlayerState.isPlaying()) {
            musicPlayerState = new MusicPlayerPlayingState();
        }
    }

    @Override
    public void pause() {
        musicPlayerState = new MusicPlayerPausedState();
    }

    @Override
    public void stop() {
        musicPlayerState = new MusicPlayerStoppedState();
        resetPlaylist();
    }

    @Override
    public void mute() {
        mediaPlayer.setVolume(0, 0);
        muted = true;
    }

    @Override
    public void unmute() {
        mediaPlayer.setVolume(1, 1);
        muted = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        musicPlayerState = new MusicPlayerPlayingState();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
