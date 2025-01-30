package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import de.tum.cit.fop.maze.level.LevelScreen;


public class CoinsAndKeys {
    private final Table coinsAndKeysTable = new Table();
    private final Label coins;
    private final float x;
    private final float y;
    private final Drawable keysIcon;
    private final Drawable coinsIcon;
    public CoinsAndKeys(Stage stage, TextureAtlas inventoryAtlas, Label.LabelStyle coinsAndKeysLabelStyle,
                        float x, float y, int coins, boolean hasKey) {

        this.coins = new Label(String.format(": " + coins), coinsAndKeysLabelStyle);
        this.x = x;
        this.y = y;
        coinsIcon = new TextureRegionDrawable(inventoryAtlas.findRegion("coin"));
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
        if (hasKey){
            pickUpKey();
        }
    }
    public void pickUpCoin(int value){

        coins.setText(": " + LevelScreen.getInstance().player.getGold());
        coinsAndKeysTable.setPosition(x + coinsAndKeysTable.getPrefWidth() / 2,
            y - coinsAndKeysTable.getPrefHeight() / 2);

    }

    public void pickUpKey() {
//  .width(keyWidth).height(keyHeight)
        coinsAndKeysTable.add(new Image(keysIcon)).width(keysIcon.getMinWidth()).height(keysIcon.getMinHeight());
        coinsAndKeysTable.setPosition(x + coinsAndKeysTable.getPrefWidth() / 2,
            y - coinsAndKeysTable.getPrefHeight() / 2);
    }

    public void dispose(){
        coinsAndKeysTable.clear();
        coinsAndKeysTable.remove();
    }

}
