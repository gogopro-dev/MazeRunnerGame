package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import de.tum.cit.fop.maze.level.LevelScreen;
import de.tum.cit.fop.maze.menu.Menu;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LoadMenu extends Game {
    private static LoadMenu instance;
    public AssetManager assetManager;
    private LoadMenu() {
        instance = this;
    }

    @Override
    public void create() {
        /// Initialize the AssetManager singleton and load all assets
        assetManager = new AssetManager();
        loadAssets();
        assetManager.finishLoading();
        /// Initialize the Menu singleton and all its dependencies
        Menu menu = Menu.getInstance();
        /// Initialize the LevelScreen singleton
        new LevelScreen();
        setScreen(menu);
    }

    private void loadAssets(){
        assetManager.load("assets/menu/menu.atlas", TextureAtlas.class);
        assetManager.load("assets/menu/menu_icons.atlas", TextureAtlas.class);
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        super.dispose();
    }

    public static LoadMenu getInstance() {
        if (instance == null) {
            instance = new LoadMenu();
        }
        return instance;
    }
}
