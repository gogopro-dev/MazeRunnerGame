package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.*;
import de.tum.cit.fop.maze.Assets;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.essentials.AlignableImageTextButton;
import de.tum.cit.fop.maze.menu.Menu;
import de.tum.cit.fop.maze.menu.MenuState;
import de.tum.cit.fop.maze.menu.PlayGameScreen;
import de.tum.cit.fop.maze.menu.SettingsUI;
import java.nio.ByteBuffer;

import static de.tum.cit.fop.maze.Globals.*;

/**
 * Class for the pause screen.</br>
 * Used to pause the game and show the pause menu.
 */
public class PauseScreen {
    private Texture lastFrame;
    private TextureRegion pauseBackgroundRegion;
    private TextureRegion smallButtonPressedRegion;
    private TextureRegion smallButtonReleasedRegion;
    private TextureRegion playIconRegion;
    private TextureRegion settingsIconRegion;
    private TextureRegion exitIconRegion;
    private final Stage stage;
    private final ShapeRenderer shapeRenderer;
    private boolean isPaused;
    private boolean wasEscapePressed; // To handle key press state
    private final Table screenTable;
    private final SettingsUI settingsUI;
    private boolean isSettings = false;
    private static PauseScreen instance = null;
    private static Pixmap pauseTexturePixmap;

    /**
     * @return The singleton instance of the pause screen
     */
    public static PauseScreen getInstance() {
        return instance;
    }

    /**
     * Constructor for the pause screen.</br>
     * Creates the pause menu.
     */
    public PauseScreen() {
        instance = this;

        stage = new Stage(new ExtendViewport(DEFAULT_SCREEN_WIDTH_WINDOWED, DEFAULT_SCREEN_HEIGHT_WINDOWED));

        shapeRenderer = new ShapeRenderer();
        isPaused = false;
        wasEscapePressed = false;
        screenTable = new Table();

        settingsUI = SettingsUI.getInstance();

        loadTextures();

        setupPauseMenu();
    }

    private void loadTextures(){
        TextureAtlas menuAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu.atlas", TextureAtlas.class);
        TextureAtlas menuIconsAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu_icons.atlas", TextureAtlas.class);

        pauseBackgroundRegion = menuAtlas.findRegion("pause_menu");

        smallButtonPressedRegion = menuAtlas.findRegion("small_button_pressed");
        smallButtonReleasedRegion = menuAtlas.findRegion("small_button_released");

        playIconRegion = menuIconsAtlas.findRegion("play");
        settingsIconRegion = menuIconsAtlas.findRegion("settings");
        exitIconRegion = menuIconsAtlas.findRegion("exit");
    }

