package de.tum.cit.fop.maze.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.*;
import de.tum.cit.fop.maze.Assets;
import de.tum.cit.fop.maze.LoadMenu;
import de.tum.cit.fop.maze.essentials.FadeOverlay;
import de.tum.cit.fop.maze.essentials.SettingsConfiguration;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.level.LevelScreen;
import games.rednblack.miniaudio.MASound;
import games.rednblack.miniaudio.MiniAudio;

/**
 * Class for the main menu.</br>
 * This class is a singleton, and it handles the
 * switching between different menu screens.</br>
 * It handles the rendering of the background in the main menu and all the children's screens.</br>
 * When switching between {@link LevelScreen} and {@link MainMenuScreen}, it fades in and out.
 */
public class Menu implements Screen {
    public int SCREEN_WIDTH = 1024;
    public int SCREEN_HEIGHT = 768;
    private MenuState menuState = MenuState.MAIN_MENU;
    private final MainMenuScreen mainMenuScreen;
    private final SettingsScreen settingsScreen;
    private final CreditsScreen creditsScreen;
    private final PlayGameScreen playGameScreen;
    private final ControlsScreen controlsScreen;
    private static Menu instance = null;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Array<TextureAtlas.AtlasRegion> backgroundRegions;
    private final SpriteBatch batch;
    private float stateTime = 0f;
    private int currentFrameIndex = 0;
    private static final float FRAME_DURATION = 1/10f;
    private final FadeOverlay fadeOverlay;
    private final MiniAudio miniAudio;
    private final MASound mainMenuMusic;
    private final MASound creditsMusic;

    /**
     * Returns the singleton instance of the menu.
     * @return The singleton instance of the menu.
     */
    public static synchronized Menu getInstance(){
        if (instance == null){
            return new Menu();
        }
        return instance;
    }

    /**
     * Constructor for the main menu.</br>
     * Creates the stage and sets the input processor.</br>
     * Creates the buttons for the main menu.
     */
    private Menu() {
        instance = this;
        this.miniAudio = LoadMenu.getInstance().getSoundEngine();
        /// Camera, Viewport nad SpriteBatch setup
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(1024, 768, camera);
        batch = new SpriteBatch();


        this.mainMenuMusic = Assets.getInstance().getSound("main_menu");
        this.mainMenuMusic.setLooping(true);
        this.creditsMusic = Assets.getInstance().getSound("credits");
        this.creditsMusic.setLooping(false);
        fadeOverlay = new FadeOverlay();

        /// Initialize all menu screens
        mainMenuScreen = new MainMenuScreen(viewport, batch);
        settingsScreen = new SettingsScreen(viewport, batch);
        creditsScreen = new CreditsScreen(viewport, batch);
        playGameScreen = new PlayGameScreen(viewport, batch);
        controlsScreen = new ControlsScreen(viewport, batch);


        /// Load background atlas and get all regions
        TextureAtlas backgroundAtlas = new TextureAtlas(Gdx.files.local("assets/background/background.atlas"));
        backgroundRegions = backgroundAtlas.getRegions();
    }

    /**
     * @return The current menu state.
     */
    public MenuState getMenuState() {
        return menuState;
    }

    /**
     * Calls the {@link #toggleMenuState(MenuState, boolean)} method with the given state.
     * @param state The state to toggle to.
     */
    public void toggleMenuState(MenuState state){
        toggleMenuState(state, false);
    }

    /**
     * Toggles the menu state.
     * @param state The state to toggle to.
     * @param fadeIn True if the fade in should be started.
     */
    public void toggleMenuState(MenuState state, boolean fadeIn){
        menuState = state;

        Gdx.input.setInputProcessor(null);
        System.out.println("Switching to " + menuState);

        switch (menuState){
            case GAME_SCREEN:
                if (creditsMusic.isPlaying()) {
                    creditsMusic.stop();
                }
                if (mainMenuMusic.isPlaying()) {
                    mainMenuMusic.fadeOut(2000);
                    Utils.scheduleFunction(creditsMusic::stop, 2f);
                }
                fadeOverlay.startFadeIn();
                break;
            case MAIN_MENU:
                if (creditsMusic.isPlaying()) {
                    creditsMusic.fadeOut(200);
                    Utils.scheduleFunction(creditsMusic::stop, 0.2f);
                }
                if (!mainMenuMusic.isPlaying()) {
                    Utils.scheduleFunction(() -> {
                        mainMenuMusic.fadeIn(200);
                        mainMenuMusic.play();
                    }, 0.2f);
                }
                if (fadeIn) {
                    /// If the state changes from game screen to main menu,
                    /// save the game
                    if (LevelScreen.getInstance() != null) {
                        LevelScreen.getInstance().saveGame();
                        fadeOverlay.startFadeIn();
                    } else {
                        /// Fade out on startup
                        fadeOverlay.startFadeOut();
                    }
                }
                mainMenuScreen.show();
                break;
            case PLAY:
                PlayGameScreen.getInstance().updateScreen();
                playGameScreen.show();
                break;
            case CONTROLS:
                controlsScreen.show();
                break;
            case CREDITS:
                if (mainMenuMusic.isPlaying()) {
                    mainMenuMusic.fadeOut(200);
                    Utils.scheduleFunction(mainMenuMusic::stop, 0.2f);
                }
                if (!creditsMusic.isPlaying()) {
                    creditsMusic.fadeIn(200);
                    creditsMusic.play();
                }
                creditsScreen.show();
                break;
            case SETTINGS:
                settingsScreen.show();
                break;
        }
    }

