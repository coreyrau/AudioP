import java.awt.Dimension;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author LONGCHENG
 */
public class Player_Frame  extends JFrame implements BasicPlayerListener{
    //static ArrayList<Player_Frame> windows;
    private int playerState;
    private int windowType;
    private static int Library = 0;
    private static int PLAYLIST = 1;
    private JFrame windowFrame;
    private JScrollPane musicTableScrollPane;
    private Music_Table musicTable;
    private JPopupMenu musicTablePopupMenu;
    private JMenu addSongToPlaylistSubMenu;
    private MusicTablePopupListener musicTablePopupListener = new MusicTablePopupListener();
    private ColumnDisplayPopupListener columnDisplayPopupListener = new ColumnDisplayPopupListener();
    private JPopupMenu playlistPopupMenu;
    private JPopupMenu showColumnsPopupMenu;
    private JTree playlistPanelTree;
    private DefaultMutableTreeNode playlistNode;
    private String selectedPlaylist;
    private Mp3Player player;
    private long timeRemaining;
    private long timeElapsed;
    private int duration;
    private boolean songCompleted;
    private JSlider volumeSlider;
    private JMenu RecentMenu;
    private JCheckBoxMenuItem shuffle;
    private JCheckBoxMenuItem repeat;
    private JLabel L_timer;
    private JLabel R_timer;
    private JProgressBar progressbar;
    
    


    public Player_Frame() {

        // Set this Window instance's type to Player_Frame.Library
        this.windowType = Player_Frame.Library;

        // Set this Window instance's table
        this.musicTable = new Music_Table();

        // Set selected playlist to "Library" (the default table in Player_Frame.Library)
        this.selectedPlaylist = "Library";

        // Set this Window instance's player
        player = new Mp3Player();
        player.getPlayer().addBasicPlayerListener(this);

        buildWindowLayout("Player");
    }

    public Player_Frame(String playlistName) {

        // Set this Window instance's type to Window.PLAYLIST
        this.windowType = Player_Frame.PLAYLIST;

        // Set selected playlist
        this.selectedPlaylist = playlistName;

        // Set this Window instance's table
        this.musicTable = new Music_Table(playlistName);

        // Set this Window instance's player
        player = new Mp3Player();
        player.getPlayer().addBasicPlayerListener(this);

        // Add this window to list of application windows
        Run.windows.add(this);

        buildWindowLayout(playlistName);
    }

    public void display() {
        windowFrame.setVisible(true);
    }

    private void buildWindowLayout(String windowTitle) {
        // Create outer shiTunes frame and set various parameters
        windowFrame = new JFrame();
        windowFrame.setTitle(windowTitle);
        windowFrame.setMinimumSize(new Dimension(1100, 600));
        windowFrame.setLocationRelativeTo(null);

        if(windowType == Player_Frame.Library) {
            windowFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } else {
            if (windowType == Player_Frame.PLAYLIST) {
                windowFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                windowFrame.addWindowListener(new PlaylistWindowListener());
            }
        }

        // Create the main panel that resides within the windowFrame
        // Layout: BoxLayout, X_AXIS
        JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainPanel.setDividerLocation(150);

        // Instantiate scroll pane for table
        musicTableScrollPane = new JScrollPane(musicTable.getTable());

        // Create the controlTablePanel that will reside within the mainPanel
        // Layout: BoxLayout, Y_AXIS
        JPanel controlTablePanel = new JPanel();
        controlTablePanel.setLayout(new BoxLayout(controlTablePanel,BoxLayout.Y_AXIS));
        controlTablePanel.add(getControlPanel());
        controlTablePanel.add(musicTableScrollPane);
        controlTablePanel.setMinimumSize(new Dimension(500, 600));

        // Create menuBar and add Menu
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getFileMenu());
        menuBar.add(getControlsMenu());

        // Build the music table
        buildMusicTable();
        
        createPlaylistPopupMenu();

        // Build main panel
        if(windowType == Player_Frame.Library) {
            mainPanel.add(getPlaylistPanel());
        }
        mainPanel.add(controlTablePanel);

