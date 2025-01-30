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

/**
 * Inventory where all picked up Items are shown.
 */
public class Inventory {
    /**
     * The Sprite inventory.
     */
    public final Table spriteInventory = new Table();
    /**
     * The Text inventory.
     */
    public final Table textInventory = new Table();
    /**
     * The Label style.
     */
    public final Label.LabelStyle labelStyle;
    /**
     * The Stage.
     */
    public final Stage stage;
    private final int inventoryRows = 2;
    private final int inventoryCols = 5;
    private final int sizeOfInvIcon = 40;
    private final int spacingBetweenIcons = 10;
    private final float inventoryWidth = inventoryCols * sizeOfInvIcon +
        (inventoryCols - 1) * spacingBetweenIcons;
    private final float inventoryHeight = inventoryRows * sizeOfInvIcon +
        (inventoryRows - 1) * spacingBetweenIcons;
    private final TextureAtlas inventoryAtlas;
    private final Map<String, Label> labelInfo = new HashMap<>();
    private final Map<String, Image> imageInfo = new HashMap<>();

    /**
     * Instantiates a new Inventory.
     *
     * @param inventoryAtlas the inventory atlas
     * @param labelStyle     the label style
     * @param stage          the stage
     */
    public Inventory(TextureAtlas inventoryAtlas, Label.LabelStyle labelStyle, Stage stage) {

        this.inventoryAtlas = inventoryAtlas;
        spriteInventory.setSize(inventoryWidth, inventoryHeight);
        textInventory.setSize(inventoryWidth, inventoryHeight);
        this.labelStyle = labelStyle;
        this.stage = stage;
        updateInventoryPosition();

    }

    /**
     * Add item to inventory.
     *
     * @param collectable the collectable
     */
    public void addItemToInventory(Collectable collectable) {
        Collectable.CollectableType collectableType = collectable.getType();
        String textureName = collectable.getCollectableAttributes().textureName;
        if (labelInfo.get(collectableType.name()) == null) {
            Drawable drawable = new TextureRegionDrawable(inventoryAtlas.findRegion(textureName));
            drawable.setMinWidth(sizeOfInvIcon);
            drawable.setMinHeight(sizeOfInvIcon);
            Image image = new Image(drawable);

            spriteInventory.add(image).width(sizeOfInvIcon).height(sizeOfInvIcon).center()
                .padRight(spacingBetweenIcons);

            Label label = new Label("x1", labelStyle);
            textInventory.add(label).width(sizeOfInvIcon).height(sizeOfInvIcon).center()
                .padRight(spacingBetweenIcons);


            labelInfo.put(collectableType.name(), label);
            imageInfo.put(collectableType.name(), image);

            if (spriteInventory.getChildren().size % inventoryCols == 0) {
                spriteInventory.row().padTop(spacingBetweenIcons);
                textInventory.row().padTop(spacingBetweenIcons);
            }
            ;
            return;
        }
        Label label = labelInfo.get(collectableType.name());
        String[] text = label.getText().toString().split("x");
        int amount = Integer.parseInt(text[1]);
        amount++;
        label.setText("x" + amount);


    }

    /**
     * Update inventory position in regard to new World Width and Height.
     */
    public void updateInventoryPosition() {

        float tableOffsetX = 20;
        float tableOffsetY = 10;
        float padding = 10;

        spriteInventory.setPosition(stage.getViewport().getWorldWidth() - inventoryWidth - tableOffsetX - padding,
            stage.getViewport().getWorldHeight() - inventoryHeight - padding);
        stage.addActor(spriteInventory);
        textInventory.setPosition(stage.getViewport().getWorldWidth() - inventoryWidth - padding,
            stage.getViewport().getWorldHeight() - inventoryHeight - tableOffsetY - padding);
        stage.addActor(textInventory);
    }

    /**
     * Hide inventory.
     */
    public void hideInventory() {
        spriteInventory.setVisible(false);
        textInventory.setVisible(false);
    }

    /**
     * Render.
     *
     * @param deltaTime the delta time
     */
    public void render(float deltaTime) {
        spriteInventory.act(deltaTime);
        textInventory.act(deltaTime);
    }

    /**
     * Show inventory.
     */
    public void showInventory() {
        spriteInventory.setVisible(true);
        textInventory.setVisible(true);
    }

    /**
     * Remove item from inventory boolean.
     *
     * @param collectable the collectable
     * @return the boolean
     */
    public boolean removeItemFromInventory(Collectable collectable) {
        Collectable.CollectableType collectableType = collectable.getType();
        if (labelInfo.get(collectableType.name()) == null) {
            return false;
        }
        Label label = labelInfo.get(collectableType.name());
        String[] text = label.getText().toString().split("x");
        int amount = Integer.parseInt(text[1]);
        amount--;

        Image image = imageInfo.get(collectableType.name());

        if (amount == 0) {
            return false;
        }
        label.setText("x" + amount);
        return true;
    }

    /**
     * Clear inventory.
     */
    public void clearInventory() {
        spriteInventory.clear();
        textInventory.clear();
        labelInfo.clear();
        imageInfo.clear();
    }

    /**
     * Dispose.
     */
    public void dispose() {
        clearInventory();
        spriteInventory.remove();
        textInventory.remove();
    }
}
