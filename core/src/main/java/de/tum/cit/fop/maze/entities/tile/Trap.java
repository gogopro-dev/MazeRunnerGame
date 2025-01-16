package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.gson.Gson;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.Arrays;

/**
 * Represents a trap in the game
 */
public class Trap extends TileEntity {
    private static final String TRAP_ANIMATION_PATH = "anim/traps/traps.atlas";
    private final Animation<TextureRegion> trapAnimation;
    private float elapsedTime = 0f;
    private float lastActivationTime = 0f;
    private final OrthographicCamera camera;
    private boolean isActivated = true;
    private final SpriteBatch spriteBatch;
    private final TrapType type;
    public final TrapAttributes attributes;

    /**
     * Creates a new trap from a given type
     *
     * @param type the type of the trap
     */
    public Trap(TrapType type) {
        super(0, 0);
        Gson gson = new Gson();
        TrapAttributes attributes = Arrays.stream(
            gson.fromJson(Gdx.files.internal("anim/traps/trapConfig.json").reader(), TrapAttributes[].class)
        ).filter(attribute -> attribute.type.equals(type)).findFirst().get();
        this.width = attributes.width;
        this.height = attributes.height;
        this.attributes = attributes;
        this.type = type;
        this.camera = LevelScreen.getInstance().camera;
        this.spriteBatch = LevelScreen.getInstance().batch;

        TextureAtlas textureAtlas = new TextureAtlas(Gdx.files.internal(TRAP_ANIMATION_PATH));
        trapAnimation = new Animation<>(
            this.attributes.frameDuration, textureAtlas.findRegions(type.name()), Animation.PlayMode.NORMAL
        );
    }

    /**
     * The attributes of a trap
     * @param type the type of the trap
     * @param frameDuration duration of each frame in seconds
     * @param damage the damage of the trap
     * @param cooldown cooldown of the trap in seconds
     * @param height height of the trap in cells
     * @param width width of the trap in cells
     */
    public record TrapAttributes
        (TrapType type,
         float frameDuration,
         float damage,
         float cooldown,
         int height,
         int width) {
    }


    public void render(float deltaTime) {
        elapsedTime += deltaTime;
        if (isActivated){
            if (trapAnimation.isAnimationFinished(elapsedTime)) {
                isActivated = false;
                lastActivationTime = elapsedTime;
            }
        } else {
            if (elapsedTime - lastActivationTime >= attributes.cooldown) {
                isActivated = true;
                elapsedTime = 0f;
            }
        }

        camera.update();
        spriteBatch.setProjectionMatrix(camera.combined);

        TextureRegion currentFrame;
        if (isActivated) {
            currentFrame = trapAnimation.getKeyFrame(elapsedTime, false);
        } else {
            /// trapAnimation idle state:
            currentFrame = trapAnimation.getKeyFrame(trapAnimation.getAnimationDuration(), false);
        }

        spriteBatch.draw(
            currentFrame,
            getSpriteDrawPosition().x(), getSpriteDrawPosition().y(),
            getSpriteDrawWidth(), getSpriteDrawHeight()
        );
    }

    public boolean isDamaging() {
        return isActivated && elapsedTime > 0.15f;
    }



}