        // Add all GUI components to player_frame application frame
        windowFrame.setJMenuBar(menuBar);
        windowFrame.setContentPane(mainPanel);
        windowFrame.pack();
        windowFrame.setLocationByPlatform(true);
    }

    private void buildMusicTable() {
        musicTable.getTable().setPreferredScrollableViewportSize(new Dimension(500, 200));
        musicTable.getTable().setFillsViewportHeight(true);
        
        
        
        /* Add listeners */
        // Create right-click popup menu and set popup listener up JTable
        createMusicTablePopupMenu();
        musicTable.getTable().addMouseListener(musicTablePopupListener);
        
        createShowColumnsPopupMenu();
        musicTable.getTable().getTableHeader().addMouseListener(columnDisplayPopupListener);

        // Add double click listener to play selected song.
        musicTable.getTable().addMouseListener(new DoubleClickListener());

        // Add drop target on table
        // enabling drag and drop of files into table
        musicTable.getTable().setDropTarget(new AddToTableDropTarget());
        musicTable.getTable().setDragEnabled(true);
        
    }
    
    
    private JScrollPane getPlaylistPanel() {
        // Create the root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

        // Create library and playlist nodes
        DefaultMutableTreeNode libraryNode = new DefaultMutableTreeNode ("Library");
        playlistNode = new DefaultMutableTreeNode ("Playlists");

        updatePlaylistNode();

        // Add library and playlist nodes to the root
        root.add(libraryNode);
        root.add(playlistNode);

        // Create playlist panel tree
        playlistPanelTree = new JTree(root);

        // Add mouse listener: manages left and right click
        playlistPanelTree.addMouseListener(new PlaylistPanelMouseListener());

        // Make the root node invisible
        playlistPanelTree.setRootVisible(false);

        // Expand playlist node (index 1)
        playlistPanelTree.expandRow(1);



        // Instantiate playlist panel pane to be returned
        // and set minimum dimensions
        JScrollPane playlistPanelPane = new JScrollPane(playlistPanelTree);
        playlistPanelPane.setMinimumSize(new Dimension(150, 600));

        return playlistPanelPane;
    }

    private void updatePlaylistNode(){
        ArrayList<String> playlistNames = Run.db.getPlaylistNames();

        playlistNode.removeAllChildren();

        for(String playlistName : playlistNames) {
            DefaultMutableTreeNode playlist = new DefaultMutableTreeNode(playlistName);

            playlistNode.add(playlist);
        }
    }

    private JPanel getControlPanel() {
        JPanel controlPanel = new JPanel();
        JButton playButton = new JButton("Play");
            JButton pauseButton = new JButton("Pause");
            JButton stopButton = new JButton("Stop");
            JButton previousButton = new JButton("Previous");
            JButton nextButton = new JButton("Next");
            // Initialize Volume Slider
            volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
            volumeSlider.setMinorTickSpacing(10);
            volumeSlider.setMajorTickSpacing(20);
            volumeSlider.setPaintTicks(false);
            volumeSlider.setPaintLabels(false);
            volumeSlider.setLabelTable(volumeSlider.createStandardLabels(10));
            
            // Set action listeners
            playButton.addActionListener(new PlayListener());
            pauseButton.addActionListener(new PauseListener());
            stopButton.addActionListener(new StopListener());
            previousButton.addActionListener(new PreviousListener());
            nextButton.addActionListener(new NextListener());
            volumeSlider.addChangeListener(new VolumeSliderListener());

            // Add buttons to controlPanel
            controlPanel.add(previousButton);
            controlPanel.add(playButton);
            controlPanel.add(pauseButton);
            controlPanel.add(stopButton);
            controlPanel.add(nextButton);
            controlPanel.add(volumeSlider);
            controlPanel.add(getProgressBar());
            
            

            controlPanel.setMaximumSize(new Dimension(1080, 40));
        return controlPanel;
    }

    private JMenu getFileMenu() {
        JMenu menu = new JMenu("Menu");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem addItem = new JMenuItem("Add Music");
        JMenuItem deleteItem = new JMenuItem("Delete Music");
        JMenuItem createPlaylistItem = new JMenuItem("Create Playlist");
        JMenuItem exitItem = new JMenuItem("Exit");

        addItem.addActionListener(new AddSongListener());
        deleteItem.addActionListener(new DeleteSongListener());
        openItem.addActionListener(new OpenItemListener());
        exitItem.addActionListener(new ExitItemListener());
        createPlaylistItem.addActionListener(new CreatePlaylistListener());

        menu.add(openItem);
        menu.add(addItem);
        menu.add(deleteItem);
        if(windowType == Player_Frame.Library) {
            menu.add(createPlaylistItem);
        }
        menu.add(exitItem);
        return menu;
    }
    private JMenu getControlsMenu() {
        JMenu menu = new JMenu("Controls");
        JMenuItem playItem = new JMenuItem("Play");
        JMenuItem nextItem = new JMenuItem("Next");
        JMenuItem previousItem = new JMenuItem("Previous");
        RecentMenu = new JMenu("Play Recent");
        JMenuItem goToCurrentItem = new JMenuItem("Go To Current Song");
        JMenuItem increaseVolumeItem = new JMenuItem("Increase Volume");
        JMenuItem decreaseVolumeItem = new JMenuItem("Decrease Volume");
        shuffle = new JCheckBoxMenuItem("Shuffle");
        repeat = new JCheckBoxMenuItem("Repeat");

        // Build play recent menu
        updateRecentSongsMenu();

        // Set accelerators
        playItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        nextItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK));
        previousItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK));
        goToCurrentItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
        increaseVolumeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
        decreaseVolumeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));

        // Add action listeners
        playItem.addActionListener(new PlayListener());
        nextItem.addActionListener(new NextListener());
        previousItem.addActionListener(new PreviousListener());
        goToCurrentItem.addActionListener(new GoToCurrentListener());
        increaseVolumeItem.addActionListener(new VolumeIncreaseListener());
        decreaseVolumeItem.addActionListener(new VolumeDecreaseListener());
        shuffle.addActionListener(new ShuffleListener());
        repeat.addActionListener(new RepeatListener());

        menu.add(playItem);
        menu.add(nextItem);
        menu.add(previousItem);
        menu.add(RecentMenu);
        menu.add(goToCurrentItem);
        menu.addSeparator();
        menu.add(increaseVolumeItem);
        menu.add(decreaseVolumeItem);
        menu.addSeparator();
        menu.add(shuffle);
        menu.add(repeat);
        return menu;
    }
    

    private void createMusicTablePopupMenu() {
        musicTablePopupMenu = new JPopupMenu();
        JMenuItem addMenuItem = new JMenuItem("Add Music");
        JMenuItem deleteMenuItem = new JMenuItem("Delete Music");
        addSongToPlaylistSubMenu = new JMenu("Add Music to Playlist");

        addMenuItem.addActionListener(new AddSongListener());
        deleteMenuItem.addActionListener(new DeleteSongListener());

        musicTablePopupMenu.add(addMenuItem);
        musicTablePopupMenu.add(deleteMenuItem);
        if(windowType == Player_Frame.Library) {
            musicTablePopupMenu.add(addSongToPlaylistSubMenu);
        }
        updateAddPlaylistSubMenu();
    }
    private void createPlaylistPopupMenu() {
        playlistPopupMenu = new JPopupMenu();
        JMenuItem deletePlaylist = new JMenuItem("Delete Playlist");
        JMenuItem newWindow = new JMenuItem("Open Playlist in New Window");
        deletePlaylist.addActionListener(new DeletePlaylistListener());
        newWindow.addActionListener(new NewWindowListener());
        playlistPopupMenu.add(deletePlaylist);
        playlistPopupMenu.add(newWindow);
    }
    
    private void createShowColumnsPopupMenu() {
        showColumnsPopupMenu = new JPopupMenu();
        final JCheckBoxMenuItem showArtist = new JCheckBoxMenuItem("Artist");
        final JCheckBoxMenuItem showAlbum = new JCheckBoxMenuItem("Album");
        final JCheckBoxMenuItem showYear = new JCheckBoxMenuItem("Year");
        final JCheckBoxMenuItem showGenre = new JCheckBoxMenuItem("Genre");
        final JCheckBoxMenuItem showComment = new JCheckBoxMenuItem("Comment");

        // Set checkbox to reflect columns' visibility state (default is unselected)
        if (Run.db.getColumnVisible("Artist")) {showArtist.setSelected(true);}
        if (Run.db.getColumnVisible("Album")) {showAlbum.setSelected(true);}
        if (Run.db.getColumnVisible("Year")) {showYear.setSelected(true);}
        if (Run.db.getColumnVisible("Genre")) {showGenre.setSelected(true);}
        if (Run.db.getColumnVisible("Comment")) {showComment.setSelected(true);}

        showColumnsPopupMenu.add(showArtist);
        showColumnsPopupMenu.add(showAlbum);
        showColumnsPopupMenu.add(showYear);
        showColumnsPopupMenu.add(showGenre);
        showColumnsPopupMenu.add(showComment);

        ActionListener artistCheckboxListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (showArtist.isSelected()) { musicTable.show("Artist"); }
                else {musicTable.hide("Artist");}
            }
        };

        ActionListener albumCheckboxListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (showAlbum.isSelected()) { musicTable.show("Album"); }
                else { musicTable.hide("Album"); }
            }
        };

        ActionListener yearCheckboxListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (showYear.isSelected()) { musicTable.show("Year"); }
                else { musicTable.hide("Year"); }
            }
        };

        ActionListener genreCheckboxListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (showGenre.isSelected()) { musicTable.show("Genre"); }
                else { musicTable.hide("Genre"); }
            }
        };

        ActionListener commentCheckboxListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (showComment.isSelected()) { musicTable.show("Comment"); }
                else { musicTable.hide("Comment"); }
            }
        };

        showArtist.addActionListener(artistCheckboxListener);
        showAlbum.addActionListener(albumCheckboxListener);
        showYear.addActionListener(yearCheckboxListener);
        showGenre.addActionListener(genreCheckboxListener);
        showComment.addActionListener(commentCheckboxListener);
    }
    
    private void updateAddPlaylistSubMenu() {
        // Get updated list of playlist names from database
        ArrayList<String> playlistNames = Run.db.getPlaylistNames();

        // Remove all items from music table popup menu - playlist sub menu
        addSongToPlaylistSubMenu.removeAll();

        // Repopulate music table popup menu - playlist sub menu
        for (String playlistName : playlistNames) {
            JMenuItem item = new JMenuItem(playlistName);
            item.addActionListener(new AddSongToPlaylistListener(playlistName));
            addSongToPlaylistSubMenu.add(item);
        }
        // Add terminating "Create Playlist" item to "Add Song to Playlist Menu"
        JMenuItem item = new JMenuItem("Create Playlist");
        item.addActionListener(new CreatePlaylistListener());
        addSongToPlaylistSubMenu.add(item);

        // Repaint the popup menu
        musicTablePopupMenu.repaint();

    }
    
    private boolean drag()
    {
        
        return true;
    }
    
  
    private class DoubleClickListener extends MouseAdapter{
        public void mousePressed(MouseEvent me) {
            if (me.getClickCount() == 2) {
                int row = musicTable.getTable().getSelectedRow();
                playSong(row);
            }
        }
    }
    private class MusicTablePopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                musicTablePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    private class ColumnDisplayPopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        public void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showColumnsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    
    
    private class AddToTableDropTarget extends DropTarget {
        @Override
        public synchronized void drop(DropTargetDropEvent dtde) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            Transferable t = dtde.getTransferable();
            java.util.List fileList;
            try {
                fileList = (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);
                
                for(Object file : fileList) {
                    Musics song = new Musics(file.toString());

                    if(musicTable.getType() == Music_Table.LIBRARY) {
                        // If this is the main application window & the music table == library
                        // Check if the music already exists
                        int id = Run.db.Insert_Music(song);
                        if (id != -1) {
                            // if music successfully added to database
                            // add music to music library table
                            
                            musicTable.addSongToTable(id, song);
                            
                            
                musicTableScrollPane.repaint();
                        }
                    } else if(musicTable.getType() == Music_Table.PLAYLIST) {
                        
                        // If the music table == playlist
                        // Try to add music to db (if already in db it won't be added)
                        int id = Run.db.Insert_Music(song);

                        // add music to the playlist
                        Run.db.addSongToPlaylist(song.getFilePath(), selectedPlaylist);

                        // Get song id if the song was already in library
                        // (ie. id in previous assignment == -1)
                        if(id == -1) {
                            id = Run.db.Get_Music_ID(song.getFilePath());
                        }

                        // add music to playlist table
                        musicTable.addSongToTable(id, song);

                        // Notify main application window table of change
                        // if this is a separate playlist window
                        if(windowType == Player_Frame.PLAYLIST) {
                            
                            Run.mainWindow.musicTable.updateTableModel("Library");
                            
                        }
                        
                    }
                }
            } catch (Exception e) {
                System.out.println("file did not moved");
            }
        }
    }
    private class NewWindowListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            
            // Switch back to Library table in MAIN app window
            musicTable.updateTableModel("Library");
            

            // Set highlighted node in playlist panel to "Library"
            playlistPanelTree.setSelectionRow(0);

            // Open new window for selected playlist
            Player_Frame newWindow = new Player_Frame(selectedPlaylist);
            
            newWindow.display();
            
        }
    }
    private class VolumeSliderListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                // slider value in range [0, 100]
                // converted to double value in range [0.0, 1.0]
                // which is the range required by BasicPlayer setGain() method
                double volume = source.getValue() / 100.00;
                player.adjustVolume(volume);
            }
        }
    }
    
    private class PlaylistPanelMouseListener extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            
            JTree tree = (JTree) e.getSource();
            String selection = tree.getSelectionPath().getLastPathComponent().toString();
            
            if(selection.equals(null)) {
                selection = "Library";  // set selection to "Library" if null, as default
            }

            if(SwingUtilities.isRightMouseButton(e) && !selection.equals("Library")
                    && !selection.equals("Playlists")) {
                // An individual playlist was right clicked,

                // Set selected playlist
                selectedPlaylist = selection;
                // show popup menu
                maybeShowPopup(e);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            
            
            JTree tree = (JTree) e.getSource();
            String selection = tree.getSelectionPath().getLastPathComponent().toString();
            if(selection.equals(null)) {
                selection = "Library";  // set selection to "Library" if null, as default
            }

            // highlight selected row
            int row = tree.getClosestRowForLocation(e.getX(), e.getY());
            tree.setSelectionRow(row);

            if(SwingUtilities.isLeftMouseButton(e)) {
                // left click pressed
                // If selection is not Playlist
                // ie. "Library" or a playlist name was selected
                if(!selection.equals("Playlists")) {
                    // Update the table model
                    
                    musicTable.updateTableModel(selection);

                    // If library selected: ensure add music to playlist sub menu gets added back
                    // Else if individual playlist selected: remove the add music to playlist sub menu, set selectedPlaylist
                    if(selection.equals("Library")) {
                        Run.updateAllWindows();
                        
                        musicTablePopupMenu.add(addSongToPlaylistSubMenu);
                        
                        
                    } else {
                        selectedPlaylist = selection;
                        musicTablePopupMenu.remove(addSongToPlaylistSubMenu);
                    }

                    // Repaint the music table scroll pane
                    musicTableScrollPane.repaint();
                    
                    
                }
            }
        }

        public void maybeShowPopup(MouseEvent e) {
            playlistPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    private class CreatePlaylistListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            
            // Display message box with a textfield for user to type into
            JFrame createPLFrame = new JFrame("Create New Playlist");
            String playlistName = (String) JOptionPane.showInputDialog(createPLFrame, "New playlist's name: ",
                    "Create New Playlist", JOptionPane.PLAIN_MESSAGE);
            Run.db.addPlaylist(playlistName);

            // Refresh GUI popupmenu playlist sub menu
            updateAddPlaylistSubMenu();
            updatePlaylistNode();
            ((DefaultTreeModel)playlistPanelTree.getModel()).reload();

            // Expand playlist node (index 1)
            playlistPanelTree.expandRow(1);

            // Select playlist node just created
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) playlistPanelTree.getModel().getRoot();
            DefaultMutableTreeNode playlistsNode = (DefaultMutableTreeNode) playlistPanelTree.getModel().getChild(rootNode, 1);
            TreePath path = new TreePath(rootNode);
            path = path.pathByAddingChild(playlistsNode);
            int numPlaylists = playlistPanelTree.getModel().getChildCount(playlistsNode);
            for(int i = 0; i < numPlaylists; i++) {
                String node = playlistPanelTree.getModel().getChild(playlistsNode, i).toString();
                if(node.equals(playlistName)) {
                    path = path.pathByAddingChild(playlistsNode.getChildAt(i));
                    playlistPanelTree.addSelectionPath(path);
                }
            }

            // Update selected playlist
            selectedPlaylist = playlistName;

            // Update table model
            musicTable.updateTableModel(playlistName);
        }
    }
    private class DeletePlaylistListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JFrame confirmDeleteFrame = new JFrame("Delete Playlist");
            int answer = JOptionPane.showConfirmDialog(confirmDeleteFrame,
                    "Confirm delete this playlist");
            if (answer == JOptionPane.YES_OPTION) {
                // Delete selected playlist from library
                Run.db.deletePlaylist(selectedPlaylist);

                // Refresh playlist panel tree
                updatePlaylistNode();

                // may need to add tree redraw or something
                ((DefaultTreeModel)playlistPanelTree.getModel()).reload(playlistNode);

                // Refresh GUI popupmenu playlist sub menu
                
                updateAddPlaylistSubMenu();

            }
        }
    }
    private class AddSongToPlaylistListener implements ActionListener {
        private String playlist;

        public AddSongToPlaylistListener(String playlistName) {
            playlist = playlistName;
        }

        public void actionPerformed(ActionEvent event) {
            int[] selectedRows = musicTable.getTable().getSelectedRows();

            for(int i = 0; i < selectedRows.length; i++) {
                String selectedSong = musicTable.getTable().getValueAt(selectedRows[i], Music_Table.Column_Filepath).toString();
                Run.db.addSongToPlaylist(selectedSong, playlist);
            }
            // Expand playlist node (index 1)
            playlistPanelTree.expandRow(1);
            
        }
    }
    private class PreviousListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int previousSongRow = player.Get_Music_Row() - 1;

            // Only skip to previous if the loaded song is not the first item in the table
            // and the loaded song is not set to -1 flag (which indicates that the
            // loaded song was opened via the File->Open menu)
            if(previousSongRow >= 0 ) {
                if(playerState == BasicPlayerEvent.PLAYING ||
                   playerState == BasicPlayerEvent.RESUMED ||
                   playerState == BasicPlayerEvent.PAUSED    ) {
                    // if player is currently playing/resumed
                    // stop current song
                    // decrement player.currentSongIndex
                    // play previous song
                    player.stop();

                    playSong(previousSongRow);
                }
            }
        }
    }
    private class PlayListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int selectedRow = musicTable.getTable().getSelectedRow();
            // boolean indicator, true if selected song is currently loaded to player
            boolean selectedSongIsLoaded =
                     selectedRow == player.Get_Music_Row();

            if (selectedSongIsLoaded && playerState == BasicPlayerEvent.PAUSED) {
                // if selected song is current song on player
                // and player.state == paused
                //playSong(selectedRow);
                player.resume();
            } 
            
