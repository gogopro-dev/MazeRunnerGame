package de.tum.cit.fop.maze.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
import de.tum.cit.fop.maze.essentials.AlignableImageTextButton;
import de.tum.cit.fop.maze.essentials.Assets;
import de.tum.cit.fop.maze.level.LevelData;
import de.tum.cit.fop.maze.level.LevelScreen;
import de.tum.cit.fop.maze.level.worldgen.MazeGenerator;
import games.rednblack.miniaudio.MASound;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/**
 * Creates the screen for the play game menu.</br>
 * The player can choose to start a new game or load an existing one.</br>
 * There are 3 possible slots for the games:
 * <ul>
 *  <li>By default, no game is present in the slots</li>
 *  <li>The player can delete a game from a slot or
 *  start a new game in an empty slot</li>
 *  <li>If the player chooses to start a new game, a new {@link CreateNewGameScreen} will appear
 *  on top of the screen</li>
 * </ul>
 */
public class PlayGameScreen implements Screen {
    private static PlayGameScreen instance = null;
    private final boolean[] isNewGame;
    private final Stage stage;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local("assets/font/YosterIslandRegular-VqMe.ttf"));
    private final FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    private final MASound clickSound;
    private String[] gameTime;
    private boolean isCreateNewGameDialogOpen = false;
    private CreateNewGameScreen createNewGameScreen;
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

    /**
     * Creates the stage and sets the input processor.</br>
     * Loads the textures and sets up the menu.
     *
     * @param viewport Viewport
     * @param batch    Spritebatch
     */
    public PlayGameScreen(Viewport viewport, SpriteBatch batch) {
        instance = this;
        this.stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        loadTextures();

        clickSound = Assets.getInstance().getSound("gui_click");
        clickSound.setSpatialization(false);

        isNewGame = new boolean[3];
        setupMenu();
    }

    /**
     * @return The singleton instance of the play game screen
     */
    public static synchronized PlayGameScreen getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PlayGameScreen has not been initialized yet.");
        }
        return instance;
    }

    /**
     * Resets gameTime to either
     * null or the one from the file if it exists
     */
    private void loadLevelData() {
        gameTime = new String[3];
        for (int i = 0; i < 3; i++) {
            isNewGame[i] =
                !Gdx.files.local("saves/" + i + ".png").exists() &&
                    !Gdx.files.local("saves/" + i + ".json").exists() &&
                    !Gdx.files.local("saves/levelData_" + i + ".json").exists();
        }
        LevelData[] levelData = new LevelData[3];
        for (int i = 0; i < 3; i++) {
            if (isNewGame[i]) {
                continue;
            }
            var reader = Gdx.files.local("saves/levelData_" + i + ".json").reader();
            levelData[i] = Assets.getInstance().gson.fromJson(reader,
                LevelData.class
            );
            try {
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (levelData[i] == null) {
                continue;
            }
            long seconds = (long) levelData[i].getPlaytime();
            long minutes = seconds / 60;
            long hours = minutes / 60;
            gameTime[i] = String.format(
                Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds % 60
            );
        }
    }

    /**
     * Load all textures for the play game screen
     */
    private void loadTextures() {
        TextureAtlas menuAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu.atlas", TextureAtlas.class);
        TextureAtlas menuIconsAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu_icons.atlas", TextureAtlas.class);

        smallButtonPressedRegion = menuAtlas.findRegion("small_button_pressed");
        smallButtonReleasedRegion = menuAtlas.findRegion("small_button_released");

        largeButtonPressedRegion = menuAtlas.findRegion("big_button_pressed");
        largeButtonReleasedRegion = menuAtlas.findRegion("big_button_released");

        deleteIconRegion = menuIconsAtlas.findRegion("delete");
        newGameIconRegion = menuIconsAtlas.findRegion("newGame");
        exitIconRegion = menuIconsAtlas.findRegion("exit");

        containerRegion = menuAtlas.findRegion("play_container");
    }

    /**
     * Creates all the UI elements for the play game screen
     */
    private void setupMenu() {

        loadLevelData();

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
        container.setSize(512 * 1.7f, 257 * 1.7f);
        container.setPosition(stage.getViewport().getWorldWidth() / 2f - container.getWidth() / 2, stage.getViewport().getWorldHeight() / 2f - container.getHeight() / 2);
        container.align(Align.top);

        stage.addActor(container);
    }

    /**
     * Creates a delete button for a game slot
     *
     * @param index The index of the game slot
     * @return {@link AlignableImageTextButton}
     */
    public AlignableImageTextButton createDeleteButton(int index) {
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
                clickSound.stop();
                clickSound.setLooping(false);
                clickSound.play();
                Gdx.files.local("saves/" + index + ".png").delete();
                Gdx.files.local("saves/" + index + ".json").delete();
                Gdx.files.local("saves/levelData_" + index + ".json").delete();

                isNewGame[index] = true;
                gameTime[index] = "";

                stage.clear();
                setupMenu();
            }
        });
        return playButton;
    }

    /**
     * Creates a play button for a game slot
     *
     * @param image {@link Image} to display on the button
     * @param index The index of the game slot
     * @return {@link AlignableImageTextButton}
     */
    public AlignableImageTextButton createPlayButton(Image image, int index) {
        ImageTextButton.ImageTextButtonStyle textButtonStyle = new ImageTextButton.ImageTextButtonStyle();
        textButtonStyle.font = font;
        textButtonStyle.up = new TextureRegionDrawable(largeButtonReleasedRegion);
        textButtonStyle.down = new TextureRegionDrawable(largeButtonPressedRegion);
        textButtonStyle.pressedOffsetX = 1;
        textButtonStyle.pressedOffsetY = -1;

        AlignableImageTextButton playButton = new AlignableImageTextButton("", textButtonStyle, image, isNewGame[index] ? 3 : 1f);
        playButton.setImagePadding(isNewGame[index] ? 70f : 13f);
        playButton.setImageTopPadding(2f);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.stop();
                clickSound.setLooping(false);
                clickSound.play();
                if (isNewGame[index]) {
                    isCreateNewGameDialogOpen = true;
                    createNewGameScreen = new CreateNewGameScreen(stage.getViewport(), (SpriteBatch) stage.getBatch(), index);
                } else {
                    LevelScreen levelScreen = Assets.getInstance().gson.fromJson(
                        Gdx.files.local("saves/" + index + ".json").reader(), LevelScreen.class
                    );
                    levelScreen.init();
                    levelScreen.setLevelIndex(index);
                    Menu.getInstance().toggleMenuState(MenuState.GAME_SCREEN);
                }
            }
        });
        return playButton;
    }

    /**
     * Creates the buttons for each game slot
     * and adds them to the vertical group
     * in the play game screen
     *
     * @see #setupMenu()
     */
    public void createButtons() {
        AlignableImageTextButton[] gameButtons = new AlignableImageTextButton[3];

        Label.LabelStyle style = new Label.LabelStyle();
        style.font = font;
        style.fontColor = new Color(0xE0E0E0FF);

        Table buttonTable = new Table();

        /// Create buttons for each game slot
        for (int i = 0; i < 3; i++) {
            /// Create game image
            Image gameImage;
            if (isNewGame[i]) {
                gameImage = new Image(new TextureRegionDrawable(newGameIconRegion));
            } else {
                /// Load game screenshot
                /// if it does not exist, create a black as a placeholder
                Texture texture;
                if (Gdx.files.local("saves/" + i + ".png").exists()) {
                    texture = new Texture(Gdx.files.local("saves/" + i + ".png"));
                } else {
                    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                    pixmap.setColor(Color.BLACK);
                    pixmap.fill();
                    texture = new Texture(pixmap);
                    pixmap.dispose();
                }
                gameImage = new Image(new TextureRegionDrawable(texture).tint(Color.GRAY));
            }

            /// Create game button
            gameButtons[i] = createPlayButton(gameImage, i);
            if (!isNewGame[i]) {
                gameButtons[i].updateImage(124 * 1.5f, 100 * 1.5f);
            }

            Table gameTable = new Table();

            /// Add game time label
            Label nameLabel = new Label(gameTime[i], style);
            nameLabel.setTouchable(Touchable.disabled);
            nameLabel.setAlignment(Align.center);
            gameTable.add(nameLabel).row();

            /// Add button
            gameTable.add(gameButtons[i])
                .width(124 * 1.7f)
                .height(100 * 1.7f)
                .pad(12 * 1.7f, 12 * 1.7f, 6 * 1.7f, 12 * 1.7f)
                .row();

            /// Create and add delete button
            AlignableImageTextButton deleteButton = createDeleteButton(i);
            if (isNewGame[i]) {
                deleteButton.setDisabled(true);
                deleteButton.setVisible(false);
            }
            gameTable.add(deleteButton).width(48).height(48);

            buttonTable.add(gameTable);
        }

        verticalGroup.addActor(buttonTable);

        /// Create exit button
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

        AlignableImageTextButton exitButton = new AlignableImageTextButton("", textButtonStyle, new Image(exitIconRegion), 1.5f);
        exitButton.setImagePadding(8f);
        exitButton.setImageTopPadding(2f);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.stop();
                clickSound.setLooping(false);
                clickSound.play();
                Menu.getInstance().toggleMenuState(MenuState.MAIN_MENU);
            }
        });

        Table exitTable = new Table();
        exitTable.add(exitButton).width(44).height(44).padRight(730).padBottom(10);

        verticalGroup.addActor(exitTable);
    }

    /**
     * Updates the position of the container
     * to the center of the screen.
     */
    public void updateContainerPosition() {
        container.setPosition(stage.getViewport().getWorldWidth() / 2f - container.getWidth() / 2, stage.getViewport().getWorldHeight() / 2f - container.getHeight() / 2);
    }

    /**
     * Updates the screen by clearing the stage
     * and setting up the menu again.
     */
    public void updateScreen() {
        stage.clear();
        setupMenu();
    }

    @Override
    public void render(float delta) {
        Gdx.input.setInputProcessor(stage);
        stage.act();
        stage.draw();
        if (isCreateNewGameDialogOpen) {
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
    public void show() {
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
        font.dispose();
        createNewGameScreen.dispose();
        generator.dispose();
    }

    /**
     * Creates a new game screen for the play game screen.</br>
     * The player can enter a seed or choose a file to load maze properties.</br>
     * The player can then create a new game with the seed or load the maze properties from the file.
     * The player can also exit the screen.
     *
     * @see PlayGameScreen
     */
    private static class CreateNewGameScreen {
        private final Stage stage;
        private final int gameIndex;
        private final FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local("assets/font/YosterIslandRegular-VqMe.ttf"));
        private final FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        private BitmapFont font;
        private TextureRegion smallButtonPressedRegion;
        private TextureRegion smallButtonReleasedRegion;
        private TextureRegion containerRegion;
        private TextureRegion cursorRegion;
        private TextureRegion textFieldRegion;
        private TextureRegion dropDownMenuRegion;
        private TextureRegion exitIconRegion;
        private FileHandle[] propertiesFiles;
        private int seed;

        /**
         * Creates the stage.</br>
         * Loads the textures and sets up the menu.
         *
         * @param viewport Viewport
         * @param batch    SpriteBatch
         * @param index    The index of the game slot
         */
        public CreateNewGameScreen(Viewport viewport, SpriteBatch batch, int index) {
            this.stage = new Stage(viewport, batch);
            this.gameIndex = index;

            loadTextures();
            setupMenu();

            generator.dispose();
        }

        /**
         * Load all textures for the create new game screen
         *
         * @see #setupMenu()
         */
        private void loadTextures() {
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

        /**
         * Creates all the UI elements for the CreateNewGameScreen
         *
         * @see #CreateNewGameScreen(Viewport, SpriteBatch, int)
         */
        private void setupMenu() {
            fontParameter.size = 30;
            fontParameter.color = new Color(0xE0E0E0FF);
            font = generator.generateFont(fontParameter);

            VerticalGroup verticalGroup = new VerticalGroup();
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

            /// Create the seed label
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

            /// Create the textField for the seed
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

            /// Load all properties files
            propertiesFiles = Gdx.files.local("assets/mazeProperties").list();
            String[] fileNames = new String[propertiesFiles.length + 1];
            fileNames[0] = "Select file";

            /// Sort the properties files by level
            Arrays.sort(propertiesFiles, (a, b) -> {
                String numA = a.name().replaceAll("[^0-9]", "");
                String numB = b.name().replaceAll("[^0-9]", "");
                try {
                    int levelA = Integer.parseInt(numA);
                    int levelB = Integer.parseInt(numB);
                    return Integer.compare(levelA, levelB);
                } catch (NumberFormatException e) {
                    return a.name().compareTo(b.name());
                }
            });

            for (int i = 1; i < propertiesFiles.length + 1; i++) {
                fileNames[i] = propertiesFiles[i - 1].name().replace(".properties", "");
            }

            selectBox.setItems(fileNames);

            selectBox.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    PlayGameScreen.getInstance().clickSound.stop();
                    PlayGameScreen.getInstance().clickSound.setLooping(false);
                    PlayGameScreen.getInstance().clickSound.play();
                    if (selectBox.getSelected().equals("Select file")) {
                        textField.setDisabled(false);
                        textField.setText(textField.getText().isEmpty() ? "" : textField.getText());
                    } else {
                        textField.setText(textField.getText().isEmpty() ? "" : textField.getText());
                        textField.setDisabled(true);
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

            /// Create the create button
            TextButton createButton = new TextButton("Create", textButtonStyle);
            createButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    PlayGameScreen.getInstance().clickSound.stop();
                    PlayGameScreen.getInstance().clickSound.setLooping(false);
                    PlayGameScreen.getInstance().clickSound.play();
                    if (selectBox.getSelected().equals("Select file")) {
                        /// Load maze from seed
                        if (!textField.getText().isEmpty()) {
                            seed = textField.getText().hashCode();
                        }
                        new LevelScreen(seed);
                        LevelScreen.getInstance().setLevelIndex(gameIndex);
                        Menu.getInstance().toggleMenuState(MenuState.GAME_SCREEN, true);
                        LevelScreen.getInstance().render(0);
                        LevelScreen.getInstance().saveGame();
                    } else {
                        /// Load maze from properties file
                        FileHandle selectedFile = propertiesFiles[selectBox.getSelectedIndex() - 1];
                        try {
                            new LevelScreen(new MazeGenerator(new FileReader(selectedFile.file())));
                            LevelScreen.getInstance().setLevelIndex(gameIndex);
                            Menu.getInstance().toggleMenuState(MenuState.GAME_SCREEN, true);
                            LevelScreen.getInstance().render(0);
                            LevelScreen.getInstance().saveGame();
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    PlayGameScreen.getInstance().isNewGame[gameIndex] = false;
                    PlayGameScreen.getInstance().isCreateNewGameDialogOpen = false;
                    PlayGameScreen.getInstance().updateScreen();
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


            /// Create exit button
            AlignableImageTextButton exitButton = new AlignableImageTextButton("", imageButtonStyle, new Image(new TextureRegionDrawable(exitIconRegion)), 1.5f);
            exitButton.setImagePadding(8f);
            exitButton.setImageTopPadding(2f);
            exitButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    PlayGameScreen.getInstance().clickSound.stop();
                    PlayGameScreen.getInstance().clickSound.setLooping(false);
                    PlayGameScreen.getInstance().clickSound.play();
                    PlayGameScreen.getInstance().isCreateNewGameDialogOpen = false;
                }
            });

            Table exitTable = new Table();
            exitTable.add(exitButton).width(44).height(44).padRight(340).padBottom(10);

            verticalGroup.addActor(exitTable);

            Container<VerticalGroup> container = new Container<>(verticalGroup);
            container.setBackground(new TextureRegionDrawable(containerRegion));
            container.setSize(280 * 1.7f, 240 * 1.7f);
            container.setPosition(stage.getViewport().getWorldWidth() / 2f - container.getWidth() / 2, stage.getViewport().getWorldHeight() / 2f - container.getHeight() / 2);
            container.align(Align.top);

            /// Add everything to the stage
            stage.addActor(container);
        }

        /**
         * Creates a select box for the CreateNewGameScreen
         *
         * @return {@link SelectBox}
         */
        private @NotNull SelectBox<String> createSelectBox() {
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

        /**
         * Renders the stage
         */
        public void render() {
            Gdx.input.setInputProcessor(stage);
            stage.act();
            stage.draw();
        }

        /**
         * Disposes the stage and font
         */
        public void dispose() {
            stage.dispose();
            font.dispose();
        }
    }
}
