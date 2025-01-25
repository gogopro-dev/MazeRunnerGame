package de.tum.cit.fop.maze.entities;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.fop.maze.Assets;
import de.tum.cit.fop.maze.BodyBits;
import de.tum.cit.fop.maze.entities.tile.Attributes;
import de.tum.cit.fop.maze.entities.tile.Collectable;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.essentials.DebugRenderer;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.level.LevelScreen;
import java.util.ArrayList;
import java.util.List;
/**
 * Represents the player character in the game.
 */
public class Player extends Entity {

    private final List<Collectable> inventory;
    private int gold = 0;
    private float staminaRecoveryElapsedTime = 0f;
    private float maxStamina = 100;


    private transient Animation<TextureRegion> idleAnimation;
    private transient Animation<TextureRegion> movementAnimation;
    private transient Animation<TextureRegion> attackAnimation;
    private transient Animation<TextureRegion> idleTorchAnimation;
    private transient Animation<TextureRegion> movementTorchAnimation;
    private transient Attributes collectableBuffs;

    private transient Animation<TextureRegion> currentAnimation;
    private transient float elapsedTime = 0f;
    private transient float elapsedTorchTime = 0f;
    private transient float attackElapsedTime = 0f;    // Tracks time for hit animation
    private transient boolean isAttacking = false;    // Track if the hit animation is active
    private transient boolean isMoving = false;     // Flag to track movement state
    private transient boolean facingRight = true;
    private transient boolean canHit;
    private transient boolean hasHit;
    private transient boolean isHoldingTorch = true;
    private transient boolean beingChased = false;
    private transient boolean onActiveTrap = false;
    private transient boolean isDamaged = false;
    private transient float damageFlashTimer = 0f;
    private transient float trapAttackElapsedTime = 0f;
    private transient PointLight torchLight;


    /**
     * Creates a new player character.
     */
    public Player() {
        super();
        this.mass = 5f;
        inventory = new ArrayList<>();
        collectableBuffs = new Attributes(0, 0, 0,
            0, 0, 0, 0, true);
        health = 40;
        maxHealth = 40;
        /// Player can hit if not holding torch
        canHit = !isHoldingTorch;
        init();
    }

