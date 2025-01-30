package de.tum.cit.fop.maze.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.entities.tile.Collectable;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.essentials.Assets;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.List;

import static de.tum.cit.fop.maze.Globals.DEFAULT_SCREEN_HEIGHT_WINDOWED;
import static de.tum.cit.fop.maze.Globals.DEFAULT_SCREEN_WIDTH_WINDOWED;

/**
 * The UI manager for the game.
 */
public class HUD {
    private final Stage stage;
    private final SpriteBatch spriteBatch;
    private final ExitArrow exitArrow;
    /**
     * The Hit elapsed time.
     */
    private final CoinsAndKeys coinsAndKeys;
    private final HpBar healthBar;
    private final StaminaBar staminaBar;
    private final TimeAndScore timeAndScore;
    private final Inventory inventory;
    private final Description description;
    private final ItemFrame itemFrame;


    /**
     * Instantiates a new Hud.
     *
     * @param player the player
     */
    public HUD(Player player) {
        this.spriteBatch = LevelScreen.getInstance().batch;
        this.stage =
            new Stage(
                new ExtendViewport(
                    (float) DEFAULT_SCREEN_WIDTH_WINDOWED, (float) DEFAULT_SCREEN_HEIGHT_WINDOWED, new OrthographicCamera()
                ), spriteBatch
            );

        Gdx.input.setInputProcessor(this.stage);


        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("font/YosterIslandRegular-VqMe.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 27;
        parameter.color = new Color(0xE0E0E0FF);
        parameter.borderWidth = 1;
        parameter.borderColor = new Color(0x000000FF);
        Label.LabelStyle timeAndScoreStyle = new Label.LabelStyle();
        timeAndScoreStyle.font = generator.generateFont(parameter);


        Label.LabelStyle coinsAndKeysLabelStyle = new Label.LabelStyle();
        coinsAndKeysLabelStyle.font = generator.generateFont(parameter);
        FreeTypeFontGenerator labelFontGenerator = new FreeTypeFontGenerator(
            Gdx.files.internal("font/VT323-Regular.ttf"));


        Label.LabelStyle descriptionStyle = new Label.LabelStyle();
        parameter.size = 25; // font size
        parameter.borderWidth = 0;
        parameter.color = new Color(1, 1, 1, 0.7f);
        parameter.gamma = 4f;
        descriptionStyle.font = labelFontGenerator.generateFont(parameter);
        description = new Description(descriptionStyle, stage);


        timeAndScore = new TimeAndScore(LevelScreen.getInstance().getLevelData(), timeAndScoreStyle, stage);


        Label.LabelStyle inventoryStyle = new Label.LabelStyle();
        int invFontSize = 17;
        inventoryStyle.font = createFont(invFontSize);
        TextureAtlas inventoryAtlas = Assets.getInstance().getAssetManager()
            .get("assets/collectables/collectables.atlas", TextureAtlas.class);
        inventory = new Inventory(inventoryAtlas, inventoryStyle, stage);
        setInventory(player.getInventory());


        TextureAtlas atlas = Assets.getInstance().getAssetManager().get("assets/hud/hud.atlas", TextureAtlas.class);


        float padding = 10f;
        healthBar = new HpBar(padding, stage.getViewport().getWorldHeight() - padding, atlas, stage);
        healthBar.createHpBar(player.getHealth(), player.getMaxHealth());


        staminaBar = new StaminaBar(player.getMaxStamina(),
            padding, healthBar.getY() - 10, atlas, healthBar.getWidth() - 10, 20);
        coinsAndKeys = new CoinsAndKeys(stage, inventoryAtlas, coinsAndKeysLabelStyle,
            padding, staminaBar.getY() - 5, player.getGold(), player.hasKey());


        exitArrow = new ExitArrow(atlas, stage);

        itemFrame = new ItemFrame(stage.getViewport().getWorldWidth() / 2, padding, atlas);
        if (player.getActiveItem() != null) {
            itemFrame.setActiveItem();
        }
    }

    /**
     * Hide inventory.
     */
    public void hideInventory() {
        inventory.hideInventory();
    }

    /**
     * Show inventory.
     */
    public void showInventory() {
        inventory.showInventory();
    }

    /**
     * Gets item description.
     *
     * @return the item description
     */
    public String getItemDescription() {
        return description.getItemDescription();
    }

    /**
     * Sets item description.
     *
     * @param description the description
     */
    public void setItemDescription(String description) {
        this.description.setItemDescription(description, stage);
    }

    private void updateDescriptionPosition() {

        description.updateDescriptionPosition(stage);
    }

    /**
     * Delete description.
     */
    public void deleteDescription() {
        description.deleteDescription();
    }

    /**
     * Heal.
     *
     * @param value the value
     */
    public void heal(int value) {
        healthBar.heal(value);
    }

    private void updateInventoryPosition() {
        inventory.updateInventoryPosition();
    }

    /**
     * Add key.
     */
    public void addKey() {
        coinsAndKeys.pickUpKey();
    }


    /**
     * Add active item.
     */
    public void addActiveItem() {
        itemFrame.setActiveItem();
    }


    /**
     * Add item to inventory.
     *
     * @param collectable the collectable
     */
    public void addItemToInventory(Collectable collectable) {
        inventory.addItemToInventory(collectable);
    }

    private void updateTimeAndScorePosition() {
        timeAndScore.updateLabelTablePosition();
    }

    /**
     * Take dmg.
     *
     * @param receivedDmg the received dmg
     */
    public void takeDmg(int receivedDmg) {
        if (receivedDmg <= 0) {
            return;
        }
        healthBar.takeDmg(receivedDmg);
    }

    public boolean isDescriptionSet() {
        return description.isDescriptionSet();
    }

    private void updateLabels() {
        timeAndScore.updateLabels();
    }

    private BitmapFont createFont(int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.local("font/YosterIslandRegular-VqMe.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1;
        parameter.borderColor = new Color(0x000000FF);
        return generator.generateFont(parameter);
    }



    /**
     * Render.
     *
     * @param deltaTime the delta time
     */
    public void render(float deltaTime) {
        updateLabels();
        stage.act();
        stage.draw();
        inventory.render(deltaTime);
        stage.getBatch().begin();
        healthBar.render(deltaTime, spriteBatch);
        staminaBar.render(deltaTime, spriteBatch);

        /// find closest exit
        AbsolutePoint playerPos = LevelScreen.getInstance().player.getPosition();
        AbsolutePoint closest = null;
        for (AbsolutePoint exitPoint : LevelScreen.getInstance().map.getExitPositions()) {
            if (closest == null) closest = exitPoint;
            if (closest.distance(playerPos) > exitPoint.distance(playerPos)) {
                closest = exitPoint;
            }
        }
        if (closest == null) {
            throw new IllegalStateException("There is no exit on the map");
        }
        exitArrow.drawExitArrow(spriteBatch, playerPos.angle(closest));
        itemFrame.render(spriteBatch, deltaTime);
        stage.getBatch().end();
    }


    /**
     * Show.
     */
    public void show() {
        Gdx.input.setInputProcessor(this.stage);
    }

    /**
     * Resize.
     */
    public void resize() {
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        updateTimeAndScorePosition();
        updateDescriptionPosition();
        updateInventoryPosition();
        exitArrow.updateArrowPosition(stage);
    }

    /**
     * Gets formatted time.
     *
     * @return the formatted time
     */
    public String getFormattedTime() {
        return timeAndScore.formatedTime().replace("Time: ", "");
    }

    /**
     * Sets health bar.
     *
     * @param health    the health
     * @param maxHealth the max health
     */
    public void setHealthBar(int health, int maxHealth) {
        healthBar.setHealthBar(health, maxHealth);
    }

    /**
     * Gets health.
     *
     * @return the health
     */
    public int getHealth() {
        return healthBar.getHealth();
    }

    /**
     * Remove item from inventory boolean.
     *
     * @param collectable the collectable
     * @return the boolean
     */
    public boolean removeItemFromInventory(Collectable collectable) {
        return inventory.removeItemFromInventory(collectable);
    }

    /**
     * Update inventory.
     *
     * @param inventory the inventory
     */
    public void updateInventory(List<Collectable> inventory) {
        this.inventory.clearInventory();
        for (Collectable collectable : inventory) {
            addItemToInventory(collectable);
        }
    }


    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(List<Collectable> inventory) {
        this.inventory.clearInventory();
        for (Collectable collectable : inventory) {
            addItemToInventory(collectable);
        }
    }
}

