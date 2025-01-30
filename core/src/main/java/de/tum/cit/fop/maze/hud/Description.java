package de.tum.cit.fop.maze.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.level.LevelScreen;

/**
 * The type Description.
 */
public class Description {

    private final Table descriptionTable = new Table();
    private final Container<Table> descriptionContainer = new Container<>(descriptionTable);
    private final Label itemDescription;
    private final float padding = 10;

    /**
     * Instantiates a new Description.
     *
     * @param labelStyle the label style
     * @param stage      the stage
     */
    public Description(Label.LabelStyle labelStyle, Stage stage) {
        itemDescription = new Label("", labelStyle);
        descriptionContainer.align(Align.center);
        descriptionContainer.setBackground(Utils.getColoredDrawable(200, 200,
            new Color(0, 0, 0, 0.7f)));
        stage.addActor(descriptionContainer);
        descriptionContainer.setVisible(false);
    }

    /**
     * Sets item description.
     *
     * @param description the description
     * @param stage       the stage
     */
    public void setItemDescription(String description, Stage stage) {

        if (descriptionTable.getChildren().size > 0) {
            descriptionTable.clear();
        }
        itemDescription.setText(description);
        itemDescription.setWrap(true);
        itemDescription.setWidth(300);
        descriptionContainer.setVisible(true);
        itemDescription.setBounds(0, 0, itemDescription.getWidth(),
            itemDescription.getPrefHeight());
        itemDescription.setAlignment(Align.center);
        descriptionTable.add(itemDescription).width(itemDescription.getWidth())
            .height(itemDescription.getPrefHeight())
            .pad(padding);
        descriptionTable.setSize(itemDescription.getWidth(), itemDescription.getPrefHeight());

        updateDescriptionPosition(stage);

        LevelScreen.getInstance().hud.hideInventory();
//        stage.addActor(descriptionContainer);
    }

    /**
     * Gets item description.
     *
     * @return the item description
     */
    public String getItemDescription() {
        return itemDescription.getText().toString();
    }

    /**
     * Update description position.
     *
     * @param stage the stage
     */
    public void updateDescriptionPosition(Stage stage) {

        float containerWidth = descriptionTable.getWidth() + 15;
        float containerHeight = descriptionTable.getHeight() + 15;
        descriptionContainer.setSize(containerWidth, containerHeight);

        float containerX = stage.getViewport().getWorldWidth() - containerWidth - padding;
        float containerY = stage.getViewport().getWorldHeight() - containerHeight - padding;
        descriptionContainer.setPosition(containerX, containerY);
    }

    /**
     * Delete description.
     */
    public void deleteDescription() {
        descriptionTable.clear();
        descriptionContainer.setVisible(false);
        LevelScreen.getInstance().hud.showInventory();
    }

    /**
     * Is description set boolean.
     *
     * @return the boolean
     */
    public boolean isDescriptionSet() {
        return !descriptionTable.getChildren().isEmpty();
    }

    /**
     * Dispose.
     */
    public void dispose() {

        descriptionContainer.clear();
        descriptionTable.clear();
        descriptionTable.remove();
        descriptionContainer.remove();
    }
}
