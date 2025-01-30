package de.tum.cit.fop.maze.hud;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.level.LevelScreen;

/**
 * The AbilityBorder class represents a visual container that displays an ability icon
 * along with a border. It supports rendering of the border and ability animation
 * on a given position within a game.
 */
public class AbilityBorder {
    private final float width = 60;
    private final float x;
    private final float y;
    private final TextureRegion border;
    private Animation<TextureRegion> abilityAnimation;
    private float elapsedTime = 0;
    private boolean withAbility = false;

    /**
     * Instantiates a new Ability border.
     *
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param atlas the HUD atlas instance
     */
    public AbilityBorder(float x, float y, TextureAtlas atlas) {
        this.x = x - width / 2;
        this.y = y;
        this.border = atlas.findRegion("border");
    }

    /**
     * Add active item.
     */
    public void setActiveItem() {
        this.withAbility = true;
        this.abilityAnimation = LevelScreen.getInstance().player.getActiveItem().getCollectable().getIdleAnimation();
        this.abilityAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    /**
     * Render active item animation.
     *
     * @param batch     the batch used for rendering
     * @param deltaTime the delta time
     */
    public void render(SpriteBatch batch, float deltaTime) {

        elapsedTime += deltaTime;
        float height = 60;
        batch.draw(border, x, y, width, height);
        if (withAbility) {
            float borderPadding = 4;
            batch.draw(abilityAnimation.getKeyFrame(elapsedTime), x + borderPadding, y + borderPadding,
                width - borderPadding * 2, height - borderPadding * 2);
        }
    }
}
