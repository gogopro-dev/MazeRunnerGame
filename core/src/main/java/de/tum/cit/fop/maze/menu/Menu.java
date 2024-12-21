package de.tum.cit.fop.maze.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Class for the main menu.
 */
public class Menu implements Screen {
    public int SCREEN_WIDTH = 1024;
    public int SCREEN_HEIGHT = 764;
    private MenuState menuState = MenuState.MAIN_MENU;
    private final MainMenuUI mainMenuUI;
    private final SettingsUI settingsUI;
    private static Menu instance = null;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Array<TextureAtlas.AtlasRegion> backgroundRegions;
    private final SpriteBatch batch;
    private float stateTime = 0f;
    private int currentFrameIndex = 0;
    private static final float FRAME_DURATION = 0.1f;
    /**
     * Constructor for the main menu.</br>
     * Creates the stage and sets the input processor.</br>
     */
    private Menu() {
        // Camera, Viewport nad SpriteBatch setup
        camera = new OrthographicCamera();
        viewport = new FitViewport(1024, 764, camera);
        batch = new SpriteBatch();

        mainMenuUI = new MainMenuUI(viewport, batch);
        settingsUI = new SettingsUI(viewport, batch);

        /// Load background atlas and get all regions
        TextureAtlas backgroundAtlas = new TextureAtlas(Gdx.files.internal("background/background.atlas"));
        backgroundRegions = backgroundAtlas.getRegions();
    }

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
     * Toggles the menu state.
     * @param state The state to toggle to.
     */
    public void toggleMenuState(MenuState state){
        menuState = state;

        Gdx.input.setInputProcessor(null);

        switch (menuState){
            case MAIN_MENU:
                mainMenuUI.show();
                break;
            case PLAY:
                break;
            case CREATE_NEW_GAME:
                break;
            case CREDITS:
                break;
            case SETTINGS:
                settingsUI.show();
                break;
        }
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update animation state time
        stateTime += delta;

        // Determine current frame
        if (stateTime >= FRAME_DURATION) {
            currentFrameIndex = (currentFrameIndex + 1) % backgroundRegions.size;
            stateTime = 0f;
        }
        // Draw current background frame
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
            case MAIN_MENU:
                mainMenuUI.render(delta);
                break;
            case CREATE_NEW_GAME:
                break;
            case CREDITS:
                break;
            case SETTINGS:
                settingsUI.render(delta);
                break;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(SCREEN_WIDTH, SCREEN_HEIGHT);
        viewport.getCamera().update();
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
                break;
            case SETTINGS:
                settingsUI.show();
                break;
        }
    }
}
