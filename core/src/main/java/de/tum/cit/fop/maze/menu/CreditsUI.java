package de.tum.cit.fop.maze.menu;

import com.badlogic.gdx.Gdx;
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
import de.tum.cit.fop.maze.essentials.AlignableImageTextButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class for the settings menu
 */
public class CreditsUI {
    private final Stage stage;
    private final TextureAtlas playButtonAtlas;
    private final BitmapFont font;
    private TextureRegion exitRegion;
    private TextureRegion containerRegion;
    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/YosterIslandRegular-VqMe.ttf"));
    FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();


    /**
     * Constructor for the settings menu.
     * Loads textures and sets up the menu
     * @param viewport Viewport
     * @param batch SpriteBatch
     */
    public CreditsUI(Viewport viewport, SpriteBatch batch) {
        loadTextures();

        fontParameter.size = 27;
        fontParameter.color = new Color(0xE0E0E0FF);
        font = generator.generateFont(fontParameter);

        /// Create stage for actors
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        playButtonAtlas = new TextureAtlas(Gdx.files.internal("menu/button.atlas"));

        setupMenu();
    }
    /**
     * Creates all widgets and adds them to the stage
     */
    private void setupMenu() {
        VerticalGroup settingElementGroup = new VerticalGroup();
        settingElementGroup.setBounds(0, 0, 336, 456);

//        settingElementGroup.center().top();
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

        fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = 22;
        fontParameter.color = new Color(0xE0E0E0FF);
        studentStyle.font = generator.generateFont(fontParameter);
        studentStyle.fontColor = new Color(0xE0E0E0FF);

        Label studentLabel = new Label("This game was created by \nfollowing students of the \nTechnical University of Munich:\n", studentStyle);
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
            playButtonAtlas.findRegion("play_button_released"),
            7, 7, 7, 7
        );
        NinePatch pressedNinePatch = new NinePatch(
            playButtonAtlas.findRegion("play_button_pressed"),
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
        Container<VerticalGroup> container = createContainer(settingElementGroup);

        stage.addActor(container);
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
        exitRegion = new TextureRegion(new TextureAtlas(Gdx.files.internal("menu/settings.atlas")).findRegion("exit"));
        containerRegion = new TextureRegion(new TextureAtlas(Gdx.files.internal("menu/credits.atlas")).findRegion("credits_container"));
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
