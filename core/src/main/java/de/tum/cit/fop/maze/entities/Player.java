package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import de.tum.cit.fop.maze.Collectable;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.essentials.DebugRenderer;
import de.tum.cit.fop.maze.level.LevelScreen;

/**
 * Represents the player character in the game.
 */
public class Player extends Entity {
    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> movementAnimation;
    private final Animation<TextureRegion> attackAnimation;
    private final Animation<TextureRegion> idleTorchAnimation;
    private final Animation<TextureRegion> movementTorchAnimation;
    private Animation<TextureRegion> currentAnimation;
    private float elapsedTime = 0f;
    private float attackElapsedTime = 0f;    // Tracks time for hit animation
    private boolean isAttacking = false;    // Track if the hit animation is active
    private boolean isMoving = false;     // Flag to track movement state
    private boolean facingRight = true;
    private boolean canHit;
    private boolean isHoldingTorch = true;
    private boolean beingChased = false;
    private boolean onActiveTrap = false;
    private final float mapWidth;
    private final float mapHeight;
    private boolean isDamaged = false;
    private float damageFlashTimer = 0f;
    private static final float DAMAGE_FLASH_DURATION = 0.2f;

    /**
     * Creates a new player character.
     * @param batch The sprite batch to render the player character
     */
    public Player(SpriteBatch batch) {
        super(batch);
        this.mapWidth = LevelScreen.getInstance().map.widthMeters;
        this.mapHeight = LevelScreen.getInstance().map.heightMeters;
        this.mass = 5f;
        this.box2dUserData = "player";

        /// Player can hit if not holding torch
        canHit = !isHoldingTorch;


        TextureAtlas animAtlas = new TextureAtlas(Gdx.files.internal("anim/player/character.atlas"));
        /// Load idle animation
        idleAnimation = new Animation<>(1f / 8f, animAtlas.findRegions("Character_idle"));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        /// Load movement animation
        movementAnimation = new Animation<>(1f /40f * 3f, animAtlas.findRegions("Character_move"));
        movementAnimation.setPlayMode(Animation.PlayMode.LOOP);

        /// Load hit animation
        attackAnimation = new Animation<>(1f / 30f, animAtlas.findRegions("Character_hit"));
        attackAnimation.setPlayMode(Animation.PlayMode.LOOP);

        /// Load idle torch animation
        idleTorchAnimation = new Animation<>(1f / 8f, animAtlas.findRegions("Character_idle_torch"));
        idleTorchAnimation.setPlayMode(Animation.PlayMode.LOOP);

        /// Load movement torch animation
        movementTorchAnimation = new Animation<>(1f /40f * 3f, animAtlas.findRegions("Character_move_torch"));
        movementTorchAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // To test the HUD, uncomment the following line
//        hud.forTesting();

    }

    /**
     * Renders the player character.
     * @param deltaTime The time since the last frame in seconds
     */
    public void render(float deltaTime) {
        /// Stamina regeneration?
        elapsedTime += deltaTime;
        if (isAttacking) {
            attackElapsedTime += deltaTime;
        }

        /// Update damage flash timer
        if (isDamaged) {
            damageFlashTimer += deltaTime;
            if (damageFlashTimer >= DAMAGE_FLASH_DURATION) {
                isDamaged = false;
                damageFlashTimer = 0f;
                /// Reset color back to normal
                batch.setColor(new Color(1,1,1, 1));
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
            batch.setColor(new Color(1,0,0, 1));
        }

        /// Draw the current frame
        float frameWidth = currentFrame.getRegionWidth() * scale;
        float frameHeight = currentFrame.getRegionHeight() * scale;
        batch.draw(currentFrame, getSpriteX(), getSpriteY(), frameWidth, frameHeight);

        if (isAttacking && attackAnimation.getAnimationDuration() / 2 < attackElapsedTime) {
            //TODO: Attack enemy
        }

        /// Check if hit animation is finished
        if (isAttacking && attackAnimation.isAnimationFinished(attackElapsedTime)) {
            isAttacking = false; // Reset hit state
            canHit = !isHoldingTorch; // Reset hit cooldown if not holding torch
            attackElapsedTime = 0f; // Reset hit animation time
        }

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
        body.setLinearVelocity(velocityX, velocityY);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && canHit) {
            isAttacking = true;
            attackElapsedTime = 0f;
            canHit = false;
        }

        /// Update animation while preserving hit animation priority
        if (isAttacking) {
            currentAnimation = attackAnimation;
        } else {
            if (isHoldingTorch){
                currentAnimation = isMoving ? movementTorchAnimation : idleTorchAnimation;
            } else {
                currentAnimation = isMoving ? movementAnimation : idleAnimation;
            }
        }

        /// Move camera with the player
        updateCameraPosition();
    }

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
        float maxX = mapWidth - effectiveViewportWidth / 2;
        newCameraX = Math.min(maxX, Math.max(minX, newCameraX));

        /// Prevent showing area beyond top and bottom edges
        float minY = effectiveViewportHeight / 2;
        float maxY = mapHeight - effectiveViewportHeight / 2;
        newCameraY = Math.min(maxY, Math.max(minY, newCameraY));

        /// Update camera position
        camera.position.x = newCameraX;
        camera.position.y = newCameraY;
    }


    public boolean isFacingRight() {
        return facingRight;
    }

    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        isDamaged = true;
        damageFlashTimer = 0f;
    }

    @Override
    public void heal(int amount) {
        super.heal(amount);
        //hud.hea(amount);
    }

    @Override
    public void useStamina(int amount) {
        super.useStamina(amount);
        //hud.useStamina(amount);
    }

    @Override
    public void restoreStamina(int amount) {
        super.restoreStamina(amount);
        //hud.restoreStamina(amount);
    }

    public void collectItem(Collectable item) {
        //TODO: implement item collection
    }
    public void collectBuff(Collectable buff) {
        // TODO: animation when collecting buff

        // hud.addStatus(buff.getTextureName());
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
}
