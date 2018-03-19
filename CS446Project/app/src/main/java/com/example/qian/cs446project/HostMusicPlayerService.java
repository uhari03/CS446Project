package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import static com.example.qian.cs446project.CS446Utils.broadcastIntentWithoutExtras;

public class HostMusicPlayerService extends SynchronicityMusicPlayerService {

    private BroadcastReceiver hostMusicPlayerServiceReceiver;
    private IntentFilter hostMusicPlayerServiceFilter;

    @Override
    void resetPlaylist() {
        super.resetPlaylist();
        // We might want to prevent users from joining a session while a session playlist is
        // playing or paused. HostMusicPlayerService broadcasts an Intent when the host starts the
        // session playlist and another when the host stops the session playlist or the playlist
        // finishes. As a result, other components know when they should prevent users from
        // joining a session.
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.playlist_stopped),
                HostMusicPlayerService.this);
    }

    public HostMusicPlayerService() {
    }

    class HostMusicPlayerBinder extends Binder {

        HostMusicPlayerService getBinder() {
            return HostMusicPlayerService.this;
        }

    }

    HostMusicPlayerBinder hostMusicPlayerBinder = new HostMusicPlayerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        // Broadcast an Intent to tell the app that the user has created a session.
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.session_created),
                this);
        hostMusicPlayerServiceFilter = new IntentFilter();
        // When the 1st participant joins the session, the connection manager requests the session
        // playlist from the host.
        hostMusicPlayerServiceFilter
                .addAction(applicationContext.getString(R.string
                .request_session_playlist));
        hostMusicPlayerServiceFilter.addAction(applicationContext.getString(R.string.receive_stop));
        hostMusicPlayerServiceFilter.addAction(applicationContext.getString(R.string.receive_play));
        hostMusicPlayerServiceFilter
                .addAction(applicationContext.getString(R.string.receive_pause));
        hostMusicPlayerServiceReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(applicationContext.getString(R.string.request_session_playlist)))
                {
                    Intent returnPlaylistIntent =
                            new Intent(applicationContext
                            .getString(R.string.return_session_playlist));
                    returnPlaylistIntent.putExtra(applicationContext
                            .getString(R.string.session_playlist), playlist);
                    LocalBroadcastManager.getInstance(HostMusicPlayerService.this)
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
                hostMusicPlayerServiceReceiver, hostMusicPlayerServiceFilter
        );
        return hostMusicPlayerBinder;
    }

    @Override
    public void setMusicPlayerState(MusicPlayerState musicPlayerState) {
        if (this.musicPlayerState.isStopped()) {
            // At least in the prototype, we might want to prevent users from joining a session
            // while a session playlist is playing or paused. HostMusicPlayerService broadcasts an
            // Intent the host starts the session playlist and another when the host stops the
            // session playlist or the playlist finishes. As a result, other components know when
            // they should prevent users from joining a session.
            broadcastIntentWithoutExtras(
                    applicationContext.getString(R.string.playlist_not_stopped),
                    this);
        }
        super.setMusicPlayerState(musicPlayerState);
    }

    @Override
    public void stop() {
        super.stop();
        // At least in the prototype, we might want to prevent users from joining a session while a
        // session playlist is playing or paused. HostMusicPlayerService broadcasts an Intent when
        // the host starts the session playlist and another when the host stops the session playlist
        // or the playlist finishes. As a result, other components know when they should prevent
        // users from joining a session.
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.playlist_stopped),
                this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(hostMusicPlayerServiceReceiver);
        hostMusicPlayerServiceFilter = null;
        hostMusicPlayerServiceReceiver = null;
    }

}
