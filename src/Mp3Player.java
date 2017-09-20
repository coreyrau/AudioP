import java.io.File;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author LONGCHENG, COREY
 */
public class Mp3Player {
    
private int loadedSongRow ;   // Table row of loaded Music
    private BasicPlayer player;
    private BasicController controller;
    private double volume;

    /**
     * MusicPlayer default constructor
     *
     */
    public Mp3Player() {
        player = new BasicPlayer();
        controller = (BasicController) player;
        volume = 0;
    }
    
    public int getSliderVolume() { return (int)(this.volume * 100);}

    public double getVolume() { return volume; };

    public BasicPlayer getPlayer() {
        return player;
    }

    /**
     * Plays the selected Music
     *
     * @param filePath the file path of the song to play
     * @return true if song plays successfully
     */
    public boolean play(String filePath) {
        try {
            controller.open(new File(filePath));
            // play
            controller.play();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Resumes a previously paused Music
     *
     * @return true if Music is resumed successfully
     */
    public boolean resume() {
        try {
            controller.resume();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Pauses the currently playing Music
     *
     * @return true if the Music is paused successfully
     */
    public boolean pause() {
        try {
            controller.pause();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Stops the currently playing Music
     *
     * @return true if Music stopped successfully
     */
    public boolean stop() {
        try {
            controller.stop();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @return the currently loaded Music row
     */
    public int Get_Music_Row() {
        return loadedSongRow;
    }

    /**
     * @param row the Music id being loaded
     */
    public void Set_Music_Row(int row) {
        this.loadedSongRow = row;
    }
    public void adjustVolume(double volume) {
        try {
            controller.setGain(volume);
            this.volume = volume;
        } catch (BasicPlayerException e) {
            e.printStackTrace();
        }
    }
}
