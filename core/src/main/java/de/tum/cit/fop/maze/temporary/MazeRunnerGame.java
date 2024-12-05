package de.tum.cit.fop.maze.temporary;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
//import games.spooky.gdx.nativefilechooser.NativeFileChooser;
/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */
public class MazeRunnerGame extends Game {
    // Screens
//    private MenuScreen menuScreen;
//    private GameScreen gameScreen;

    // Sprite Batch for rendering
    private SpriteBatch spriteBatch;
    private TextureAtlas playerAtlas;
    private Animation<TextureRegion> playerAnimation;
    private Sprite player;
    private OrthographicCamera camera;
    private Texture playerTexture;


    private float elapsedTime = 0f;
    private boolean isHit = false; // Track if the hit animation is active

    private float hitElapsedTime = 0f; // Tracks time for hit animation
    private float scale = 5f;
    private boolean isMoving = false; // Flag to track movement state
    private Animation<TextureRegion> movementAnimation;
    private Animation<TextureRegion> currentAnimation; // Active animation
    private float playerX = 0f, playerY = 0f; // Player's position
    private float playerSpeed = 500f; // Player movement speed (pixels per second)
    private boolean facingRight = true;
    private boolean canHit = true;


    private Animation<TextureRegion> hitAnimation;
    // UI Skin
    private Skin skin;

    // Character animation downwards
    private Animation<TextureRegion> characterDownAnimation;

    /**
     * Constructor for MazeRunnerGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environment.
     */
//    public MazeRunnerGame(NativeFileChooser fileChooser) {
//        super();
//    }

    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     */
    @Override
    public void create() {
        spriteBatch = new SpriteBatch(); // Create SpriteBatch
        //skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json")); // Load UI skin
        //this.loadCharacterAnimation(); // Load character animation

        // Play some background music
        // Background sound
//        Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
//        backgroundMusic.setLooping(true);
//        backgroundMusic.play();

        //goToMenu(); // Navigate to the menu screen


        spriteBatch = new SpriteBatch(); // Create SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0,0, 0);
        playerAtlas = new TextureAtlas(Gdx.files.internal("temporary/Character_stay.atlas"));

        playerAnimation = new Animation<>(1f/8f, playerAtlas.getRegions());
        playerAnimation.setPlayMode(Animation.PlayMode.LOOP);


        spriteBatch = new SpriteBatch(); // Create SpriteBatch
        camera = new OrthographicCamera();

        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0, 0, 0);

        // Load idle animation
        TextureAtlas idleAtlas = new TextureAtlas(Gdx.files.internal("temporary/Character_stay.atlas"));
        playerAnimation = new Animation<>(1f / 8f, idleAtlas.getRegions());
        playerAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Load movement animation
        TextureAtlas moveAtlas = new TextureAtlas(Gdx.files.internal("temporary/Character_move.atlas"));
        movementAnimation = new Animation<>(1f /40f * 3f, moveAtlas.getRegions());
        movementAnimation.setPlayMode(Animation.PlayMode.LOOP);

        TextureAtlas hitAtlas = new TextureAtlas(Gdx.files.internal("temporary/character_hit.atlas"));
        hitAnimation = new Animation<>(1f / 30f, hitAtlas.getRegions());
        hitAnimation.setPlayMode(Animation.PlayMode.LOOP);

        currentAnimation = playerAnimation; // Start with idle animation
    }

    /**
     * Switches to the menu screen.
     */
//    public void goToMenu() {
//        this.setScreen(new MenuScreen(this)); // Set the current screen to MenuScreen
//        if (gameScreen != null) {
//            gameScreen.dispose(); // Dispose the game screen if it exists
//            gameScreen = null;
//        }
//    }

    /**
     * Switches to the game screen.
     */
