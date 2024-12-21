package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import de.tum.cit.fop.maze.menu.Menu;

import java.util.Arrays;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LoadMenu extends Game {
    @Override
    public void create() {
        Menu menu = Menu.getInstance();
        setScreen(menu);
    }
}
