package de.tum.cit.fop.maze.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.essentials.Assets;
import de.tum.cit.fop.maze.essentials.AlignableImageTextButton;
import games.rednblack.miniaudio.MASound;

/**
 * Class for the UI of the main menu.</br>
 * This class is a singleton and is used to switch between
 * different menu screens with the help of the {@link Menu} class.
 */
public class MainMenuScreen implements Screen {
    private static MainMenuScreen instance = null;
    private TextureRegion menuContainerRegion;
    private TextureRegion smallButtonPressedRegion;
    private TextureRegion smallButtonReleasedRegion;
    private TextureRegion playIconRegion;
    private TextureRegion controlsIconRegion;
    private TextureRegion creditsIconRegion;
    private TextureRegion settingsIconRegion;
    private TextureRegion exitIconRegion;
    private final Stage stage;
    private final BitmapFont font;
    private Container<Table> container;
    private final MASound clickSound;

    /**
     * @return The singleton instance of the main menu
     */
    public static synchronized MainMenuScreen getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MainMenuScreen has not been initialized yet.");
        }
        return instance;
    }

    /**
     * Creates the stage and sets the input processor.</br>
     * Creates the buttons for the main menu and
     * loads the textures for the buttons.
     * @param viewport The viewport for the stage
     * @param batch The sprite batch for the stage
     */
    public MainMenuScreen(Viewport viewport, SpriteBatch batch) {
        instance = this;
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local("assets/font/YosterIslandRegular-VqMe.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 27;
        parameter.color = new Color(0xE0E0E0FF);
        font = generator.generateFont(parameter);
        generator.dispose();
        clickSound = Assets.getInstance().getSound("gui_click");
        clickSound.setSpatialization(false);


        /// Create stage for actors
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        loadTextures();

        setupMenu();
    }

    /**
     * Loads the textures for the MainMenuScreen.
     */
    private void loadTextures(){
        TextureAtlas menuAtlas = Assets.getInstance().getAssetManager().get(
            "assets/menu/menu.atlas", TextureAtlas.class);
        TextureAtlas iconsAtlas = Assets.getInstance().getAssetManager().get(
            "assets/menu/menu_icons.atlas", TextureAtlas.class
        );

        menuContainerRegion = menuAtlas.findRegion("menu");
        smallButtonPressedRegion = menuAtlas.findRegion("small_button_pressed");
        smallButtonReleasedRegion = menuAtlas.findRegion("small_button_released");

        playIconRegion = iconsAtlas.findRegion("play");
        controlsIconRegion = iconsAtlas.findRegion("controls");
        creditsIconRegion = iconsAtlas.findRegion("credits");
        settingsIconRegion = iconsAtlas.findRegion("settings");
        exitIconRegion = iconsAtlas.findRegion("exit");


    }

    /**
     * Sets up the main menu buttons.
     */
    private void setupMenu() {
        Table mainTable = new Table();

        ImageTextButton.ImageTextButtonStyle textButtonStyle = new ImageTextButton.ImageTextButtonStyle();
        textButtonStyle.font = font;
        textButtonStyle.up = new TextureRegionDrawable(smallButtonReleasedRegion);
        textButtonStyle.down = new TextureRegionDrawable(smallButtonPressedRegion);
        textButtonStyle.pressedOffsetX = 1;
        textButtonStyle.pressedOffsetY = -1;

        /// Create Play button
        Image image = new Image(playIconRegion);

        AlignableImageTextButton playButton = new AlignableImageTextButton("Play", textButtonStyle, image, 1.5f);
        playButton.setLabelPadding(8f);
        playButton.setLabelTopPadding(2f);
        playButton.setImagePadding(12f);
        playButton.setImageTopPadding(2f);
        playButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.stop();
                clickSound.setLooping(false);
                clickSound.play();
                Menu.getInstance().toggleMenuState(MenuState.PLAY);
            }
        });

        /// Create Game Lore button
        image = new Image(controlsIconRegion);

        AlignableImageTextButton controlsButton = new AlignableImageTextButton("Controls", textButtonStyle, image, 3f);
        controlsButton.setLabelPadding(12f);
        controlsButton.setLabelTopPadding(2f);
        controlsButton.setImagePadding(10f);
        controlsButton.setImageTopPadding(2f);
        controlsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
            clickSound.stop();
            clickSound.setLooping(false);
            clickSound.play();
            Menu.getInstance().toggleMenuState(MenuState.CONTROLS);
            }
        });

        /// Create Credits button
        image = new Image(creditsIconRegion);

        AlignableImageTextButton creditsButton = new AlignableImageTextButton("Credits", textButtonStyle, image, 1.2f);
        creditsButton.setLabelPadding(10f);
        creditsButton.setLabelTopPadding(2f);
        creditsButton.setImagePadding(10f);
        creditsButton.setImageTopPadding(2f);
        creditsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.stop();
                clickSound.setLooping(false);
                clickSound.play();
                Menu.getInstance().toggleMenuState(MenuState.CREDITS);
            }
        });

        /// Create Settings button
        image = new Image(settingsIconRegion);
        AlignableImageTextButton settingsButton = new AlignableImageTextButton("Settings", textButtonStyle, image, 1.3f);
        settingsButton.setLabelPadding(12f);
        settingsButton.setLabelTopPadding(2f);
        settingsButton.setImagePadding(8f);
        settingsButton.setImageTopPadding(2f);
        settingsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.stop();
                clickSound.setLooping(false);
                clickSound.play();
                Menu.getInstance().toggleMenuState(MenuState.SETTINGS);
            }
        });

        /// Create Exit button
        Image exitImage = new Image(exitIconRegion);

        AlignableImageTextButton exitButton = new AlignableImageTextButton("Exit", textButtonStyle, exitImage, 1.5f);
        exitButton.setLabelPadding(10f);
        exitButton.setLabelTopPadding(2f);
        exitButton.setImagePadding(10f);
        exitButton.setImageTopPadding(2f);
        exitButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        mainTable.add(playButton).width(224).height(48).pad(10);
        mainTable.row();
        mainTable.add(creditsButton).width(224).height(48).pad(10);
        mainTable.row();
        mainTable.add(settingsButton).width(224).height(48).pad(10);
        mainTable.row();
        mainTable.add(controlsButton).width(224).height(48).pad(10);
        mainTable.row();
        mainTable.add(exitButton).width(224).height(48).pad(10);

        /// Create container for all buttons
        container = new Container<>(mainTable);
        container.setBackground(new TextureRegionDrawable(menuContainerRegion));
        container.setSize(306, 456);

        container.setPosition(stage.getViewport().getWorldWidth()/2f - container.getWidth()/2, stage.getViewport().getWorldHeight()/2f - container.getHeight()/2);

        /// Add everything to the stage
        stage.addActor(container);
    }

    /**
     * Updates the position of the container to the center of the screen.
     */
    public void updateContainerPosition(){
        container.setPosition(
            stage.getViewport().getWorldWidth()/2f - container.getWidth()/2,
            stage.getViewport().getWorldHeight()/2f - container.getHeight()/2
        );

    }

    @Override
    public void render(float delta) {
        Gdx.input.setInputProcessor(stage);
        // Update and draw stage (buttons)
        stage.act(delta);
        stage.draw();
    }
    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {

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
}