    @Override
    public void render(float delta) {
            /// Clear the screen
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            /// Update animation state time
            stateTime += delta;

            /// Determine current frame
            if (stateTime >= FRAME_DURATION) {
                currentFrameIndex = (currentFrameIndex + 1) % backgroundRegions.size;
                stateTime = 0f;
            }
            /// Draw current background frame
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            batch.draw(
                backgroundRegions.get(currentFrameIndex),
                0, 0,
                viewport.getWorldWidth(),
                viewport.getWorldHeight()
            );
            batch.end();

            switch (menuState){
                case GAME_SCREEN:
                    /// Check if fade in/out is finished
                    if (fadeOverlay.isFinishedIn() || fadeOverlay.isFinishedOut()) {
                    /// Switch to rendering level screen
                        LevelScreen.getInstance().render(delta);
                    } else {
                        /// Continue rendering menu while fading
                        playGameScreen.render(delta);
                        Gdx.input.setInputProcessor(null);
                    }

                    /// if fading out is not finished, render overlay
                    if (!fadeOverlay.isFinishedOut()) {
                        fadeOverlay.render(delta);
                    }
                    break;
                case PLAY:
                    playGameScreen.render(delta);
                    break;
                case MAIN_MENU:
                    if (fadeOverlay.isFinishedIn() || fadeOverlay.isFinishedOut()) {
                        if (menuState != MenuState.GAME_SCREEN && LevelScreen.getInstance() != null) {
                            LevelScreen.getInstance().dispose();
                        }

                        /// Switch to rendering menu screen
                        mainMenuScreen.render(delta);
                        if (!fadeOverlay.isFinishedOut()){
                            Gdx.input.setInputProcessor(null);
                        }
                    } else {
                        /// Continue rendering level screen fading
                        if (LevelScreen.getInstance() != null){
                            LevelScreen.getInstance().render(delta);
                        }
                        Gdx.input.setInputProcessor(null);
                    }

                    /// if fading out is not finished, render overlay
                    if (!fadeOverlay.isFinishedOut()) {
                        fadeOverlay.render(delta);
                    }
                    break;
                case CONTROLS:

                    controlsScreen.render(delta);
                    break;
                case CREDITS:
                    creditsScreen.render(delta);
                    break;
                case SETTINGS:
                    settingsScreen.render(delta);
                    break;
        }
    }

    /**
     * Updates the positions of the children's main containers.
     */
    public void updateChildPositions() {
        mainMenuScreen.updateContainerPosition();
        creditsScreen.updateContainerPosition();
        playGameScreen.updateContainerPosition();
        settingsScreen.updateContainerPosition();
        controlsScreen.updateContainerPosition();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        /// Center the viewport in the window
        viewport.setScreenBounds(
            (width - SCREEN_WIDTH) / 2,
            (height - SCREEN_HEIGHT) / 2,
            SCREEN_WIDTH,
            SCREEN_HEIGHT
        );

        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        MainMenuScreen.getInstance().updateContainerPosition();
        camera.update();
    }

    @Override
    public void dispose() {
        mainMenuScreen.dispose();
        settingsScreen.dispose();
        creditsScreen.dispose();
        controlsScreen.dispose();
        playGameScreen.dispose();
        fadeOverlay.dispose();
        batch.dispose();
        miniAudio.dispose();
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
    public void show() {
    }

    public MiniAudio getMiniAudio() {
        return miniAudio;
    }
}
