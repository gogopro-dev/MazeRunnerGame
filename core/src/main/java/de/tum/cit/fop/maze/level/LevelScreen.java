package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.Entity.*;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.essentials.DebugRenderer;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static de.tum.cit.fop.maze.Globals.MPP;
import static de.tum.cit.fop.maze.Globals.PPM;

public class LevelScreen implements Screen {

    private static LevelScreen instance = null;
    public final float w, h;
    EnemyManager manager;
    public final Viewport viewport;
    public TileMap map;
    public final OrthographicCamera camera;
    private final TiledMapRenderer tiledMapRenderer;
    public final SpriteBatch batch;
    public final Player player;
    public final HUDv2 hud;
    /// Box2D world
    public final World world;
    public final ReentrantLock worldLock = new ReentrantLock();
    Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    private float accumulator = 0;

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
        worldLock.lock();
        doPhysicsStep(delta);
        worldLock.unlock();
        DebugRenderer.getInstance().begin();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();


        //TODO: render player
        camera.update();
        debugRenderer.render(world, camera.combined);
        batch.setProjectionMatrix(camera.combined);
        DebugRenderer.getInstance().setProjectionMatrix(camera.combined);
        batch.begin();

        manager.renderEnemies(delta);
        player.render(delta);
        batch.end();
        hud.render(delta);
        DebugRenderer.getInstance().end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {

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
        manager.dispose();
    }

    public LevelScreen() {
        if (instance != null) {
            throw new IllegalStateException("LevelScreen already exists");
        }


        instance = this;
        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new EntityContactListener());

        w = Gdx.graphics.getWidth() / PPM;
        h = Gdx.graphics.getHeight() / PPM;
        map = new TileMap(15, 15, 2);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, w / 2, h / 2);

        viewport = new FitViewport(w, h, camera);
        viewport.apply();

        this.batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(map.getMap(), MPP * Globals.TILEMAP_SCALE);
        player = new Player(batch);
        player.spawn(map.widthMeters / 2, map.heightMeters / 2, world);

        /// Set camera at the center of the players position in Box2D world
        camera.position.set(player.getSpriteX(),player.getSpriteY(), 0);
        camera.zoom = 1.25f;
        camera.update();

        hud = new HUDv2(player);


        /*enemy = new Enemy(EnemyType.SKELETON, camera, batchEnemy);
        enemy.spawn(map.widthMeters / 2 - 10, map.heightMeters / 2 + 15.5f, world);
        List<AbsolutePoint> path = map.pathfinder.aStar(
            enemy, new AbsolutePoint(player)
        );
        // draw a dot at each point in the path
        if (path != null) {
            for (AbsolutePoint point : path) {
                BodyDef bodyDef = new BodyDef();
                bodyDef.type = BodyDef.BodyType.StaticBody;
                bodyDef.position.set(point.x(), point.y());
                Body body = world.createBody(bodyDef);
                CircleShape shape = new CircleShape();
                shape.setRadius(0.1f);
                body.createFixture(shape, 0.0f);
                shape.dispose();
            }
        }*/
        manager = new EnemyManager(this);
        manager.createEnemy(new Enemy(EnemyType.ZOMBIE, batch), 8.3f, 10);
        manager.createEnemy(new Enemy(EnemyType.SKELETON, batch), 8.3f, 12);
        manager.createEnemy(new Enemy(EnemyType.ZOMBIE, batch), 8.3f, 11.2f);
        manager.createEnemy(new Enemy(EnemyType.ZOMBIE, batch), 8.3f, 11.4f);
        manager.createEnemy(new Enemy(EnemyType.ZOMBIE, batch), 8.3f, 11.6f);
        manager.createEnemy(new Enemy(EnemyType.ZOMBIE, batch), 8.3f, 11.7f);
        manager.createEnemy(new Enemy(EnemyType.ZOMBIE, batch), 8.1f, 11.5f);

//        enemy2 = new Enemy(map.widthMeters, map.heightMeters, EnemyType.ZOMBIE, camera);

    }

    public static LevelScreen getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LevelScreen not yet created");
        }
        return instance;
    }
}
