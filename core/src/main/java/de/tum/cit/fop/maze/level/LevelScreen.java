package de.tum.cit.fop.maze.level;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.*;
import de.tum.cit.fop.maze.entities.*;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.entities.tile.*;
import de.tum.cit.fop.maze.essentials.DebugRenderer;
import de.tum.cit.fop.maze.essentials.Direction;
import de.tum.cit.fop.maze.menu.Menu;
import de.tum.cit.fop.maze.menu.MenuState;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import static de.tum.cit.fop.maze.Globals.*;

public class LevelScreen implements Screen {
    private static LevelScreen instance = null;


    public Player player;
    public TileMap map;
    public EnemyManager enemyManager;
    public TileEntityManager tileEntityManager;
    public final long seed = 2;

    public transient float w, h;
    public transient FillViewport viewport;
    public transient final OrthographicCamera camera;
    private transient TiledMapRenderer tiledMapRenderer;
    public transient final SpriteBatch batch;
    public transient HUD hud;
    public transient final RayHandler rayHandler;

    transient Random random = new Random(2);
    public transient final EntityPathfinder pathfinder = new EntityPathfinder();
    private transient boolean gameOver;

    /// Box2D world
    public transient final World world;
    public transient ReentrantLock worldLock = new ReentrantLock();
    private transient final Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    private transient float accumulator = 0;
    private transient final PauseScreen pauseScreen;
    private transient final Stage stage;
    private transient boolean needsRestoring = false;
    private transient boolean endGame = false;
    private transient int levelIndex;

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
        /// Clear the screen
        ScreenUtils.clear(0, 0, 0, 1, true);

        /// Render the pause screen if paused
        /// In case menu state is not game screen then
        /// the screenshot will be rendered to place as a placeholder
        /// while fading to the main Menu
        if (pauseScreen.isPaused() || Menu.getInstance().getMenuState() != MenuState.GAME_SCREEN) {
            /// Render the last frame before pausing
            pauseScreen.drawLastFrame(batch);

            /// Update and render pause screen
            pauseScreen.update();
            pauseScreen.render(delta);
            return;
        }

        if (gameOver){
            pauseScreen.drawLastFrame(batch);
            GameOverScreen.getInstance().render(delta);
            return;
        }

        /// Render the game if not paused
        renderWorld(delta);

        /// Update and render pause screen
        pauseScreen.update();
        pauseScreen.render(delta);