//    public void goToGame() {
//        this.setScreen(new GameScreen(this)); // Set the current screen to GameScreen
//        if (menuScreen != null) {
//            menuScreen.dispose(); // Dispose the menu screen if it exists
//            menuScreen = null;
//        }
//    }

    /**
     * Loads the character animation from the character.png file.
     */
    private void loadCharacterAnimation() {
        Texture walkSheet = new Texture(Gdx.files.internal("character.png"));

        int frameWidth = 16;
        int frameHeight = 32;
        int animationFrames = 4;

        // libGDX internal Array instead of ArrayList because of performance
        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);

        // Add all frames to the animation
        for (int col = 0; col < animationFrames; col++) {
            walkFrames.add(new TextureRegion(walkSheet, col * frameWidth, 0, frameWidth, frameHeight));
        }

        characterDownAnimation = new Animation<>(0.1f, walkFrames);
    }

    /**
     * Cleans up resources when the game is disposed.
     */
    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        playerAtlas.dispose();
        skin.dispose(); // Dispose the skin
    }

    @Override
    public void render() {
//        elapsedTime += Gdx.graphics.getDeltaTime();
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        spriteBatch.setProjectionMatrix(camera.combined);
//
//        spriteBatch.begin();
//        TextureRegion currentFrame = playerAnimation.getKeyFrame(elapsedTime, true);
//
//        // Draw the scaled animation frame
//        float frameWidth = currentFrame.getRegionWidth() * scale;
//        float frameHeight = currentFrame.getRegionHeight() * scale;
//        spriteBatch.draw(currentFrame, 0, 0, frameWidth, frameHeight);
//
//        spriteBatch.end();
//        camera.update();
//
//        super.render();

        elapsedTime += Gdx.graphics.getDeltaTime();
        if (isHit) {
            hitElapsedTime += Gdx.graphics.getDeltaTime();
        }

        // Handle player input and movement
        handleInput();

        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(220/255f, 220/255f, 220/255f, 1);

        // Update camera
        camera.update();
        spriteBatch.setProjectionMatrix(camera.combined);

        // Begin rendering
        spriteBatch.begin();

        // Get the current animation frame
        TextureRegion currentFrame = (isHit ? hitAnimation : currentAnimation)
            .getKeyFrame(isHit ? hitElapsedTime : elapsedTime, true);

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
        if (isHit && hitAnimation.isAnimationFinished(hitElapsedTime)) {
            isHit = false; // Reset hit state
            canHit = true; // Reset hit cooldownI
            hitElapsedTime = 0f; // Reset hit animation time
        }

        // Call parent render
        super.render();
    }

    private void handleInput() {
        playerSpeed = 500f; // Reset player speed
        isMoving = false; // Reset movement flag



        if (Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerX += (float) (playerSpeed * Gdx.graphics.getDeltaTime() / Math.sqrt(2));
            playerY += (float) (playerSpeed * Gdx.graphics.getDeltaTime() / Math.sqrt(2));
            isMoving = true;
            facingRight = true; // Character now faces right
        } else
        if (Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isKeyPressed(Input.Keys.S)) {
            playerX += (float) (playerSpeed * Gdx.graphics.getDeltaTime() / Math.sqrt(2));
            playerY -= (float) (playerSpeed * Gdx.graphics.getDeltaTime() / Math.sqrt(2));
            isMoving = true;
            facingRight = true; // Character now faces right
        } else
        if (Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerX -= (float) (playerSpeed * Gdx.graphics.getDeltaTime() / Math.sqrt(2));
            playerY += (float) (playerSpeed * Gdx.graphics.getDeltaTime() / Math.sqrt(2));
            isMoving = true;
            facingRight = false; // Character now faces right
        } else
        if (Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isKeyPressed(Input.Keys.S)) {
            playerX -= (float) (playerSpeed * Gdx.graphics.getDeltaTime() / Math.sqrt(2));
            playerY -= (float) (playerSpeed * Gdx.graphics.getDeltaTime() / Math.sqrt(2));
            isMoving = true;
            facingRight = false; // Character now faces right
        } else
            // Check for movement input and update position/direction
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                playerY += playerSpeed * Gdx.graphics.getDeltaTime();
                isMoving = true;
            }else
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                playerY -= playerSpeed * Gdx.graphics.getDeltaTime();
                isMoving = true;
            }else
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                playerX -= playerSpeed * Gdx.graphics.getDeltaTime();
                isMoving = true;
                facingRight = false; // Character now faces left
            }else
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                playerX += playerSpeed * Gdx.graphics.getDeltaTime();
                isMoving = true;
                facingRight = true; // Character now faces right
            }
        // Update the current animation based on movement state
        if (isMoving && !currentAnimation.equals(hitAnimation)) {
            currentAnimation = movementAnimation;
        } else {
            currentAnimation = playerAnimation;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && canHit) {
            isHit = true; // Activate hit animation
            hitElapsedTime = 0f; // Reset hit animation time
            canHit = false;
            currentAnimation = hitAnimation; // Set hit animation as current
        }
        // Update the current animation based on movement state (if not in hit state)
        if (!isHit) {
            if (isMoving) {
                currentAnimation = movementAnimation;
            } else {
                currentAnimation = playerAnimation;
            }
        }
    }
    // Getter methods
    public Skin getSkin() {
        return skin;
    }

    public Animation<TextureRegion> getCharacterDownAnimation() {
        return characterDownAnimation;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}

