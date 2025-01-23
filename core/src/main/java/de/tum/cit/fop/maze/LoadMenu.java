package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import de.tum.cit.fop.maze.level.LevelScreen;
import de.tum.cit.fop.maze.menu.Menu;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LoadMenu extends Game {
    private static LoadMenu instance;
    private LoadMenu() {
        instance = this;
    }
    @Override
    public void create() {
        /// Initialize the AssetManager singleton and load all assets

        Assets.getInstance().loadAllBlocking();
        /// Initialize the Menu singleton and all its dependencies
        Menu menu = Menu.getInstance();
        /// Initialize the LevelScreen singleton
        setScreen(new LevelScreen());
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
