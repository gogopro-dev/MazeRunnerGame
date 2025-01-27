package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import de.tum.cit.fop.maze.essentials.SettingsConfiguration;
import de.tum.cit.fop.maze.level.GameOverScreen;
import de.tum.cit.fop.maze.menu.Menu;

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
    private LoadMenu() {
        instance = this;
    }


    @Override
    public void create() {
        /// Initialize the AssetManager singleton and load all assets
        Assets.getInstance().loadAllBlocking();
        //Assets.getInstance().tileTextureHelper.getTexture()
        /// Initialize the Menu singleton and all its dependencies
        initConfigurations();
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
    private void initConfigurations(){
        if (Gdx.files.local("saves/settings.json").exists()) {
            Assets.getInstance().gson.fromJson(
              Gdx.files.local("saves/settings.json").reader(), SettingsConfiguration.class
            );
        }
        if (SettingsConfiguration.getInstance().isFullScreen()){
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            Menu.getInstance().SCREEN_HEIGHT = Gdx.graphics.getHeight();
            Menu.getInstance().SCREEN_WIDTH = Gdx.graphics.getWidth();
        } else {
            String[] resolution = SettingsConfiguration.getInstance().getResolution().split("x");
            Gdx.graphics.setWindowedMode(Integer.parseInt(resolution[0]), Integer.parseInt(resolution[1]));
            Menu.getInstance().SCREEN_HEIGHT = Integer.parseInt(resolution[1]);
            Menu.getInstance().SCREEN_WIDTH = Integer.parseInt(resolution[0]);
        }
        Menu.getInstance().resize(Menu.getInstance().SCREEN_WIDTH, Menu.getInstance().SCREEN_HEIGHT);
        Menu.getInstance().updateChildPositions();
        GameOverScreen.getInstance().updateViewport();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public static LoadMenu getInstance() {
        if (instance == null) {
            instance = new LoadMenu();
        }
        return instance;
    }
}