        /// Check if the game should end
        if (endGame){
            pauseScreen.takeScreenshot();
            GameOverScreen.getInstance().drawInventory(hud.spriteInventory, hud.textInventory);
            GameOverScreen.getInstance().setTimePlayed(hud.getTime());
            GameOverScreen.getInstance().setScore(hud.getScore());
            gameOver = true;
        }
    }

    public void saveGame(){
        try {
            SaveManager.saveGame(levelIndex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Render the game world
     * @param delta The time since the last render in seconds
     */
    public void renderWorld(float delta){
        /// Render the game if not paused
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
        player.renderEntity(delta);
        batch.end();

        rayHandler.setCombinedMatrix(camera);
        rayHandler.updateAndRender();
        hud.render(delta);
        if (Globals.DEBUG) debugRenderer.render(world, camera.combined);
        DebugRenderer.getInstance().end();
    }

    @Override
    public void resize(int width, int height) {
        pauseScreen.resize(width, height);

        viewport.update(width, height);
        viewport.setScreenSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
        tileEntityManager.dispose();
        pauseScreen.dispose();
        hud.dispose();
        rayHandler.dispose();
        world.dispose();
        debugRenderer.dispose();
        instance = null;
    }

    private LevelScreen() {
        this.pauseScreen = new PauseScreen();
        this.batch = new SpriteBatch();
        if (instance != null) {
            throw new IllegalStateException("LevelScreen already exists");
        }
        needsRestoring = true;
        instance = this;
        /// Init GameOverScreen
        GameOverScreen.getInstance();
        /// Init world
        RayHandler.useDiffuseLight(false);
        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new TileEntityContactListener());
        RayHandler.useDiffuseLight(true);
        rayHandler = new RayHandler(world);
        rayHandler.setShadows(true);

        Color lightColor = new Color(0.082f, 0.067f, 0.122f, 0.5f);
        if (Globals.FULLBRIGHT) lightColor = Color.WHITE;
        rayHandler.setAmbientLight(lightColor);
        rayHandler.setBlurNum(33);

        w = DEFAULT_SCREEN_WIDTH_WINDOWED / PPM;
        h = DEFAULT_SCREEN_HEIGHT_WINDOWED / PPM;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, w / 2, h / 2);

        viewport = new FillViewport(w,h, camera);
        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        stage = new Stage(viewport, batch);
        tileEntityManager = new TileEntityManager();
        enemyManager = new EnemyManager();
        player = new Player();
    }

    public LevelScreen(long seed) {
        this();
        this.needsRestoring = false;
        this.random = new Random(seed);
        generate();
        spawnDebug();
        init();
    }

    private void generate() {
        map = new TileMap(15, 15, random);
    }


    public void init() {
        tiledMapRenderer = new OrthogonalTiledMapRenderer(map.getMap(), MPP * Globals.TILEMAP_SCALE);
        if (needsRestoring) {
            this.tileEntityManager.restore();
            this.enemyManager.restore();
            this.map.restore();
            player.restore();
            player.spawn(
                player.getSavedPosition().x(),
                player.getSavedPosition().y()
            );

        } else {
            player.spawn(
                map.widthMeters / 2, map.heightMeters / 2
            );
        }
        hud = new HUD(player);
        /// Set camera at the center of the players position in Box2D world
        if (player.getPosition() != null) {
            camera.position.set(player.getPosition().x(), player.getPosition().y(), 0);
        } else {
            camera.position.set(map.widthMeters / 2, map.heightMeters / 2, 0);
        }
        batch.setProjectionMatrix(camera.combined);
        camera.zoom = Globals.DEFAULT_CAMERA_ZOOM;
        camera.update();

        updateViewport();
        PauseScreen.getInstance().updateViewport();
        hud.resize();
    }

    public void spawnDebug() {
        Collectable collectable1 = new Collectable(Collectable.CollectableType.HEART);
        Collectable collectable2 = new Collectable(Collectable.CollectableType.GOLD_COIN);
        Collectable collectable3 = new Collectable(Collectable.CollectableType.DAMAGE_COIN);
        Collectable collectable4 = new Collectable(Collectable.CollectableType.DEFENSE_COIN);
        Collectable collectable5 = new Collectable(Collectable.CollectableType.SPEED_BOOTS);
        Collectable collectable6 = new Collectable(Collectable.CollectableType.VAMPIRE_AMULET);
        Collectable collectable7 = new Collectable(Collectable.CollectableType.RESURRECTION_AMULET);
        Collectable collectable8 = new Collectable(Collectable.CollectableType.HEART);
        Collectable collectable9 = new Collectable(Collectable.CollectableType.KEY);

        tileEntityManager.createTileEntity(collectable1,
            map.widthMeters / 2 + 10, map.heightMeters / 2
        );
        tileEntityManager.createTileEntity(collectable2,
            map.widthMeters / 2 + 10, map.heightMeters / 2 - 2
        );
        tileEntityManager.createTileEntity(collectable3,
            map.widthMeters / 2 + 10, map.heightMeters / 2 - 4
        );
        tileEntityManager.createTileEntity(collectable4,
            map.widthMeters / 2 + 10, map.heightMeters / 2 - 6
        );
        tileEntityManager.createTileEntity(collectable5,
            map.widthMeters / 2 + 12, map.heightMeters / 2
        );
        tileEntityManager.createTileEntity(collectable6,
            map.widthMeters / 2 + 12, map.heightMeters / 2 - 2
        );
        tileEntityManager.createTileEntity(collectable7,
            map.widthMeters / 2 + 12, map.heightMeters / 2 - 4
        );
        tileEntityManager.createTileEntity(collectable8,
            map.widthMeters / 2 + 12, map.heightMeters / 2 - 6
        );
        tileEntityManager.createTileEntity(collectable9,
            map.widthMeters / 2 + 14, map.heightMeters / 2 - 8
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

        tileEntityManager.createTileEntity(
            new LootContainer(LootContainer.LootContainerType.CRATE),
            map.widthMeters / 2 + 4, map.heightMeters / 2 - 1
        );

        tileEntityManager.createTileEntity(
            new LootContainer(LootContainer.LootContainerType.BARREL),
            map.widthMeters / 2 + 4 + CELL_SIZE_METERS, map.heightMeters / 2 - 1
        );
        tileEntityManager.createTileEntity(
            new LootContainer(LootContainer.LootContainerType.VASE),
            map.widthMeters / 2 + 4 + CELL_SIZE_METERS * 2, map.heightMeters / 2 - 1
        );

        enemyManager.createEnemy(new Enemy(EnemyType.ZOMBIE), 8.3f, 10);
        enemyManager.createEnemy(new Enemy(EnemyType.SKELETON), 8.3f, 12);
    }

    /**
     * Update the viewport after resizing the window
     */
    public void updateViewport() {
        float h = Gdx.graphics.getHeight() / PPM;
        float w = Gdx.graphics.getWidth() / PPM;
        float ratio = viewport.getWorldWidth() / w;


        float playerRelativeX = player.getPosition().x() - camera.position.x;
        float playerRelativeY = player.getPosition().y() - camera.position.y;

        camera.viewportWidth = w / 2;
        camera.viewportHeight = h / 2;
        Globals.DEFAULT_CAMERA_ZOOM *= ratio;
        camera.zoom = Globals.DEFAULT_CAMERA_ZOOM;

        /// Set camera at the position of the player it was before resizing
        camera.position.set(player.getPosition().x() - playerRelativeX, player.getPosition().y() - playerRelativeY, 0);

        viewport.setScreenSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.setWorldSize(w, h);

        hud.resize();
        viewport.apply();
    }
    public void endGame(boolean hasWon) {
        this.endGame = true;
        GameOverScreen.getInstance().setHasWon(hasWon);
    }

    /**
     * Get the instance of the LevelScreen
     * @return The instance of the LevelScreen
     */
    public static LevelScreen getInstance() {
        return instance;
    }

    public Random getRandom() {
        return random;
    }

    public Stage getStage() {
        return stage;
    }

    public void setLevelIndex(int levelIndex) {
        this.levelIndex = levelIndex;
    }

    public int getLevelIndex() {
        return levelIndex;
    }
}
