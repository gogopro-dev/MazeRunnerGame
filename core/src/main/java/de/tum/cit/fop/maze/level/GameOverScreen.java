package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import de.tum.cit.fop.maze.Assets;
import de.tum.cit.fop.maze.essentials.AlignableImageTextButton;
import de.tum.cit.fop.maze.menu.Menu;
import de.tum.cit.fop.maze.menu.MenuState;

/**
 * Class for the UI of the game over screen.
 */
public class GameOverScreen implements Screen {
    private TextureRegion gameOverBackgroundRegion;
    private TextureRegion smallButtonPressedRegion;
    private TextureRegion smallButtonReleasedRegion;
    private TextureRegion exitIconRegion;
    private Label timePlayedLabel;
    private Label scoreLabel;
    private Label gameOverLabel;
    private Table inventoryTable;
    private Table textInventoryTable;
    private Stack inventoryStack;
    private final Stage stage;
    private final ShapeRenderer shapeRenderer;
    private final Table screenTable;
    private static GameOverScreen instance;

    /**
     * @return the singleton instance of the {@link GameOverScreen} class
     */
    public static synchronized GameOverScreen getInstance() {
        if (instance == null) {
            System.out.println("Creating new GameOverScreen");
            return new GameOverScreen(new Table(), new Table());
        }
        return instance;
    }

    /**
     * Private constructor to prevent instantiation from outside the class</br>
     * This constructor initializes the {@link Stage} object and the {@link ShapeRenderer} object.
     * @param inventoryTable Table
     * @param textInventoryTable Table
     */
    private GameOverScreen(Table inventoryTable, Table textInventoryTable) {
        instance = this;
        this.inventoryTable = inventoryTable;
        this.textInventoryTable = textInventoryTable;
        stage = new Stage(new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        shapeRenderer = new ShapeRenderer();
        screenTable = new Table();

        loadTextures();

        setupScreen();
        deleteGame();
    }

    /**
     * Sets up the game over screen
     * Creates the labels and buttons for the screen
     * and adds them to the stage
     */
    private void setupScreen() {
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

        //create ninepatch from game over background
        NinePatch ninePatch = new NinePatch(new TextureRegion(gameOverBackgroundRegion), 1, 1, 1, 1);


        /// Create table for pause menu
        screenTable.setBackground(new NinePatchDrawable(ninePatch));
        screenTable.setSize(304*1.6f, 224*1.6f + 80); // Adjust size as needed
        screenTable.setPosition(
            Gdx.graphics.getWidth() / 2f - screenTable.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f - screenTable.getHeight() / 2f - 40
        );
        screenTable.center().top();

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        parameter.size = 35;
        parameter.color = new Color(0xE0E0E0FF);
        labelStyle.font = generator.generateFont(parameter);
        gameOverLabel = new Label("", labelStyle);

        labelStyle = new Label.LabelStyle();
        parameter.size = 26;
        parameter.color = new Color(0xE0E0E0FF);
        labelStyle.font = generator.generateFont(parameter);
        timePlayedLabel = new Label("", labelStyle);

        scoreLabel = new Label("", labelStyle);

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
                Menu.getInstance().toggleMenuState(MenuState.MAIN_MENU, true);
            }
        });

        /// Create label for inventory
        labelStyle = new Label.LabelStyle();
        parameter.size = 22;
        parameter.color = new Color(0xE0E0E077);
        labelStyle.font = generator.generateFont(parameter);
        Label inventoryLabel = new Label("Your game ended\nwith these items:", labelStyle);

        screenTable.add(gameOverLabel).padTop(10f).row();
        screenTable.add(timePlayedLabel).padTop(20f).row();
        screenTable.add(scoreLabel).padTop(20f).row();
        screenTable.add(exitButton).padTop(20f).width(224f * 1.2f).height(48f * 1.2f).row();
        screenTable.add(inventoryLabel).padTop(10f).row();

        inventoryStack = new Stack();
        screenTable.add(inventoryStack).padTop(10f).row();

        stage.addActor(screenTable);
    }

    /**
     * Loads the textures for the game over screen
     * from the asset manager
     * and assigns them to the corresponding variables
     *
     * @see Assets
     */
    private void loadTextures() {
        TextureAtlas menuAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu.atlas", TextureAtlas.class);
        TextureAtlas menuIconsAtlas = Assets.getInstance().getAssetManager().get("assets/menu/menu_icons.atlas", TextureAtlas.class);

        gameOverBackgroundRegion = menuAtlas.findRegion("pause_menu");

        smallButtonPressedRegion = menuAtlas.findRegion("small_button_pressed");
        smallButtonReleasedRegion = menuAtlas.findRegion("small_button_released");

        exitIconRegion = menuIconsAtlas.findRegion("exit");
    }

    /**
     * Updates the viewport of the stage
     * and the position of the screen table
     * to the center of the screen
     */
    public void updateViewport() {
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

    /**
     * Draws the inventory of the player
     * on the game over screen
     * @param inventoryTable
     * @param textInventoryTable
     */
    public void drawInventory(Table inventoryTable, Table textInventoryTable) {
        this.inventoryTable = inventoryTable;
        this.textInventoryTable = textInventoryTable;
        inventoryStack.add(this.inventoryTable);
        inventoryStack.add(this.textInventoryTable);
    }

    /**
     * Deletes the game save file
     * after the game is over
     */
    private void deleteGame(){
        int index = LevelScreen.getInstance().getLevelIndex();
        Gdx.files.local("saves/" + index + ".json").delete();
        Gdx.files.local("saves/" + index + ".png").delete();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (gameOverLabel.getText().isEmpty()) return;
        Gdx.input.setInputProcessor(stage);
        // Draw semi-transparent grey overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.5f);
        shapeRenderer.rect(0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // Draw game over screen
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        shapeRenderer.dispose();
    }

    public void setHasWon(boolean hasWon) {
        gameOverLabel.setText(hasWon ? "Victory!" : "Defeat");
    }

    public void setTimePlayed(String timePlayed) {
        timePlayedLabel.setText("Time played: " + timePlayed);
    }

    public void setScore(int score) {
        scoreLabel.setText("Score: " + score);
    }
}
