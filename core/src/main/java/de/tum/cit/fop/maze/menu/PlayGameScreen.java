package de.tum.cit.fop.maze.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.Assets;
import de.tum.cit.fop.maze.LoadMenu;
import de.tum.cit.fop.maze.essentials.AlignableImageTextButton;
import org.jetbrains.annotations.NotNull;

import java.util.Random;


public class PlayGameScreen implements Screen {
    private final boolean[] isNewGame;
    private final String[] gameTime;
    private boolean isCreateNewGameDialogOpen = false;
    private CreateNewGameScreen createNewGameScreen;
    private final Stage stage;
    private TextureRegion smallButtonPressedRegion;
    private TextureRegion smallButtonReleasedRegion;
    private TextureRegion largeButtonPressedRegion;
    private TextureRegion largeButtonReleasedRegion;
    private TextureRegion deleteIconRegion;
    private TextureRegion newGameIconRegion;
    private TextureRegion exitIconRegion;
    private BitmapFont font;
    private TextureRegion containerRegion;
    private VerticalGroup verticalGroup;
    private Container<VerticalGroup> container;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local("assets/font/YosterIslandRegular-VqMe.ttf"));
    FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

    private static PlayGameScreen instance = null;

    public static synchronized PlayGameScreen getInstance(){
        if (instance == null){
            throw new IllegalStateException("PlayGameScreen has not been initialized yet.");
        }
        return instance;
    }
    public PlayGameScreen(Viewport viewport, SpriteBatch batch) {
        instance = this;
        this.stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        loadTextures();

        isNewGame = new boolean[3];
        gameTime = new String[]{"0:00", "0:00", "0:00"};
        setupMenu();
    }

    private void loadTextures(){
        TextureAtlas menuAtlas = new TextureAtlas("menu/menu.atlas");
        TextureAtlas menuIconsAtlas = new TextureAtlas("menu/menu_icons.atlas");

        smallButtonPressedRegion = menuAtlas.findRegion("small_button_pressed");
        smallButtonReleasedRegion = menuAtlas.findRegion("small_button_released");

        largeButtonPressedRegion = menuAtlas.findRegion("big_button_pressed");
        largeButtonReleasedRegion = menuAtlas.findRegion("big_button_released");

        deleteIconRegion = menuIconsAtlas.findRegion("delete");
        newGameIconRegion = menuIconsAtlas.findRegion("newGame");
        exitIconRegion = menuIconsAtlas.findRegion("exit");

        containerRegion = menuAtlas.findRegion("play_container");

//        menuAtlas.dispose();
//        menuIconsAtlas.dispose();
    }

    private void setupMenu() {
        fontParameter.size = 30;
        fontParameter.color = new Color(0xE0E0E0FF);
        font = generator.generateFont(fontParameter);

        verticalGroup = new VerticalGroup();
        verticalGroup.top();

        Label.LabelStyle style = new Label.LabelStyle();
        style.font = font;
        style.fontColor = new Color(0xE0E0E0FF);


        /// Creates the PLAY label
        Label label = new Label("PLAY", style);
        label.setTouchable(Touchable.disabled);
        label.setAlignment(Align.center);
        Actor padding = new Actor();
        padding.setHeight(10f);
        verticalGroup.addActor(padding);
        verticalGroup.addActor(label);

        padding = new Actor();
        padding.setHeight(40f);
        verticalGroup.addActor(padding);

        fontParameter.size = 25;
        fontParameter.color = new Color(0xE0E0E0FF);
        font = generator.generateFont(fontParameter);

        createButtons();

        container = new Container<>(verticalGroup);
        container.setBackground(new TextureRegionDrawable(containerRegion));
        container.setSize(512*1.7f, 257*1.7f);
        container.setPosition(stage.getViewport().getWorldWidth()/2f - container.getWidth()/2, stage.getViewport().getWorldHeight()/2f - container.getHeight()/2);
        container.align(Align.top);

        stage.addActor(container);
    }

    public AlignableImageTextButton createDeleteButton(GameButton gameButton, int index){
        ImageTextButton.ImageTextButtonStyle textButtonStyle = new ImageTextButton.ImageTextButtonStyle();

        NinePatch releasedNinePatch = new NinePatch(
            smallButtonReleasedRegion,
            7, 7, 7, 7
        );
        NinePatch pressedNinePatch = new NinePatch(
            smallButtonPressedRegion,
            7, 7, 7, 7
        );

        textButtonStyle.font = font;
        textButtonStyle.up = new NinePatchDrawable(releasedNinePatch);
        textButtonStyle.down = new NinePatchDrawable(pressedNinePatch);
        textButtonStyle.pressedOffsetX = 1;
        textButtonStyle.pressedOffsetY = -1;

        AlignableImageTextButton playButton = new AlignableImageTextButton("", textButtonStyle, new Image(deleteIconRegion), 1.5f);
        playButton.setImagePadding(11f);
        playButton.setImageTopPadding(2f);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //TODO: Delete the actual save file
                gameButton.isNewGame = true;
                isNewGame[index] = true;
                gameTime[index] = "";

                stage.clear();
                setupMenu();
            }
        });
        return playButton;
    }

    public AlignableImageTextButton createPlayButton(Image image, int index){
        ImageTextButton.ImageTextButtonStyle textButtonStyle = new ImageTextButton.ImageTextButtonStyle();
        textButtonStyle.font = font;
        textButtonStyle.up = new TextureRegionDrawable(largeButtonReleasedRegion);
        textButtonStyle.down = new TextureRegionDrawable(largeButtonPressedRegion);
        textButtonStyle.pressedOffsetX = 1;
        textButtonStyle.pressedOffsetY = -1;

        AlignableImageTextButton playButton = new AlignableImageTextButton("", textButtonStyle, image, isNewGame[index] ? 3 : 1);
        playButton.setImagePadding(isNewGame[index] ? 70f : 11f);
        playButton.setImageTopPadding(2f);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isNewGame[index]){
                    isCreateNewGameDialogOpen = true;
                    createNewGameScreen = new CreateNewGameScreen(stage.getViewport(), (SpriteBatch) stage.getBatch(), index);
                } else {
                    System.out.println("Load game " + index);
                    Menu.getInstance().toggleMenuState(MenuState.GAME_SCREEN);
                }
            }
        });
        return playButton;
    }

    public void createButtons() {
        GameButton[] gameButtons = new GameButton[3];

        Label.LabelStyle style = new Label.LabelStyle();
        style.font = font;
        style.fontColor = new Color(0xE0E0E0FF);

        Table buttonTable = new Table();

        /// Create buttons for each game slot
        for (int i = 0; i < 3; i++) {
            // TODO: Update these from save folder
            boolean isNewGame = this.isNewGame[i];

            /// Create game image
            Image gameImage;
            if (isNewGame) {
                gameImage = new Image(new TextureRegionDrawable(newGameIconRegion));
            } else {
                /// TODO: Load game screenshot
                gameImage = new Image(new TextureRegionDrawable(newGameIconRegion).tint(Color.GRAY));
            }

            /// Create game button
            gameButtons[i] = new GameButton(
                createPlayButton(gameImage, i),
                isNewGame
            );

            Table gameTable = new Table();

            /// Add game time label
            Label nameLabel = new Label(gameTime[i], style);
            nameLabel.setTouchable(Touchable.disabled);
            nameLabel.setAlignment(Align.center);
            gameTable.add(nameLabel).row();

            /// Add button
            gameTable.add(gameButtons[i].button)
                .width(124 * 1.7f)
                .height(100 * 1.7f)
                .pad(12 * 1.7f, 12 * 1.7f, 6*1.7f, 12 * 1.7f)
                .row();

            /// Create and add delete button
            AlignableImageTextButton deleteButton = createDeleteButton(gameButtons[i], i);
            if (isNewGame) {
                deleteButton.setDisabled(true);
                deleteButton.setVisible(false);
            }
            gameTable.add(deleteButton).width(48).height(48);

            buttonTable.add(gameTable);
        }

        verticalGroup.addActor(buttonTable);

        /// Create exit button
        ImageTextButton.ImageTextButtonStyle textButtonStyle = new ImageTextButton.ImageTextButtonStyle();
        textButtonStyle.font = font;
        textButtonStyle.up = new TextureRegionDrawable(smallButtonReleasedRegion);
        textButtonStyle.down = new TextureRegionDrawable(smallButtonPressedRegion);
        textButtonStyle.pressedOffsetX = 1;
        textButtonStyle.pressedOffsetY = -1;

        AlignableImageTextButton exitButton = new AlignableImageTextButton("", textButtonStyle, new Image(exitIconRegion), 1.5f);
        exitButton.setImagePadding(8f);
        exitButton.setImageTopPadding(2f);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Menu.getInstance().toggleMenuState(MenuState.MAIN_MENU);
            }
        });

        Table exitTable = new Table();
        exitTable.add(exitButton).width(44).height(44).padRight(730).padBottom(10);

        verticalGroup.addActor(exitTable);
    }

    public void updateContainerPosition(){
        container.setPosition(stage.getViewport().getWorldWidth()/2f - container.getWidth()/2, stage.getViewport().getWorldHeight()/2f - container.getHeight()/2);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.input.setInputProcessor(stage);
        stage.act();
        stage.draw();
        if (isCreateNewGameDialogOpen){
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 0.5f);
            shapeRenderer.rect(0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            createNewGameScreen.render();
        }
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        createNewGameScreen.dispose();
    }

    private static class GameButton {
        public AlignableImageTextButton button;
        public boolean isNewGame;

        public GameButton(AlignableImageTextButton button, boolean isNewGame){
            this.button = button;
            this.isNewGame = isNewGame;
        }
    }

    private static class CreateNewGameScreen {
        private final Stage stage;
        private BitmapFont font;
        private TextureRegion smallButtonPressedRegion;
        private TextureRegion smallButtonReleasedRegion;
        private TextureRegion containerRegion;
        private TextureRegion cursorRegion;
        private TextureRegion textFieldRegion;
        private TextureRegion dropDownMenuRegion;
        private TextureRegion exitIconRegion;
        private VerticalGroup verticalGroup;
        private Container<VerticalGroup> container;
        private int gameIndex;
        private FileHandle[] propertiesFiles;
        private int seed;
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local("assets/font/YosterIslandRegular-VqMe.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        public CreateNewGameScreen(Viewport viewport, SpriteBatch batch, int index) {
            this.stage = new Stage(viewport, batch);
            this.gameIndex = index;

            loadTextures();
            setupMenu();
        }

        private void loadTextures(){
            TextureAtlas menuAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu.atlas", TextureAtlas.class);
            TextureAtlas menuIconsAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu_icons.atlas", TextureAtlas.class);

            smallButtonPressedRegion = menuAtlas.findRegion("small_button_pressed");
            smallButtonReleasedRegion = menuAtlas.findRegion("small_button_released");
            containerRegion = menuAtlas.findRegion("create_game_container");
            cursorRegion = menuAtlas.findRegion("cursor");
            textFieldRegion = menuAtlas.findRegion("textField");
            dropDownMenuRegion = menuAtlas.findRegion("drop_down_selection");

            exitIconRegion = menuIconsAtlas.findRegion("exit");
        }

        private void setupMenu(){
            fontParameter.size = 30;
            fontParameter.color = new Color(0xE0E0E0FF);
            font = generator.generateFont(fontParameter);

            verticalGroup = new VerticalGroup();
            verticalGroup.top();

            Label.LabelStyle style = new Label.LabelStyle();
            style.font = font;
            style.fontColor = new Color(0xE0E0E0FF);

            /// Creates the CREATE NEW GAME label
            Label label = new Label("CREATE NEW GAME", style);
            label.setTouchable(Touchable.disabled);
            label.setAlignment(Align.center);
            Actor padding = new Actor();
            padding.setHeight(10f);
            verticalGroup.addActor(padding);
            verticalGroup.addActor(label);

            padding = new Actor();
            padding.setHeight(20f);
            verticalGroup.addActor(padding);

            fontParameter.size = 22;
            fontParameter.color = new Color(0xE0E0E0FF);
            font = generator.generateFont(fontParameter);
            style.font = font;

            ///

            label = new Label("Enter a seed or choose\na file to load maze properties", style);
            label.setTouchable(Touchable.disabled);
            label.setAlignment(Align.center);
            padding = new Actor();
            padding.setHeight(10f);
            verticalGroup.addActor(padding);
            verticalGroup.addActor(label);

            padding = new Actor();
            padding.setHeight(10f);
            verticalGroup.addActor(padding);

            TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
            textFieldStyle.cursor = new TextureRegionDrawable(cursorRegion);
            textFieldStyle.font = font;
            textFieldStyle.fontColor = new Color(0xE0E0E0FF);
            textFieldStyle.messageFontColor = new Color(0xE0E0E099);
            textFieldStyle.background = new TextureRegionDrawable(textFieldRegion);
            TextField textField = new TextField("", textFieldStyle);
            textField.getStyle().background.setLeftWidth(20);
            textField.getStyle().background.setRightWidth(20);
            textField.setAlignment(Align.left);

            seed = (new Random()).nextInt();

            textField.setMessageText(String.valueOf(seed));

            Table table = new Table();
            table.add(textField).width(125 * 2f).height(32 * 2f).pad(10).row();

            verticalGroup.addActor(table);

            padding = new Actor();
            padding.setHeight(5f);
            verticalGroup.addActor(padding);

            fontParameter.size = 18;
            font = generator.generateFont(fontParameter);

            TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
            textButtonStyle.font = font;
            textButtonStyle.up = new TextureRegionDrawable(smallButtonReleasedRegion);
            textButtonStyle.down = new TextureRegionDrawable(smallButtonPressedRegion);
            textButtonStyle.pressedOffsetX = 1;
            textButtonStyle.pressedOffsetY = -1;

            SelectBox<String> selectBox = createSelectBox();
            selectBox.setMaxListCount(4);
            selectBox.setAlignment(Align.center);
            selectBox.getList().setAlignment(Align.center);
            selectBox.getList();

            propertiesFiles = Gdx.files.local("assets/assets/mazeProperties").list();
            String[] fileNames = new String[propertiesFiles.length+1];
            fileNames[0] = "Select file";

            for (int i = 1; i < propertiesFiles.length+1; i++) {
                fileNames[i] = propertiesFiles[i-1].name().replace(".properties", "");
            }

            selectBox.setItems(fileNames);

            selectBox.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (selectBox.getSelected().equals("Select file")){
                        textField.setDisabled(false);
                        textField.setText(textField.getText().isEmpty() ? "" : textField.getText());
                    } else {
                        textField.setText(textField.getText().isEmpty() ? "" : textField.getText());
                        textField.setDisabled(true);
                        /// TODO: Load maze properties from file
                    }
                }
            });

            Table selectBoxTable = new Table();
            selectBoxTable.add(selectBox).center().padLeft(10);

            verticalGroup.addActor(selectBoxTable);

            padding = new Actor();
            padding.setHeight(5f);
            verticalGroup.addActor(padding);

            fontParameter.size = 28;
            font = generator.generateFont(fontParameter);
            textButtonStyle.font = font;

            TextButton createButton = new TextButton("Create", textButtonStyle);
            createButton.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (selectBox.getSelected().equals("Select file")){
                        if (!textField.getText().isEmpty()){
                            seed = textField.getText().hashCode();
                        }
                        /// TODO: Create new game with seed
                    } else {
                        /// TODO: Load maze properties from file
                        FileHandle file = propertiesFiles[selectBox.getSelectedIndex()-1];
                        System.out.println("File: " + file.name());
                    }
                    /// TODO: Create new game  with seed
                    PlayGameScreen.getInstance().isNewGame[gameIndex] = false;
                    PlayGameScreen.getInstance().gameTime[gameIndex] = "0:00";
                    PlayGameScreen.getInstance().isCreateNewGameDialogOpen = false;
                    PlayGameScreen.getInstance().stage.clear();
                    PlayGameScreen.getInstance().setupMenu();
                }
            });

            Table buttonTable = new Table();
            buttonTable.add(createButton).width(125 * 1.8f).height(32 * 1.8f).padTop(10);
            verticalGroup.addActor(buttonTable);



            ImageTextButton.ImageTextButtonStyle imageButtonStyle = new ImageTextButton.ImageTextButtonStyle();

            NinePatch releasedNinePatch = new NinePatch(
                smallButtonReleasedRegion,
                7, 7, 7, 7
            );
            NinePatch pressedNinePatch = new NinePatch(
                smallButtonPressedRegion,
                7, 7, 7, 7
            );

            imageButtonStyle.font = font;
            imageButtonStyle.up = new NinePatchDrawable(releasedNinePatch);
            imageButtonStyle.down = new NinePatchDrawable(pressedNinePatch);
            imageButtonStyle.pressedOffsetX = 1;
            imageButtonStyle.pressedOffsetY = -1;


            //exit button
            AlignableImageTextButton exitButton = new AlignableImageTextButton("", imageButtonStyle, new Image(new TextureRegionDrawable(exitIconRegion)), 1.5f);
            exitButton.setImagePadding(8f);
            exitButton.setImageTopPadding(2f);
            exitButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    PlayGameScreen.getInstance().isCreateNewGameDialogOpen = false;
                }
            });

            Table exitTable = new Table();
            exitTable.add(exitButton).width(44).height(44).padRight(340).padBottom(10);

            verticalGroup.addActor(exitTable);

            ///

            container = new Container<>(verticalGroup);
            container.setBackground(new TextureRegionDrawable(containerRegion));
            container.setSize(280 * 1.7f, 240 * 1.7f);
            container.setPosition(stage.getViewport().getWorldWidth()/2f - container.getWidth()/2, stage.getViewport().getWorldHeight()/2f - container.getHeight()/2);
            container.align(Align.top);

            stage.addActor(container);
        }

        private @NotNull SelectBox<String> createSelectBox(){
            NinePatch selectBoxNinePatch = new NinePatch(
                    dropDownMenuRegion,
                    7, 15, 2, 2
            );
            SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle();
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local("assets/font/YosterIslandRegular-VqMe.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 22;
            parameter.color = new Color(0xE0E0E0FF);
            // Create ScrollPane style with background to prevent black flash
            ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
            scrollStyle.background = new NinePatchDrawable(selectBoxNinePatch);
            selectBoxStyle.scrollStyle = scrollStyle;
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

        public void render() {
            Gdx.input.setInputProcessor(stage);
            stage.act();
            stage.draw();
        }

        public void dispose() {
            stage.dispose();
            font.dispose();
        }
    }
}
