import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author LONGCHENG
 */
public class Music_Database {
    private static final String Database_Name = "test111";
    private static final String MUSIC_TABLE = "SONG";
    private static final String PLAYLIST_TABLE = "PLAYLIST";
    private static final String PLAYLIST_MUSIC_TABLE = "PLAYLIST_SONG";
    private static final String COLUMN_CONFIG_TABLE = "COLUMN_CONFIG";
    private static final String RECENT_SONGS_TABLE = "RECENT_SONGS";
    private static final String[] MUSIC_COLUMNS =  {"MusicID", "FilePath", "title", "artist", "album", "yearReleased",
            "genre", "comment"};
    private static final String[] PLAYLIST_COLUMNS = {"playlistId", "playlistName"};
    private static final String[] PLAYLIST_SONG_COLUMNS = {"playlistId", "MusicID"};
    private static final String[] COLUMN_CONFIG_COLUMNS = {"columnName", "columnIndex", "columnVisible"};
    private Connection conn;
    private PreparedStatement statement;
    private boolean connected;
    private int orderby = 0;

    public Music_Database() {
        connect();      // creates db if not already present
        createTables(); // if not already present
    }

    // ======Gernal Database method====== 
    private void connect() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            //Get a connection
            conn = DriverManager.getConnection("jdbc:derby:" + Database_Name);
            // getConnection() can also have a second parameter, Properties,  to add username/password etc
            connected = true;
        } catch (Exception except) {
            // If database does not exist; create database
            createDatabase();
        }
    }

    /*
     * // Create Music Database
     * @return true if database was created successfully
     */
    private boolean createDatabase() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
          
            //Get a connection
            conn = DriverManager.getConnection("jdbc:derby:" + Database_Name + ";create=true");
            // getConnection() can also have a second parameter, Properties,  to add username/password etc
            connected = true;
            return true;
        } catch (Exception except) {
            except.printStackTrace();
            return false;
        }
    }

    /*
     * Creates the database tables, if not already present
     */
    private void createTables() {
        Create_Music_Table();
        createPlaylistTable();
        createPlaylistSongTable();
        createColumnConfigTable();
        createRecentSongTable();
    }

    /**
     * Checks if database is connected
     *
     * @return true if database is connected
     */
    public boolean isConnected() {
        return connected;
    }
    
    public void sort(String MUSIC_TABLE, String choice, int orderby) {
        if (orderby == 0) {
            choice = choice + " asc"; 
        } else {
            choice = choice + " desc";
        }
        try {
            String query = "SELECT * FROM " + MUSIC_TABLE +
                    " ORDER BY " + choice;
            statement = conn.prepareStatement(query);
            statement.execute();
            statement.close();
        }
        catch (SQLException sqlExcept) {
            // Table Exists
        }
        if (orderby == 0) {
            orderby = 1; 
        } else {
            orderby = 0;
        }
    }

    public String get_databasename()
    {
        return Database_Name;
    }
    /* Create Music table method , if it doesn't already exist
     * @return true if table was created successfully
     */
    private boolean Create_Music_Table() {
        try {
            String query = "CREATE TABLE " + MUSIC_TABLE +
                    " (MusicID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "FilePath VARCHAR(200) UNIQUE NOT NULL, " +
                    "title VARCHAR(150), " +
                    "artist VARCHAR(100), " +
                    "album VARCHAR(150), " +
                    "yearReleased VARCHAR(4), " +
                    "genre VARCHAR(20), " +
                    "comment VARCHAR(200), " +
                    "PRIMARY KEY (MusicID))";
            statement = conn.prepareStatement(query);
            statement.execute();
            statement.close();
            return true;
        }
        catch (SQLException sqlExcept) {
            // Table Exists
        }
        return false;
    }

    /**
     * Inserts the given song into the ShiBase database
     *
     * @param song the song to insert into the database
     * @return the song id in db if the song was inserted successfully
     *         -1 if the song already exists, or the insert failed
     */
    public int Insert_Music(Musics music) {
        // To store the song id, or return -1 if db insert fails
        int id = -1;
        ResultSet keys = null;

        if(!If_Music_Exists(music.getFilePath())) {
            try {
                String query = "INSERT INTO " + MUSIC_TABLE +
                        " (FilePath, title, artist, album, yearReleased, genre, comment)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?)";
                statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, music.getFilePath());
                statement.setString(2, music.getTitle());
                statement.setString(3, music.getArtist());
                statement.setString(4, music.getAlbum());
                statement.setString(5, music.getYear());
                statement.setString(6, music.getGenre());
                statement.setString(7, music.getComment());
                statement.execute();
                keys = statement.getGeneratedKeys();
                while (keys.next()) {
                    id = keys.getInt(1);
                }
                keys.close();
                statement.close();
            } catch (SQLException sqlExcept) {
                sqlExcept.printStackTrace();
            }
        }
        return id;
    }

    /**
     * @param filePath the filePath of the song to look for in the database
     * @return true if the song exists in the database
     */
    public boolean If_Music_Exists(String FilePath) {
        int rowCount = 0;

        try {
            String query = "SELECT count(*) AS rowcount FROM " + MUSIC_TABLE +
                    " WHERE FilePath=?";
            statement = conn.prepareStatement(query);
            statement.setString(1, FilePath);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            rowCount = resultSet.getInt("rowcount");
            statement.close();
            if(rowCount != 0) {
                // song exists, return true
                return true;
            }
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
            return false;
        }
        // song doesn't exist
        return false;
    }

    /**
     * @param MusicID the unique song id of the song to delete
     * @return true if the song was successfully deleted, false if otherwise
     */
    public boolean Delete_Music(int MusicID) {
        try {
            String query = "DELETE FROM " + MUSIC_TABLE + " WHERE MusicID=?";
            statement = conn.prepareStatement(query);
            statement.setInt(1, MusicID);
            statement.execute();
            statement.close();
            return true;
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return false;
    }

    /**
     * @return all songs from the database as a multidimensional object array
     */
    public Object[][] getAllSongs()
    {
        Object[][] allSongs;
        int rowCount = 0;
        int index = 0;

        try {
            // Get record count
            String rowCountQuery = "SELECT count(*) AS rowcount FROM " + MUSIC_TABLE;
            statement = conn.prepareStatement(rowCountQuery);
            ResultSet rowCountRS = statement.executeQuery();
            rowCountRS.next();
            rowCount = rowCountRS.getInt("rowcount");

            // Initialize multidimensional array large enough to hold all songs
            allSongs = new Object[rowCount][MUSIC_COLUMNS.length];

            // Get all records
            String allSongsQuery = "SELECT * FROM " + MUSIC_TABLE + " ORDER BY title";
            statement = conn.prepareStatement(allSongsQuery);
            ResultSet allSongsRS = statement.executeQuery();

            while(allSongsRS.next()) {
                allSongs[index] = Get_Row(allSongsRS);
                index++;
            }
            statement.close();
            return allSongs;
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return new Object[0][0];
    }

    /**
     * @param filePath the file path of the song being searched for
     * @return the unique integer id of the song being searched for
     *         returns -1 if not found
     */
    public int Get_Music_ID(String FilePath) {
        int MusicID = -1;
        try {
            String query = "SELECT * FROM " + MUSIC_TABLE + " WHERE FilePath=?";
            statement = conn.prepareStatement(query);
            statement.setString(1, FilePath);
            ResultSet MusicIDRS = statement.executeQuery();
            if(MusicIDRS.next()) {
                MusicID = MusicIDRS.getInt("MusicID");
            }
            statement.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return MusicID;
    }

    /**
     * @param MusicID the song id of the song being searched for
     * @return the unique file path of the song being searched for
     */
    public String Get_Music_Filepath(int MusicID) {
        String songFilePath = null;
        try {
            String query = "SELECT * FROM " + MUSIC_TABLE + " WHERE MusicID=?";
            statement = conn.prepareStatement(query);
            statement.setInt(1, MusicID);
            ResultSet MusicIDRS = statement.executeQuery();
            if(MusicIDRS.next()) {
                songFilePath = MusicIDRS.getString("FilePath");
            }
            statement.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return songFilePath;
    }


    /**
     * @param MusicID the song id of the song being searched for
     * @return the song title of the song being searched for
     */
    public String Get_Title(int MusicID) {
        String title = null;
        try {
            String query = "SELECT title FROM " + MUSIC_TABLE + " WHERE MusicID=?";
            statement = conn.prepareStatement(query);
            statement.setInt(1, MusicID);
            ResultSet MusicIDRS = statement.executeQuery();
            if(MusicIDRS.next()) {
                title = MusicIDRS.getString("title");
            }
            statement.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return title;
    }

    /*
     * Returns the given SONG row as a String array
     *
     * @param rs the current result set item
     * @return the given result from the SONG table as a String array
     */
    private String[] Get_Row(ResultSet rs) {
        String[] song = new String[MUSIC_COLUMNS.length];
        try {
            for(int i = 0; i < MUSIC_COLUMNS.length; i++) {
                song[i] = rs.getString(MUSIC_COLUMNS[i]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return song;
    }

    /*
     * Create the PLAYLIST table
     *
     * @return true if table created successfully
     */
    private boolean createPlaylistTable() {
        try {
            String query = "CREATE TABLE " + PLAYLIST_TABLE +
                    " (playlistId INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "playlistName VARCHAR(100) UNIQUE NOT NULL, " +
                    "PRIMARY KEY (playlistId))";
            statement = conn.prepareStatement(query);
            statement.execute();
            statement.close();
            return true;
        }
        catch (SQLException sqlExcept) {
            // Table Exists
        }
        return false;
    }

    /**
     * accessors method to get all play list names
     *
     * @return an ArrayList of play list names as Strings
     */
    public ArrayList<String> getPlaylistNames() {
        ArrayList<String> playlistNames = new ArrayList<String>();
        try {
            // Get all playlist names
            String query = "SELECT playlistName FROM " + PLAYLIST_TABLE +
                    " ORDER BY playlistName ASC";
            statement = conn.prepareStatement(query);
            ResultSet playlistRS = statement.executeQuery();
            while(playlistRS.next()) {
                playlistNames.add(playlistRS.getString("playlistName"));
            }
            statement.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return playlistNames;
    }

    /**
     * Add a new play list to the PLAY LIST table
     *
     * @param playlist the name of the newly created play list
     * @return true if entry successfully added to table
     */
    public boolean addPlaylist(String playlist) {
        try {
            String query = "INSERT INTO " + PLAYLIST_TABLE + " (playlistName) VALUES (?)";
            statement = conn.prepareStatement(query);
            statement.setString(1, playlist);
            statement.execute();
            statement.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Delete a play list from the PLAYLIST table
     *
     * @param playlist the name of play list to be deleted
     * @return true if entry successfully deleted from table
     */
    public boolean deletePlaylist(String playlist) {
        try {
            String query = "DELETE FROM " + PLAYLIST_TABLE +
                    " WHERE playlistName = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, playlist);
            statement.execute();
            statement.close();

            //playlistNames.remove(playlist);
            return true;
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return false;
    }

    /**
     * Adds the given song to given play list
     *
     * @param filePath the filePath of the song being added
     * @param playlistName the name of the play list to add the given song to
     * @return true if song successfully added to play list
     */
    public boolean addSongToPlaylist(String filePath, String playlistName) {
        try {
            int MusicID = Get_Music_ID(filePath);
            int playlistId = getPlaylistId(playlistName);
            if(MusicID!= -1 && playlistId != -1) {
                // SUCCESS: song and playlist id's found
                String query = "INSERT INTO " + PLAYLIST_MUSIC_TABLE +
                        " (playlistId, MusicID) " +
                        " VALUES (" + playlistId + ", " + MusicID + ")";
                statement = conn.prepareStatement(query);
                statement.execute();
                statement.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes the given song to given play list
     *
     * @param MusicID the unique song id of the song being deleted
     * @param playlist the play list to delete the given song from
     * @return true if song successfully deleted to play list
     */
    public boolean deleteSongFromPlaylist(int MusicID, String playlist) {
        try {
            int playlistId = getPlaylistId(playlist);
            if(MusicID!= -1 && playlistId != -1) {
                // SUCCESS: song and playlist id's found
                String query = "DELETE FROM " + PLAYLIST_MUSIC_TABLE +
                        " WHERE playlistId = " + playlistId +
                        " AND MusicID = " + MusicID;
                statement = conn.prepareStatement(query);
                statement.execute();
                statement.close();
                return true;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * Create the junction table that will associate a Song with
     * a Playlist by Primary Key (id)
     *
     * @return true if table created successfully
     */
    private boolean createPlaylistSongTable() {
        try {
            String query = "CREATE TABLE " + PLAYLIST_MUSIC_TABLE +
                    "(playlistId INTEGER NOT NULL, " +
                    "MusicID INTEGER NOT NULL, " +
                    "CONSTRAINT fk_MusicID FOREIGN KEY (MusicID) " +
                    "REFERENCES " + MUSIC_TABLE + " (MusicID) " +
                    "ON DELETE CASCADE, " +
                    "CONSTRAINT fk_playlistId FOREIGN KEY (playlistId) " +
                    "REFERENCES " + PLAYLIST_TABLE + " (playlistId) " +
                    "ON DELETE CASCADE )";
            statement = conn.prepareStatement(query);
            statement.execute();
            statement.close();
            return true;
        }
        catch (SQLException sqlExcept) {
            // Table Exists
        }
        return false;
    }

    /**
     * Get all the songs associated with the given playlistId
     *
     * @param playlistName the name of the play list to get all songs from
     * @return an ArrayList of Songs associated with the given play list
     */
    public Object[][] getPlaylistSongs(String playlistName) {
        Object[][] playlistSongs;
        int playlistId = getPlaylistId(playlistName);
        int rowCount;
        int index = 0;

        try {
            // Get record count - which will be the size of
            // the first dimension of the multidimensional array
            // this method returns (ie. the number of songs in playlist)
            String rowCountQuery = "SELECT count(*) AS rowcount FROM " + PLAYLIST_MUSIC_TABLE +
                    " WHERE playlistId = " + playlistId;
            statement = conn.prepareStatement(rowCountQuery);
            ResultSet rowCountRS = statement.executeQuery();
            rowCountRS.next();
            rowCount = rowCountRS.getInt("rowcount");

            // Initialize multidimensional array large enough to hold all songs in playlist
            playlistSongs = new Object[rowCount][MUSIC_COLUMNS.length];

            // Get all playlist songs
            String query = "SELECT * FROM " + MUSIC_TABLE +
                    " JOIN " + PLAYLIST_MUSIC_TABLE +
                    " USING (MusicID) WHERE playlistID = " + playlistId +
                    " ORDER BY title";
            statement = conn.prepareStatement(query);
            ResultSet playlistSongsRS = statement.executeQuery();

            while(playlistSongsRS.next()) {
                playlistSongs[index] = Get_Row(playlistSongsRS);
                index++;
            }
            statement.close();
            return playlistSongs;
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return new Object[0][0];
    }

    /*
     * Get the unique integer id of a playlist based on its
     * name (which is also unique)
     *
     * @param playlistName the name of the playlist being searched for
     * @return the unique integer id of the playlist being searched for
     *         returns -1 if not found
     */
    private int getPlaylistId(String playlistName) {
        int playlistId = -1;
        try {
            String query = "SELECT * FROM " + PLAYLIST_TABLE + " WHERE playlistName=?";
            statement = conn.prepareStatement(query);
            statement.setString(1, playlistName);
            ResultSet MusicIDRS = statement.executeQuery();
            if(MusicIDRS.next()) {
                playlistId = MusicIDRS.getInt("playlistId");
            }
            statement.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return playlistId;
    }

    
    private boolean createColumnConfigTable() {
        String query;
        try {
            // Create Table
            query = "CREATE TABLE " + COLUMN_CONFIG_TABLE +
                    " (columnName VARCHAR(50)," +
                    "columnVisible BOOLEAN NOT NULL)";
            statement = conn.prepareStatement(query);
            statement.execute();
            statement.close();

            // Populate table with default values
            for(int i = 0; i < Music_Table.Column_Name.length; i++) {
                query = "INSERT INTO " + COLUMN_CONFIG_TABLE +
                        " (columnName, columnVisible)" +
                        " VALUES (?, ?)";
                statement = conn.prepareStatement(query);
                String columnName = Music_Table.Column_Name[i];
                statement.setString(1, columnName);
                if (columnName.equals("ID") || columnName.equals("FilePath")) {
                    statement.setBoolean(2, false); // set File path and ID is not visible in player frame
                }
                else {
                    statement.setBoolean(2, true);   // default state for all other columns is visible
                }
                statement.execute();
                statement.close();
            }
            return true;
        } catch (SQLException sqlExcept) {
            // Table Exists
        }
        return false;
    }
    
    public boolean getColumnVisible(String columnName) {
        boolean columnVisible = false;
        try {
            // Get all playlist names
            String query = "SELECT columnVisible FROM " + COLUMN_CONFIG_TABLE +
                    " WHERE columnName=?";
            statement = conn.prepareStatement(query);
            statement.setString(1, columnName);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            columnVisible = resultSet.getBoolean("columnVisible");
            statement.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return columnVisible;
    }

    /**
     * @return boolean indicating whether column is visible
     * @param columnName the column name of the column being changed
     * @param visible the visible to set for the column
     */
    public void setColumnVisible(String columnName, boolean visible) {
        try {
            // Get all playlist names
            String query = "UPDATE " + COLUMN_CONFIG_TABLE +
                    " SET columnVisible=? " +
                    " WHERE columnName=?";
            statement = conn.prepareStatement(query);
            statement.setBoolean(1, visible);
            statement.setString(2, columnName);
            statement.execute();
            statement.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    private boolean createRecentSongTable() {
        try {
            String query = "CREATE TABLE " + RECENT_SONGS_TABLE +
                    " (MusicID INTEGER NOT NULL, " +
                    "CONSTRAINT fk_recent_songId FOREIGN KEY (MusicID) " +
                    "REFERENCES " + MUSIC_TABLE + " (MusicID) " +
                    "ON DELETE CASCADE)";
            statement = conn.prepareStatement(query);
            statement.execute();
            statement.close();
            return true;
        } catch (SQLException sqlExcept) {
            System.out.println("Table does not exist");
        }
        return false;
    }

    /**
     * Adds the given song to recent songs
     *
     * @param MusicID the song to add to recent songs
     * @return true if song successfully added to recent songs
     */
    public boolean addRecentSong(int songId) {
        try {
            // Insert given song into recent songs table
            String query = "INSERT INTO " + RECENT_SONGS_TABLE +
                    " (MusicID) " +
                    " VALUES (?)";
            statement = conn.prepareStatement(query);
            statement.setInt(1, songId);
            statement.execute();
            statement.close();

            // Get row count after insert
            query = "SELECT count(*) as rowCount FROM " + RECENT_SONGS_TABLE;
            statement = conn.prepareStatement(query);
            ResultSet countRS = statement.executeQuery();
            int rowCount = 0;
            while(countRS.next()) {
                rowCount = countRS.getInt("rowCount");
            }
            statement.close();

            // If rowCount > 10 (ie. 11) delete the oldest song in recent songs table
            if(rowCount > 10) {
                query = "DELETE FROM " + RECENT_SONGS_TABLE +
                        " WHERE MusicID IN (SELECT MusicID FROM " + RECENT_SONGS_TABLE +
                        " FETCH FIRST ROW ONLY)";
                statement = conn.prepareStatement(query);
                statement.executeUpdate();
                statement.close();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int[] getRecentSongs() {
        ArrayList<Integer> recentSongsList = new ArrayList<Integer>();
        try {
            // Get all recent song ids
            String query = "SELECT MusicID FROM " + RECENT_SONGS_TABLE;
            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                recentSongsList.add(rs.getInt("MusicID"));
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int[] recentSongs = new int[recentSongsList.size()];
        for(int i = 0; i < recentSongs.length; i++) {
            recentSongs[i] = recentSongsList.get(i);
        }
        return recentSongs;
    }
}
