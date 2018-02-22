package com.example.qian.cs446project;

/**
 * Class Song represents a local audio file.
 */

public class Song {
    private String title;
    private String album;
    private String artist;
    private String filePath;
    private int duration;
    private int localDeviceFileID;

    // Constructor simply assigns given values to class attributes.
    public Song(String title, String album, String artist, int duration, String filePath, int localDeviceFileID) {
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.filePath = filePath;
        this.localDeviceFileID = localDeviceFileID;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public int getDuration() {
        return duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLocalDeviceFileID() {
        return localDeviceFileID;
    }
}
