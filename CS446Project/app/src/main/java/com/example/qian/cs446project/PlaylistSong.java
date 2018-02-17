package com.example.qian.cs446project;

import android.widget.SeekBar;

/**
 * Created by Qian on 2018-02-15.
 */

public class PlaylistSong {

    private String fileName;
    private int identifyingNumber;

    public PlaylistSong(String fileName, int identifyingNumber) {
        this.fileName = fileName;
        this.identifyingNumber = identifyingNumber;
    }

    public String getFileName() { return fileName; }

    public int getIdentifyingNumber() {
        return identifyingNumber;
    }

}
