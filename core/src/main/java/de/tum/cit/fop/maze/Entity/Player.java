package de.tum.cit.fop.maze.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import de.tum.cit.fop.maze.Collectable;

/**
 * Represents the player character in the game.
 */
public class Player extends Entity {
    private final HUD hud;
    private final OrthographicCamera camera;
    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> movementAnimation;
    private final Animation<TextureRegion> hitAnimation;
    private final Animation<TextureRegion> idleTorchAnimation;
    private final Animation<TextureRegion> movementTorchAnimation;
    private Animation<TextureRegion> currentAnimation;
    private float elapsedTime = 0f;
    private boolean isHitting = false;    // Track if the hit animation is active
    private float hitElapsedTime = 0f;    // Tracks time for hit animation
    private boolean isMoving = false;     // Flag to track movement state
    private boolean facingRight = true;
    private boolean canHit = true;
    private boolean isHoldingTorch = true;
    private final float mapWidth;
    private final float mapHeight;

    /**
     * Creates a new player character.
     * @param camera The camera to follow the player
     * @param mapWidth The width of the map in pixels
     * @param mapHeight The height of the map in pixels
     */
    public Player(OrthographicCamera camera, float mapWidth, float mapHeight, SpriteBatch batch) {

        super(batch);
        this.box2dUserData = "player";
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;

        /// Player can hit if not holding torch
        canHit = !isHoldingTorch;

        this.camera = camera;

        TextureAtlas animAtlas = new TextureAtlas(Gdx.files.internal("anim/player/character.atlas"));
        /// Load idle animation
        idleAnimation = new Animation<>(1f / 8f, animAtlas.findRegions("Character_idle"));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        /// Load movement animation
        movementAnimation = new Animation<>(1f /40f * 3f, animAtlas.findRegions("Character_move"));
        movementAnimation.setPlayMode(Animation.PlayMode.LOOP);

        /// Load hit animation
        hitAnimation = new Animation<>(1f / 30f, animAtlas.findRegions("Character_hit"));
        TextureAtlas animAtlas = new TextureAtlas(Gdx.files.internal("anim/player/character.atlas"));
        /// Load idle animation
        idleAnimation = new Animation<>(1f / 8f, animAtlas.findRegions("Character_idle"));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        /// Load movement animation
        movementAnimation = new Animation<>(1f /40f * 3f, animAtlas.findRegions("Character_move"));
        /// Load hit animation
        hitAnimation = new Animation<>(1f / 30f, animAtlas.findRegions("Character_hit"));
        hitAnimation.setPlayMode(Animation.PlayMode.LOOP);

        /// Load idle torch animation
        idleTorchAnimation = new Animation<>(1f / 8f, animAtlas.findRegions("Character_idle_torch"));
        idleTorchAnimation.setPlayMode(Animation.PlayMode.LOOP);

        /// Load movement torch animation
        movementTorchAnimation = new Animation<>(1f /40f * 3f, animAtlas.findRegions("Character_move_torch"));
        movementTorchAnimation.setPlayMode(Animation.PlayMode.LOOP);

        hud = new HUD(health, maxHealth);
        // To test the HUD, uncomment the following line
//        hud.forTesting();

    }
    /**
     * Renders the player character.
     * @param deltaTime The time since the last frame in seconds
     */
    public void render(float deltaTime) {
        /// Stamina regeneration?
        hud.restoreStamina(1f/60f);
        hud.render();
        elapsedTime += deltaTime;
        if (isHitting) {
            hitElapsedTime += deltaTime;
        }
        /// Handle player input and movement
        handleInput();

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        /// Begin rendering
        batch.begin();

        /// Get the current animation frame
        TextureRegion currentFrame = (isHitting ? hitAnimation : currentAnimation)
            .getKeyFrame(isHitting ? hitElapsedTime : elapsedTime, true);

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


        /// Check if hit animation is finished
        if (isHitting && hitAnimation.isAnimationFinished(hitElapsedTime)) {
            isHitting = false; // Reset hit state
            canHit = !isHoldingTorch; // Reset hit cooldown if not holding torch
            hitElapsedTime = 0f; // Reset hit animation time
        }
        batch.end();

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
        if (!isHitting) {
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
            isHitting = true;
            hitElapsedTime = 0f;
            canHit = false;
        }

        /// Update animation while preserving hit animation priority
        if (isHitting) {
            currentAnimation = hitAnimation;
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
        /// 0.3f is a constant that can be adjusted to change the camera movement threshold
        /// Less than 0.2f: Player can go out ob bounds of the screen
        /// More than 0.4f: Camera moves too early
        float boundaryLeft = cameraX - cameraWidth * 0.3f;
        float boundaryRight = cameraX + cameraWidth * 0.3f;
        float boundaryBottom = cameraY - cameraHeight * 0.3f;
        float boundaryTop = cameraY + cameraHeight * 0.3f;

        float playerX = getSpriteX();
        float playerY = getSpriteY();

        /// Calculate new camera position based on player position

        float newCameraX = camera.position.x;
        float newCameraY = camera.position.y;

        /// Handle X-axis camera movement
        if (playerX < boundaryLeft) {
            newCameraX = playerX + cameraWidth * 0.3f;
        } else if (playerX > boundaryRight) {
            newCameraX = playerX - cameraWidth * 0.3f;
        }

        /// Handle Y-axis camera movement
        if (playerY < boundaryBottom) {
            newCameraY = playerY + cameraHeight * 0.3f;
        } else if (playerY > boundaryTop) {
            newCameraY = playerY - cameraHeight * 0.3f;
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
        hud.takeDmg(damage);
    }

    @Override
    public void heal(int amount) {
        super.heal(amount);
        hud.heal(amount);
    }

    @Override
    public void useStamina(int amount) {
        super.useStamina(amount);
        hud.useStamina(amount);
    }

    @Override
    public void restoreStamina(int amount) {
        super.restoreStamina(amount);
        hud.restoreStamina(amount);
    }

    public void collectItem(Collectable item) {
        //TODO: implement item collection
    }
    public void collectBuff(Collectable buff) {
        // TODO: animation when collecting buff

        hud.addStatus(buff.getTextureName());
    }


}
