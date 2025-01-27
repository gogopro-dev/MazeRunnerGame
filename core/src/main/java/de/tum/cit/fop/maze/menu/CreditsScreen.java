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
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.Assets;
import de.tum.cit.fop.maze.essentials.AlignableImageTextButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Singleton class for the UI of the credits screen.</br>
 * This class is used to display the credits of the game.
 */
public class CreditsScreen implements Screen {
    private final Stage stage;
    private final BitmapFont font;
    private TextureRegion exitRegion;
    private TextureRegion containerRegion;
    private TextureRegion smallButtonPressedRegion;
    private TextureRegion smallButtonReleasedRegion;
    private Container<VerticalGroup> container;
    private final FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local("assets/font/YosterIslandRegular-VqMe.ttf"));
    private final FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();


    /**
     * Loads textures and sets up the menu
     * @param viewport Viewport
     * @param batch SpriteBatch
     */
    public CreditsScreen(Viewport viewport, SpriteBatch batch) {

        fontParameter.size = 27;
        fontParameter.color = new Color(0xE0E0E0FF);
        font = generator.generateFont(fontParameter);

        /// Create stage for actors
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        loadTextures();
        setupMenu();

        generator.dispose();
    }

    /**
     * Creates all widgets and adds them to the stage
     */
    private void setupMenu() {
        VerticalGroup settingElementGroup = new VerticalGroup();
        settingElementGroup.setBounds(0, 0, 336, 456);

        Label.LabelStyle style = new Label.LabelStyle();
        style.font = font;
        style.fontColor = new Color(0xE0E0E0FF);


        /// Creates the credits label
        Label label = new Label("CREDITS", style);
        label.setTouchable(Touchable.disabled);
        label.setAlignment(Align.center);
        Actor padding = new Actor();
        padding.setHeight(12f);
        settingElementGroup.addActor(padding);
        settingElementGroup.addActor(label);

        ArrayList<String> students = new ArrayList<>(Arrays.asList("Hlib Zabudko", "Eriks Spaks", "Maxim Kaskeev"));
        Label.LabelStyle studentStyle = new Label.LabelStyle();

        fontParameter.size = 22;
        fontParameter.color = new Color(0xE0E0E0FF);
        studentStyle.font = generator.generateFont(fontParameter);
        studentStyle.fontColor = new Color(0xE0E0E0FF);

        Label studentLabel = new Label("This game was created by \nthe following students of the \nTechnical University of Munich:\n", studentStyle);
        studentLabel.setTouchable(Touchable.disabled);
        studentLabel.setAlignment(Align.center);

        padding = new Actor();
        padding.setHeight(30f);

        settingElementGroup.addActor(padding);
        settingElementGroup.addActor(studentLabel);

        fontParameter.size = 20;
        fontParameter.color = new Color(0xE0E0E0FF);
        studentStyle.font = generator.generateFont(fontParameter);
        for (String student : students) {
            Label studentName = new Label(student, studentStyle);
            studentName.setTouchable(Touchable.disabled);
            studentName.setAlignment(Align.center);
            settingElementGroup.addActor(studentName);
        }



        /// Creates the button to exit the settings menu
        Image exitSettingsImage = new Image(exitRegion);

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

        AlignableImageTextButton exitSettingsButton = new AlignableImageTextButton("", textButtonStyle, exitSettingsImage, 1.5f);
        exitSettingsButton.setImagePadding(5f);
        exitSettingsButton.setImageTopPadding(2f);
        exitSettingsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Menu.getInstance().toggleMenuState(MenuState.MAIN_MENU);
            }
        });


        Table table = new Table();
        table.add(exitSettingsButton).width(exitSettingsButton.getPrefHeight());

        HorizontalGroup horizontalGroup = new HorizontalGroup();
        horizontalGroup.addActor(table);

        padding = new Actor();
        padding.setHeight(40f);
        settingElementGroup.addActor(padding);
        settingElementGroup.addActor(horizontalGroup);

        /// Create the main texture for the settings menu to place all widgets on
        container = createContainer(settingElementGroup);

        stage.addActor(container);
    }

    /**
     * Creates a container for the settings menu
     * @param settingElementGroup {@link VerticalGroup} containing all widgets
     * @return {@link Container<VerticalGroup>}
     */
    private @NotNull Container<VerticalGroup> createContainer(VerticalGroup settingElementGroup) {
        Container<VerticalGroup> container = new Container<>(settingElementGroup);
        container.setBackground(new TextureRegionDrawable(containerRegion));
        /// Set the size of the container to the size of the texture
        container.setSize(493f, 612f);
        /// Set container position to center of the screen
        container.setPosition(stage.getViewport().getWorldWidth()/2f - container.getWidth()/2, stage.getViewport().getWorldHeight()/2f - container.getHeight()/2);
        container.align(Align.top);
        return container;
    }

    /**
     * Loads the textures for the settings menu
     */
    private void loadTextures() {
        TextureAtlas menuAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu.atlas", TextureAtlas.class);
        TextureAtlas menuIconsAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu_icons.atlas", TextureAtlas.class);

        exitRegion = menuIconsAtlas.findRegion("exit");
        containerRegion = menuAtlas.findRegion("credits_container");
        smallButtonPressedRegion = menuAtlas.findRegion("small_button_pressed");
        smallButtonReleasedRegion = menuAtlas.findRegion("small_button_released");

    }

    /**
     * Updates the position of the container to the center of the screen
     */
    public void updateContainerPosition(){
        container.setPosition(stage.getViewport().getWorldWidth()/2f - container.getWidth()/2, stage.getViewport().getWorldHeight()/2f - container.getHeight()/2);
    }

    @Override
    public void render(float delta) {
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
