package com.example.qian.cs446project;

import android.content.Context;
import android.media.MediaPlayer;
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
// - This class in the tutorial only has 1 MediaPlayer, whereas I have an ArrayList of MediaPlayers.
// I implemented the class this way because I need to show the length of each song in the playlist,
// including songs that have not yet started to play. Therefore, in MainActivity.java's
// onCreate(Bundle savedInstanceState) method, I create a MediaPlayer for each song in the playlist
// so that I can find out the duration of each song.
// - Because MainActivity.java must update a song's progress bar, elapsed time, and remaining time
// as the song plays, I created ArrayLists for each of these widgets and implemented get methods for
// them.
// - In the method getView(int position, View convertView, ViewGroup parent), I only set the widgets
//  of each ViewHolder once (ViewHolder represents the GUI widgets for a song in the playlist)
// because songs' file names remain constant and MainActivity.java later updates the progress bar,
// elapsed time, and remaining time. That is, I only set the widgets of a viewHolder if convertView
// is null, meaning the GUI widgets for a song have not yet been created and the program is calling
// getView(int position, View convertView, ViewGroup parent) to create them.
public class CustomMusicAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private ArrayList<PlaylistSong> playlistSongs;
    private MediaPlayer mediaPlayer;
    private ArrayList<SeekBar> songProgressBars = new ArrayList<>();
    private ArrayList<TextView> elapsedTimes = new ArrayList<>();
    private ArrayList<TextView> remainingTimes = new ArrayList<>();
    private HashMap<Integer, View> displayedSongs = new HashMap<>();
    private static final TimeFormatter timeFormatter = new TimeFormatter();

    public CustomMusicAdapter(Context context, int layout, ArrayList<PlaylistSong> playlistSongs,
                              MediaPlayer mediaPlayer) {
        this.context = context;
        this.layout = layout;
        this.playlistSongs = playlistSongs;
        this.mediaPlayer = mediaPlayer;
    }

    @Override
    public int getCount() {
        return playlistSongs.size();
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
        PlaylistSong playlistSong = playlistSongs.get(position);
        if (!displayedSongs.containsKey(position)) {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(layout, null);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.artist = convertView.findViewById(R.id.artist);
            viewHolder.album = convertView.findViewById(R.id.album);
            viewHolder.songProgressBar = convertView.findViewById(R.id.songProgressBar);
            songProgressBars.add(viewHolder.songProgressBar);
            elapsedTimes.add((TextView) convertView.findViewById(R.id.elapsedTime));
            viewHolder.remainingTime = convertView.findViewById(R.id.remainingTime);
            remainingTimes.add(viewHolder.remainingTime);
            viewHolder.title.setText(viewHolder.title.getText() + playlistSong.getTitle());
            viewHolder.artist.setText(viewHolder.artist.getText() + playlistSong.getArtist());
            viewHolder.album.setText(viewHolder.album.getText() + playlistSong.getAlbum());
            int currentSongLength = 0;
            // If an instance of HostMusicPlayer instantiates CustomMusicAdapter, the
            // HostMusicPlayer will pass a MediaPlayer to CustomMusicAdapter's constructor. An
            // instance of ParticipantMusicPlayer will pass null because the music files must be
            // sent to non-host devices before they can create a MediaPlayer instance for those
            // files.
            if (mediaPlayer != null) {
                currentSongLength = mediaPlayer.getDuration();
            }
            viewHolder.songProgressBar.setMax(currentSongLength);
            viewHolder.remainingTime.setText("-" + timeFormatter.formatTime(currentSongLength));
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

}
