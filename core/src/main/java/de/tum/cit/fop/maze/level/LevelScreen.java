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
import de.tum.cit.fop.maze.essentials.Assets;
import de.tum.cit.fop.maze.entities.*;
import de.tum.cit.fop.maze.essentials.Globals;
import de.tum.cit.fop.maze.entities.tile.*;
import de.tum.cit.fop.maze.essentials.DebugRenderer;
import de.tum.cit.fop.maze.essentials.Direction;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.hud.HUD;
import de.tum.cit.fop.maze.level.worldgen.MazeGenerator;
import de.tum.cit.fop.maze.menu.Menu;
import de.tum.cit.fop.maze.menu.MenuState;
import de.tum.cit.fop.maze.menu.PlayGameScreen;
import games.rednblack.miniaudio.MASound;
import games.rednblack.miniaudio.MiniAudio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import static de.tum.cit.fop.maze.essentials.Globals.*;

/**
 * The main game screen
 * <p>
 *     <ul>
 *         <li>Manages the game world</li>
 *         <li>Manages the player</li>
 *         <li>Manages the enemies</li>
 *         <li>Manages the tile entities</li>
 *         <li>Manages the HUD</li>
 *         <li>Manages the Box2D world</li>
 *         <li>Manages the rendering of the game world</li>
 *     </ul>
 * </p>
 */
public class LevelScreen implements Screen {
    private static LevelScreen instance = null;

    public Player player;
    public TileMap map;
    public EnemyManager enemyManager;
    public TileEntityManager tileEntityManager;
    public transient float w, h;
    public transient FillViewport viewport;
    public transient final OrthographicCamera camera;
    private transient TiledMapRenderer tiledMapRenderer;
    public transient final SpriteBatch batch;
    public transient HUD hud;
    public transient final RayHandler rayHandler;
    private final LevelData levelData = new LevelData();
    transient Random random = new Random();
    public transient final EntityPathfinder pathfinder = new EntityPathfinder();
    private transient boolean gameOver;

    /// Box2D world
    public transient final World world;
    public transient ReentrantLock worldLock = new ReentrantLock();
    private transient final Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    private transient float accumulator = 0;
    private transient final PauseScreen pauseScreen;
    private transient final Stage stage;
    private transient boolean needsRestoring;
    private transient boolean endGame = false;
    private transient int levelIndex;
    private transient float ratio;
    private transient MASound lossSound;
    public transient final MiniAudio sound;
    private transient ArrayList<MASound> gameplayMusic;
    private transient int currentMusicIndex = 0;
    private transient float musicDelayActive = 30;
    private transient MASound currentMusic = null;
    private transient boolean isMusicPaused = false;
    private transient boolean allMusicStopped = false;


    private void doPhysicsStep(float deltaTime) {
        /// Fixed time step
        /// Limit frame time to avoid spiral of death (i.e. endless loop of physics updates) on slow devices
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        while (accumulator >= Globals.BOX2D_TIME_STEP) {
            world.step(Globals.BOX2D_TIME_STEP, Globals.BOX2D_VELOCITY_ITERATIONS, Globals.BOX2D_POSITION_ITERATIONS);
            accumulator -= Globals.BOX2D_TIME_STEP;
        }

    }


    public void tickAudioEngine(float deltaTime) {
        boolean isAnythingPlaying = false;
        for (MASound sound : gameplayMusic) {
            isAnythingPlaying |= sound.isPlaying();
        }
        if (!isAnythingPlaying) {
            musicDelayActive -= deltaTime;
        }
        if (musicDelayActive <= 0 && !isAnythingPlaying && player.isBeingChased()) {
            musicDelayActive = random.nextFloat() * 20 + 20;
            if (currentMusicIndex >= gameplayMusic.size()) {
                currentMusicIndex = 0;
            }
            currentMusic = gameplayMusic.get(currentMusicIndex);
            currentMusic.setLooping(false);
            currentMusic.setSpatialization(false);
            currentMusic.play();
            currentMusic.fadeIn(0.8f);
            currentMusicIndex++;
        }
    }

