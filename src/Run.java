import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Corey Rau and Robbie
 */
public class Run {
    
    static Music_Database db;
    static Player_Frame mainWindow;
    static ArrayList<Player_Frame> windows;

    public static void main(String[] args) {
        // initialize db
        db = new Music_Database();

        // An array list of shiTunes application windows
        windows = new ArrayList<>();

        // The main shiTunes application window
        mainWindow = new Player_Frame();

        // Display main window
        mainWindow.display();

        // Add main application window to list of ShiTunes windows
        windows.add(mainWindow);
        
    }

    /*
    * Updates the table model for all application windows
    *
    */
    public static void updateAllWindows() {
        for(Player_Frame w : windows) {
            
            if (w.getMusicTable().getType() == Music_Table.LIBRARY) {
                w.getMusicTable().updateTableModel("Library");
            } else {
                w.getMusicTable().updateTableModel(w.getSelectedPlaylist());
            }
        }
    }
}
