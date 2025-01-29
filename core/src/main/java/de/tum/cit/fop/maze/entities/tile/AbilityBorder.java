package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.level.LevelScreen;

public class AbilityBorder {
    private final float height = 60;
    private final float width = 60;
    private final float x;
    private final float y;
    private final TextureRegion border;
    private Animation<TextureRegion> abilityAnimation;
    private float elapsedTime = 0;
    private final float borderPadding = 4;
    private boolean withAbility = false;

    public AbilityBorder(float x, float y, TextureAtlas atlas) {
        this.x = x - width / 2;
        this.y = y;
        this.border = atlas.findRegion("border");
    }

    public void addActiveItem(){

        this.withAbility = true;
        this.abilityAnimation = LevelScreen.getInstance().player.getActiveItem().getCollectable().getIdleAnimation();
        this.abilityAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }
    public void render(SpriteBatch batch, float deltaTime) {

        elapsedTime += deltaTime;
        batch.draw(border, x, y, width, height);
        if (withAbility) {
            batch.draw(abilityAnimation.getKeyFrame(elapsedTime), x + borderPadding, y + borderPadding,
                    width - borderPadding * 2, height - borderPadding*2);
        }
    }
}
