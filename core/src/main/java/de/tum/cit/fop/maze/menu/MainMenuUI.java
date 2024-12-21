package de.tum.cit.fop.maze.menu;

import com.badlogic.gdx.Gdx;
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
import de.tum.cit.fop.maze.essentials.AlignableImageTextButton;

/**
 * Class for the main screen in menu.
 */
public class MainMenuUI {
    private final Stage stage;
    private final TextureAtlas playButtonAtlas;
    private final Skin play_button_skin;
    private final BitmapFont font;

    /**
     * Constructor for the main menu.</br>
     * Creates the stage and sets the input processor.</br>
     * Creates the buttons for the main menu.
     * @param viewport The viewport for the stage
     * @param batch The sprite batch for the stage
     */
    public MainMenuUI(Viewport viewport, SpriteBatch batch) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/YosterIslandRegular-VqMe.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 27;
        parameter.color = new Color(0xE0E0E0FF);
        font = generator.generateFont(parameter);

        /// Create stage for actors
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        playButtonAtlas = new TextureAtlas(Gdx.files.internal("menu/button.atlas"));
        play_button_skin = new Skin(playButtonAtlas);

        setupMenu();
    }
    /**
     * Sets up the main menu buttons.
     */
    private void setupMenu() {
        TextureAtlas iconsAtlas = new TextureAtlas(Gdx.files.internal("icons/main_menu_icons.atlas"));
        Table mainTable = new Table();

        ImageTextButton.ImageTextButtonStyle textButtonStyle = new ImageTextButton.ImageTextButtonStyle();
        textButtonStyle.font = font;
        textButtonStyle.up = play_button_skin.getDrawable("play_button_released");
        textButtonStyle.down = play_button_skin.getDrawable("play_button_pressed");
        textButtonStyle.pressedOffsetX = 1;
        textButtonStyle.pressedOffsetY = -1;

        /// Create Play button
        TextureRegion iconRegion = iconsAtlas.findRegion("play");
        Image image = new Image(iconRegion);

        AlignableImageTextButton playButton = new AlignableImageTextButton("Play", textButtonStyle, image, 1.5f);
        playButton.setLabelPadding(8f);
        playButton.setLabelTopPadding(2f);
        playButton.setImagePadding(12f);
        playButton.setImageTopPadding(2f);
        playButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Menu.getInstance().toggleMenuState(MenuState.PLAY);
            }
        });

        /// Create New Game button
        iconRegion = iconsAtlas.findRegion("newGame");
        image = new Image(iconRegion);

        AlignableImageTextButton newGameButton = new AlignableImageTextButton("New Game", textButtonStyle, image, 1.2f);
        newGameButton.setLabelPadding(12f);
        newGameButton.setLabelTopPadding(2f);
        newGameButton.setImagePadding(8f);
        newGameButton.setImageTopPadding(2f);
        newGameButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Menu.getInstance().toggleMenuState(MenuState.CREATE_NEW_GAME);
            }
        });

        /// Create Credits button
        iconRegion = iconsAtlas.findRegion("credits");
        image = new Image(iconRegion);

        AlignableImageTextButton creditsButton = new AlignableImageTextButton("Credits", textButtonStyle, image, 1.2f);
        creditsButton.setLabelPadding(10f);
        creditsButton.setLabelTopPadding(2f);
        creditsButton.setImagePadding(10f);
        creditsButton.setImageTopPadding(2f);
        creditsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Menu.getInstance().toggleMenuState(MenuState.CREDITS);
            }
        });

        /// Create Settings button
        iconRegion = iconsAtlas.findRegion("settings");
        image = new Image(iconRegion);
        AlignableImageTextButton settingsButton = new AlignableImageTextButton("Settings", textButtonStyle, image, 1.3f);
        settingsButton.setLabelPadding(12f);
        settingsButton.setLabelTopPadding(2f);
        settingsButton.setImagePadding(8f);
        settingsButton.setImageTopPadding(2f);
        settingsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Menu.getInstance().toggleMenuState(MenuState.SETTINGS);
            }
        });

        /// Create Exit button
        iconRegion = iconsAtlas.findRegion("exit");
        Image exitImage = new Image(iconRegion);

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
        mainTable.add(newGameButton).width(224).height(48).pad(10);
        mainTable.row();
        mainTable.add(creditsButton).width(224).height(48).pad(10);
        mainTable.row();
        mainTable.add(settingsButton).width(224).height(48).pad(10);
        mainTable.row();
        mainTable.add(exitButton).width(224).height(48).pad(10);

        /// Create container for all buttons
        Container<Table> container = new Container<>(mainTable);
        container.setBackground(new TextureRegionDrawable(iconsAtlas.findRegion("menu")));
        container.setSize(306, 456);

        container.setPosition(stage.getViewport().getWorldWidth()/2f - container.getWidth()/2, stage.getViewport().getWorldHeight()/2f - container.getHeight()/2);

        stage.addActor(container);
    }

    public void render(float delta) {
        // Update and draw stage (buttons)
        stage.act(delta);
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
        playButtonAtlas.dispose();
        font.dispose();
    }

    public void show() {
        Gdx.input.setInputProcessor(stage);
    }
}
