package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.essentials.AlignableImageTextButton;
import de.tum.cit.fop.maze.menu.Menu;
import de.tum.cit.fop.maze.menu.MenuState;
import de.tum.cit.fop.maze.menu.SettingsUI;
import java.nio.ByteBuffer;

/**
 * Class for the pause screen.</br>
 * Used to pause the game and show the pause menu.
 */
public class PauseScreen {
    private Pixmap lastFrame;
    private final Texture pauseBackground;
    private final Stage stage;
    private final ShapeRenderer shapeRenderer;
    private boolean isPaused;
    private boolean wasEscapePressed; // To handle key press state
    private final TextureAtlas playButtonAtlas;
    private final Skin play_button_skin;
    private final Table screenTable;
    private final SettingsUI settingsUI;
    private boolean isSettings = false;
    private static PauseScreen instance = null;

    /**
     * @return The singleton instance of the pause screen
     */
    public static PauseScreen getInstance() {
        if (instance == null) {
            instance = new PauseScreen();
        }
        return instance;
    }

    /**
     * Constructor for the pause screen.</br>
     * Creates the pause menu.
     */
    public PauseScreen() {
        pauseBackground = new Texture("menu/pause_menu.png");
        stage = new Stage(new ScreenViewport());
        shapeRenderer = new ShapeRenderer();
        isPaused = false;
        wasEscapePressed = false;
        screenTable = new Table();
        settingsUI = SettingsUI.getInstance();

        playButtonAtlas = new TextureAtlas(Gdx.files.internal("menu/button.atlas"));
        play_button_skin = new Skin(playButtonAtlas);
        setupPauseMenu();
    }

    /**
     * Sets up the pause menu
     */
    public void setupPauseMenu(){
        /// Load icons for buttons
        TextureAtlas iconsAtlas = new TextureAtlas(Gdx.files.internal("icons/main_menu_icons.atlas"));
        TextureRegion iconRegion;

        /// Load font for text
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/YosterIslandRegular-VqMe.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 27;
        parameter.color = new Color(0xE0E0E0FF);

        /// Create style for buttons
        ImageTextButton.ImageTextButtonStyle textButtonStyle = new ImageTextButton.ImageTextButtonStyle();
        textButtonStyle.font = generator.generateFont(parameter);
        textButtonStyle.up = play_button_skin.getDrawable("play_button_released");
        textButtonStyle.down = play_button_skin.getDrawable("play_button_pressed");
        textButtonStyle.pressedOffsetX = 1;
        textButtonStyle.pressedOffsetY = -1;

        /// Create table for pause menu
        screenTable.setBackground(new TextureRegionDrawable(new TextureRegion(pauseBackground)));
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
        iconRegion = iconsAtlas.findRegion("play");
        Image playImage = new Image(iconRegion);

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
        iconRegion = iconsAtlas.findRegion("settings");
        Image settingsImage = new Image(iconRegion);

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
        iconRegion = iconsAtlas.findRegion("exit");
        Image exitImage = new Image(iconRegion);

        AlignableImageTextButton exitButton = new AlignableImageTextButton("Exit to menu", textButtonStyle, exitImage, 1.5f);
        exitButton.setLabelPadding(10f);
        exitButton.setLabelTopPadding(4f);
        exitButton.setImagePadding(10f);
        exitButton.setImageTopPadding(4f);
        exitButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause();
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
     * Toggles the pause state
     */
    public void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            /// Take a screenshot of the current frame
            /// To place the pause menu on top of it
            Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            ByteBuffer pixels = pixmap.getPixels();
            // This loop makes sure the whole screenshot is opaque and looks exactly like what the user is seeing
            int size = Gdx.graphics.getBackBufferWidth() * Gdx.graphics.getBackBufferHeight() * 4;
            for (int i = 3; i < size; i += 4) {
                pixels.put(i, (byte) 255);
            }
            lastFrame = pixmap;
        }
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
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
        pauseBackground.dispose();
        stage.dispose();
        shapeRenderer.dispose();
        playButtonAtlas.dispose();
        lastFrame.dispose();
    }

    /**
     * @return The last frame before pausing
     */
    public Pixmap getLastFrame() {
        return lastFrame;
    }

    /**
     * @return True if the settings menu is open
     */
    public boolean isSettings() {
        return isSettings;
    }
}