//            else if(playerState == BasicPlayerEvent.PAUSED)
//            
//            {
//                player.resume();
//            }
            else {
                if(selectedRow == -1) {
                    // if no row selected:
                    // set loaded song to first song in table
                    selectedRow = 0;
                }
                if (playerState == BasicPlayerEvent.PLAYING ||
                    playerState == BasicPlayerEvent.RESUMED ||
                    playerState == BasicPlayerEvent.PAUSED) {
                    // stop player
                    player.stop();
                }
                playSong(selectedRow);
            }
        }
    }
    private class PauseListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(playerState == BasicPlayerEvent.PLAYING || playerState == BasicPlayerEvent.RESUMED ) {
                player.pause();
            }
            else if (playerState == BasicPlayerEvent.PAUSED)
                player.resume();
        }
    }
    private class StopListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            player.stop();
            clearProgressBar();
        }
    }
    private class NextListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int nextSongIndex = player.Get_Music_Row() + 1;
            int lastItemInTable = musicTable.getTable().getRowCount() - 1;

            if(nextSongIndex > lastItemInTable ) {
                nextSongIndex = 0;
                playSong(nextSongIndex);
            }
            else
                playSong(nextSongIndex);
        }
    }
    private class OpenItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 Files", "mp3");
                chooser.setFileFilter(filter);  //filters for mp3 files only
                //file chooser menu
                if (chooser.showDialog(windowFrame, "Open Song") == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    Musics selectedSong = new Musics(selectedFile.getPath());
                    if (playerState == BasicPlayerEvent.PLAYING ||
                        playerState == BasicPlayerEvent.RESUMED ||
                        playerState == BasicPlayerEvent.PAUSED) {
                        // player.state == playing/resumed/paused
                        // stop player
                        player.stop();
                    }

                    player.play(selectedSong.getFilePath());
                }
            }
    }
    
    private class AddSongListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 Files", "mp3");
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(filter);  //filters for mp3 files only
            //file chooser menu
            if (chooser.showDialog(windowFrame, "Add Song") == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                Musics selectedSong = new Musics(selectedFile.getPath());
                int id = Run.db.Insert_Music(selectedSong);  // -1 if failure

                if(musicTable.getType() == Music_Table.LIBRARY) {
                    // If the music table == library
                    // Only add music to library table if it is not already present in db
                    if (id != -1) {
                        // if song successfully added to database
                        // add music to music library table
                        musicTable.addSongToTable(id, selectedSong);
                    }
                } else if(musicTable.getType() == Music_Table.PLAYLIST){
                    // If the music table == playlist
                    // add music to the playlist
                    Run.db.addSongToPlaylist(selectedSong.getFilePath(), selectedPlaylist);
                    // add music to playlist table
                    musicTable.addSongToTable(id, selectedSong);
                }
                
                Run.updateAllWindows();
            }
        }
    }
    private class DeleteSongListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            
            int[] selectedRows = musicTable.getTable().getSelectedRows();

            DefaultTableModel model = (DefaultTableModel) musicTable.getTable().getModel();
            for(int i = 0; i < selectedRows.length; i++) {
                int selectedSongRow = selectedRows[i];
                int selectedSongId = Integer.parseInt(musicTable.getTable().getValueAt(selectedSongRow, Music_Table.Column_ID).toString());

                // Stop player if song being deleted is the current song on the player
                // and clear progress bar
                if(selectedSongRow == player.Get_Music_Row()) {
                    player.stop();
                    clearProgressBar();
                }

                model.removeRow(selectedSongRow);

                if(musicTable.getType() == Music_Table.LIBRARY) {
                    // Delete song from database by using filepath as an identifier
                    Run.db.Delete_Music(selectedSongId);
                } else if(musicTable.getType() == Music_Table.PLAYLIST){
                    Run.db.deleteSongFromPlaylist(selectedSongId, selectedPlaylist);
                }
            }

            // Update all windows in the event that the song(s) being removed from the table
            // is also present in another window/table
            
            Run.updateAllWindows();
            updateRecentSongsMenu();
        }
    }
    private class ExitItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if(windowType == Player_Frame.Library) {
                System.exit(0);
            } else if(windowType == Player_Frame.PLAYLIST) {
                windowFrame.dispatchEvent(new WindowEvent(windowFrame, WindowEvent.WINDOW_CLOSING));
            }
        }
    }
    public void opened(Object stream, Map properties)
    {
        duration = Integer.parseInt(properties.get("duration").toString()) / 1000;
    }
    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties)
    {
        timeElapsed = microseconds/1000;
        timeRemaining = duration - timeElapsed;
        updateProgress();

        // if time remaining less than 1 second, set songCompleted flag to true
        if(timeRemaining < 1000) {
            songCompleted = true;
            clearProgressBar();
            
        }
    }
    public void stateUpdated(BasicPlayerEvent event)
    {
        // Notification of BasicPlayer states (opened, playing, end of media, ...)
        if(event.getCode() != BasicPlayerEvent.GAIN) {
            // if state is not GAIN (due to volume change)
            // update state code
            playerState = event.getCode();
        } else {
            // do nothing, retain previous state
        }

        if(playerState == BasicPlayerEvent.STOPPED && songCompleted) {
            NextListener nextListener = new NextListener();
            nextListener.actionPerformed(null);
            songCompleted = false;
        }
    }
    public int getState() {
        return playerState;
    }
    public void setController(BasicController controller)
    {
        System.out.println("setController : " + controller);
    }
    private class PlaylistWindowListener implements WindowListener {
         @Override
         public void windowActivated(WindowEvent e) {
         }

         @Override
         public void windowClosed(WindowEvent e) {
             // Remove window from list of application windows
             Run.windows.remove(this);
         }

         @Override
         public void windowClosing(WindowEvent e) {
         }

         @Override
         public void windowDeactivated(WindowEvent e) {
         }

         @Override
         public void windowDeiconified(WindowEvent e) {
         }

         @Override
         public void windowOpened(WindowEvent e) {
         }

         @Override
         public void windowIconified(WindowEvent e) {
         }
    }
    private class GoToCurrentListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!(musicTable.getTable().getParent() instanceof JViewport)) {
                return;
            }

            // Get cell rectangle for loaded song row
            Rectangle rect = musicTable.getTable().getCellRect(player.Get_Music_Row(), 0, true);

            musicTable.getTable().scrollRectToVisible(rect);
        }
    }
    
    private void playSong(int row) {
        if (shuffle.isSelected()) {
            Random r = new Random();
            row = r.nextInt(musicTable.getTable().getRowCount());
        } else if(repeat.isSelected()) {
            row = player.Get_Music_Row();
        }
        clearProgressBar();
        int songId = Integer.parseInt(musicTable.getTable().getValueAt(row, Music_Table.Column_ID).toString());
        player.Set_Music_Row(row);
        musicTable.getTable().setRowSelectionInterval(row, row);
        player.play(Run.db.Get_Music_Filepath(songId));
        Run.db.addRecentSong(songId);
        updateRecentSongsMenu();
        GoToCurrentListener goToCurrentSong = new GoToCurrentListener();
        goToCurrentSong.actionPerformed(null);
    }
    
    public Music_Table getMusicTable ()
    {
        return musicTable;
    }
    public String getSelectedPlaylist()
    {
        return selectedPlaylist;
    }
    private void updateRecentSongsMenu() {
        // Clear menu entries
        RecentMenu.removeAll();

        // Repopulate with 10 most recent songs from db
        int[] recentSongs = Run.db.getRecentSongs();
        for(int i = 0; i < recentSongs.length; i++) {
            JMenuItem recentSongItem = new JMenuItem(Run.db.Get_Title(recentSongs[i]));
            RecentMenu.add(recentSongItem);
        }
    }
    
    private class VolumeIncreaseListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            double volume = player.getVolume();
            if(volume < .95) {
                // Increase by .05 (which is 5% of total gain value)
                player.adjustVolume(volume + .05);
            } else {
                // if volume > .95, increase volume to 100
                // (otherwise [volume + .05] in if statement would have increased gain above 1.0)
                player.adjustVolume(1.0);
            }
            // Adjust volume slider
            volumeSlider.setValue(player.getSliderVolume());
        }
    }

    /**
     * Volume decrement listener:
     * <p>
     * Decreases volume by 5%
     *
     */
    private class VolumeDecreaseListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            double volume = player.getVolume();
            if(volume > 0.05) {
                // Decrease by .05 (which is 5% of total gain value)
                player.adjustVolume(volume - .05);
            } else {
                // if volume < .05, reduce volume to zero
                // (otherwise [volume - .05] in if statement would have reduced gain below zero)
                player.adjustVolume(0);
            }
            // Adjust volume slider
            volumeSlider.setValue(player.getSliderVolume());
        }
    }
    
     private class ShuffleListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(shuffle.isSelected()){
                repeat.setEnabled(false);
                if(playerState != BasicPlayerEvent.PLAYING) {
                    // player was not playing - thus, play random song
                    // (value passed to playSong does not matter as a random song will be selected)
                    playSong(0);
                }
            } else {
                repeat.setEnabled(true);
            }
        }
    }

    /**
     * Repeat listener
     *
     * If repeat has been checked: shuffle is disabled
     * If repeat has been unchecked: shuffle is enabled
     *
     */
    private class RepeatListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(repeat.isSelected()){
                shuffle.setEnabled(false);
            } else {
                shuffle.setEnabled(true);
            }
        }
    }

    private JPanel getProgressBar()
    {
        JPanel progressBarPanel = new JPanel();
        L_timer = new JLabel ("00:00:00");
        R_timer = new JLabel ("00:00:00");
        progressbar = new JProgressBar(0, 100);
        progressbar.setValue(0);
        progressbar.setStringPainted(true);
        progressbar.setString("");

        //timer is used for the timer. Similar to threads, it runs in the background: actions happen based on time
        progressBarPanel.add(L_timer);
        progressBarPanel.add(progressbar);
        progressBarPanel.add(R_timer);

        return progressBarPanel;
    }
    
    private void updateProgress()
    {
        //used to format timer number
        NumberFormat format;
        format = NumberFormat.getNumberInstance();
        format.setMinimumIntegerDigits(2); //pad with 0 if necessary

        //right timer
        int rightHours = (int) (timeRemaining / (60000*60));
        int rightMinutes = (int) (timeRemaining / 60000);
        int rightSeconds = (int) ((timeRemaining % 60000) / 1000);

        //left timer
        int leftHours = (int) (timeElapsed / (60000*60));
        int leftMinutes = (int) (timeElapsed / 60000);
        int leftSeconds = (int) ((timeElapsed %60000)/1000);
        //set the left and right timer labels with new time

        //set the labels with format
        R_timer.setText(format.format(rightHours) +":" + format.format(rightMinutes) + ":" + format.format(rightSeconds));
        L_timer.setText(format.format(leftHours) +":" + format.format(leftMinutes) + ":" + format.format(leftSeconds));

        if(duration > 0) {
            // set progress bar value based on percentage of timeElapsed
            progressbar.setValue((int) ( ( (float) timeElapsed / duration) * 100));
        }
    }
    
    private void clearProgressBar()
    {
        timeRemaining = 0;
        timeElapsed = 0;
        updateProgress();
        R_timer.setText("00:00:00");
        L_timer.setText("00:00:00");
        progressbar.setValue(0);
    }
}