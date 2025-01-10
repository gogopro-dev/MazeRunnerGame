package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.Entity.Player;

public class LevelScreen implements Screen {
    Viewport viewport;
    TileMap map;
    OrthographicCamera camera;
    TiledMapRenderer tiledMapRenderer;

    Player player;

    @Override
    public void render(float delta) {
        camera.update();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        //TODO: render player
        player.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {
        camera.position.set((float) map.widthPX / 2, (float) map.heightPX / 2, 0);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        player.dispose();
    }

    public LevelScreen() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        map = new TileMap(15, 20, 2);
        camera = new OrthographicCamera();
        camera.position.set((float) map.widthPX / 2, (float) map.heightPX / 2, 0);
        camera.setToOrtho(false, w, h);

        viewport = new ScreenViewport(camera);
        viewport.apply();
        camera.update();

        tiledMapRenderer = new OrthogonalTiledMapRenderer(map.getMap());
        player = new Player(camera, map.widthPX, map.heightPX);
    }
}
