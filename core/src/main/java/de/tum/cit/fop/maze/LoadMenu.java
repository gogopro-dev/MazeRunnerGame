package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import de.tum.cit.fop.maze.level.LevelScreen;
import de.tum.cit.fop.maze.menu.Menu;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LoadMenu extends Game {
    private static LoadMenu instance;

    @Override
    public void create() {
        /// Initialize the Menu singleton and all its dependencies
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
