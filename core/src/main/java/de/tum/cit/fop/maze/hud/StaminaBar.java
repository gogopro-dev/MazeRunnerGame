package de.tum.cit.fop.maze.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.level.LevelScreen;

/**
 * The StaminaBar class represents a visual component that displays the player's
 * stamina in the user interface. This component is constructed using a progress bar
 * and is rendered using a sprite batch.
 */
public class StaminaBar {
    private final float scalingX;
    private final float scalingY;
    private final ProgressBar staminaBar;
    private final float maxStamina;
    private final TextureRegion staminaBarBorder;
    private final float x;
    private final float y;

    /**
     * Instantiates a new Stamina bar.
     *
     * @param maxStamina the max stamina
     * @param x          the x
     * @param y          the y
     * @param atlas      the HUD atlas instance
     * @param width      the width
     * @param height     the height
     */
    public StaminaBar(float maxStamina, float x, float y,
                      TextureAtlas atlas, float width, float height) {
        scalingX = width / 129;
        scalingY = height / 8;
        this.maxStamina = maxStamina;
        /// alignment of the bar with respect to the border
        int staminaBarAlignmentY = (int) (4 * scalingY);
        int staminaBarAlignmentX = (int) (15 * scalingX);
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
        /// to prevent the bar from being cut off on the left side.
        staminaBar.setBounds(this.x + staminaBarAlignmentX + 1, this.y + staminaBarAlignmentY, width, height);

    }

    /**
     * Draw.
     * @param batch Spritebatch
     * @param deltaTime Time since the last frame
     */
    public void draw(SpriteBatch batch, float deltaTime) {
        staminaBar.act(deltaTime);
        staminaBar.draw(batch, 1);
        batch.draw(staminaBarBorder, x, y,
            staminaBarBorder.getRegionWidth() * scalingX,
            staminaBarBorder.getRegionHeight() * scalingY);

    }

    public float getStamina() {
        return staminaBar.getValue();
    }

    public void setStamina(float stamina) {
        staminaBar.setValue(Math.max(Math.min(stamina, maxStamina), 0));
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void render(float deltaTime, SpriteBatch batch) {
        staminaBar.setValue(LevelScreen.getInstance().player.getStamina());
        draw(batch, deltaTime);
    }

    public void dispose() {
        staminaBarBorder.getTexture().dispose();
    }

}
