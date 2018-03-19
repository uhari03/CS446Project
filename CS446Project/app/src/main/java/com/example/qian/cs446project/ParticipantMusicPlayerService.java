package com.example.qian.cs446project;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class ParticipantMusicPlayerService extends SynchronicityMusicPlayerService {

    private BroadcastReceiver participantMusicPlayerServiceReceiver;
    private IntentFilter participantMusicPlayerServiceFilter;

    public ParticipantMusicPlayerService() {
    }

    class ParticipantMusicPlayerBinder extends Binder {

        ParticipantMusicPlayerService getBinder() {
            return ParticipantMusicPlayerService.this;
        }

    }

    ParticipantMusicPlayerService.ParticipantMusicPlayerBinder participantMusicPlayerBinder =
            new ParticipantMusicPlayerService.ParticipantMusicPlayerBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        participantMusicPlayerServiceFilter = new IntentFilter();
        // When the 1st song in the session playlist finishes downloading onto the participant's
        // device, it creates a MediaPlayer with that song.
        participantMusicPlayerServiceFilter
                .addAction(applicationContext.getString(R.string.song_finished_downloading));
        participantMusicPlayerServiceReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(applicationContext.getString(R.string.song_finished_downloading)))
                {
                    createMediaPlayer();
                }
            }

        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                participantMusicPlayerServiceReceiver, participantMusicPlayerServiceFilter
        );
        return participantMusicPlayerBinder;
    }

    @Override
    public void play() {
        super.play();
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public void stop() {
        super.stop();
        mediaPlayer.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(participantMusicPlayerServiceReceiver);
        participantMusicPlayerServiceFilter = null;
        participantMusicPlayerServiceReceiver = null;
    }

}
