package com.example.qian.cs446project;

/**
 * Created by Qian on 2018-02-15.
 */

public class PlaylistSong {

    private String filePath;
    private String title;
    private String artist;
    private String album;
    private int duration;

    public PlaylistSong(String filePath, String title, String artist, String album, int duration) {
        this.filePath = filePath;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public int getDuration() {
        return duration;
    }
}
