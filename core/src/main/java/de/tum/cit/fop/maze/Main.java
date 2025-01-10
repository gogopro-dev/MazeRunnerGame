package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {

    public SpriteBatch batch;
    //public Player player;

    public Main() {

        //player = new Player();
    }

    @Override
    public void create() {

        batch = new SpriteBatch();
        setScreen(new FirstScreen(this));

    }
    @Override
    public void render() {

        super.render();
    }

    @Override
    public void resize (int width, int height) {
    }

    @Override
    public void dispose () {

        batch.dispose();
    }
}
