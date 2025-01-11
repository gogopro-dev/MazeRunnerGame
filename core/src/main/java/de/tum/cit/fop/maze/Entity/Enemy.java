package de.tum.cit.fop.maze.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.gson.Gson;

public class Enemy extends Entity {

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> movementAnimation;
    private Animation<TextureRegion> movementTPAnimation;
    private Animation<TextureRegion> attackAnimation;
    private Animation<TextureRegion> dieAnimation;
    private Animation<TextureRegion> currentAnimation;
    private final EnemyType enemyType;
    private EnemyConfig config;
    private float elapsedTime = 0f;
    private boolean isHitting = false;
    private float hitElapsedTime = 0f;    // Tracks time for hit animation
    private boolean isMoving = false;

    private boolean facingRight = true;
    private boolean canHit = true;
    private SpriteBatch spriteBatch;
    private OrthographicCamera camera;

    public Enemy(float mapWidth, float mapHeight, EnemyType enemyType, OrthographicCamera camera) {
        super(mapWidth, mapHeight);
        this.enemyType = enemyType;
        this.camera = camera;
        spriteBatch = new SpriteBatch();

        loadAnimations();
    }

    public void render(float deltaTime) {
        elapsedTime += deltaTime;
        if (isHitting) {
            hitElapsedTime += deltaTime;
        }

        //TODO: Implement enemy AI (@Hlib and @Erik)

        camera.update();
        spriteBatch.setProjectionMatrix(camera.combined);

        currentAnimation = idleAnimation;
        // Begin rendering
        spriteBatch.begin();
        // Get the current animation frame
        TextureRegion currentFrame = currentAnimation.getKeyFrame(elapsedTime, true);

        // Draw the current frame
        float frameWidth = currentFrame.getRegionWidth() * scale;
        float frameHeight = currentFrame.getRegionHeight() * scale;
        spriteBatch.draw(currentFrame, getSpriteX(), getSpriteY(), frameWidth, frameHeight);

        spriteBatch.end();

        // Check if hit animation is finished
        if (isHitting && attackAnimation.isAnimationFinished(hitElapsedTime)) {
            isHitting = false; // Reset hit state
            canHit = true; // Reset hit cooldownI
            hitElapsedTime = 0f; // Reset hit animation time
        }
    }

    public void dispose(){
        spriteBatch.dispose();
    }

    private void handleInput() {
        // Update animation while preserving hit animation priority
        if (isHitting) {
            currentAnimation = attackAnimation;
        } else {
            currentAnimation = isMoving ? movementAnimation : idleAnimation;
        }
    }

    public void loadAnimations() {
        Gson gson = new Gson();
        FileHandle file = Gdx.files.internal("anim/enemies/enemyConfig.json");
        Enemy.EnemyConfig[] enemyConfigs = gson.fromJson(file.readString(), EnemyConfig[].class);
        for (Enemy.EnemyConfig enemyConfig : enemyConfigs) {
            if (enemyConfig.enemyType == enemyType) {
                config = enemyConfig;
                break;
            }
        }

        // Load idle animation
        TextureAtlas animationAtlas = new TextureAtlas(Gdx.files.internal(config.pathToAnim));
        idleAnimation = new Animation<>(1f / 6.25f, animationAtlas.findRegions("idle"));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Load movement animation
        movementAnimation = new Animation<>(1f / 10f, animationAtlas.findRegions("walk"));
        movementAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Load movement to player animation
        movementTPAnimation = new Animation<>(1f / 10f, animationAtlas.findRegions("walkTP"));
        movementTPAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Load hitting animation
        attackAnimation = new Animation<>(1f / 8f, animationAtlas.findRegions("attack"));
        attackAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Load die animation
        dieAnimation = new Animation<>(1f / 7f, animationAtlas.findRegions("die"));
        dieAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        currentAnimation = idleAnimation;
    }

    public record EnemyConfig (String pathToAnim, EnemyType enemyType, Attributes attributes) {
        public record Attributes (int speed, int heal, int maxHealth, int damage){}
    }

    //TODO: Implement enemy AI (@Hlib and @Erik)
    public static class EnemyAI {
        public EnemyAI() {

        }
    }
}
