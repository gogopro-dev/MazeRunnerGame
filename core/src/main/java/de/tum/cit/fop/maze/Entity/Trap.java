package de.tum.cit.fop.maze.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.gson.Gson;
import de.tum.cit.fop.maze.level.LevelScreen;

public class Trap {
    private static final String TRAP_ANIMATION_PATH = "anim/traps/traps.atlas";
    private Animation<TextureRegion> trapAnimation;
    private float elapsedTime = 0f;
    private float lastActivationTime = 0f;
    private OrthographicCamera camera;
    private boolean isActivated = true;
    private SpriteBatch spriteBatch;
    private TextureRegion currentFrame;
    private TrapAttributes trapAttributes;
    private final String type;
    public Trap(OrthographicCamera camera, String type) {

        spriteBatch = LevelScreen.getInstance().batch;

        this.type = type;

        this.camera = camera;

        loadAnimations();
        TextureAtlas textureAtlas = new TextureAtlas(Gdx.files.internal(TRAP_ANIMATION_PATH));
        trapAnimation = new Animation<>(trapAttributes.frameDuration, textureAtlas.findRegions(type), Animation.PlayMode.NORMAL);
        /// trapAnimation keyframe's stateTime:
        /// 0.0f, 1/6f, 1/4f, 1/2f
    }

    public void loadAnimations() {
        Gson gson = new Gson();
        TrapAttributes[] TrapAttributes = gson.fromJson(Gdx.files.internal("anim/traps/trapConfig.json").reader(), TrapAttributes[].class);
        for (TrapAttributes attribute : TrapAttributes) {
            if (attribute.type.equals(type)) {
                this.trapAttributes = attribute;
            }
        }

    }

    public record TrapAttributes (String type, float frameDuration, float damage, float cooldown) {}

    public void activate() {
//        trapAnimation.
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
        spriteBatch.draw(currentFrame, LevelScreen.getInstance().player.getPosition().x()+1, LevelScreen.getInstance().player.getPosition().y()+1, 1, 1);
    }
}
