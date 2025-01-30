package de.tum.cit.fop.maze.hud;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * The ExitArrow class represents a visual component that displays an arrow pointing towards the exit
 * in the user interface. This component is constructed using a sprite and is added to a stage for rendering.
 * The arrow is rotated based on the player's position in the maze.
 */
public class ExitArrow {
    final Sprite exitArrow;

    /**
     * Instantiates a new Exit arrow.
     * @param atlas the HUD atlas instance
     * @param stage the stage
     */
    public ExitArrow(TextureAtlas atlas, Stage stage) {
        exitArrow = new Sprite(atlas.findRegion("arrow"));

        float arrowSize = 50;
        exitArrow.setSize(arrowSize, arrowSize);
        updateArrowPosition(stage);
    }

    /**
     * Update arrow position.
     * @param stage the stage
     */
    public void updateArrowPosition(Stage stage) {
        float arrowPadding = 20;
        exitArrow.setPosition(stage.getViewport().getWorldWidth() - exitArrow.getWidth() - arrowPadding,
            arrowPadding);
        exitArrow.setOriginCenter();
    }

    /**
     * Draw exit arrow.
     * @param spriteBatch the sprite batch
     * @param angle the angle
     */
    public void drawExitArrow(SpriteBatch spriteBatch, float angle) {
        exitArrow.setRotation(angle);
        exitArrow.draw(spriteBatch);
    }
}
