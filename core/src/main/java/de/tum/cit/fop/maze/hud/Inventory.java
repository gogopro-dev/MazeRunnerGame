package de.tum.cit.fop.maze.hud;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import de.tum.cit.fop.maze.entities.tile.Collectable;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private Label itemDescription;
    private int inventoryRows = 2;
    private int inventoryCols = 5;
    private int sizeOfInvIcon = 40;
    private int invFontSize = 17;
    private int spacingBetweenIcons = 10;
    private float tableOffsetX = 20;
    private float tableOffsetY = 10;
    private final float inventoryWidth = inventoryCols * sizeOfInvIcon +
            (inventoryCols - 1) * spacingBetweenIcons;
    private final float inventoryHeight = inventoryRows * sizeOfInvIcon +
            (inventoryRows - 1) * spacingBetweenIcons;
    private final TextureAtlas inventoryAtlas;
    public final Table spriteInventory = new Table();
    public final Table textInventory = new Table();
    public final Label.LabelStyle labelStyle;
    public final Stage stage;
    private Map<String, Label> invInfo = new HashMap<>();
    private float padding = 10;

    public Inventory(TextureAtlas inventoryAtlas, Label.LabelStyle labelStyle, Stage stage) {

        this.inventoryAtlas = inventoryAtlas;
        spriteInventory.setSize(inventoryWidth, inventoryHeight);
        textInventory.setSize(inventoryWidth, inventoryHeight);
        this.labelStyle = labelStyle;
        this.stage = stage;
        updateInventoryPosition();

    }

    public void addItemToInventory(Collectable collectable) {
        Collectable.CollectableType collectableType = collectable.getType();
        String textureName = collectable.getCollectableAttributes().textureName;
        if (invInfo.get(collectableType.name()) == null) {
            Drawable drawable = new TextureRegionDrawable(inventoryAtlas.findRegion(textureName));
            drawable.setMinWidth(sizeOfInvIcon);
            drawable.setMinHeight(sizeOfInvIcon);
            spriteInventory.add(new Image(drawable)).width(sizeOfInvIcon).height(sizeOfInvIcon).center()
                    .padRight(spacingBetweenIcons);

            Label label = new Label("x1", labelStyle);
            textInventory.add(label).width(sizeOfInvIcon).height(sizeOfInvIcon).center()
                    .padRight(spacingBetweenIcons);

            invInfo.put(collectableType.name(), label);
            if (spriteInventory.getChildren().size % inventoryCols == 0) {
                spriteInventory.row().padTop(spacingBetweenIcons);
                textInventory.row().padTop(spacingBetweenIcons);
            };
            return;
        }
        Label label = invInfo.get(collectableType.name());
        String[] text = label.getText().toString().split("x");
        int amount = Integer.parseInt(text[1]);
        amount++;
        label.setText("x" + amount);


    }

    public void updateInventoryPosition() {
        spriteInventory.setPosition(stage.getViewport().getWorldWidth() - inventoryWidth - tableOffsetX - padding,
                stage.getViewport().getWorldHeight() - inventoryHeight - padding);
        stage.addActor(spriteInventory);
        textInventory.setPosition(stage.getViewport().getWorldWidth() - inventoryWidth - padding,
                stage.getViewport().getWorldHeight() - inventoryHeight - tableOffsetY - padding);
        stage.addActor(textInventory);
    }

    public void hideInventory() {
        spriteInventory.setVisible(false);
        textInventory.setVisible(false);
    }

    public void render(float deltaTime) {
        spriteInventory.act(deltaTime);
        textInventory.act(deltaTime);
    }

    public void showInventory() {
        spriteInventory.setVisible(true);
        textInventory.setVisible(true);
    }
}
