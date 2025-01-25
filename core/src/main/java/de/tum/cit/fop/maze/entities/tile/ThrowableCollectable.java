package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.level.LevelScreen;

public class ThrowableCollectable extends TileEntity {
    private transient Animation<TextureRegion> idleAnimation;
    private transient Animation<TextureRegion> flyingAnimation;
    private transient Animation<TextureRegion> explodeAnimation;
    private transient State currentState = State.IDLE;
    private transient float stateTime = 0f;
    private transient Vector2 throwDirection;
    private transient final float throwSpeed = 10f;

    public enum State {
        IDLE, FLYING, EXPLODING, DONE
    }

    public ThrowableCollectable() {
        super(2, 1);
        currentState = State.IDLE;
        init();
    }

    @Override
    protected void init() {
        TextureAtlas atlas = new TextureAtlas(Gdx.files.local(
            "assets/temporary/collectables/fireball_throwable.atlas"
        ));

        this.idleAnimation = new Animation<>(0.1f, atlas.findRegions("fireball_idle"));
        this.idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        this.flyingAnimation = new Animation<>(0.1f, atlas.findRegions("fireball_flying"));
        this.flyingAnimation.setPlayMode(Animation.PlayMode.LOOP);

        this.explodeAnimation = new Animation<>(0.15f, atlas.findRegions("fireball_explode"));
        this.explodeAnimation.setPlayMode(Animation.PlayMode.NORMAL);
    }


    @Override
    public void render(float delta) {
        super.render(delta);

        // Update fireball state
        stateTime += delta;

        switch (currentState) {
            case FLYING:
                updateFlying(delta);
                break;
            case EXPLODING:
                if (explodeAnimation.isAnimationFinished(stateTime)) {
                    currentState = State.DONE;
                    toDestroy = true;
                }
                break;
            default:
        }

        if (currentState != State.IDLE) {
            TextureRegion currentFrame = getCurrentFrame();
            batch.begin();
            batch.draw(currentFrame, LevelScreen.getInstance().player.getPosition().x() + 200, LevelScreen.getInstance().player.getPosition().y() + 50);
            batch.end();
        };
    }

    public void throwFireball() {
        if (currentState != State.IDLE) return;

        currentState = State.FLYING;
        stateTime = 0f;

        // Determine throw direction based on player's facing direction
        throwDirection = LevelScreen.getInstance().player.isFacingRight() ?
            new Vector2(1, 0) : new Vector2(-1, 0);

        // Set body velocity for throwing
        body.setLinearVelocity(
            throwDirection.x * throwSpeed,
            throwDirection.y * throwSpeed
        );
    }

    private void updateFlying(float delta) {
        // Check for collisions or distance traveled
        // You may want to add more sophisticated collision detection
        // For now, we'll use a simple distance check
        if (stateTime > 2f) {
            explode();
        }
    }

    private void explode() {
        currentState = State.EXPLODING;
        stateTime = 0f;
        body.setLinearVelocity(0, 0);

        // Implement damage logic here
        // For example, query nearby enemies and apply damage
        LevelScreen.getInstance().world.QueryAABB(
            fixture -> {
                // Similar to Player's attackAllEnemiesInRange method
                // Apply damage to enemies in explosion radius
                return true;
            },
            getPosition().x() - 1, getPosition().y() - 1,
            getPosition().x() + 1, getPosition().y() + 1
        );
    }

    public TextureRegion getCurrentFrame() {
        return switch (currentState) {
            case IDLE -> this.idleAnimation.getKeyFrame(stateTime);
            case FLYING -> flyingAnimation.getKeyFrame(stateTime);
            case EXPLODING -> explodeAnimation.getKeyFrame(stateTime);
            default -> null;
        };
    }
}
