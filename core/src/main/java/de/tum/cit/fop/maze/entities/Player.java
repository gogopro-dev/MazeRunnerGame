package de.tum.cit.fop.maze.entities;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import de.tum.cit.fop.maze.ActiveItem;
import de.tum.cit.fop.maze.Assets;
import de.tum.cit.fop.maze.BodyBits;
import de.tum.cit.fop.maze.entities.tile.Attributes;
import de.tum.cit.fop.maze.entities.tile.Collectable;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.essentials.DebugRenderer;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.hud.HUD;
import de.tum.cit.fop.maze.level.LevelScreen;
import games.rednblack.miniaudio.MASound;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static de.tum.cit.fop.maze.Globals.*;

/**
 * Represents the player character in the game.
 */
public class Player extends Entity {

    private final Attributes attributes;
    private final List<Collectable> inventory;
    private int gold = 0;
    private float staminaRecoveryElapsedTime = 0f;
    private final float maxStamina = 100;
    private boolean hasKey = false;
    private ActiveItem activeItem;
    private transient boolean isMoving = false;
    private transient Animation<TextureRegion> idleAnimation;
    private transient Animation<TextureRegion> movementAnimation;
    private transient Animation<TextureRegion> attackAnimation;
    private transient Animation<TextureRegion> dieAnimation;
    private transient Animation<TextureRegion> idleTorchAnimation;
    private transient Animation<TextureRegion> movementTorchAnimation;
    private transient Animation<TextureRegion> currentAnimation;
    private transient float elapsedTime = 0f;
    private transient float elapsedTorchTime = 0f;
    private transient float attackElapsedTime = 0f;    // Tracks time for hit animation
    private transient boolean isAttacking = false;    // Track if the hit animation is active
    private boolean facingRight = true;
    private transient boolean canHit;
    private transient boolean hasHit;
    private transient boolean isHoldingTorch = false;
    private transient boolean beingChased = false;
    private transient boolean onActiveTrap = false;
    private transient boolean isDamaged = false;
    private transient float damageFlashTimer = 0f;
    private transient PointLight torchLight;
    private transient float shadowWaitElapsedTime;
    //private transient MASound attack
    private transient float deadElapsedTime = 0f;


    /**
     * Creates a new player character.
     */
    public Player() {
        super();
        this.mass = 5f;
        inventory = new ArrayList<>();
        health = 40;
        maxHealth = 40;
        attributes = new Attributes(0, 0, 0,
            0, 0, 0, 0);
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
        Assets.getInstance().soundEngine.setListenerPosition(getPosition().x(), getPosition().y(), 0);
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
        if (isAttacking) {
            attackElapsedTime += deltaTime;
        }

        if (onActiveTrap) {
            takeDamage(Globals.TRAP_DAMAGE);
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
            /// Set red tint if damaged
            batch.setColor(new Color(1, 0, 0, 1));
        }

        /// Handle player input and movement
        handleInput();

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        /// Get the current animation frame
        TextureRegion currentFrame = (isAttacking ? attackAnimation : currentAnimation)
            .getKeyFrame(isAttacking ? attackElapsedTime : elapsedTime, true);

        /// If the player is dead, play the die animation and hold for 2 seconds
        if (isDead()){
            deadElapsedTime += deltaTime;
            if (deadElapsedTime > dieAnimation.getAnimationDuration() + 2f){
                LevelScreen.getInstance().hud.setHealthBar(0, maxHealth);
                LevelScreen.getInstance().endGame(false);
            }
            currentFrame = dieAnimation.getKeyFrame(deadElapsedTime, false);
        }

        /// Flip the frame if needed
        if (facingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false); // Flip horizontally if facing right
        } else if (!facingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false); // Flip horizontally if facing left
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


        shadowWaitElapsedTime += deltaTime;