    /**
     * Renders the player character.
     *
     * @param deltaTime The time since the last frame in seconds
     */
    @Override
    protected void render(float deltaTime) {
        /// TODO Stamina regeneration?
        elapsedTime += deltaTime;
        if (isHoldingTorch) {
            elapsedTorchTime += deltaTime;
            if (this.torchLight.getDistance() < Globals.TORCH_LIGHT_RADIUS) {
                this.torchLight.setDistance(
                    Math.min(
                        Globals.TORCH_LIGHT_RADIUS,
                        Utils.easeOutCirc(elapsedTorchTime * 0.77f) * Globals.TORCH_LIGHT_RADIUS
                    )
                );
            }
        }
        trapAttackElapsedTime += deltaTime;
        if (isAttacking) {
            attackElapsedTime += deltaTime;
        }

        if (onActiveTrap && trapAttackElapsedTime > 0.75f) {
            takeDamage(Globals.TRAP_DAMAGE);
            trapAttackElapsedTime = 0f;
        }
        /// Update damage flash timer
        if (isDamaged) {
            damageFlashTimer += deltaTime;
            if (damageFlashTimer >= Globals.IMMUNITY_FRAME_DURATION) {
                isDamaged = false;
                damageFlashTimer = 0f;
                /// Reset color back to normal
                batch.setColor(new Color(1, 1, 1, 1));
            }
        }

        /// Handle player input and movement
        handleInput();

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        /// Get the current animation frame
        TextureRegion currentFrame = (isAttacking ? attackAnimation : currentAnimation)
            .getKeyFrame(isAttacking ? attackElapsedTime : elapsedTime, true);

        /// Flip the frame if needed
        if (facingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false); // Flip horizontally if facing right
        } else if (!facingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false); // Flip horizontally if facing left
        }

        /// Set red tint if damaged
        if (isDamaged) {
            batch.setColor(new Color(1, 0, 0, 1));
        }

        /// Draw the current frame
        float frameWidth = currentFrame.getRegionWidth() * scale;
        float frameHeight = currentFrame.getRegionHeight() * scale;
        batch.draw(currentFrame, getSpriteX(), getSpriteY(), frameWidth, frameHeight);
        batch.setColor(Color.WHITE);
        if (isAttacking && !hasHit && attackElapsedTime > attackAnimation.getAnimationDuration() / 2) {
            System.out.println("Attack");
            hasHit = true;
            this.attackAllEnemiesInRange();
        }

        /// Check if hit animation is finished
        if (isAttacking && attackAnimation.isAnimationFinished(attackElapsedTime)) {
            isAttacking = false; // Reset hit state
            canHit = !isHoldingTorch; // Reset hit cooldown if not holding torch
            attackElapsedTime = 0f; // Reset hit animation time
        }


    }

    private void loadAnimations() {
        TextureAtlas animAtlas = Assets.getInstance().getAssetManager().get("assets/anim/player/character.atlas", TextureAtlas.class);
        /// Load idle animation
        idleAnimation = new Animation<>(1f / 8f, animAtlas.findRegions("Character_idle"));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        /// Load movement animation
        movementAnimation = new Animation<>(1f / 40f * 3f, animAtlas.findRegions("Character_move"));
        movementAnimation.setPlayMode(Animation.PlayMode.LOOP);

        /// Load hit animation
        attackAnimation = new Animation<>(1f / 30f, animAtlas.findRegions("Character_hit"));
        attackAnimation.setPlayMode(Animation.PlayMode.LOOP);

        /// Load idle torch animation
        idleTorchAnimation = new Animation<>(1f / 8f, animAtlas.findRegions("Character_idle_torch"));
        idleTorchAnimation.setPlayMode(Animation.PlayMode.LOOP);

        /// Load movement torch animation
        movementTorchAnimation = new Animation<>(1f / 40f * 3f, animAtlas.findRegions("Character_move_torch"));
        movementTorchAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    @Override
    void init() {
        loadAnimations();
        for (Collectable collectable : inventory) {
            collectable.init();
        }
    }

    private void attackAllEnemiesInRange() {
        AbsolutePoint playerPos = getPosition();
        AbsolutePoint rectangleStart, rectangleEnd;
        float playerTop = playerPos.y() - boundingRectangle.height() / 2;
        float playerBottom = playerPos.y() + boundingRectangle.height() / 2;
        if (isFacingRight()) {
            rectangleStart = new AbsolutePoint(
                playerPos.x(),
                playerTop
            );
            rectangleEnd =
                new AbsolutePoint(
                    playerPos.x() + Globals.PLAYER_ATTACK_DISTANCE,
                    playerBottom
                );
        } else {
            rectangleStart = new AbsolutePoint(
                playerPos.x() - Globals.PLAYER_ATTACK_DISTANCE,
                playerTop
            );
            rectangleEnd =
                new AbsolutePoint(
                    playerPos.x(),
                    playerBottom
                );
        }
        DebugRenderer.getInstance().spawnRectangle(
            rectangleStart,
            rectangleEnd,
            Color.CYAN
        );
        LevelScreen.getInstance().world.QueryAABB(
            fixture -> {
                System.out.println("Fixture detected: " + fixture);
                /// Do not permit a self-harm attempt ^_^
                if (fixture.getBody().getUserData() instanceof Player) {
                    return true;
                }

                if (fixture.getBody().getUserData() instanceof Attackable attackable) {
                    System.out.println("Hit " + attackable);
                    attackable.takeDamage(Globals.PLAYER_DAMAGE);
                }
                if (fixture.getBody().getUserData() instanceof Enemy enemy) {
                    enemy.setKnockbackVector(
                        new Vector2(
                            Globals.PLAYER_ATTACK_KNOCKBACK * Globals.CELL_SIZE_METERS * (isFacingRight() ? 1 : -1),
                            0
                        )
                    );
                }
                System.out.println("Hit " + fixture.getBody().getUserData());
                return true;
            },
            rectangleStart.x(), rectangleStart.y(), rectangleEnd.x(), rectangleEnd.y()
        );
    }

    /**
     * Handles player input and updates the player's position.
     */
    private void handleInput() {
        isMoving = false;
        /*if (isBeingChased()) {
            isHoldingTorch = false;
            canHit = true;
            elapsedTorchTime = 0f;
            torchLight.setDistance(0);
            torchLight.setActive(isHoldingTorch);
        }*/

        /// If pressed 'R', toggle torch
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            isHoldingTorch = !isHoldingTorch;
            canHit = !isHoldingTorch;
            elapsedTorchTime = 0f;
            torchLight.setDistance(0);
            torchLight.setActive(isHoldingTorch);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            LevelScreen.getInstance().tileEntityManager.createTileEntity(
                new Collectable(Collectable.CollectableType.GOLD_COIN, true),
                getPosition().x(), getPosition().y()
            );
        }


        /// Only update facing direction if not in hit animation
        if (!isAttacking) {
            if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                facingRight = true;
            } else if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                facingRight = false;
            }
        }

        /// Handle keyboard input and allow movement during hit animation
        float velocityX = 0;
        float velocityY = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            velocityY = entitySpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            velocityY = -entitySpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocityX = -entitySpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocityX = entitySpeed;
        }
        if (velocityX != 0 || velocityY != 0) isMoving = true;
        if (velocityX != 0 && velocityY != 0) {
            velocityX /= (float) Math.sqrt(2);
            velocityY /= (float) Math.sqrt(2);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && stamina > 0) {
            velocityX *= 1.3f;
            velocityY *= 1.3f;
            useStamina(2); // amount of stamina drained per second of running
            staminaRecoveryElapsedTime = 0;
        }


        if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)){
            if (staminaRecoveryElapsedTime == 0){
                LevelScreen.getInstance().hud.stopStaminaDrain();
            }
            if(staminaRecoveryElapsedTime > 2f){
                int staminaRecoveryPerSec = 2;
                restoreStamina(staminaRecoveryPerSec);
            }
            staminaRecoveryElapsedTime += Gdx.graphics.getDeltaTime();
        }

        System.out.println(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)+ " + " + staminaRecoveryElapsedTime);

        //TODO stamina regen (func in hud is ready)

        body.setLinearVelocity(velocityX, velocityY);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && canHit) {
            isAttacking = true;
            attackElapsedTime = 0f;
            hasHit = false;
            canHit = false;
        }

        /// Update animation while preserving hit animation priority
        if (isAttacking) {
            currentAnimation = attackAnimation;
        } else {
            if (isHoldingTorch) {
                currentAnimation = isMoving ? movementTorchAnimation : idleTorchAnimation;
            } else {
                currentAnimation = isMoving ? movementAnimation : idleAnimation;
            }
        }

        /// Move camera with the player
        updateCameraPosition();

        /// Handle camera zoom
        if ((Gdx.input.isKeyPressed(Input.Keys.EQUALS) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_ADD)
        ) && camera.zoom > Globals.DEFAULT_CAMERA_ZOOM - 0.5f) {
            camera.zoom -= 0.0015f;
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.MINUS) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_SUBTRACT))
            && camera.zoom < Globals.DEFAULT_CAMERA_ZOOM + 0.5f) {
            camera.zoom += 0.0015f;
        }
    }

    public List<Collectable> getInventory() {
        return inventory;
    }

    /**
     * Updates the camera position based on the player's position.
     */
    public void updateCameraPosition() {
        float cameraX = camera.position.x;
        float cameraY = camera.position.y;
        float cameraWidth = camera.viewportWidth;
        float cameraHeight = camera.viewportHeight;

        /// Calculate screen boundaries with consistent ratios
        /// The less the constant is, the earlier the camera will start moving
        float constX = 0.2f;
        float constY = 0.15f;


        float boundaryLeft = cameraX - cameraWidth * constX;
        float boundaryRight = cameraX + cameraWidth * constX;
        float boundaryBottom = cameraY - cameraHeight * constY;
        float boundaryTop = cameraY + cameraHeight * constY;
        DebugRenderer.getInstance().drawRectangle(
            new AbsolutePoint(boundaryLeft, boundaryBottom),
            new AbsolutePoint(boundaryRight, boundaryTop), Color.OLIVE
        );

        float playerX = getPosition().x();
        float playerY = getPosition().y();

        /// Calculate new camera position based on player position

        float newCameraX = camera.position.x;
        float newCameraY = camera.position.y;


        /// Handle X-axis camera movement
        if (playerX < boundaryLeft) {
            newCameraX = playerX + cameraWidth * constX;
        } else if (playerX > boundaryRight) {
            newCameraX = playerX - cameraWidth * constX;
        }

        /// Handle Y-axis camera movement
        if (playerY < boundaryBottom) {
            newCameraY = playerY + cameraHeight * constY;
        } else if (playerY > boundaryTop) {
            newCameraY = playerY - cameraHeight * constY;
        }

        /// Clamp camera position to map boundaries, accounting for zoom
        float effectiveViewportWidth = cameraWidth * camera.zoom;
        float effectiveViewportHeight = cameraHeight * camera.zoom;


        /// Prevent showing area beyond left and right edges
        float minX = effectiveViewportWidth / 2;
        float maxX = LevelScreen.getInstance().map.widthMeters - effectiveViewportWidth / 2;
        newCameraX = Math.min(maxX, Math.max(minX, newCameraX));

        /// Prevent showing area beyond top and bottom edges
        float minY = effectiveViewportHeight / 2;
        float maxY = LevelScreen.getInstance().map.heightMeters - effectiveViewportHeight / 2;
        newCameraY = Math.min(maxY, Math.max(minY, newCameraY));

        /// Update camera position
        camera.position.x = newCameraX;
        camera.position.y = newCameraY;
    }

    @Override
    protected void spawnInnerHitbox() {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(boundingRectangle.width() / 3f, boundingRectangle.height() / 3f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.filter.categoryBits = BodyBits.ENEMY;
        fixtureDef.filter.maskBits = BodyBits.ENEMY_MASK;
        fixtureDef.shape = shape;
        fixtureDef.restitution = 0f;
        fixtureDef.density = mass;
        fixtureDef.friction = 0f;
        this.body.createFixture(fixtureDef);
        shape.dispose();
    }

    @Override
    public void spawn(float x, float y) {
        super.spawn(x, y);
        torchLight = new PointLight(
            LevelScreen.getInstance().rayHandler, Globals.RAY_AMOUNT, Globals.TORCH_LIGHT_COLOR, 0, x, y
        );
        torchLight.setContactFilter(BodyBits.LIGHT, (short) 0, BodyBits.LIGHT_MASK);
        torchLight.setActive(false);
        torchLight.attachToBody(body);
    }


    public boolean isFacingRight() {
        return facingRight;
    }

    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        isDamaged = true;
        damageFlashTimer = 0f;
        LevelScreen.getInstance().hud.takeDmg(damage);
    }

    @Override
    public void heal(int amount) {
        super.heal(amount);
        //TODO: HUD heal
        //hud.hea(amount);
    }

    @Override
    public void useStamina(float amount) {
        if (stamina <= 0) {
            stamina = 0;
            return;
        }
        super.useStamina(amount*Gdx.graphics.getDeltaTime());
        LevelScreen.getInstance().hud.beginStaminaDrain(amount);
        //hud.useStamina(amount);
    }

    @Override
    public void restoreStamina(float amount) {
        if (stamina >= maxStamina) {
            stamina = maxStamina;
            return;
        }
        super.restoreStamina(amount*Gdx.graphics.getDeltaTime());
        LevelScreen.getInstance().hud.beginStaminaRecovery(amount);
        //hud.restoreStamina(amount);
    }

    /**
     * Collects a collectable item.
     *
     * @param collectable The collectable item to collect
     */
    public void collect(Collectable collectable) {
        collectableBuffs.sum(collectable.getCollectableAttributes());
        inventory.add(collectable);
        LevelScreen.getInstance().hud.addItemToInventory(collectable);;
        System.out.println("Current Buffs: " + collectableBuffs);
    }

    public boolean isBeingChased() {
        return beingChased;
    }

    public void setBeingChased(boolean beingChased) {
        this.beingChased = beingChased;
    }


    public boolean isOnActiveTrap() {
        return onActiveTrap;
    }

    public void setOnActiveTrap(boolean onActiveTrap) {
        this.onActiveTrap = onActiveTrap;
    }

    public boolean isHoldingTorch() {
        return isHoldingTorch;
    }
}
