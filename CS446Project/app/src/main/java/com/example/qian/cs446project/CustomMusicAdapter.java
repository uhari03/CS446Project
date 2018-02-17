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

/**
 * Created by Qian on 2018-02-15.
 */

public class CustomMusicAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private ArrayList<PlaylistSong> playlistSongs;
    private ArrayList<MediaPlayer> mediaPlayers = new ArrayList<>();
    private ArrayList<SeekBar> songProgressBars = new ArrayList<>();
    private ArrayList<TextView> elapsedTimes = new ArrayList<>();
    private ArrayList<TextView> remainingTimes = new ArrayList<>();
    private MainActivity mainActivity = new MainActivity();

    public CustomMusicAdapter(Context context, int layout, ArrayList<PlaylistSong> playlistSongs,
                              ArrayList<MediaPlayer> mediaPlayers) {
        this.context = context;
        this.layout = layout;
        this.playlistSongs = playlistSongs;
        this.mediaPlayers = mediaPlayers;
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
        TextView textFileName, remainingTime;
        SeekBar songProgressBar;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        PlaylistSong playlistSong = playlistSongs.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(layout, null);
            viewHolder.textFileName = (TextView) convertView.findViewById(R.id.fileName);
            viewHolder.songProgressBar = (SeekBar) convertView.findViewById(R.id.songProgressBar);
            songProgressBars.add(viewHolder.songProgressBar);
            elapsedTimes.add((TextView) convertView.findViewById(R.id.elapsedTime));
            viewHolder.remainingTime = (TextView) convertView.findViewById(R.id.remainingTime);
            remainingTimes.add(viewHolder.remainingTime);
            convertView.setTag(viewHolder);
            viewHolder.textFileName.setText(playlistSong.getFileName());
            int currentSongLength = mediaPlayers.get(position).getDuration();
            viewHolder.songProgressBar.setMax(currentSongLength);
            viewHolder.remainingTime.setText("-" + mainActivity.formatTime(currentSongLength));
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
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
