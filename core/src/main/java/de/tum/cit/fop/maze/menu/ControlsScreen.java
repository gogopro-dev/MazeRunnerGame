package de.tum.cit.fop.maze.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

/**
 * Class for the UI of the controls screen.</br>
 * Displays the controls of the game.
 */
public class ControlsScreen implements Screen {
    private TextureRegion smallButtonReleasedRegion;
    private TextureRegion smallButtonPressedRegion;
    private TextureRegion exitIconRegion;
    private TextureRegion containerRegion;
    private Container<Table> container;
    private final Stage stage;
    private final Table controlsElementTable = new Table();
    private final FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local("assets/font/YosterIslandRegular-VqMe.ttf"));
    private final FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();


    /**
     * Constructor for the {@link ControlsScreen}
     * @param viewport Viewport
     * @param batch SpriteBatch
     */
    public ControlsScreen(Viewport viewport, SpriteBatch batch) {
        stage = new Stage(viewport, batch);

        loadTextures();
        setupMenu();
    }

    /**
     * Loads the textures for the menu from the asset manager
     * and assigns them to the corresponding variables
     */
    private void loadTextures(){
        TextureAtlas menuAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu.atlas", TextureAtlas.class);
        TextureAtlas menuIconsAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu_icons.atlas", TextureAtlas.class);

        exitIconRegion = menuIconsAtlas.findRegion("exit");

        containerRegion = menuAtlas.findRegion("controls_container");
        smallButtonPressedRegion = menuAtlas.findRegion("small_button_pressed");
        smallButtonReleasedRegion = menuAtlas.findRegion("small_button_released");
    }

    /**
     * Sets up the menu</br>
     * Creates the labels and buttons for the menu
     * and adds them to the stage
     */
    private void setupMenu() {
        parameter.size = 27;

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = generator.generateFont(parameter);
        labelStyle.fontColor = new Color(0xE0E0E0FF);

        Label label = new Label("Controls", labelStyle);
        label.setTouchable(Touchable.disabled);
        label.setAlignment(Align.center);
        Actor padding = new Actor();
        padding.setHeight(4f);
        controlsElementTable.add(padding).row();
        controlsElementTable.add(label).row();

        parameter.size = 32;
        labelStyle.font = generator.generateFont(parameter);
        label = new Label("Use to move the\ncharacter", labelStyle);
        label.setTouchable(Touchable.disabled);
        label.setAlignment(Align.right);
        padding = new Actor();
        padding.setHeight(20f);

        controlsElementTable.add(padding).row();
        controlsElementTable.add(label).width(665.6f).padRight(150f).row();

        label = new Label("Press R to light up\n the torch", labelStyle);
        label.setTouchable(Touchable.disabled);
        label.setAlignment(Align.right);
        padding = new Actor();
        padding.setHeight(32f);

        controlsElementTable.add(padding).row();
        controlsElementTable.add(label).width(665.6f).padRight(150f).row();

        label = new Label("Press ENTER\nto attack", labelStyle);
        label.setTouchable(Touchable.disabled);
        label.setAlignment(Align.right);
        padding = new Actor();
        padding.setHeight(32f);

        controlsElementTable.add(padding).row();
        controlsElementTable.add(label).width(665.6f).padRight(150f).row();

        label = new Label("Press SPACE\nto buy an item", labelStyle);
        label.setTouchable(Touchable.disabled);
        label.setAlignment(Align.right);
        padding = new Actor();
        padding.setHeight(32f);

        controlsElementTable.add(padding).row();
        controlsElementTable.add(label).width(665.6f).padRight(150f).row();

        label = new Label("Press Q to use\nan active item", labelStyle);
        label.setTouchable(Touchable.disabled);
        label.setAlignment(Align.right);
        padding = new Actor();
        padding.setHeight(32f);

        controlsElementTable.add(padding).row();
        controlsElementTable.add(label).width(665.6f).padRight(150f).row();

        label = new Label("If you stay in the\nshadows long enough...", labelStyle);
        label.setTouchable(Touchable.disabled);
        label.setAlignment(Align.right);
        padding = new Actor();
        padding.setHeight(32f);

        controlsElementTable.add(padding).row();
        controlsElementTable.add(label).width(665.6f).padRight(150f).row();


        NinePatch releasedNinePatch = new NinePatch(
            smallButtonReleasedRegion,
            7, 7, 7, 7
        );
        NinePatch pressedNinePatch = new NinePatch(
            smallButtonPressedRegion,
            7, 7, 7, 7
        );
        ImageTextButton.ImageTextButtonStyle textButtonStyle = new ImageTextButton.ImageTextButtonStyle();
        textButtonStyle.font = labelStyle.font;
        textButtonStyle.up = new NinePatchDrawable(releasedNinePatch);
        textButtonStyle.down = new NinePatchDrawable(pressedNinePatch);
        textButtonStyle.pressedOffsetX = 1;
        textButtonStyle.pressedOffsetY = -1;

        AlignableImageTextButton exitButton = new AlignableImageTextButton("", textButtonStyle, new Image(exitIconRegion), 1.5f);
        exitButton.setImagePadding(5f);
        exitButton.setImageTopPadding(2f);
        exitButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Menu.getInstance().toggleMenuState(MenuState.MAIN_MENU);
            }
        });

        Table table = new Table();
        table.add(exitButton).width(exitButton.getPrefHeight());

        padding = new Actor();
        padding.setHeight(24f);

        controlsElementTable.add(padding).row();
        controlsElementTable.add(table).left().padLeft(118f).row();

        container = new Container<>(controlsElementTable);
        container.setBackground(new TextureRegionDrawable(containerRegion));
        /// Set the size of the container to the size of the texture
        container.setSize(665.6f, 728f);
        /// Set container position to center of the screen
        container.setPosition(stage.getViewport().getWorldWidth()/2f - container.getWidth()/2, stage.getViewport().getWorldHeight()/2f - container.getHeight()/2);
        container.align(Align.top);

        stage.addActor(container);
    }

    /**
     * Updates the position of the container to the center of the screen
     */
    public void updateContainerPosition() {
        container.setPosition(stage.getViewport().getWorldWidth()/2f - container.getWidth()/2, stage.getViewport().getWorldHeight()/2f - container.getHeight()/2);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
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

    @Override
    public void dispose() {
        stage.dispose();
        generator.dispose();
    }
}
