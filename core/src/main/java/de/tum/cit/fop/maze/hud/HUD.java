package de.tum.cit.fop.maze.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import de.tum.cit.fop.maze.ActiveItem;
import de.tum.cit.fop.maze.entities.CoinsAndKeys;
import de.tum.cit.fop.maze.entities.Description;
import de.tum.cit.fop.maze.entities.ExitArrow;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.entities.tile.AbilityBorder;
import de.tum.cit.fop.maze.entities.tile.Collectable;
import de.tum.cit.fop.maze.entities.tile.CollectableAttributes;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.*;
import java.util.List;

import static de.tum.cit.fop.maze.Globals.*;

public class HUD {
    private final Stage stage;
    private final SpriteBatch spriteBatch;
    private TextureAtlas atlas;
    private Label.LabelStyle descriptionStyle;
    public final Table spriteInventory = new Table();
    public final Table textInventory = new Table();
    public final Table descriptionTable = new Table();
    public final Container<Table> descriptionContainer = new Container<>(descriptionTable);
    private int invFontSize = 17;
    private final TextureAtlas inventoryAtlas;
    private final ExitArrow exitArrow;
    private Map<String, Animation<TextureRegion>> animations;
    float hitElapsedTime = 0;
    final float padding = 10f;
    private final CoinsAndKeys coinsAndKeys;
    private final HpBar healthBar;
    private final StaminaBar staminaBar;
    private final TimeAndScore timeAndScore;
    private final Inventory inventory;
    private final Label.LabelStyle inventoryStyle;
    private final Description description;
    private final AbilityBorder abilityBorder;


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
        descriptionStyle = new Label.LabelStyle();
        parameter.size = 25; // font size
        parameter.borderWidth = 0;
        parameter.color = new Color(1, 1, 1, 0.7f);
        parameter.gamma = 4f;
        descriptionStyle.font = labelFontGenerator.generateFont(parameter);
        //TODO change background to png
        description = new Description(descriptionStyle, stage);


        generator.dispose();
        timeAndScore = new TimeAndScore(LevelScreen.getInstance().getLevelData(), timeAndScoreStyle, stage);


        inventoryStyle = new Label.LabelStyle();
        inventoryStyle.font = createFont(invFontSize, Color.WHITE);
        inventoryAtlas = new TextureAtlas(Gdx.files.local("assets/temporary/collectables/collectables.atlas"));
        inventory = new Inventory(inventoryAtlas, inventoryStyle, stage);


        atlas = new TextureAtlas(Gdx.files.local("assets/temporary/HUDv2/HUDv2.atlas"));


        healthBar = new HpBar(padding, stage.getViewport().getWorldHeight() - padding, atlas, stage);
        float x = stage.getViewport().getWorldHeight() - padding;
        healthBar.createHpBar(player.getHealth(), player.getMaxHealth());


        staminaBar = new StaminaBar(player.getMaxStamina(),
            padding, healthBar.getY() - 10, atlas, healthBar.getWidth() - 10, 20);


        coinsAndKeys = new CoinsAndKeys(stage, inventoryAtlas,
            coinsAndKeysLabelStyle, padding, staminaBar.getY() - 5);


        exitArrow = new ExitArrow(atlas, stage);

        abilityBorder = new AbilityBorder(stage.getViewport().getWorldWidth()/2, padding, atlas);
    }

    public void hideInventory() {
        inventory.hideInventory();
    }

    public void showInventory() {
        inventory.showInventory();
    }


    public void setItemDescription(String description) {
        this.description.setItemDescription(description, stage);
    }

    public String getItemDescription() {
        return description.getItemDescription();
    }

    private void updateDescriptionPosition() {

        description.updateDescriptionPosition(stage);
    }

    public void deleteDescription() {
        description.deleteDescription();
    }

    public void heal(int value) {
        healthBar.heal(value);
    }

    private void updateInventoryPosition() {
        inventory.updateInventoryPosition();
    }

    public void addKey() {
        coinsAndKeys.pickUpKey();
    }

    public void addCoin(int amount) {
        coinsAndKeys.pickUpCoin(amount);
    }

    public void addActiveItem() {
        abilityBorder.addActiveItem();
    }


    public void addItemToInventory(Collectable collectable) {
//        Collectable.CollectableType collectableType = collectable.getType();
//        switch (collectableType) {
//            case HEART:
//                heal(collectable.getCollectableAttributes().getImmediateHealing());
//                break;
//            case KEY:
//                coinsAndKeys.pickUpKey();
//                break;
//            case GOLD_COIN:
//                coinsAndKeys.pickUpCoin(collectable.getCollectableAttributes().getImmediateCoins());
//                break;
//            default:
        inventory.addItemToInventory(collectable);
//                break;

//        }


    }
    private void updateTimeAndScorePosition() {
        timeAndScore.updateLabelTablePosition();
    }

    public void takeDmg(int receivedDmg) {
        if (receivedDmg <= 0) {
            return;
        }
        hitElapsedTime = 0f;
        healthBar.takeDmg(receivedDmg);
    }

    public boolean isDescriptionSet() {
        return description.isDescriptionSet();
    }

    private void updateLabels() {
        timeAndScore.updateLabels();
    }
//        time.setText(getLabelTime(elapsedTime));
//        scoreLabel.setText(getLabelScore(currentScore - (int) elapsedTime));

    private BitmapFont createFont(int size, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.local("font/YosterIslandRegular-VqMe.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.borderWidth = 1;
        parameter.borderColor = new Color(0x000000FF);
        return generator.generateFont(parameter);
    }

    public void dispose() {
        atlas.dispose();
        stage.dispose();
    }


    public void render(float deltaTime) {
        updateLabels();
        stage.act();
        stage.draw();
        inventory.render(deltaTime);
        stage.getBatch().begin();
        healthBar.render(deltaTime, spriteBatch);
        staminaBar.render(deltaTime, spriteBatch);
        exitArrow.drawExitArrow(spriteBatch, LevelScreen.getInstance().player.getPosition().angle(
            LevelScreen.getInstance().map.getExitPosition()
        ));
        abilityBorder.render(spriteBatch, deltaTime);
        stage.getBatch().end();
    }


    public void show() {
        Gdx.input.setInputProcessor(this.stage);
    }

    public void resize() {
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        updateTimeAndScorePosition();
        updateDescriptionPosition();
        updateInventoryPosition();
        exitArrow.updateArrowPosition(stage);
    }

    public String getFormattedTime() {
        return timeAndScore.formatedTime().replace("Time: ", "");
    }

    public void setHealthBar(int health, int maxHealth) {
        healthBar.setHealthBar(health, maxHealth);
    }

    public int getHealth() {
        return healthBar.getHealth();
    }

    public boolean removeItemFromInventory(Collectable collectable) {
        return inventory.removeItemFromInventory(collectable);
    }

    public void updateInventory(List<Collectable> inventory) {
        this.inventory.clearInventory();
        for (Collectable collectable : inventory) {
            addItemToInventory(collectable);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }
}

