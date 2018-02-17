package com.example.qian.cs446project;

import android.widget.SeekBar;

/**
 * Created by Qian on 2018-02-15.
 */

// Ke Qiao Chen: I based this class on the class shown in
// https://github.com/quocnguyenvan/media-player-demo/blob/master/app/src/main/java/com/quocnguyen/mediaplayerdemo/Music.java
// and made the following change:
// - The variable that I named "uri" is named "song" in the tutorial. I named this variable "uri"
// because the value that I set it to is the uri for the song that the PlaylistSong instance
// represents.
public class PlaylistSong {

    private String fileName;
    private int uri;

    public PlaylistSong(String fileName, int uri) {
        this.fileName = fileName;
        this.uri = uri;
    }

    public String getFileName() { return fileName; }

    public int getUri() {
        return uri;
    }

}
