import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author LONGCHENG, COREY
 */
public class Music_Table {
    
    // Table types
    public static int LIBRARY = 0;
    public static int PLAYLIST = 1;

    // To indicate MusicTable type
    private JTable table;
    private String name;   // the table name, "Library" or "Play list name"
    private int type;

    /**
     * The columns of the Music table
     */
    public static final String[] Column_Name =  {"ID", "FilePath", "Title", "Artist", "Album", "Year",
                                                        "Genre", "Comment"};
    public static final int Column_ID = 0;
    public static final int Column_Filepath = 1;

    /**
     * Default constructor for MusicTable
     * by default populates a JTable with the entire
     * contents of the users music library
     *
     */
    public Music_Table(){
        table = new JTable();
        buildTable(Run.db.getAllSongs());
        name = "Library";
        type = LIBRARY;
    }

    /**
     * @param playlistName the play list to populate the JTable with
     */
    public Music_Table(String playlistName) {
        table = new JTable();
        buildTable(Run.db.getPlaylistSongs(playlistName));
        name = playlistName;
        type = PLAYLIST;
    }

    /**
     * @param songs
     */
    private void buildTable(Object[][] songs) {
        DefaultTableModel tableModel = new DefaultTableModel(songs, Column_Name) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // all cells false -
                // this prevents individual cells from being editable
                return false;
            }
        };

        table.setModel(tableModel);
        table.getTableHeader().setReorderingAllowed(false); // don't allow reordering of columns
        setColumnVisibility();
    }
    
    public void setColumnVisibility() {
        for(int i = 0; i < Column_Name.length; i++) {
            String columnName = Column_Name[i];
            if (Run.db.getColumnVisible(columnName)) {show(columnName);}
            else {hide(columnName);}
        }
    }
    /**
     * Hides given column from view in the table model
     * and sets column visible state in db to false
     *
     * @param columnName column to hide
     */
    public void hide(String columnName) {
        int index = table.getColumnModel().getColumnIndex(columnName);

        TableColumn column = table.getColumnModel().getColumn(index);
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setWidth(0);

        // update visibility in db based on view index
        Run.db.setColumnVisible(columnName, false);
    }
    public void show(String columnName) {
        int index = table.getColumnModel().getColumnIndex(columnName);

        TableColumn column = table.getColumnModel().getColumn(index);
        column.setMinWidth(10);
        column.setMaxWidth(500);
        column.setWidth(10);
        column.setPreferredWidth(80);

        // update visibility in db
        Run.db.setColumnVisible(columnName, true);
    }
    
    /**
     * Updates where model is either "Library" or "Play List Name"
     *
     * @param name the table name, either "Library" or play list-name
     */
    public void updateTableModel(String name) {
        this.name = name;
        if (name.equals("Library")) {
            // update with library contents
            buildTable(Run.db.getAllSongs());
            type = LIBRARY;
        } else {
            // update table with playlist songs (type == playlist name)
            buildTable(Run.db.getPlaylistSongs(name));
            type = PLAYLIST;
        }
    }

    /**
     * @return the table
     */
    public JTable getTable() {
        return table;
    }

    /**
     * Adds the given song to the library list and database
     *
     * @param id the unique database id of the song
     * @param song the song to add to the library/database
     */
    public void addSongToTable(int id, Musics music) {
        // Add row to JTable
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addRow(new Object[]{String.valueOf(id), music.getFilePath(), music.getTitle(), music.getArtist(), music.getAlbum(),
                music.getYear(), music.getGenre(), music.getComment()});
        updateTableModel(name);
    }

    /**
     * Gets this MusicTable object's type (LIBRARY = 0 or PLAY LIST = 1)
     *
     * @return the table type
     */
    public int getType() {
        return type;
    }

}
