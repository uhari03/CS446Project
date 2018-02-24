package com.example.qian.cs446project;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Qian on 2018-02-24.
 */

// Ke Qiao Chen: I based this class on
// http://www.vogella.com/tutorials/AndroidParcelable/article.html
public class PlaylistParcelable implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public PlaylistParcelable createFromParcel(Parcel in) {
            return new PlaylistParcelable(in);
        }

        public PlaylistParcelable[] newArray(int size) {
            return new PlaylistParcelable[size];
        }

    };

    private Playlist playlist;

    public PlaylistParcelable(Playlist playlist) {
        this.playlist = playlist;
    }

    public PlaylistParcelable(Parcel in) {
        this.playlist = (Playlist) in.readValue(playlist.getClass().getClassLoader());
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(playlist);
    }

}