    public void fadeOutMusic() {
        if (allMusicStopped) return;
        allMusicStopped = true;
        for (MASound sound : gameplayMusic) {
            if (sound.isPlaying()) {
                sound.fadeOut(0.2f);
                Utils.scheduleFunction(sound::stop, 0.2f);
            }
        }
    }

    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
            isMusicPaused = true;
        }
    }

    public void resumeMusic() {
        if (currentMusic != null && isMusicPaused) {
            currentMusic.play();
        }
        isMusicPaused = false;
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

        if (gameOver) {
            pauseScreen.drawLastFrame(batch);
            GameOverScreen.getInstance().render(delta);
            return;
        }

        /// Render the game if not paused
        tickAudioEngine(delta);
        renderWorld(delta);

        /// Update and render pause screen
        pauseScreen.update();
        pauseScreen.render(delta);

        /// Check if the game should end
        if (endGame) {
            pauseScreen.takeScreenshot();
            GameOverScreen.getInstance().drawInventory(
                hud.getInventory().spriteInventory, hud.getInventory().textInventory
            );
            GameOverScreen.getInstance().setTimePlayed(hud.getFormattedTime());
            GameOverScreen.getInstance().setScore(levelData.getScore());
            gameOver = true;
        }
    }

    /**
     * Saves the game state
     */
    public void saveGame(){
        if (endGame) return;
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
        levelData.addPlaytime(delta);
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
        this.sound = Menu.getInstance().getMiniAudio();
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


    /**
     * Create a new level screen with a given seed
     * @param seed The seed to use for the random number generator
     */
    public LevelScreen(long seed) {
        this();
        this.needsRestoring = false;
        this.random = new Random(seed);
        generate();
        spawnDebug();
        init();
    }

    /**
     * Create a new level screen with a given maze generator (from properties file)
     * @param generator The maze generator to use
     */
    public LevelScreen(MazeGenerator generator) {
        this();
        this.needsRestoring = false;
        generate(generator);
        spawnDebug();
        init();
    }

    private void generate() {
        map = new TileMap(35, 35, random);
    }

    private void generate(MazeGenerator generator) {
        map = new TileMap(generator);
    }


    /**
     * Initialize the level screen
     * <p>
     *     <ul>
     *         <li>Restore the game state if needed</li>
     *         <li>Initialize the tile map renderer</li>
     *         <li>Initialize the player</li>
     *         <li>Initialize the HUD</li>
     *         <li>Set the camera at the center of the player's position in Box2D world</li>
     *     </ul>
     *  </p>
     */
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
                map.playerPosition.x(), map.playerPosition.y()
            );
        }
        this.lossSound = Assets.getInstance().getSound("loss");
        this.lossSound.setSpatialization(false);
        this.lossSound.setVolume(0.7f);

        this.gameplayMusic = new ArrayList<>();
        this.gameplayMusic.addAll(
            List.of(
                Assets.getInstance().getSound("fight0"),
                Assets.getInstance().getSound("fight1")
            )
        );

        hud = new HUD(player);
        /// Set camera at the center of the players position in Box2D world

        camera.position.set(player.getPosition().x(), player.getPosition().y(), 0);
        batch.setProjectionMatrix(camera.combined);
        camera.zoom = CURRENT_CAMERA_ZOOM;
        camera.update();

        updateViewport();
        PauseScreen.getInstance().updateViewport();
        hud.resize();
    }

    /**
     * Spawns debug entities for testing purposes
     * <p>
     *
     *     <ul>
     *         <li>Collectables</li>
     *         <li>Torches</li>
     *         <li>Loot Containers</li>
     *         <li>Enemies</li>
     *         <li>...</li>
     *     </ul>
     * </p>
     */
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
     * Update the viewport and the camera after resizing the window
     */
    public void updateViewport() {
        float h = Gdx.graphics.getHeight() / PPM;
        float w = Gdx.graphics.getWidth() / PPM;

        /// Ratio is calculated based on height
        /// so that after changing resolution to the one
        /// with the different aspect ratio (e.g. 4:3 -> 16:9),
        /// camera's zoom will be adjusted, so that the player
        /// does not see more world vertically.
        /// E.g.: 1920x1440 -> 1920x1080
        /// Player will see the same amount of world vertically,
        /// but the width will be cropped.
        /// (Overall camera zoom will be increased accordingly)
        ratio = DEFAULT_CAMERA_VIEWPORT_HEIGHT_METERS / h;

        float playerRelativeX = player.getPosition().x() - camera.position.x;
        float playerRelativeY = player.getPosition().y() - camera.position.y;

        camera.viewportWidth = w / 2;
        camera.viewportHeight = h / 2;


        float minZoom = Globals.DEFAULT_CAMERA_ZOOM * 0.5f * ratio;
        float maxZoom = Globals.DEFAULT_CAMERA_ZOOM * ratio;
        camera.zoom = (minZoom + maxZoom)/2;

        /// Set camera at the position of the player it was before resizing
        camera.position.set(player.getPosition().x() - playerRelativeX, player.getPosition().y() - playerRelativeY, 0);

        viewport.setScreenSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.setWorldSize(w, h);

        hud.resize();
        viewport.apply();
    }

    /**
     * Sets the {@code endGame} flag to true and sets the {@code hasWon} flag in the GameOverScreen
     * @param hasWon Whether the player has won the game or has died
     */
    public void endGame(boolean hasWon) {
        this.endGame = true;
        if (!hasWon) {
            fadeOutMusic();
            this.lossSound.stop();
            this.lossSound.setLooping(false);
            this.lossSound.play();
        }
        GameOverScreen.getInstance().setHasWon(hasWon);
        GameOverScreen.getInstance().deleteGame();
        PlayGameScreen.getInstance().updateScreen();
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

    public float getRatio() {
        return ratio;
    }

    public LevelData getLevelData() {
        return levelData;
    }
}
