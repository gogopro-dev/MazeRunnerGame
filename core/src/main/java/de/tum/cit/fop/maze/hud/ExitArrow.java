package de.tum.cit.fop.maze.hud;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class ExitArrow {
    Sprite exitArrow;

    public ExitArrow(TextureAtlas atlas, Stage stage) {
        exitArrow = new Sprite(atlas.findRegion("arrow"));

        float arrowSize = 50;
        exitArrow.setSize(arrowSize, arrowSize);
        updateArrowPosition(stage);
    }

    public void updateArrowPosition(Stage stage) {
        float arrowPadding = 20;
        exitArrow.setPosition(stage.getViewport().getWorldWidth() - exitArrow.getWidth() - arrowPadding,
            arrowPadding);
        exitArrow.setOriginCenter();
    }

    public void drawExitArrow(SpriteBatch spriteBatch , float angle) {
        exitArrow.setRotation(angle);
        exitArrow.draw(spriteBatch);
    }
}
