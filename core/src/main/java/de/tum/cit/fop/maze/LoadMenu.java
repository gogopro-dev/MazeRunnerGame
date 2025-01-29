package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import de.tum.cit.fop.maze.essentials.SettingsConfiguration;
import de.tum.cit.fop.maze.level.GameOverScreen;
import de.tum.cit.fop.maze.level.SaveManager;
import de.tum.cit.fop.maze.menu.Menu;
import games.rednblack.miniaudio.MiniAudio;
import de.tum.cit.fop.maze.menu.MenuState;

import java.io.IOException;

import static de.tum.cit.fop.maze.Globals.*;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 * This class is the entry point of the game and is responsible for loading all assets and initializing the menu.
 * @see Game
 * @see Assets
 * @see Menu
 * @see SettingsConfiguration
 * @see GameOverScreen
 */
public class LoadMenu extends Game {
    private static LoadMenu instance;
    private final MiniAudio miniAudio = new MiniAudio();

    /**
     * Private constructor to prevent instantiation from outside the class
     * @see LoadMenu#getInstance()
     */
    private LoadMenu() {
        instance = this;
    }


    @Override
    public void create() {
        //Assets.getInstance().tileTextureHelper.getTexture()
        /// Initialize the Menu singleton and all its dependencies
        /// Initialize the AssetManager singleton and load all assets
        Assets.getInstance().loadAllBlocking();
        LoadMenu.getInstance().initConfigurations();
        Menu.getInstance().toggleMenuState(MenuState.MAIN_MENU, true);
        setScreen(Menu.getInstance());
    }

    /**
     * Initializes the configurations of the game:
     * <ul>
     *  <li>If the settings file exists, it will be loaded.</li>
     *  <li>If the game is in fullscreen mode, the screen will be set to fullscreen.</li>
     *  <li>If the game is in windowed mode, the screen will be set to the resolution specified in the settings file.</li>
     *  <li>The menu and game over screen will be resized and updated.</li>
     *  <li>If the settings file does not exist, the default settings will be used.</li>
     * </ul>
     * @see SettingsConfiguration
     */
    public void initConfigurations() {
        /// Sets the instance of the SettingsConfiguration singleton to the values in the settings.json file
        if (Gdx.files.local("saves/settings.json").exists()) {
            Assets.getInstance().gson.fromJson(
              Gdx.files.local("saves/settings.json").reader(), SettingsConfiguration.class
            );
        }

        Menu.getInstance();

        String[] resolution = SettingsConfiguration.getInstance().getResolution().split("x");
        CURRENT_SCREEN_WIDTH_WINDOWED = Integer.parseInt(resolution[0]);
        CURRENT_SCREEN_HEIGHT_WINDOWED = Integer.parseInt(resolution[1]);

        calculateListOfResolutions();

        if (SettingsConfiguration.getInstance().isFullScreen()){
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            Menu.getInstance().SCREEN_HEIGHT = Gdx.graphics.getHeight();
            Menu.getInstance().SCREEN_WIDTH = Gdx.graphics.getWidth();
        } else {
            Gdx.graphics.setWindowedMode(CURRENT_SCREEN_WIDTH_WINDOWED, CURRENT_SCREEN_HEIGHT_WINDOWED);
            Menu.getInstance().SCREEN_HEIGHT = CURRENT_SCREEN_HEIGHT_WINDOWED;
            Menu.getInstance().SCREEN_WIDTH = CURRENT_SCREEN_WIDTH_WINDOWED;
        }

        Menu.getInstance().resize(Menu.getInstance().SCREEN_WIDTH, Menu.getInstance().SCREEN_HEIGHT);
        Menu.getInstance().updateChildPositions();
        GameOverScreen.getInstance().updateViewport();

        Assets.getInstance().music.setVolume(SettingsConfiguration.getInstance().getMusicVolume());
        Assets.getInstance().sfx.setVolume(SettingsConfiguration.getInstance().getSfxVolume());
    }

    /**
     * Removes resolutions that are higher than the current display resolution
     */
    private void calculateListOfResolutions() {
        int displayWidth = Gdx.graphics.getDisplayMode().width;
        int displayHeight = Gdx.graphics.getDisplayMode().height;
        for (int i = WINDOWED_RESOLUTIONS.size()-1; i > 1; i--) {
            int width = Integer.parseInt(WINDOWED_RESOLUTIONS.get(i).split("x")[0]);
            int height = Integer.parseInt(WINDOWED_RESOLUTIONS.get(i).split("x")[1]);
            if (width >= displayWidth || height >= displayHeight) {
                WINDOWED_RESOLUTIONS.remove(i);
            }
        }
        /// if the current resolution is bigger than the biggest resolution in the list,
        /// change the resolution to the biggest one in the list
        if (CURRENT_SCREEN_WIDTH_WINDOWED >= displayWidth || CURRENT_SCREEN_HEIGHT_WINDOWED >= displayHeight) {
            CURRENT_SCREEN_WIDTH_WINDOWED = Integer.parseInt(WINDOWED_RESOLUTIONS.get(WINDOWED_RESOLUTIONS.size()-1).split("x")[0]);
            CURRENT_SCREEN_HEIGHT_WINDOWED = Integer.parseInt(WINDOWED_RESOLUTIONS.get(WINDOWED_RESOLUTIONS.size()-1).split("x")[1]);

            Menu.getInstance().SCREEN_HEIGHT = CURRENT_SCREEN_HEIGHT_WINDOWED;
            Menu.getInstance().SCREEN_WIDTH = CURRENT_SCREEN_WIDTH_WINDOWED;

            SettingsConfiguration.getInstance().setResolution(CURRENT_SCREEN_WIDTH_WINDOWED + "x" + CURRENT_SCREEN_HEIGHT_WINDOWED);
            try {
                SaveManager.saveConfigurations();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * @return the instance of the LoadMenu singleton
     */
    public static LoadMenu getInstance() {
        if (instance == null) {
            instance = new LoadMenu();
        }
        return instance;
    }

    @Override
    public void dispose() {
        super.dispose();
        this.miniAudio.dispose();
    }

    public MiniAudio getSoundEngine() {
        return miniAudio;
    }
}
