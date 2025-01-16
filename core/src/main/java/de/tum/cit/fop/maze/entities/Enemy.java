package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.google.gson.Gson;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;

import java.util.*;

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
    private boolean isAttacking = false;
    private float attackElapsedTime = 0f;    // Tracks time for attack animation
    private boolean isMovingToPlayer = false;
    private boolean facingRight = true;
    private boolean canHit = true;

    private final List<AbsolutePoint> path;
    private AbsolutePoint currentPathPoint;


    public Enemy(EnemyType enemyType, SpriteBatch batch) {
        super(batch);
        this.bodyType = BodyDef.BodyType.DynamicBody;
        this.box2dUserData = "enemy";
        this.enemyType = enemyType;
        this.path = Collections.synchronizedList(new LinkedList<>());
        loadAnimations();
    }

    @Override
    public void spawn(float x, float y, World world) {
        super.spawn(x, y, world);


    }

    public void render(float deltaTime) {
        elapsedTime += deltaTime;
        if (isAttacking) {
            attackElapsedTime += deltaTime;
        }

        if (isAttacking && attackAnimation.getAnimationDuration() / 2 < attackElapsedTime) {
            //TODO: Attack player
        }

        // Check if hit animation is finished
        if (isAttacking && attackAnimation.isAnimationFinished(attackElapsedTime)) {
            isAttacking = false; // Reset hit state
            canHit = true; // Reset hit cooldownI
            attackElapsedTime = 0f; // Reset hit animation time
        } else if (isMoving() && isMovingToPlayer) {
            currentAnimation = movementTPAnimation;
        } else if (isMoving() && !isMovingToPlayer) {
            currentAnimation = movementAnimation;
        } else {
            currentAnimation = idleAnimation;
        }
        // Get the current animation frame
        TextureRegion currentFrame = currentAnimation.getKeyFrame(elapsedTime, true);

        // Draw the current frame
        float frameWidth = currentFrame.getRegionWidth() * scale;
        float frameHeight = currentFrame.getRegionHeight() * scale;
        if (this.body != null && isMoving()) {
            this.facingRight = (this.body.getLinearVelocity().x > 0 + config.attributes.speed / 2f);

            if (facingRight && currentFrame.isFlipX()) {
                currentFrame.flip(true, false); // Flip horizontally if facing right
            } else if (!facingRight && !currentFrame.isFlipX()) {
                currentFrame.flip(true, false); // Flip horizontally if facing left
            }
        }

        batch.draw(currentFrame, getSpriteX(), getSpriteY(), frameWidth, frameHeight);
    }
    public void die() {
        currentAnimation = dieAnimation;
    }

    public void attack() {
        if (canHit) {
            isAttacking = true;
            canHit = false;
            currentAnimation = attackAnimation;
        }
    }

    /**
     * Load animations from the enemyConfig.json file
     */
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

    public boolean isFacingRight() {
        return facingRight;
    }

    public synchronized void setMovingToPlayer(boolean b) {
        this.body.setLinearVelocity(0, 0);
        isMovingToPlayer = b;
    }


    public record EnemyConfig (String pathToAnim, EnemyType enemyType, Attributes attributes) {
        public record Attributes(float speed, float heal, float maxHealth, float damage, float visionRange) {
        }
    }

    public boolean isMoving() {
        return !this.body.getLinearVelocity().isZero();
    }

    public boolean isMovingToPlayer() {
        return isMovingToPlayer;
    }

    public List<AbsolutePoint> getPath() {
        return path;
    }

    public void updatePath(List<AbsolutePoint> path) {
        synchronized (this.path) {
            this.path.clear();
            this.path.addAll(path);
            currentPathPoint = path.isEmpty() ? null : path.get(0);
        }
    }

    public synchronized void followPath() {
        synchronized (path) {
            if (currentPathPoint == null) {
                if (path.isEmpty()) {
                    return;
                }
                currentPathPoint = path.remove(0);
            }
            if (this.getPosition().distance(currentPathPoint) < 0.1f) {
                if (path.isEmpty()) {
                    currentPathPoint = null;
                } else {
                    currentPathPoint = path.remove(0);
                }
            }
            if (currentPathPoint != null) {
                AbsolutePoint direction = new AbsolutePoint(
                    currentPathPoint.x() - getPosition().x(),
                    currentPathPoint.y() - getPosition().y()
                );
                this.body.setLinearVelocity(direction.toVector2().nor().scl(config.attributes.speed));
            } else {
                this.body.setLinearVelocity(0, 0);
            }
        }
    }

    public EnemyConfig getConfig() {
        return config;
    }

}
