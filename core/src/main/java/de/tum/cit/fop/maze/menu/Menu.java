package de.tum.cit.fop.maze.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.*;
import de.tum.cit.fop.maze.essentials.FadeOverlay;
import de.tum.cit.fop.maze.level.LevelScreen;

/**
 * Class for the main menu.
 */
public class Menu implements Screen {
    public int SCREEN_WIDTH = 1024;
    public int SCREEN_HEIGHT = 764;
    private MenuState menuState = MenuState.MAIN_MENU;
    private final MainMenuUI mainMenuUI;
    private final SettingsUI settingsUI;
    private final CreditsUI creditsUI;
    private static Menu instance = null;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Array<TextureAtlas.AtlasRegion> backgroundRegions;
    private final SpriteBatch batch;
    private float stateTime = 0f;
    private int currentFrameIndex = 0;
    private static final float FRAME_DURATION = 0.1f;
    private final FadeOverlay fadeOverlay;

    /**
     * Returns the singleton instance of the menu.
     * @return The singleton instance of the menu.
     */
    public static synchronized Menu getInstance(){
        if (instance == null){
            instance = new Menu();
        }
        return instance;
    }

    /**
     * Constructor for the main menu.</br>
     * Creates the stage and sets the input processor.</br>
     */
    private Menu() {
        /// Camera, Viewport nad SpriteBatch setup
        camera = new OrthographicCamera();
        viewport = new FitViewport(1024, 764, camera);
        batch = new SpriteBatch();

        fadeOverlay = new FadeOverlay();

        mainMenuUI = new MainMenuUI(viewport, batch);
        settingsUI = new SettingsUI(viewport, batch);
        creditsUI = new CreditsUI(viewport, batch);

        /// Load background atlas and get all regions
        TextureAtlas backgroundAtlas = new TextureAtlas(Gdx.files.internal("background/background.atlas"));
        backgroundRegions = backgroundAtlas.getRegions();
    }

    /**
     * Toggles the menu state.
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

        switch (menuState){
            case PLAY:
                fadeOverlay.startFadeIn();
                break;
            case MAIN_MENU:
                if (fadeIn) {
                    fadeOverlay.startFadeIn();
                }
                mainMenuUI.show();
                break;
            case CREATE_NEW_GAME:
                break;
            case CREDITS:
                creditsUI.show();
                break;
            case SETTINGS:
                settingsUI.show();
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
            case PLAY:
                /// Check if fade in/out is finished
                if (fadeOverlay.isFinishedIn() || fadeOverlay.isFinishedOut()) {
                    /// Switch to rendering level screen
                    LevelScreen.getInstance().render(delta);
                } else {
                    /// Continue rendering menu while fading
                    mainMenuUI.render(delta);
                }

                /// if fading out is not finished, render overlay
                if (!fadeOverlay.isFinishedOut()) {
                    fadeOverlay.render(delta);
                }

                break;
            case MAIN_MENU:
                if (fadeOverlay.isFinishedIn() || fadeOverlay.isFinishedOut()) {
                    /// Switch to rendering menu screen
                    mainMenuUI.show();
                    mainMenuUI.render(delta);
                } else {
                    /// Continue rendering level screen fading
                    LevelScreen.getInstance().render(delta);
                }

                /// if fading out is not finished, render overlay
                if (!fadeOverlay.isFinishedOut()) {
                    fadeOverlay.render(delta);
                }
                break;
            case CREATE_NEW_GAME:
                break;
            case CREDITS:
                creditsUI.render(delta);
                break;
            case SETTINGS:
                settingsUI.render(delta);
                break;
        }
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
        camera.update();
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
        mainMenuUI.dispose();
        settingsUI.dispose();
        creditsUI.dispose();
        fadeOverlay.dispose();
        batch.dispose();
    }

    @Override
    public void show() {
        switch (menuState){
            case MAIN_MENU:
                mainMenuUI.show();
                break;
            case CREATE_NEW_GAME:
                break;
            case CREDITS:
                creditsUI.show();
                break;
            case SETTINGS:
                settingsUI.show();
                break;
        }
    }
}
