import java.util.LinkedList;

/**
 * Class Playlist encapsulates multiple Song objects.
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
