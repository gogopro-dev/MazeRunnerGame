package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import de.tum.cit.fop.maze.essentials.PostProcessable;
import de.tum.cit.fop.maze.level.LevelScreen;
import de.tum.cit.fop.maze.menu.Menu;
import de.tum.cit.fop.maze.menu.MenuState;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.HashMap;
import java.util.HashSet;

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
        //Assets.getInstance().tileTextureHelper.getTexture()
        /// Initialize the Menu singleton and all its dependencies
        Menu menu = Menu.getInstance();
        menu.toggleMenuState(MenuState.GAME_SCREEN);
        /// Initialize the LevelScreen singleton

        LevelScreen screen = Assets.getInstance().gson.fromJson(
            Gdx.files.local("level.json").reader(), LevelScreen.class
        );
        screen.init();
        setScreen(screen);

        //setScreen(new LevelScreen(true));

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
