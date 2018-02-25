package com.example.qian.cs446project;

import java.util.LinkedList;

/**
 * Class com.example.qian.cs446project.Playlist encapsulates multiple com.example.qian.cs446project.Song objects.
 */

public class Playlist {
    public LinkedList<Song> songs;

    private String playlistName;
    private int playlistID;

    // Constructor simply initializes its members.
    public Playlist(String playlistName, int playlistID) {
        songs = new LinkedList<Song>();
        this.playlistName = playlistName;
        this.playlistID = playlistID;
    }

    public String toString() { return getPlaylistName(); }

    public String getPlaylistName() {
        return playlistName;
    }

    public int getPlaylistID() {
        return playlistID;
    }

    public void close() {
        songs = null;
        playlistName = null;
        playlistID = -1;
    }
}
