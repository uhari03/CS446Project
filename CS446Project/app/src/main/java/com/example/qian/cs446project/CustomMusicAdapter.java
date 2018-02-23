package com.example.qian.cs446project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Qian on 2018-02-15.
 */

// Ke Qiao Chen: I based this class on
// https://github.com/quocnguyenvan/media-player-demo/blob/master/app/src/main/java/com/quocnguyen/mediaplayerdemo/CustomMusicAdapter.java
// except for the following changes:
// - In the tutorial, play, pause, and stop buttons are available for each song. Since my
// implementation only allows the user to play, pause, and stop an entire playlist, I do not
// include these buttons in this class. Instead, I use a similar idea to show for each song a
// progress bar, the elapsed time, and the remaining time.
// - Because HostMusicPlayer.java and ParticipantMusicPlayer.java must update a song's progress bar,
// elapsed time, and remaining time as the song plays, as well as bold the metadata of the playing
// song, I created ArrayLists for each of these widgets and implemented get methods for them.
public class CustomMusicAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private Playlist playlist;
    private ArrayList<TextView> titles = new ArrayList<>();
    private ArrayList<TextView> artists = new ArrayList<>();
    private ArrayList<TextView> albums = new ArrayList<>();
    private ArrayList<SeekBar> songProgressBars = new ArrayList<>();
    private ArrayList<TextView> elapsedTimes = new ArrayList<>();
    private ArrayList<TextView> remainingTimes = new ArrayList<>();
    private HashMap<Integer, View> displayedSongs = new HashMap<>();
    private static final CS446Utils cs446Utils = new CS446Utils();

    public CustomMusicAdapter(Context context, int layout, Playlist playlist) {
        this.context = context;
        this.layout = layout;
        this.playlist = playlist;
    }

    @Override
    public int getCount() {
        return playlist.songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        TextView title, artist, album, remainingTime;
        SeekBar songProgressBar;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        Song song = playlist.songs.get(position);
        if (!displayedSongs.containsKey(position)) {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(layout, null);
            viewHolder.title = convertView.findViewById(R.id.title);
            titles.add(viewHolder.title);
            viewHolder.artist = convertView.findViewById(R.id.artist);
            artists.add(viewHolder.artist);
            viewHolder.album = convertView.findViewById(R.id.album);
            albums.add(viewHolder.album);
            viewHolder.songProgressBar = convertView.findViewById(R.id.songProgressBar);
            songProgressBars.add(viewHolder.songProgressBar);
            elapsedTimes.add((TextView) convertView.findViewById(R.id.elapsedTime));
            viewHolder.remainingTime = convertView.findViewById(R.id.remainingTime);
            remainingTimes.add(viewHolder.remainingTime);
            viewHolder.title.setText(viewHolder.title.getText() + song.getTitle());
            viewHolder.artist.setText(viewHolder.artist.getText() + song.getArtist());
            viewHolder.album.setText(viewHolder.album.getText() + song.getAlbum());
            viewHolder.songProgressBar.setMax(song.getDuration());
            viewHolder.remainingTime.setText("-" + cs446Utils.formatTime(song.getDuration()));
            convertView.setTag(viewHolder);
            displayedSongs.put(position, convertView);
        } else {
            convertView = displayedSongs.get(position);
        }
        return convertView;
    }

    public ArrayList<SeekBar> getSongProgressBars() {
        return songProgressBars;
    }

    public ArrayList<TextView> getElapsedTimes() {
        return elapsedTimes;
    }

    public ArrayList<TextView> getRemainingTimes() {
        return remainingTimes;
    }

    public ArrayList<TextView> getTitles() {
        return titles;
    }

    public ArrayList<TextView> getArtists() {
        return artists;
    }

    public ArrayList<TextView> getAlbums() {
        return albums;
    }

}
