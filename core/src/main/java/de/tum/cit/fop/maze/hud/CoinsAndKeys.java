package de.tum.cit.fop.maze.hud;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import de.tum.cit.fop.maze.level.LevelScreen;


/**
 * The CoinsAndKeys class represents a visual component that displays the player's
 * collected coins and keys in the user interface. This component is constructed using
 * a table and is added to a stage for rendering.
 */
public class CoinsAndKeys {
    private final Table coinsAndKeysTable = new Table();
    private final Label coins;
    private final float x;
    private final float y;
    private final Drawable keysIcon;

    /**
     * Instantiates a new Coins and keys.
     *
     * @param stage                  the stage
     * @param inventoryAtlas         the inventory atlas
     * @param coinsAndKeysLabelStyle the coins and keys label style
     * @param x                      the x
     * @param y                      the y
     * @param coins                  the coins
     * @param hasKey                 the has key
     */
    public CoinsAndKeys(Stage stage, TextureAtlas inventoryAtlas, Label.LabelStyle coinsAndKeysLabelStyle,
                        float x, float y, int coins, boolean hasKey) {

        this.coins = new Label(String.format(": " + coins), coinsAndKeysLabelStyle);
        this.x = x;
        this.y = y;
        Drawable coinsIcon = new TextureRegionDrawable(inventoryAtlas.findRegion("coin"));
        float iconSize = 40;
        coinsIcon.setMinWidth(iconSize);
        coinsIcon.setMinHeight(iconSize);

        float keyWidth = 45;
        float keyHeight = 35;

        keysIcon = new TextureRegionDrawable(inventoryAtlas.findRegion("key", 1));
        keysIcon.setMinHeight(keyHeight);
        keysIcon.setMinWidth(keyWidth);

        coinsAndKeysTable.add(new Image(coinsIcon)).width(iconSize).height(iconSize);
        coinsAndKeysTable.add(this.coins);
        coinsAndKeysTable.row();
        coinsAndKeysTable.setPosition(x + coinsAndKeysTable.getPrefWidth() / 2,
            y - coinsAndKeysTable.getPrefHeight() / 2);
        stage.addActor(coinsAndKeysTable);
        if (hasKey) {
            pickUpKey();
        }
    }

    /**
     * Pick up coin.
     *
     * @param value the value
     */
    public void pickUpCoin(int value) {

        coins.setText(": " + LevelScreen.getInstance().player.getGold());
        coinsAndKeysTable.setPosition(x + coinsAndKeysTable.getPrefWidth() / 2,
            y - coinsAndKeysTable.getPrefHeight() / 2);

    }

    /**
     * Pick up key.
     */
    public void pickUpKey() {
//  .width(keyWidth).height(keyHeight)
        coinsAndKeysTable.add(new Image(keysIcon)).width(keysIcon.getMinWidth()).height(keysIcon.getMinHeight());
        coinsAndKeysTable.setPosition(x + coinsAndKeysTable.getPrefWidth() / 2,
            y - coinsAndKeysTable.getPrefHeight() / 2);
    }

    /**
     * Dispose.
     */
    public void dispose() {
        coinsAndKeysTable.clear();
        coinsAndKeysTable.remove();
    }

}
