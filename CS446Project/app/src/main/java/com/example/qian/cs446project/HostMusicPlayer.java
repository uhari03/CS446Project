package com.example.qian.cs446project;

import android.media.MediaPlayer;
import android.view.View;

/**
 * Created by Qian on 2018-03-14.
 */

public interface HostMusicPlayer {

    // Plays the session playlist if it is not playing and pauses the session playlist otherwise.
    void onTogglePlay(View v);

    // Stops the session playlist if it is not stopped.
    void onStop(View v);

    // Mutes the app on the user's phone if it is not muted and unmutes the app on the user's
    // phone otherwise.
    void onToggleMute(View v);

    // Handles work for transitioning to the next song in the playlist, if applicable, when a song
    // finishes and for resetting the playlist when the last song finishes.
    void onCompletion(MediaPlayer mp);

}
