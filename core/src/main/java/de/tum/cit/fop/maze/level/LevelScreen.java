package de.tum.cit.fop.maze.level;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.entities.*;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.entities.tile.TileEntityManager;
import de.tum.cit.fop.maze.entities.tile.Torch;
import de.tum.cit.fop.maze.essentials.DebugRenderer;
import de.tum.cit.fop.maze.entities.tile.TileEntityContactListener;
import de.tum.cit.fop.maze.essentials.Direction;
import de.tum.cit.fop.maze.entities.tile.Collectable;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import static de.tum.cit.fop.maze.Globals.MPP;
import static de.tum.cit.fop.maze.Globals.PPM;

public class LevelScreen implements Screen {
    private static LevelScreen instance = null;

    public final float w, h;
    private final EnemyManager enemyManager;
    public final TileEntityManager tileEntityManager;
    public final Viewport viewport;
    public TileMap map;
    public final OrthographicCamera camera;
    private final TiledMapRenderer tiledMapRenderer;
    public final SpriteBatch batch;
    public final Player player;
    public final HUDv2 hud;
    public final RayHandler rayHandler;

    /// Box2D world
    public final World world;
    public final ReentrantLock worldLock = new ReentrantLock();
    Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    private float accumulator = 0;
    private final PauseScreen pauseScreen;
    private final Stage stage;

    private void doPhysicsStep(float deltaTime) {
        // Fixed time step
        // Limit frame time to avoid spiral of death (i.e. endless loop of physics updates) on slow devices
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        while (accumulator >= Globals.BOX2D_TIME_STEP) {
            world.step(Globals.BOX2D_TIME_STEP, Globals.BOX2D_VELOCITY_ITERATIONS, Globals.BOX2D_POSITION_ITERATIONS);
            accumulator -= Globals.BOX2D_TIME_STEP;
        }
    }


    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1, true);

        /// Render the game if not paused
        if (!pauseScreen.isPaused()) {
            worldLock.lock();
            doPhysicsStep(delta);
            worldLock.unlock();

            DebugRenderer.getInstance().begin();
            tiledMapRenderer.setView(camera);
            tiledMapRenderer.render();
            camera.update();

            batch.setProjectionMatrix(camera.combined);
            DebugRenderer.getInstance().setProjectionMatrix(camera.combined);

            batch.begin();
            tileEntityManager.render(delta);
            enemyManager.render(delta);
            player.render(delta);
            batch.end();

            rayHandler.setCombinedMatrix(camera);
            rayHandler.updateAndRender();
            hud.render(delta);
            if (Globals.DEBUG) debugRenderer.render(world, camera.combined);
            DebugRenderer.getInstance().end();
        } else {
            /// Render the last frame before pausing
            Texture pauseTexture = new Texture(pauseScreen.getLastFrame());
            batch.begin();
            batch.draw(pauseTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, pauseTexture.getWidth(), pauseTexture.getHeight(), false, true);
            batch.end();
            pauseTexture.dispose();
        }
        /// Update and render pause screen
        pauseScreen.update();
        pauseScreen.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        pauseScreen.resize(width, height);
    }

    @Override
    public void show() {
        hud.show();
        Gdx.input.setInputProcessor(stage);
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
        enemyManager.dispose();
        pauseScreen.dispose();
    }

    public LevelScreen() {
        if (instance != null) {
            throw new IllegalStateException("LevelScreen already exists");
        }
        instance = this;
        pauseScreen = PauseScreen.getInstance();

        /// Init world
        RayHandler.useDiffuseLight(false);
        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new TileEntityContactListener());
        RayHandler.useDiffuseLight(true);
        rayHandler = new RayHandler(world);
        rayHandler.setShadows(true);

        Color lightColor = new Color(0.082f, 0.067f, 0.122f, 0.5f);
        rayHandler.setAmbientLight(lightColor);
        rayHandler.setBlurNum(33);




        w = Gdx.graphics.getWidth() / PPM;
        h = Gdx.graphics.getHeight() / PPM;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, w / 2, h / 2);

        viewport = new FitViewport(w, h, camera);
        viewport.apply();

        this.batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);
        stage = new Stage(viewport, batch);
        tileEntityManager = new TileEntityManager();
        enemyManager = new EnemyManager();
        map = new TileMap(15, 15, 2);


        tiledMapRenderer = new OrthogonalTiledMapRenderer(map.getMap(), MPP * Globals.TILEMAP_SCALE);
        player = new Player(batch);
        player.spawn(map.widthMeters / 2 + 8, map.heightMeters / 2, world);

        /// Set camera at the center of the players position in Box2D world
        camera.position.set(player.getPosition().x() + 5, player.getPosition().y(), 0);
        camera.zoom = Globals.DEFAULT_CAMERA_ZOOM;
        camera.update();

        hud = new HUDv2(player);
        Collectable collectable = new Collectable("key", null);
        tileEntityManager.createTileEntity(collectable,
         map.widthMeters / 2 + 10, map.heightMeters / 2
        );
        tileEntityManager.createTileEntity(
            new Torch(Direction.UP),
            map.widthMeters / 2, map.heightMeters / 2
        );
        tileEntityManager.createTileEntity(
            new Torch(Direction.LEFT),
            map.widthMeters / 2 + 1, map.heightMeters / 2
        );
        tileEntityManager.createTileEntity(
            new Torch(Direction.RIGHT),
            map.widthMeters / 2 + 2, map.heightMeters / 2
        );
        tileEntityManager.createTileEntity(
            new Torch(Direction.DOWN),
            map.widthMeters / 2 + 3, map.heightMeters / 2
        );

        /*enemyManager.createEnemy(new Enemy(EnemyType.ZOMBIE, batch), 8.3f, 10);
        enemyManager.createEnemy(new Enemy(EnemyType.SKELETON, batch), 8.3f, 12);
        enemyManager.createEnemy(new Enemy(EnemyType.ZOMBIE, batch), 8.3f, 11.2f);
        enemyManager.createEnemy(new Enemy(EnemyType.ZOMBIE, batch), 8.3f, 11.4f);
        enemyManager.createEnemy(new Enemy(EnemyType.ZOMBIE, batch), 8.3f, 11.6f);
        enemyManager.createEnemy(new Enemy(EnemyType.ZOMBIE, batch), 8.3f, 11.7f);*/
        enemyManager.createEnemy(new Enemy(EnemyType.ZOMBIE, batch), 8.1f, 11.5f);

        //enemyManager.createEnemy(; = new Enemy(map.widthMeters, map.heightMeters, EnemyType.ZOMBIE, camera);

    }

    public static LevelScreen getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LevelScreen not yet created");
        }
        return instance;
    }

    public Stage getStage() {
        return stage;
    }
}
