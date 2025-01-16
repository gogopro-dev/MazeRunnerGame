package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import de.tum.cit.fop.maze.level.LevelScreen;
import de.tum.cit.fop.maze.menu.Menu;

import java.awt.event.WindowStateListener;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LoadMenu extends Game {
    private static LoadMenu instance;

    @Override
    public void create() {
        Menu menu = Menu.getInstance();
        /// Initialize the LevelScreen singleton
        new LevelScreen();
        setScreen(menu);
    }

    public static LoadMenu getInstance() {
        if (instance == null) {
            instance = new LoadMenu();
        }
        return instance;
    }
}