    /**
     * Sets up the pause menu
     */
    public void setupPauseMenu(){

        /// Load font for text
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/YosterIslandRegular-VqMe.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 27;
        parameter.color = new Color(0xE0E0E0FF);

        /// Create style for buttons
        ImageTextButton.ImageTextButtonStyle textButtonStyle = new ImageTextButton.ImageTextButtonStyle();
        textButtonStyle.font = generator.generateFont(parameter);
        textButtonStyle.up = new TextureRegionDrawable(smallButtonReleasedRegion);
        textButtonStyle.down = new TextureRegionDrawable(smallButtonPressedRegion);
        textButtonStyle.pressedOffsetX = 1;
        textButtonStyle.pressedOffsetY = -1;

        /// Create table for pause menu
        screenTable.setBackground(new TextureRegionDrawable(pauseBackgroundRegion));
        screenTable.setSize(304*1.6f, 224*1.6f); // Adjust size as needed
        screenTable.setPosition(
            Gdx.graphics.getWidth() / 2f - screenTable.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f - screenTable.getHeight() / 2f
        );
        screenTable.center().top();

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        parameter.size = 35;
        parameter.color = new Color(0xE0E0E0FF);
        labelStyle.font = generator.generateFont(parameter);
        Label pauseLabel = new Label("Game Paused", labelStyle);

        /// Add buttons
        Image playImage = new Image(playIconRegion);

        /// Create Resume button
        AlignableImageTextButton resumeButton = new AlignableImageTextButton("Resume", textButtonStyle, playImage, 1.5f);
        resumeButton.setLabelPadding(10f);
        resumeButton.setLabelTopPadding(4f);
        resumeButton.setImagePadding(10f);
        resumeButton.setImageTopPadding(4f);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause();
            }
        });

        /// Create Settings button
        Image settingsImage = new Image(settingsIconRegion);

        AlignableImageTextButton settingsButton = new AlignableImageTextButton("Settings", textButtonStyle, settingsImage, 1.5f);
        settingsButton.setLabelPadding(10f);
        settingsButton.setLabelTopPadding(4f);
        settingsButton.setImagePadding(10f);
        settingsButton.setImageTopPadding(4f);
        settingsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isSettings = true;
            }
        });


        /// Create Exit button
        Image exitImage = new Image(exitIconRegion);

        AlignableImageTextButton exitButton = new AlignableImageTextButton("Exit to menu", textButtonStyle, exitImage, 1.5f);
        exitButton.setLabelPadding(10f);
        exitButton.setLabelTopPadding(4f);
        exitButton.setImagePadding(10f);
        exitButton.setImageTopPadding(4f);
        exitButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause();
                PlayGameScreen.getInstance().updateScreen();
                Menu.getInstance().toggleMenuState(MenuState.MAIN_MENU, true);
            }
        });

        /// Add buttons to table
        screenTable.add(pauseLabel).padTop(6f).padBottom(20f).row();
        screenTable.add(resumeButton).padTop(20f).padBottom(10f).width(224*1.2f).height(48f*1.2f).row();
        screenTable.add(settingsButton).padTop(10f).padBottom(10f).width(224*1.2f).height(48f*1.2f).row();
        screenTable.add(exitButton).padTop(10f).padBottom(10f).width(224*1.2f).height(48f*1.2f).row();

        stage.addActor(screenTable);

    }

    /**
     * Sets the pause state
     * @param settings The new pause state
     */
    public void setSettings(boolean settings) {
        isSettings = settings;
    }

    /**
     * Takes a screenshot of the current frame </br>
     * Used to display the pause menu
     */
    public void takeScreenshot(boolean save) {
        pauseTexturePixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        ByteBuffer pixels = pauseTexturePixmap.getPixels();
        // This loop makes sure the whole screenshot is opaque and looks exactly like what the user is seeing
        int size = Gdx.graphics.getBackBufferWidth() * Gdx.graphics.getBackBufferHeight() * 4;
        for (int i = 3; i < size; i += 4) {
            pixels.put(i, (byte) 255);
        }

        if(lastFrame != null) {
            lastFrame.dispose();
        }

        /// Flip the image vertically
        for (int y = 0; y < pauseTexturePixmap.getHeight() / 2; y++) {
            for (int x = 0; x < pauseTexturePixmap.getWidth(); x++) {
                int topPixel = pauseTexturePixmap.getPixel(x, y);
                int bottomPixel = pauseTexturePixmap.getPixel(x, pauseTexturePixmap.getHeight() - 1 - y);

                pauseTexturePixmap.drawPixel(x, y, bottomPixel);
                pauseTexturePixmap.drawPixel(x, pauseTexturePixmap.getHeight() - 1 - y, topPixel);
            }
        }
        if (save) {
            PixmapIO.writePNG(Gdx.files.local(
                "saves/" + LevelScreen.getInstance().getLevelIndex() + ".png"
            ), pauseTexturePixmap);
        }

        lastFrame = new Texture(pauseTexturePixmap);
    }

    /**
     * Calls {@link #takeScreenshot(boolean save)} with save set to false
     */
    public void takeScreenshot(){
        takeScreenshot(false);
    }


    /**
     * Toggles the pause state
     */
    public void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            /// Take a screenshot of the current frame
            /// To place the pause menu on top of it
            takeScreenshot();
            return;
        }
        isSettings = false;
    }

    /**
     * Update the pause menu state and input processor
     */
    public void update() {
        /// Handle escape key press
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !wasEscapePressed) {
            togglePause();
            wasEscapePressed = true;
        } else if (!Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            wasEscapePressed = false;
        }

        if(isPaused){
            /// Set input processor to pause menu
            Gdx.input.setInputProcessor(stage);
        } else {
            /// Set input processor back to level screen
            Gdx.input.setInputProcessor(LevelScreen.getInstance().getStage());
        }
    }

    /**
     * @return True if the game is paused
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Renders the pause menu
     */
    public void render(float delta) {
        if (!isPaused) return;

        // Draw semi-transparent grey overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.5f);
        shapeRenderer.rect(0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        if (!isSettings) {
            // Draw pause menu
            stage.act();
            stage.draw();
        } else {
            settingsUI.show();
            settingsUI.render(delta);
        }
    }

    /**
     * Resizes the pause menu
     * @param width The new width
     * @param height The new height
     */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Disposes of the pause menu
     */
    public void dispose() {
        stage.dispose();
        shapeRenderer.dispose();
        lastFrame.dispose();
        pauseTexturePixmap.dispose();
    }

    /**
     * Draws the last frame before pausing
     * @param batch The sprite batch to draw with
     */
    public void drawLastFrame(SpriteBatch batch) {
        batch.begin();
        batch.draw(lastFrame, 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight(),
            0, 0, lastFrame.getWidth(), lastFrame.getHeight(),
            false, false);
        batch.end();
    }

    /**
     * @return The last frame before pausing
     */
    public Texture getLastFrame() {
        return lastFrame;
    }

    /**
     * @return True if the settings menu is open
     */
    public boolean isSettings() {
        return isSettings;
    }

    /**
     * Updates the viewport
     */
    public void updateViewport() {
        /// Update the pause menu to match new resolution
        LevelScreen.getInstance().renderWorld(0);
        takeScreenshot();
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        stage.getViewport().update(width, height, true);
        stage.getViewport().apply();
        screenTable.setPosition(
            stage.getViewport().getWorldWidth()/2f - screenTable.getWidth()/2,
            stage.getViewport().getWorldHeight()/2f - screenTable.getHeight()/2
        );
        // Update shapeRenderer projection matrix
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
    }
}
