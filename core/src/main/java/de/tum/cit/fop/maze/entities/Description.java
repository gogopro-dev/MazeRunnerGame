package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.level.LevelScreen;

public class Description {

    private Table descriptionTable = new Table();
    private Container<Table> descriptionContainer = new Container<>(descriptionTable);
    private Label itemDescription;
    private float padding = 10;

    public Description(Label.LabelStyle labelStyle, Stage stage) {
        itemDescription = new Label("", labelStyle);
        descriptionContainer.align(Align.center);
        descriptionContainer.setBackground(Utils.getColoredDrawable(200, 200,
            new Color(0, 0, 0, 0.7f)));
        stage.addActor(descriptionContainer);
        descriptionContainer.setVisible(false);
    }
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

    public String getItemDescription() {
        return itemDescription.getText().toString();
    }

    public void updateDescriptionPosition(Stage stage) {

        float containerWidth = descriptionTable.getWidth() + 15;
        float containerHeight = descriptionTable.getHeight() + 15;
        descriptionContainer.setSize(containerWidth, containerHeight);

        float containerX = stage.getViewport().getWorldWidth() - containerWidth - padding;
        float containerY = stage.getViewport().getWorldHeight() - containerHeight - padding;
        descriptionContainer.setPosition(containerX, containerY);
    }

    public void deleteDescription() {
        descriptionTable.clear();
        descriptionContainer.setVisible(false);
        LevelScreen.getInstance().hud.showInventory();
    }

    public boolean isDescriptionSet() {
        return !descriptionTable.getChildren().isEmpty();
    }

    public void dispose() {

        descriptionContainer.clear();
        descriptionTable.clear();
        descriptionTable.remove();
        descriptionContainer.remove();
    }
}
