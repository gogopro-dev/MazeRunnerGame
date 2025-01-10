package de.tum.cit.fop.maze.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;

/**
 * Represents the player character in the game.
 */
public class Player extends Entity {
    private final OrthographicCamera camera;
    private final SpriteBatch spriteBatch;
    private final Animation<TextureRegion> playerAnimation;
    private final Animation<TextureRegion> movementAnimation;
    private final Animation<TextureRegion> hitAnimation;
    private Animation<TextureRegion> currentAnimation;
    private float elapsedTime = 0f;
    private boolean isHitting = false;    // Track if the hit animation is active
    private float hitElapsedTime = 0f;    // Tracks time for hit animation
    private final float scale = 4f;       // Scale factor for player sprite
    private boolean isMoving = false;     // Flag to track movement state
    private float playerX, playerY;       // Player's position
    private float playerSpeed = 500f;     // Player movement speed (pixels per second)
    private boolean facingRight = true;
    private boolean canHit = true;
    private final int mapWidth;
    private final int mapHeight;

    /**
     * Creates a new player character.
     * @param camera The camera to follow the player
     * @param mapWidth The width of the map in pixels
     * @param mapHeight The height of the map in pixels
     */
    public Player(OrthographicCamera camera, int mapWidth, int mapHeight) {
        super();
        this.camera = camera;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;

        playerX = (float) mapWidth / 2;
        playerY = (float) mapHeight / 2;


        spriteBatch = new SpriteBatch(); // Create SpriteBatch
        // Load idle animation
        TextureAtlas idleAtlas = new TextureAtlas(Gdx.files.internal("anim/player/Character_stay.atlas"));
        playerAnimation = new Animation<>(1f / 8f, idleAtlas.getRegions());
        playerAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Load movement animation
        TextureAtlas moveAtlas = new TextureAtlas(Gdx.files.internal("anim/player/Character_move.atlas"));
        movementAnimation = new Animation<>(1f /40f * 3f, moveAtlas.getRegions());
        movementAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Load hit animation
        TextureAtlas hitAtlas = new TextureAtlas(Gdx.files.internal("anim/player/character_hit.atlas"));
        hitAnimation = new Animation<>(1f / 30f, hitAtlas.getRegions());
        hitAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }
    /**
     * Renders the player character.
     * @param deltaTime The time since the last frame in seconds
     */
    public void render(float deltaTime) {

        elapsedTime += deltaTime;
        if (isHitting) {
            hitElapsedTime += deltaTime;
        }
        // Handle player input and movement
        handleInput(deltaTime);

        camera.update();
        spriteBatch.setProjectionMatrix(camera.combined);

        // Begin rendering
        spriteBatch.begin();
        // Get the current animation frame
        TextureRegion currentFrame = (isHitting ? hitAnimation : currentAnimation)
            .getKeyFrame(isHitting ? hitElapsedTime : elapsedTime, true);

        // Flip the frame if needed
        if (facingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false); // Unflip horizontally if facing right
        } else if (!facingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false); // Flip horizontally if facing left
        }

        // Draw the current frame
        float frameWidth = currentFrame.getRegionWidth() * scale;
        float frameHeight = currentFrame.getRegionHeight() * scale;
        spriteBatch.draw(currentFrame, playerX, playerY, frameWidth, frameHeight);

        spriteBatch.end();

        // Check if hit animation is finished
        if (isHitting && hitAnimation.isAnimationFinished(hitElapsedTime)) {
            isHitting = false; // Reset hit state
            canHit = true; // Reset hit cooldownI
            hitElapsedTime = 0f; // Reset hit animation time
        }
    }

    /**
     * Handles player input and updates the player's position.
     * @param deltaTime The time since the last frame in seconds
     */
    private void handleInput(float deltaTime) {
        playerSpeed = 500f;
        isMoving = false;

        // Only update facing direction if not in hit animation
        if (!isHitting) {
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                facingRight = true;
            } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                facingRight = false;
            }
        }

        // Handle keyboard input and allow movement during hit animation
        if (Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerX += (float) (playerSpeed * deltaTime / Math.sqrt(2));
            playerY += (float) (playerSpeed * deltaTime / Math.sqrt(2));
            isMoving = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isKeyPressed(Input.Keys.S)) {
            playerX += (float) (playerSpeed * deltaTime / Math.sqrt(2));
            playerY -= (float) (playerSpeed * deltaTime / Math.sqrt(2));
            isMoving = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerX -= (float) (playerSpeed * deltaTime / Math.sqrt(2));
            playerY += (float) (playerSpeed * deltaTime / Math.sqrt(2));
            isMoving = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isKeyPressed(Input.Keys.S)) {
            playerX -= (float) (playerSpeed * deltaTime / Math.sqrt(2));
            playerY -= (float) (playerSpeed * deltaTime / Math.sqrt(2));
            isMoving = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerY += playerSpeed * deltaTime;
            isMoving = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            playerY -= playerSpeed * deltaTime;
            isMoving = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerX -= playerSpeed * deltaTime;
            isMoving = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerX += playerSpeed * deltaTime;
            isMoving = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && canHit) {
            isHitting = true;
            hitElapsedTime = 0f;
            canHit = false;
            currentAnimation = hitAnimation;
        }

        // Update animation while preserving hit animation priority
        if (isHitting) {
            currentAnimation = hitAnimation;
        } else {
            currentAnimation = isMoving ? movementAnimation : playerAnimation;
        }

        float cameraX = camera.position.x;
        float cameraY = camera.position.y;
        float cameraWidth = camera.viewportWidth;
        float cameraHeight = camera.viewportHeight;

        /// Move camera with player
        if (playerX > cameraX + cameraWidth / 6){
            camera.position.x += playerSpeed * deltaTime;
        }
        if (playerY > cameraY + cameraHeight / 6){
            camera.position.y += playerSpeed * deltaTime;
        }
        if (playerX < cameraX - cameraWidth / 4){
            camera.position.x -= playerSpeed * deltaTime;
        }
        if (playerY < cameraY - cameraHeight / 4){
            camera.position.y -= playerSpeed * deltaTime;
        }

        /// Clamp camera position to map bounds
        if (camera.position.x < cameraWidth/2){
            camera.position.x = cameraWidth/2;                  // Clamp camera to left edge
        }
        if (camera.position.y < cameraHeight/2){
            camera.position.y = cameraHeight/2;                 // Clamp camera to bottom edge
        }
        if (camera.position.x > mapWidth - cameraWidth/2){
            camera.position.x = mapWidth - cameraWidth/2;       // Clamp camera to right edge
        }
        if (camera.position.y > mapHeight - cameraHeight/2){
            camera.position.y = mapHeight - cameraHeight/2;     // Clamp camera to top edge
        }
    }

    public void dispose() {
        spriteBatch.dispose();
    }
}
