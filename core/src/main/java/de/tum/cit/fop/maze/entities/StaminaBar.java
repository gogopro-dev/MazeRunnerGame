package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import de.tum.cit.fop.maze.essentials.Utils;
import com.badlogic.gdx.graphics.Color;

public class StaminaBar {
    private final float scalingX;
    private final float scalingY;
    private final ProgressBar staminaBar;
    private float currentStamina;
    private final float maxStamina;
    private final TextureRegion staminaBarBorder;
    private float x;
    private float y;
    /// alignment of the bar with respect to the border
    private int staminaBarAlignmentY;
    private int staminaBarAlignmentX;


    public StaminaBar(float maxStamina, float x, float y,
                      TextureAtlas atlas, float width, float height) {
        scalingX = width/129;
        scalingY = height/8;
        this.maxStamina = maxStamina;
        this.currentStamina = maxStamina;
        staminaBarAlignmentY = (int) (4 * scalingY);
        staminaBarAlignmentX = (int) (15 * scalingX);
        this.x = x;
        this.y = y - height - staminaBarAlignmentY;


        staminaBar = new ProgressBar(
            0f, maxStamina, 0.001f, false, new ProgressBar.ProgressBarStyle()
        );
        staminaBarBorder = atlas.findRegion("staminaBarBorder");


        staminaBar.getStyle().background = Utils.getColoredDrawable((int) width, (int) height, Color.DARK_GRAY);
        staminaBar.getStyle().knob = Utils.getColoredDrawable(0, (int) height, Color.GOLD);
        staminaBar.getStyle().knobBefore = Utils.getColoredDrawable((int) width, (int) height, Color.GOLD);

        staminaBar.setWidth(width);
        staminaBar.setHeight(height);
        staminaBar.setValue(maxStamina);
        staminaBar.setAnimateDuration(0.25f);

        /// +1 due to problems with converting float to int in Utils.getColoredDrawable. The +1 is a workaround
        /// to prevent the bar from being cut off on the left side. Magic numbers)
        staminaBar.setBounds(this.x + staminaBarAlignmentX + 1 , this.y + staminaBarAlignmentY, width, height);

    }

    public void draw(SpriteBatch batch, float deltaTime) {
        staminaBar.act(deltaTime);
        staminaBar.draw(batch, 1);
        batch.draw(staminaBarBorder, x, y,
                staminaBarBorder.getRegionWidth() * scalingX,
                staminaBarBorder.getRegionHeight() * scalingY);

    }

    public void setStamina(float stamina) {
        staminaBar.setValue(Math.max(Math.min(stamina, maxStamina), 0));
    }

    public float getStamina() {
        return staminaBar.getValue();
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }

    public void render(float deltaTime, SpriteBatch batch) {
        draw(batch, deltaTime);
    }

}
