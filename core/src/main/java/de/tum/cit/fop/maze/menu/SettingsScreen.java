package de.tum.cit.fop.maze.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.essentials.Assets;
import de.tum.cit.fop.maze.essentials.AlignableImageTextButton;
import de.tum.cit.fop.maze.essentials.SettingsConfiguration;
import de.tum.cit.fop.maze.level.GameOverScreen;
import de.tum.cit.fop.maze.level.LevelScreen;
import de.tum.cit.fop.maze.level.PauseScreen;
import de.tum.cit.fop.maze.level.SaveManager;
import games.rednblack.miniaudio.MASound;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import static de.tum.cit.fop.maze.essentials.Globals.*;

/**
 * Creates the Settings screen.</br>
 * In this screen the player can change the settings of the game:
 * <ul>
 *  <li>Audio settings:</li>
 *      <ul>
 *          <li>Music volume</li>
 *          <li>SFX volume</li>
 *      </ul>
 *  <li>Graphics settings:</li>
 *      <ul>
 *          <li>Vsync</li>
 *          <li>Full screen</li>
 *          <li>Resolution (if full screen is turned off)</li>
 *      </ul>
 * </ul>
 */
public class SettingsScreen implements Screen {
    private static SettingsScreen instance;
    private final Stage stage;
    private final BitmapFont font;
    private TextureRegion exitRegion;
    private TextureRegion vsyncOnRegion;
    private TextureRegion vsyncOffRegion;
    private TextureRegion fullScreenOnRegion;
    private TextureRegion fullScreenOffRegion;
    private TextureRegion sliderRegion;
    private TextureRegion knobRegion;
    private TextureRegion dropDownMenuRegion;
    private TextureRegion containerRegion;
    private TextureRegion smallButtonPressedRegion;
    private TextureRegion smallButtonReleasedRegion;
    private Container<VerticalGroup> container;
    private final FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local("assets/font/YosterIslandRegular-VqMe.ttf"));
    private final FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    private final MASound clickSound;

    /**
     * @return The singleton instance of the settings menu
     */
    public static synchronized SettingsScreen getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SettingsScreen has not been initialized yet");
        }
        return instance;
    }

    /**
     * Loads textures and sets up the menu
     * @param viewport Viewport
     * @param batch SpriteBatch
     */
    public SettingsScreen(Viewport viewport, SpriteBatch batch) {
        instance = this;
        loadTextures();

        parameter.size = 27;
        parameter.color = new Color(0xE0E0E0FF);
        font = generator.generateFont(parameter);

        clickSound = Assets.getInstance().getSound("gui_click");
        clickSound.setSpatialization(false);

        /// Create stage for actors
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        setupMenu();
    }
    /**
     * Creates all widgets and adds them to the stage
     */
    private void setupMenu() {
        /// Creates the essentials for the settings menu
        VerticalGroup settingElementGroup = new VerticalGroup();
        settingElementGroup.setBounds(0, 0, 336, 456);

        settingElementGroup.center().top();
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = font;
        style.fontColor = new Color(0xE0E0E0FF);


        /// Creates the settings label
        Label label = new Label("SETTINGS", style);
        label.setTouchable(Touchable.disabled);
        label.setAlignment(Align.center);
        Actor padding = new Actor();
        padding.setHeight(12f);
        settingElementGroup.addActor(padding);
        settingElementGroup.addActor(label);

        /// Creates the audio label
        label = new Label("AUDIO", style);
        label.setTouchable(Touchable.disabled);
        label.setAlignment(Align.center);

        padding = new Actor();
        padding.setHeight(30f);
        settingElementGroup.addActor(padding);
        settingElementGroup.addActor(label);

        /// Creates the section for changing music volume
        label = new Label("MUSIC", style);
        label.setAlignment(Align.right);

        NinePatch releasedNinePatch = new NinePatch(
            smallButtonReleasedRegion,
            7, 7, 7, 7
        );
        NinePatch pressedNinePatch = new NinePatch(
            smallButtonPressedRegion,
            7, 7, 7, 7
        );

        ImageTextButton.ImageTextButtonStyle textButtonStyle = new ImageTextButton.ImageTextButtonStyle();
        textButtonStyle.font = font;
        textButtonStyle.up = new NinePatchDrawable(releasedNinePatch);
        textButtonStyle.down = new NinePatchDrawable(pressedNinePatch);
        textButtonStyle.pressedOffsetX = 1;
        textButtonStyle.pressedOffsetY = -1;

        Slider sliderMusic = createSlider();
        sliderMusic.setValue(SettingsConfiguration.getInstance().getMusicVolume());
        sliderMusic.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Assets.getInstance().music.setVolume(sliderMusic.getValue());
                System.out.println("MUSIC value: " + sliderMusic.getValue());
                SettingsConfiguration.getInstance().setMusicVolume(sliderMusic.getValue());
            }
        });

        /// Wrap the label and the table in a horizontal group
        /// to center them without changing the alignment of other children in the vertical group
        HorizontalGroup horizontalGroup = new HorizontalGroup();
        horizontalGroup.addActor(label);
        Actor actor = new Actor();
        actor.setWidth(100f);
        horizontalGroup.addActor(actor);
        horizontalGroup.addActor(sliderMusic);
        horizontalGroup.right();

        padding = new Actor();
        padding.setHeight(15f);
        settingElementGroup.addActor(padding);
        settingElementGroup.addActor(horizontalGroup);
        settingElementGroup.fill();


        /// Creates the section for changing SFX volume
        label = new Label("SFX", style);
        label.setTouchable(Touchable.disabled);
        label.setAlignment(Align.center);

        Slider sliderSFX = createSlider();
        sliderSFX.setValue(SettingsConfiguration.getInstance().getSfxVolume());
        sliderSFX.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Assets.getInstance().sfx.setVolume(sliderSFX.getValue());
                System.out.println("SFX value: " + sliderSFX.getValue());
                SettingsConfiguration.getInstance().setSfxVolume(sliderSFX.getValue());
            }
        });

        Table table = new Table();
        table.add(sliderSFX);

        horizontalGroup = new HorizontalGroup();
        horizontalGroup.addActor(label);
        actor = new Actor();
        actor.setWidth(100f);
        horizontalGroup.addActor(actor);
        horizontalGroup.addActor(table);
        horizontalGroup.right();

        padding = new Actor();
        padding.setHeight(8f);
        settingElementGroup.addActor(padding);
        settingElementGroup.addActor(horizontalGroup);


        /// Creates the graphics label
        label = new Label("GRAPHICS", style);
        label.setTouchable(Touchable.disabled);
        label.setAlignment(Align.center);

        padding = new Actor();
        padding.setHeight(67f);
        settingElementGroup.addActor(padding);
        settingElementGroup.addActor(label);

        /// Creates the section for turning on/off Vsync
        label = new Label("Vsync", style);
        label.setAlignment(Align.center);

        Image toggleVsyncImage = new Image(vsyncOnRegion);

        AlignableImageTextButton toggleVSYNCButton = new AlignableImageTextButton("", textButtonStyle, toggleVsyncImage, 1.5f);
        if (SettingsConfiguration.getInstance().isVsync()) {
            toggleVSYNCButton.setImagePadding(10f);
            toggleVSYNCButton.getImage().setDrawable(new TextureRegionDrawable(vsyncOnRegion));
            Gdx.graphics.setVSync(true);
        } else {
            toggleVSYNCButton.setImagePadding(12f);
            toggleVSYNCButton.getImage().setDrawable(new TextureRegionDrawable(vsyncOffRegion));
            Gdx.graphics.setVSync(false);
        }
        toggleVSYNCButton.setImageTopPadding(2f);
        toggleVSYNCButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.stop();
                clickSound.setLooping(false);
                clickSound.play();
                SettingsConfiguration.getInstance().setVsync(!SettingsConfiguration.getInstance().isVsync());
                if (SettingsConfiguration.getInstance().isVsync()) {
                    toggleVSYNCButton.setImagePadding(10f);
                    toggleVsyncImage.setDrawable(new TextureRegionDrawable(vsyncOnRegion));
                    Gdx.graphics.setVSync(true);
                } else {
                    toggleVSYNCButton.setImagePadding(12f);
                    toggleVsyncImage.setDrawable(new TextureRegionDrawable(vsyncOffRegion));
                    Gdx.graphics.setVSync(false);
                }
            }
        });

        table = new Table();
        table.add(toggleVSYNCButton).width(toggleVSYNCButton.getPrefHeight());

        horizontalGroup = new HorizontalGroup();
        horizontalGroup.addActor(label);
        actor = new Actor();
        actor.setWidth(130f);
        horizontalGroup.addActor(actor);
        horizontalGroup.addActor(table);

        padding = new Actor();
        padding.setHeight(20f);
        settingElementGroup.addActor(padding);
        settingElementGroup.addActor(horizontalGroup);

        /// Creates the section for turning on/off Fullscreen
        label = new Label("Full Screen", style);
        label.setAlignment(Align.center);

        Image toggleFullScreen = new Image(fullScreenOffRegion);

        /// Create the select box for resolution first
        /// to enable it to toggle after the full screen button is clicked

        SelectBox<String> selectBox = createSelectBox();
        /// Create the label for the resolution select box
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = new Color(0xE0E0E0FF);
        Label resLabel = new Label("Resolution", labelStyle);

        /// Create the full screen button
        AlignableImageTextButton toggleFullButton = createFullscreenButton(textButtonStyle, toggleFullScreen, selectBox, resLabel);
        if (SettingsConfiguration.getInstance().isFullScreen()){
            toggleFullButton.setImagePadding(10f);
            toggleFullButton.getImage().setDrawable(new TextureRegionDrawable(fullScreenOnRegion));
        } else {
            toggleFullButton.setImagePadding(12f);
            toggleFullButton.getImage().setDrawable(new TextureRegionDrawable(fullScreenOffRegion));
        }

        table = new Table();
        table.add(toggleFullButton).width(toggleFullButton.getPrefHeight());

        horizontalGroup = new HorizontalGroup();
        horizontalGroup.addActor(label);
        actor = new Actor();
        actor.setWidth(54f);
        horizontalGroup.addActor(actor);
        horizontalGroup.addActor(table);

        padding = new Actor();
        padding.setHeight(10f);
        settingElementGroup.addActor(padding);
        settingElementGroup.addActor(horizontalGroup);


        /// Creates the section for changing resolution if full screen is off
        selectBox.setMaxListCount(4);
        selectBox.setAlignment(Align.center);
        selectBox.getList().setAlignment(Align.left);

        selectBox.setItems(WINDOWED_RESOLUTIONS.toArray(new String[0]));
        selectBox.setSelected(SettingsConfiguration.getInstance().getResolution());

        selectBox.setDisabled(SettingsConfiguration.getInstance().isFullScreen());
        if (SettingsConfiguration.getInstance().isFullScreen()) {
            selectBox.getStyle().fontColor = new Color(0x404040FF);
        }

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.stop();
                clickSound.setLooping(false);
                clickSound.play();
                String[] resolution = selectBox.getSelected().split("x");
                Gdx.graphics.setWindowedMode(Integer.parseInt(resolution[0]), Integer.parseInt(resolution[1]));
                Menu.getInstance().SCREEN_HEIGHT = Integer.parseInt(resolution[1]);
                Menu.getInstance().SCREEN_WIDTH = Integer.parseInt(resolution[0]);
                CURRENT_SCREEN_WIDTH_WINDOWED = Integer.parseInt(resolution[0]);
                CURRENT_SCREEN_HEIGHT_WINDOWED = Integer.parseInt(resolution[1]);
                SettingsConfiguration.getInstance().setResolution(CURRENT_SCREEN_WIDTH_WINDOWED + "x" + CURRENT_SCREEN_HEIGHT_WINDOWED);
                Menu.getInstance().resize(Integer.parseInt(resolution[0]), Integer.parseInt(resolution[1]));
                container.setPosition(stage.getViewport().getWorldWidth()/2f - container.getWidth()/2, stage.getViewport().getWorldHeight()/2f - container.getHeight()/2);
                Menu.getInstance().updateChildPositions();
                if (LevelScreen.getInstance() != null) {
                    LevelScreen.getInstance().updateViewport();
                    PauseScreen.getInstance().updateViewport();
                }
                GameOverScreen.getInstance().updateViewport();
            }
        });

        table = new Table();
        table.add(selectBox).height(30);
        padding = new Actor();
        padding.setHeight(15f);

        horizontalGroup = new HorizontalGroup();
        horizontalGroup.addActor(resLabel);
        actor = new Actor();
        actor.setWidth(20f);
        horizontalGroup.addActor(actor);
        horizontalGroup.addActor(table);

        settingElementGroup.addActor(padding);
        settingElementGroup.addActor(horizontalGroup);


        /// Creates the button to exit the settings menu
        Image exitSettingsImage = new Image(exitRegion);

        AlignableImageTextButton exitSettingsButton = new AlignableImageTextButton("", textButtonStyle, exitSettingsImage, 1.5f);
        exitSettingsButton.setImagePadding(5f);
        exitSettingsButton.setImageTopPadding(2f);
        exitSettingsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.stop();
                clickSound.setLooping(false);
                clickSound.play();
                /// If player exits the settings menu, save the configurations
                try {
                    SaveManager.saveConfigurations();
                    if (PauseScreen.getInstance() == null) {
                        Menu.getInstance().toggleMenuState(MenuState.MAIN_MENU);
                        return;
                    }

                    if (PauseScreen.getInstance().isSettings()) {
                        PauseScreen.getInstance().setSettings(false);
                    } else {
                        Menu.getInstance().toggleMenuState(MenuState.MAIN_MENU);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        padding = new Actor();
        padding.setHeight(40f);

        table = new Table();
        table.add(exitSettingsButton).width(exitSettingsButton.getPrefHeight());

        horizontalGroup = new HorizontalGroup();
        horizontalGroup.addActor(table);

        settingElementGroup.addActor(padding);
        settingElementGroup.addActor(horizontalGroup);


        /// Create the main texture for the settings menu to place all widgets on
        container = createContainer(settingElementGroup);

        stage.addActor(container);
    }

    /**
     * Creates a button to toggle full screen
     * @param textButtonStyle Style for the button
     * @param toggleFullScreenImage Image for the button
     * @param selectBox SelectBox for resolution
     * @param resLabel Label for the resolution select box
     * @return AlignableImageTextButton
     */
    private @NotNull AlignableImageTextButton createFullscreenButton(ImageTextButton.ImageTextButtonStyle textButtonStyle, Image toggleFullScreenImage, SelectBox<String> selectBox, Label resLabel) {
        AlignableImageTextButton toggleFullButton = new AlignableImageTextButton("", textButtonStyle, toggleFullScreenImage, 1.5f);
        toggleFullButton.setImagePadding(12f);
        toggleFullButton.setImageTopPadding(2f);
        toggleFullButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.stop();
                clickSound.setLooping(false);
                clickSound.play();
                boolean isFullScreen = !SettingsConfiguration.getInstance().isFullScreen();
                SettingsConfiguration.getInstance().setFullScreen(isFullScreen);
                /// Disable the resolution select box if full screen is on
                selectBox.setDisabled(SettingsConfiguration.getInstance().isFullScreen());
                selectBox.getStyle().fontColor = isFullScreen ? new Color(0x404040FF) : new Color(0xE0E0E0FF);

                resLabel.getStyle().fontColor = isFullScreen ? new Color(0x404040FF) : new Color(0xE0E0E0FF);

                if (isFullScreen) {
                    /// Set the button padding to 10f to center the image
                    toggleFullButton.setImagePadding(10f);
                    toggleFullScreenImage.setDrawable(new TextureRegionDrawable(fullScreenOnRegion));

                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                    Menu.getInstance().SCREEN_HEIGHT = Gdx.graphics.getHeight();
                    Menu.getInstance().SCREEN_WIDTH = Gdx.graphics.getWidth();
                    /// resize the stage to the new resolution
                    Menu.getInstance().resize(Menu.getInstance().SCREEN_WIDTH, Menu.getInstance().SCREEN_HEIGHT);
                    Menu.getInstance().updateChildPositions();
                } else {
                    /// Set the button padding to 12f to center the image
                    toggleFullButton.setImagePadding(12f);
                    toggleFullScreenImage.setDrawable(new TextureRegionDrawable(fullScreenOffRegion));
                    /// Set the screen to the default resolution
                    Menu.getInstance().SCREEN_HEIGHT = CURRENT_SCREEN_HEIGHT_WINDOWED;
                    Menu.getInstance().SCREEN_WIDTH = CURRENT_SCREEN_WIDTH_WINDOWED;

                    Gdx.graphics.setWindowedMode(Menu.getInstance().SCREEN_WIDTH, Menu.getInstance().SCREEN_HEIGHT);
                    Menu.getInstance().resize( CURRENT_SCREEN_WIDTH_WINDOWED, CURRENT_SCREEN_HEIGHT_WINDOWED);
                    Menu.getInstance().updateChildPositions();
                }
                if (LevelScreen.getInstance() != null) {
                    LevelScreen.getInstance().updateViewport();
                    PauseScreen.getInstance().updateViewport();
                }
                GameOverScreen.getInstance().updateViewport();

                updateContainerPosition();

            }
        });
        return toggleFullButton;
    }

    public void updateContainerPosition(){
        container.setPosition(
            stage.getViewport().getWorldWidth() / 2f - container.getWidth() / 2,
            stage.getViewport().getWorldHeight() / 2f - container.getHeight() / 2
        );
    }

    /**
     * Creates a select box with a custom style
     * @return SelectBox<String>
     */
    private @NotNull SelectBox<String> createSelectBox(){
        NinePatch selectBoxNinePatch = new NinePatch(
            dropDownMenuRegion,
            7, 15, 2, 2
        );
        SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle();
        parameter.size = 22;
        parameter.color = new Color(0xE0E0E0FF);
        selectBoxStyle.font = generator.generateFont(parameter);
        selectBoxStyle.fontColor = new Color(0xE0E0E0FF);
        selectBoxStyle.background = new NinePatchDrawable(selectBoxNinePatch);
        selectBoxStyle.scrollStyle = new ScrollPane.ScrollPaneStyle();
        selectBoxStyle.listStyle = new List.ListStyle();
        selectBoxStyle.listStyle.font = generator.generateFont(parameter);
        selectBoxStyle.listStyle.fontColorSelected = new Color(0x707070FF);
        selectBoxStyle.listStyle.fontColorUnselected = new Color(0xE0E0E0FF);
        selectBoxStyle.listStyle.selection = new NinePatchDrawable(selectBoxNinePatch);
        selectBoxStyle.listStyle.background = new NinePatchDrawable(selectBoxNinePatch);
        selectBoxStyle.disabledFontColor = new Color(0x404040FF);
        return new SelectBox<>(selectBoxStyle);
    }

    /**
     * Creates a container for the settings menu
     * @param settingElementGroup VerticalGroup containing all widgets
     * @return Container<VerticalGroup>
     */
    private @NotNull Container<VerticalGroup> createContainer(VerticalGroup settingElementGroup) {
        Container<VerticalGroup> container = new Container<>(settingElementGroup);
        container.setBackground(new TextureRegionDrawable(containerRegion));
        /// Set the size of the container to the size of the texture
        container.setSize(493f, 600);
        /// Set container position to center of the screen
        container.setPosition(stage.getViewport().getWorldWidth()/2f - container.getWidth()/2, stage.getViewport().getWorldHeight()/2f - container.getHeight()/2);
        container.align(Align.top);
        return container;
    }

    /**
     * Creates a basic slider with a custom style
     * @return Slider
     */
    private @NotNull Slider createSlider(){
        NinePatch sliderNinePatch = new NinePatch(
            sliderRegion,
            7, 7, 2, 2
        );
        sliderNinePatch.setTopHeight(2);
        sliderNinePatch.setMiddleHeight(10);
        sliderNinePatch.setBottomHeight(2);
        sliderNinePatch.setLeftWidth(2);
        sliderNinePatch.setMiddleWidth(40);
        sliderNinePatch.setRightWidth(2);
        NinePatchDrawable background = new NinePatchDrawable(sliderNinePatch);
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = background;
        TextureRegionDrawable knob_default = new TextureRegionDrawable(knobRegion);
        TextureRegionDrawable knob_selected = new TextureRegionDrawable(knobRegion);
        knob_default.setMinSize(knob_default.getMinWidth()*1.7f, knob_default.getMinHeight()*1.7f);
        sliderStyle.knob = knob_default;
        knob_selected.setMinSize(knob_selected.getMinWidth()*2f, knob_selected.getMinHeight()*2f);
        sliderStyle.knobDown = knob_selected;

        return new Slider(0f, 0.5f, 0.005f, false, sliderStyle);
    }

    /**
     * Loads the textures for the settings menu
     */
    private void loadTextures() {
        TextureAtlas menuAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu.atlas", TextureAtlas.class);
        TextureAtlas menuIconsAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu_icons.atlas", TextureAtlas.class);

        smallButtonPressedRegion = new TextureRegion(menuAtlas.findRegion("small_button_pressed"));
        smallButtonReleasedRegion = new TextureRegion(menuAtlas.findRegion("small_button_released"));
        exitRegion = new TextureRegion(menuIconsAtlas.findRegion("exit"));
        vsyncOnRegion = new TextureRegion(menuAtlas.findRegion("on"));
        vsyncOffRegion = new TextureRegion(menuAtlas.findRegion("off"));
        fullScreenOnRegion = new TextureRegion(menuAtlas.findRegion("on"));
        fullScreenOffRegion = new TextureRegion(menuAtlas.findRegion("off"));
        sliderRegion = new TextureRegion(menuAtlas.findRegion("slider"));
        knobRegion = new TextureRegion(menuAtlas.findRegion("knob"));
        dropDownMenuRegion = new TextureRegion(menuAtlas.findRegion("drop_down_selection"));
        containerRegion = new TextureRegion(menuAtlas.findRegion("settings_container"));

    }

    @Override
    public void render(float delta) {
        // Update and draw stage (buttons)
        stage.act();
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        generator.dispose();
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
