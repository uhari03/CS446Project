/**
 * Class Song represents a local audio file.
 */

public class Song {
    private String title;
    private String album;
    private String artist;
    private String filePath;
    private int localDeviceFileID;

    // Constructor simply assigns given values to class attributes.
    public Song(String title, String album, String artist, String filePath, int localDeviceFileID) {
        this.title = title;
        this.album = album;
        this.artist = artist;
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

    public String getFilePath() {
        return filePath;
    }

    public int getLocalDeviceFileID() {
        return localDeviceFileID;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setLocalDeviceFileID(int localDeviceFileID) {
        this.localDeviceFileID = localDeviceFileID;
    }
}