        checkPlayerInShadow(shadowWaitElapsedTime);
        if (this.activeItem != null) {
            this.activeItem.tick(deltaTime);
        }

    }

    /**
     * Loads the player character animations
     * from the asset manager and
     * assigns them to the corresponding fields.
     *
     * @see Assets
     */
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

        /// Load die animation
        dieAnimation = new Animation<>(1f / 10f, animAtlas.findRegions("character_die"));
        dieAnimation.setPlayMode(Animation.PlayMode.NORMAL);

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
        if (this.activeItem != null) {
            this.activeItem.restore();
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
                    attackable.takeDamage(Globals.PLAYER_DAMAGE + attributes.getDamageBoost());
                    if (attributes.getVampirism() > 0 && attackable instanceof Enemy) {
                        heal(LevelScreen.getInstance().getRandom().nextFloat() <= attributes.getVampirism() ? 1 : 0);
                    }
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

        /// If pressed 'R', toggle torch
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            isHoldingTorch = !isHoldingTorch;
            canHit = !isHoldingTorch;
            elapsedTorchTime = 0f;
            torchLight.setDistance(0);
            torchLight.setActive(isHoldingTorch);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q) && this.activeItem != null && stamina > maxStamina / 3) {
            this.activeItem.use();
            useStamina(maxStamina / 2);

        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            /// Only if the player has more than 1 gold coin
            if (gold > 0) {
                LevelScreen.getInstance().tileEntityManager.createTileEntity(
                    new Collectable(Collectable.CollectableType.GOLD_COIN, true),
                    getPosition().x(), getPosition().y()
                );
                /// Remove gold from
                removeGold(1);
            }
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
            velocityX *= 1.2f + attributes.getSpeedBoost();
            velocityY *= 1.2f + attributes.getSpeedBoost();
            useStamina(14 * Gdx.graphics.getDeltaTime()); // amount of stamina drained per second of running
            staminaRecoveryElapsedTime = 0;
        }

        if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            if (staminaRecoveryElapsedTime > 2f) {
                restoreStamina(14 * Gdx.graphics.getDeltaTime());
            } else {
                staminaRecoveryElapsedTime += Gdx.graphics.getDeltaTime();
            }
        }


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
        float ratio = LevelScreen.getInstance().getRatio();

        float minZoom = Globals.DEFAULT_CAMERA_ZOOM * 0.5f * ratio;
        float maxZoom = Globals.DEFAULT_CAMERA_ZOOM * ratio;

        /// Handle camera zoom
        if ((Gdx.input.isKeyPressed(Input.Keys.EQUALS) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_ADD))) {
            camera.zoom = Math.max(camera.zoom - 0.003f * ratio, minZoom);
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.MINUS) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_SUBTRACT))) {
            camera.zoom = Math.min(camera.zoom + 0.003f * ratio, maxZoom);
        }
        updateCameraPosition();
    }

    /**
     * Check if the player is in the shadow
     * and if so, attack the player</br>
     * If the player is in the shadow for more than 3 seconds,
     * the player will be attacked
     *
     * @param shadowWaitElapsedTime The time elapsed since the player entered the shadow
     */
    public void checkPlayerInShadow(float shadowWaitElapsedTime) {
        HUD hud = LevelScreen.getInstance().hud;
        if (Utils.isEntityInLight(this)) {
            if (hud.isDescriptionSet() && Objects.equals(hud.getItemDescription(), PLAYER_SCARED_TEXT)) {
                hud.deleteDescription();
            }
            this.shadowWaitElapsedTime = 0;
            return;
        }
        /// Deprioritize fear of the dark label
        if (!hud.isDescriptionSet()) {
            hud.setItemDescription(PLAYER_SCARED_TEXT);
        }
        if (shadowWaitElapsedTime < 3f) {
            return;
        }

        takeDamage(1);
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

        if (Gdx.graphics.isFullscreen()) {
            cameraWidth = DEFAULT_CAMERA_VIEWPORT_WIDTH_METERS;
            cameraHeight = DEFAULT_CAMERA_VIEWPORT_HEIGHT_METERS;
        }

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
        /// Light is coming from the legs to avoid some visual bugs with ray-casting vast walking on LootContainer(s)
        torchLight.attachToBody(body, 0, -0.3f);
    }


    public boolean isFacingRight() {
        return facingRight;
    }

    @Override
    public void takeDamage(int damage) {
        /// Damage taken by player is at least 1 after applying all resits
        damage = Math.max(1, damage - attributes.getResistanceBoost());
        if (isDamaged) return;
        super.takeDamage(damage);
        isDamaged = true;
        damageFlashTimer = 0f;
        if (isDead()) {
            /// If there was a resurrection amulet in the inventory
            if (removeItem(Collectable.CollectableType.RESURRECTION_AMULET)) {
                health = maxHealth;
                LevelScreen.getInstance().hud.setHealthBar(health, maxHealth);
                return;
            }
        }
        LevelScreen.getInstance().hud.takeDmg(damage);
    }

    public boolean removeItem(Collectable.CollectableType type) {
        for (Collectable c : inventory) {
            if (c.getType() == type) {
                inventory.remove(c);
                /// If item is not in inventory or is in a single instance
                if (!LevelScreen.getInstance().hud.removeItemFromInventory(c)) {
                    /// Update inventory, else amount will be updated in removeItemFromInventory
                    LevelScreen.getInstance().hud.updateInventory(inventory);
                }
                /// Remove corresponding attributes
                attributes.sub(c.getCollectableAttributes());
                return true;
            }
        }
        return false;
    }

    @Override
    public void heal(int amount) {
        super.heal(amount);
        LevelScreen.getInstance().hud.setHealthBar(health, maxHealth);
    }

    @Override
    public void useStamina(float amount) {
        if (stamina <= 0) {
            stamina = 0;
            return;
        }
        super.useStamina(amount);
    }

    @Override
    public void restoreStamina(float amount) {
        if (stamina >= maxStamina) {
            stamina = maxStamina;
            return;
        }
        super.restoreStamina(amount);
    }

    /**
     * Collects a collectable activeItem.
     *
     * @param collectable The collectable activeItem to collect
     */
    public void collect(Collectable collectable) {
        switch (collectable.getType()) {
            case HEART:
                heal(collectable.getCollectableAttributes().getImmediateHealing());
                break;
            case KEY:
                hasKey = true;
                addKey();
                break;
            case GOLD_COIN:
                addGold(collectable.getCollectableAttributes().getImmediateCoins());
                break;
            default:
                if (collectable.getCollectableAttributes().associatedActiveItem != null) {
                    activeItem = new ActiveItem(collectable.getCollectableAttributes().associatedActiveItem);
                    LevelScreen.getInstance().hud.addActiveItem();
                    return;

                }
                inventory.add(collectable);
                attributes.sum(collectable.getCollectableAttributes());
                LevelScreen.getInstance().hud.addItemToInventory(collectable);
        }
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


    public void addKey() {
        LevelScreen.getInstance().hud.addKey();
    }

    public boolean hasKey() {
        return hasKey;
    }

    public int getGold() {
        return gold;
    }

    public void addGold(int gold) {
        this.gold += gold;
        LevelScreen.getInstance().hud.addCoin(gold);
    }

    public void removeGold(int gold) {
        this.gold -= gold;
        LevelScreen.getInstance().hud.addCoin(-gold);
    }

    public float getMaxStamina() {
        return maxStamina;
    }

    public ActiveItem getActiveItem() {
        return activeItem;
    }
}
