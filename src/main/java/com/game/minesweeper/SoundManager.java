package com.game.minesweeper;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URISyntaxException;
import java.net.URL;


public class SoundManager {
    private boolean isSoundEnabled = true;
    public void setSoundEnabled(boolean enabled) {
        this.isSoundEnabled = enabled;
    }
    private static final String CLICK_SOUND = "/sounds/click.wav";
    private static final String FLAG_SOUND = "/sounds/flag.wav";
    private static final String VICTORY_SOUND = "/sounds/victory.mp3";
    private static final String DEFEAT_SOUND = "/sounds/defeat.mp3";
    public void playClickSound() {
        playSound(CLICK_SOUND);
    }
    public void playFlagSound() {
        playSound(FLAG_SOUND);
    }
    public void playVictorySound() {
        playSound(VICTORY_SOUND);
    }
    public void playDefeatSound() {
        playSound(DEFEAT_SOUND);
    }
    private void playSound(String soundPath) {
        if (!isSoundEnabled) {
            return;
        }
        try {
            URL resource = getClass().getResource(soundPath);
            if (resource == null) {
                System.err.println("Sound file not found: " + soundPath);
                return;
            }
            Media sound = new Media(resource.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (URISyntaxException e) {
            System.err.println("Failed to load sound: " + soundPath);
            e.printStackTrace();
        }
    }

}
