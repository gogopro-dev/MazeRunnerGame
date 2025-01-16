package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import de.tum.cit.fop.maze.level.LevelScreen;
import de.tum.cit.fop.maze.menu.Menu;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LoadMenu extends Game {
    @Override
    public void create() {
        Menu.getInstance();
        setScreen(new LevelScreen());
    }
}
