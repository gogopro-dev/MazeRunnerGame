package de.tum.cit.fop.maze.essentials;

import static de.tum.cit.fop.maze.essentials.Globals.CURRENT_SCREEN_HEIGHT_WINDOWED;
import static de.tum.cit.fop.maze.essentials.Globals.CURRENT_SCREEN_WIDTH_WINDOWED;

/**
 * Singleton class that holds the settings configuration of the game.
 * The settings configuration includes:
 * <ul>
 *     <li>Music volume</li>
 *     <li>SFX volume</li>
 *     <li>Resolution</li>
 *     <li>Fullscreen mode</li>
 *     <li>Vsync</li>
 * </ul>
 * The settings configuration can be accessed from any class in the game.
 */
public class SettingsConfiguration {
    private float musicVolume = 0.1f;
    private float sfxVolume = 0.1f;
    private String resolution = CURRENT_SCREEN_WIDTH_WINDOWED + "x" + CURRENT_SCREEN_HEIGHT_WINDOWED;
    private boolean fullScreen = true;
    private boolean vsync = true;
    private static SettingsConfiguration instance;

    /**
     * @return The singleton instance of the settings configuration.
     */
    public static SettingsConfiguration getInstance(){
        if (instance == null) {
            return new SettingsConfiguration();
        }
        return instance;
    }

    /**
     * Constructor for the settings configuration.
     * Initializes the singleton instance.
     */
    private SettingsConfiguration() {
        instance = this;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = sfxVolume;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public boolean isVsync() {
        return vsync;
    }

    public void setVsync(boolean vSync) {
        this.vsync = vSync;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

}
