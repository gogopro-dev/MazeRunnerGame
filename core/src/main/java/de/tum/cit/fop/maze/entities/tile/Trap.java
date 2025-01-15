package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.google.gson.Gson;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.Arrays;

public class Trap extends TileEntity {
    private static final String TRAP_ANIMATION_PATH = "anim/traps/traps.atlas";
    private Animation<TextureRegion> trapAnimation;
    private float elapsedTime = 0f;
    private float lastActivationTime = 0f;
    private OrthographicCamera camera;
    private boolean isActivated = true;
    private SpriteBatch spriteBatch;
    private TextureRegion currentFrame;
    private TrapAttributes trapAttributes;
    private Body body;
    private final String type;

    public Trap(String type) {
        super(0, 0);
        Gson gson = new Gson();
        TrapAttributes attributes = Arrays.stream(
            gson.fromJson(Gdx.files.internal("anim/traps/trapConfig.json").reader(), TrapAttributes[].class)
        ).filter(attribute -> attribute.type.equals(type)).findFirst().get();
        this.width = attributes.width;
        this.height = attributes.height;
        this.trapAttributes = attributes;
        this.type = type;
        this.camera = LevelScreen.getInstance().camera;
        this.spriteBatch = LevelScreen.getInstance().batch;

        TextureAtlas textureAtlas = new TextureAtlas(Gdx.files.internal(TRAP_ANIMATION_PATH));
        trapAnimation = new Animation<>(trapAttributes.frameDuration, textureAtlas.findRegions(type), Animation.PlayMode.NORMAL);
        /// trapAnimation keyframe's stateTime:
        /// 0.0f, 1/6f, 1/4f, 1/2f
    }


    public record TrapAttributes
        (String type, float frameDuration, float damage, float cooldown, int height, int width) {
    }


    public void render(float deltaTime) {
        elapsedTime += deltaTime;
        if (isActivated){
            if (trapAnimation.isAnimationFinished(elapsedTime)) {
                isActivated = false;
                lastActivationTime = elapsedTime;
            }
        } else {
            if (elapsedTime - lastActivationTime >= trapAttributes.cooldown) {
                isActivated = true;
                elapsedTime = 0f;
            }
        }

        //currentFrame = trapAnimation.getKeyFrame(0f, false);
        camera.update();
        spriteBatch.setProjectionMatrix(camera.combined);

        if (isActivated) {
            currentFrame = trapAnimation.getKeyFrame(elapsedTime, false);
        } else {
            /// trapAnimation idle state:
            currentFrame = trapAnimation.getKeyFrame(trapAnimation.getAnimationDuration(), false);
        }
        //TODO: change x and y position
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
