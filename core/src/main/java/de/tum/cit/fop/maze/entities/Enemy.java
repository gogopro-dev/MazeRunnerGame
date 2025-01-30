package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.essentials.Assets;
import de.tum.cit.fop.maze.essentials.Globals;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.essentials.DebugRenderer;
import de.tum.cit.fop.maze.level.LevelScreen;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Enemy extends Entity implements Attackable {
    private transient Animation<TextureRegion> idleAnimation;
    private transient Animation<TextureRegion> movementAnimation;
    private transient Animation<TextureRegion> movementTPAnimation;
    private transient Animation<TextureRegion> attackAnimation;
    private transient Animation<TextureRegion> dieAnimation;
    private transient Animation<TextureRegion> currentAnimation;
    private EnemyType enemyType;
    private transient Vector2 knockbackVector = new Vector2(0, 0);
    private transient float knockbackActivationElapsedTime = 0;
    private EnemyConfig config;
    private float elapsedTime = 0f;
    private transient boolean isAttacking = false;
    private transient float attackElapsedTime = 0f;    // Tracks time for attack animation
    private boolean isMovingToPlayer = false;
    private boolean facingRight = true;
    private boolean addedScore = false;
    private transient boolean canHit = true;
    private transient final List<AbsolutePoint> path;
    private transient AbsolutePoint currentPathPoint;
    private transient boolean isDamaged = false;
    private boolean deadAnimationReset = false;
    private float damageFlashTimer = 0f;

    public Enemy(EnemyType enemyType) {
        this();
        this.enemyType = enemyType;
        init();
    }

    /// Gson constructor
    private Enemy() {
        super();
        this.bodyType = BodyDef.BodyType.DynamicBody;
        this.path = Collections.synchronizedList(new LinkedList<>());
    }

    @Override
    public void render(float deltaTime) {
        elapsedTime += deltaTime;
        if (path != null && !isAttacking && !isDead()) this.followPath();
        if (isDead() && !deadAnimationReset) {
            deadAnimationReset = true;
            elapsedTime = 0f;
        }
        if (isDead()) {
            this.getBody().setLinearVelocity(Vector2.Zero);
        }
        if (isDead() && !addedScore) {
            addedScore = true;
            LevelScreen.getInstance().getLevelData().addScore(200);
        }
        isAttacking &= !isDead();
        if (isDead() && dieAnimation.isAnimationFinished(elapsedTime * 3) &&
            body.getFixtureList().size > 0) {
            Iterator<Fixture> fixtureIterator = new Array.ArrayIterator<>(body.getFixtureList());
            List<Fixture> toDestroy = new ArrayList<>();
            while (fixtureIterator.hasNext()) {
                toDestroy.add(fixtureIterator.next());
            }
            for (Fixture fixture : toDestroy) {
                body.destroyFixture(fixture);
            }
            this.clearPath();
            this.setMovingToPlayer(false);
        }
        if (isAttacking) {
            this.body.setLinearVelocity(Vector2.Zero);
            this.facingRight = getPosition().onTheLeftFrom(LevelScreen.getInstance().player.getPosition());
            attackElapsedTime += deltaTime;
        }

        if (isDamaged) {
            damageFlashTimer += deltaTime;
            if (damageFlashTimer >= Globals.IMMUNITY_FRAME_DURATION) {
                isDamaged = false;
                damageFlashTimer = 0f;
                /// Reset color back to normal
                batch.setColor(new Color(1, 1, 1, 1));
            }
            /// Set red tint if damaged
            batch.setColor(new Color(1, 0, 0, 1));
        }

        if (isAttacking && attackAnimation.getAnimationDuration() / 2 < attackElapsedTime) {
            attackPlayer();
            attackElapsedTime += attackAnimation.getAnimationDuration();
        }

        // Check if hit animation is finished
        if (isDead()) {
            currentAnimation = dieAnimation;
        } else if (isAttacking && attackAnimation.isAnimationFinished(attackElapsedTime)) {
            isAttacking = false; // Reset hit state
            canHit = true; // Reset hit cooldownI
            attackElapsedTime = 0f; // Reset hit animation time
        } else if (isMoving() && isMovingToPlayer) {
            currentAnimation = movementTPAnimation;
        } else if (isMoving() && !isMovingToPlayer) {
            currentAnimation = movementAnimation;
        } else if (isAttacking) {
            currentAnimation = attackAnimation;
        } else {
            currentAnimation = idleAnimation;
        }
        // Get the current animation frame
        TextureRegion currentFrame = currentAnimation.getKeyFrame(elapsedTime, !isDead());

        // Draw the current frame
        float frameWidth = currentFrame.getRegionWidth() * scale;
        float frameHeight = currentFrame.getRegionHeight() * scale;
        if (this.body != null && isMoving() && !isAttacking) {
            if (this.body.getLinearVelocity() != Vector2.Zero) {
                this.facingRight = (this.body.getLinearVelocity().x > 0 + config.attributes.speed / 2f);
            }
        }
        if (!isDead()) {
            if (facingRight && currentFrame.isFlipX()) {
                currentFrame.flip(true, false); // Flip horizontally if facing right
            } else if (!facingRight && !currentFrame.isFlipX()) {
                currentFrame.flip(true, false); // Flip horizontally if facing left
            }
        }

        if (!knockbackVector.isZero()) {
            knockbackActivationElapsedTime += deltaTime;
            body.setLinearVelocity(knockbackVector);
            if (knockbackActivationElapsedTime > .05f) {
                knockbackVector.setZero();
            }
        }

        if (LevelScreen.getInstance().player.getPosition().distance(getPosition()) <= Globals.ENEMY_ATTACK_DISTANCE &&
                !isDead()) {
            attack();
        }

        batch.draw(currentFrame, getSpriteX(), getSpriteY(), frameWidth, frameHeight);
        batch.setColor(new Color(1, 1, 1, 1));
    }

    @Override
    void init() {
        loadAnimations();
    }

    private void attackPlayer() {
        AbsolutePoint enemyPosition = getPosition();
        AbsolutePoint rectangleStart, rectangleEnd;
        float enemyTop = enemyPosition.y() - boundingRectangle.height() / 2;
        float enemyBottom = enemyPosition.y() + boundingRectangle.height() / 2;
        if (isFacingRight()) {
            rectangleStart = new AbsolutePoint(
                enemyPosition.x(),
                enemyTop
            );
            rectangleEnd =
                new AbsolutePoint(
                    enemyPosition.x() + Globals.ENEMY_ATTACK_DISTANCE,
                    enemyBottom
                );
        } else {
            rectangleStart = new AbsolutePoint(
                enemyPosition.x() - Globals.ENEMY_ATTACK_DISTANCE,
                enemyTop
            );
            rectangleEnd =
                new AbsolutePoint(
                    enemyPosition.x(),
                    enemyBottom
                );
        }
        DebugRenderer.getInstance().spawnRectangle(
            rectangleStart,
            rectangleEnd,
            Color.PINK
        );
        LevelScreen.getInstance().world.QueryAABB(
            fixture -> {
                if (fixture.getBody().getUserData() instanceof Player player) {
                    player.takeDamage(this.config.attributes.damage);
                    return false;
                }
                return true;
            },
            rectangleStart.x(), rectangleStart.y(), rectangleEnd.x(), rectangleEnd.y()
        );
    }

    @Override
    public void takeDamage(int damage) {
        if (isDamaged) return;
        super.takeDamage(damage);
        isDamaged = true;
        damageFlashTimer = 0f;
    }

    public void die() {
        elapsedTime = 0;
    }

    public void attack() {

        if (canHit) {
            this.body.setLinearVelocity(Vector2.Zero);
            isAttacking = true;
            canHit = false;
            currentAnimation = attackAnimation;
        }
    }

    /**
     * Load animations from the enemyConfig.json file
     */
    public void loadAnimations() {
        if (this.config == null) {
            Enemy.EnemyConfig[] enemyConfigs = Assets.getInstance().getEnemies().toArray(new EnemyConfig[0]);
            for (Enemy.EnemyConfig enemyConfig : enemyConfigs) {
                if (enemyConfig.enemyType == enemyType) {
                    config = enemyConfig;
                    break;
                }
            }

            this.health = (int) config.attributes.maxHealth;
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
        public record Attributes(float speed, float heal, float maxHealth, int damage, float visionRange) {
        }
    }

    public boolean isMoving() {
        return !this.body.getLinearVelocity().isZero();
    }

    public boolean isMovingToPlayer() {
        return isMovingToPlayer;
    }

    public void updatePath(List<AbsolutePoint> path) {
        synchronized (this.path) {
            this.path.clear();
            this.path.addAll(path);
            currentPathPoint = path.isEmpty() ? null : path.get(0);
        }
    }

    public void clearPath() {
        synchronized (this.path) {
            this.path.clear();
            currentPathPoint = null;
        }
    }

    public boolean isPathEmpty() {
        synchronized (this.path) {
            return this.path.isEmpty();
        }
    }

    public int getPathSize() {
        synchronized (this.path) {
            return this.path.size();
        }
    }

    public AbsolutePoint getPathElement(int index) {
        synchronized (this.path) {
            if (index < 0 || index >= this.path.size()) {
                return null;
            }
            return this.path.get(index);
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
                this.body.setLinearVelocity(
                    direction.toVector2().nor().scl(config.attributes.speed)
                );
            } else {
                this.body.setLinearVelocity(0, 0);
            }
        }
    }

    public void setKnockbackVector(@Nullable Vector2 knockbackVector) {
        this.knockbackVector = knockbackVector;
        this.knockbackActivationElapsedTime = 0;
    }

    public EnemyConfig getConfig() {
        return config;
    }

}
