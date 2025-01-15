package de.tum.cit.fop.maze.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import de.tum.cit.fop.maze.commonFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * The HUD class represents the Heads-Up Display for the game, which includes health and stamina bars,
 * and handles animations and user interactions related to the HUD elements.
 */
public class HUD {
    private int health;
    private int maxHealth;

    private final Stage stage;
    //TODO: replace w texture atlas
    private Skin skin;

    private final List<Image> hpBar;
    private final List<Image> statusBar;
//    private Map<String, Float> statusDurations;
    private TextureAtlas statuses;
    private Button takeDmgButton;
    private int receivedDmg;

    private Animation<TextureRegion> animationHeartFullToHalf;
    private Animation<TextureRegion> animationHeartHalfToEmpty;
    private Animation<TextureRegion> animationHeartFullToEmpty;
    private final float heartsAnimationFrameDuration;
    private final SpriteBatch animations;

    private ProgressBar staminaBar;
    private int currentSeconds;
    private boolean gotHit;
    /// Time elapsed since the last update
    private float hitElapsedTime;

    /**
     * Constructor to initialize the HUD elements.
     */
    public HUD(int health, int maxHealth) {
        hpBar = new ArrayList<>();
        statusBar = new ArrayList<>();

        currentSeconds = 0;
        heartsAnimationFrameDuration = 0.25f;
        animations = new SpriteBatch();
        this.stage = new Stage();
        Gdx.input.setInputProcessor(this.stage);
        gotHit = false;

        initSkins();
        initAnimations();


        this.health = health;
        this.maxHealth = maxHealth;

        updateHPBar();
        createStaminaBar();
    }

    /**
     * Checks if the player's health is at a half-heart level.
     *
     * @return true if the health is at a half-heart level, false otherwise.
     */
    public boolean hpCheckHeartIsHalf() {
        return health % 2 != 0;
    }

    /**
     * Updates the health bar based on the current health value.
     */
    public void updateHPBar() {
        int hearts = health / 2;
        int emptyHearts = (maxHealth - health) / 2;
        if (health < 0) {
            return;
        }

        float offset = 0;

        // Create full hearts
        Texture heart = skin.get("full", Texture.class);
        for (int i = 0; i < hearts; i++) {
            Image eachHeart = new Image(heart);
            hpBar.add(eachHeart);
            eachHeart.setPosition(offset, Gdx.graphics.getHeight() - eachHeart.getHeight());
            offset += eachHeart.getWidth();
            eachHeart.setName("full");
            stage.addActor(eachHeart);
        }

        // Create half hearts
        if (hpCheckHeartIsHalf()) {
            Image halfHeartImg = new Image(skin.get("half", Texture.class));
            this.hpBar.add(halfHeartImg);
            halfHeartImg.setPosition(offset, Gdx.graphics.getHeight() - halfHeartImg.getHeight());
            stage.addActor(halfHeartImg);
            halfHeartImg.setName("half");
            offset += halfHeartImg.getWidth();
        }

        // Create empty hearts
        if (emptyHearts != 0) {
            heart = skin.get("empty", Texture.class);
            for (int i = 0; i < emptyHearts; i++) {
                Image eachHeart = new Image(heart);
                hpBar.add(eachHeart);
                eachHeart.setPosition(offset, Gdx.graphics.getHeight() - eachHeart.getHeight());
                offset += eachHeart.getWidth();
                eachHeart.setName("empty");
                stage.addActor(eachHeart);
            }
        }
    }
    /**
     * Disposes of the health bar elements.
     */
    public void disposeHPBar() {
        for (Image image : hpBar) {
            image.remove();
        }
        hpBar.clear();
    }

    /**
     * Initializes the stamina bar.
     */
    public void createStaminaBar() {
        int width = 100;
        int height = 10;

        staminaBar = new ProgressBar(0f, 100f, 0.01f, false, new ProgressBar.ProgressBarStyle());
        staminaBar.getStyle().background = commonFunctions.getColoredDrawable(width, height, Color.WHITE);
        staminaBar.getStyle().knob = commonFunctions.getColoredDrawable(0, height, Color.GOLD);
        staminaBar.getStyle().knobBefore = commonFunctions.getColoredDrawable(width, height, Color.GOLD);

        staminaBar.setWidth(width);
        staminaBar.setHeight(height);

        staminaBar.setValue(100f);
        staminaBar.setAnimateDuration(0.25f);

        staminaBar.setPosition(5, hpBar.get(0).getY() - 15);
        stage.addActor(staminaBar);
    }



    public void useStamina(float amount) {
        staminaBar.setValue(staminaBar.getValue() - amount);
    }

    public void restoreStamina(float amount) {
        staminaBar.setValue(staminaBar.getValue() + amount);
    }


    public void addStatus(String statusName) {
        TextureAtlas.AtlasRegion region = statuses.findRegion(statusName);
        Image statusImg = new Image(region);
        statusBar.add(statusImg);
//        statusDurations.put(statusName, duration);
    }

//    /**
//     * Updates the status effects based on the elapsed time.
//     */
//    public void updateStatuses() {
//        for (Image status : statusBar) {
//            statusDurations.put(status.getName(), statusDurations.get(status.getName()) - 1);
//            if (statusDurations.get(status.getName()) <= 0) {
//                stat`us.remove();
//                statusBar.remove(status);
//            }
//        }
//    }

    /**
     * Renders the status effects on the screen.
     */
    public void renderStatuses() {
        if (statusBar.isEmpty()) {
            return;
        }
        float x = Math.max(hpBar.get(hpBar.size() - 1).getX() + hpBar.get(hpBar.size() - 1).getWidth(), staminaBar.getX() + staminaBar.getWidth());
        for (Image status : statusBar) {
            status.setPosition(x, Gdx.graphics.getHeight() - statusBar.get(0).getHeight());
            x += status.getWidth();
            stage.addActor(status);
        }
    }

    /**
     * Animates the hearts from full to empty.
     */
    public void animateHeartsFullToEmpty() {
        int dmg = this.receivedDmg;
        int even = (dmg % 2 + (health + dmg) % 2) % 2;

        for (int i = health / 2 + even; i < (health + this.receivedDmg) / 2; i++) {
            TextureRegion currentFrame = animationHeartFullToEmpty.getKeyFrame(hitElapsedTime);
            Image currentHeart = hpBar.get(i);
            int frameWidth = currentFrame.getRegionWidth();
            int frameHeight = currentFrame.getRegionHeight();
            animations.draw(currentFrame, currentHeart.getX(), currentHeart.getY(), frameWidth, frameHeight);
        }
    }

    /**
     * Animates a heart from full to half.
     *
     * @param indexOfHeartToAnimate the index of the heart to animate.
     */
    public void animateHeartFullToHalf(int indexOfHeartToAnimate) {
        TextureRegion currentFrame = animationHeartFullToHalf.getKeyFrame(hitElapsedTime);
        Image currentHeart = hpBar.get(indexOfHeartToAnimate);
        int frameWidth = currentFrame.getRegionWidth();
        int frameHeight = currentFrame.getRegionHeight();
        animations.draw(currentFrame, currentHeart.getX(), currentHeart.getY(), frameWidth, frameHeight);
    }

    /**
     * Animates a heart from half to empty.
     *
     * @param indexOfHeartToAnimate the index of the heart to animate.
     */
    public void animateHeartHalfToEmpty(int indexOfHeartToAnimate) {
        TextureRegion currentFrame = animationHeartHalfToEmpty.getKeyFrame(hitElapsedTime);
        Image currentHeart = hpBar.get(indexOfHeartToAnimate);
        int frameWidth = currentFrame.getRegionWidth();
        int frameHeight = currentFrame.getRegionHeight();
        animations.draw(currentFrame, currentHeart.getX(), currentHeart.getY(), frameWidth, frameHeight);
    }

    /**
     * Checks if all heart animations are finished.
     *
     * @return true if all animations are finished, false otherwise.
     */
    public boolean getAllAnimationsFinished() {
        return animationHeartHalfToEmpty.isAnimationFinished(hitElapsedTime) && animationHeartFullToHalf.isAnimationFinished(hitElapsedTime) && animationHeartFullToEmpty.isAnimationFinished(hitElapsedTime);
    }

    /**
     * Handles taking damage and updating the health bar.
     *
     * @param dmg the amount of damage received.
     */
    public void takeDmg(int dmg) {
        receivedDmg = dmg;
        health -= receivedDmg;
        gotHit = true;
    }

    /**
     * Animates the health bar based on the received damage.
     */
    public void animateTakeDmg() {
        if (health <= 0) {
            health = 0;
            animateHeartsFullToEmpty();
            return;
        }
        disposeHPBar();
        updateHPBar();
        if (receivedDmg == 0) {
            return;
        }
        if ((health + receivedDmg) % 2 != 0) {
            animateHeartHalfToEmpty((receivedDmg + health) / 2);
        }
        if (health % 2 != 0) {
            animateHeartFullToHalf(health / 2);
        }
        animateHeartsFullToEmpty();

        if (getAllAnimationsFinished()) {
            hitElapsedTime = 0f;
        }
    }


    //TODO: Implement animateHeal
    public void animateHeal() {
    }

    /**
     * Renders the stamina bar and updates its value based on the elapsed time.
     *
     * @param elapsedTime the time elapsed since the last update.
     *
     */
    public void testStaminaBar(float elapsedTime) {
        if (staminaBar.getValue() <= 0 && currentSeconds < Math.round(elapsedTime)) {
            staminaBar.setValue(staminaBar.getValue() + 0.1f);
            currentSeconds = Math.round(elapsedTime);
        } else if (currentSeconds < Math.round(elapsedTime)) {
            staminaBar.setValue(staminaBar.getValue() - 0.1f);
            currentSeconds = Math.round(elapsedTime);
        }
    }

    /**
     * Initializes the skins for the HUD elements.
     */
    public void initSkins() {
        skin = new Skin();
        skin.add("status1", new Texture(Gdx.files.internal("temporary/statuses/status1.png")));
        skin.add("status2", new Texture(Gdx.files.internal("temporary/statuses/status2.png")));
        skin.add("status3", new Texture(Gdx.files.internal("temporary/statuses/status3.png")));
        skin.add("full", new Texture(Gdx.files.internal("temporary/heart/Health1.png")));
        skin.add("half", new Texture(Gdx.files.internal("temporary/heart/Health3.png")));
        skin.add("empty", new Texture(Gdx.files.internal("temporary/heart/Health7.png")));

        statuses = new TextureAtlas(Gdx.files.internal("temporary/statuses/statuses.atlas"));
        // buffHeal, buffSpeed, buffStamina, buffMaxHP buffDamage
    }

    /**
     * Initializes the animations for heart transitions.
     */
    public void initAnimations() {
        TextureAtlas heartFullToHalf = new TextureAtlas(Gdx.files.internal("temporary/heart/health_full.atlas"));
        animationHeartFullToHalf = new Animation<>(heartsAnimationFrameDuration, heartFullToHalf.getRegions());
        animationHeartFullToHalf.setPlayMode(Animation.PlayMode.LOOP);

        TextureAtlas heartHalfToEmpty = new TextureAtlas(Gdx.files.internal("temporary/heart/health_half.atlas"));
        animationHeartHalfToEmpty = new Animation<>(heartsAnimationFrameDuration, heartHalfToEmpty.getRegions());
        animationHeartHalfToEmpty.setPlayMode(Animation.PlayMode.LOOP);

        TextureAtlas heartFullToEmpty = new TextureAtlas(Gdx.files.internal("temporary/heart/fullToEmpty.atlas"));
        animationHeartFullToEmpty = new Animation<>(heartsAnimationFrameDuration, heartFullToEmpty.getRegions());
        animationHeartFullToEmpty.setPlayMode(Animation.PlayMode.LOOP);

        hitElapsedTime = Math.max(animationHeartFullToHalf.getAnimationDuration(), animationHeartFullToEmpty.getAnimationDuration());
    }

    /**
     * Renders the HUD elements.
     */
    public void render() {
        stage.act();
        stage.draw();
        animations.begin();
        if (gotHit) {
            animateTakeDmg();
            hitElapsedTime += Gdx.graphics.getDeltaTime();
            if (getAllAnimationsFinished()) {
                gotHit = false;
            }
        }

        renderStatuses();
        animations.end();
    }

    /**
     * Resizes the HUD elements based on the new width and height.
     *
     * @param width  the new width.
     * @param height the new height.
     */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Disposes of the HUD elements.
     */
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
    /**
     * Heals the player by a specified amount.
     *
     * @param amount the amount to heal the player by.
     */
    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) {
            health = maxHealth;
        }
        gotHit = false;
        updateHPBar();
    }
    /**
     * Creates a button to simulate healing for testing purposes.
     */
    public void createHealButton(){
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.font = new BitmapFont();

        TextButton healButton = new TextButton("Heal", textButtonStyle);
        healButton.setPosition(20, takeDmgButton.getY() - 30);
        healButton.addCaptureListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
                buttonStyle.fontColor = Color.GREEN;
                buttonStyle.font = new BitmapFont();
                healButton.setStyle(buttonStyle);
                System.out.println("Heal button clicked");
                heal(3);
                return true;
            }
        });

        stage.addActor(healButton);
    }

    /**
     * Creates a button to simulate taking damage for testing purposes.
     */
    public void createDamageButton() {
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.font = new BitmapFont();

        takeDmgButton = new TextButton("Take Damage", textButtonStyle);
        takeDmgButton.setPosition(20, Gdx.graphics.getHeight() / 2f);
        takeDmgButton.addCaptureListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
                buttonStyle.fontColor = Color.RED;
                buttonStyle.font = new BitmapFont();
                takeDmgButton.setStyle(buttonStyle);
                System.out.println("Damage button clicked");
                takeDmg(3);
                return true;
            }
        });

        stage.addActor(takeDmgButton);
    }
    /**
     * Creates a button to simulate using stamina for testing purposes.
     */
    public void createStaminaButton() {
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.font = new BitmapFont();

        TextButton staminaButton = new TextButton("Use Stamina", textButtonStyle);
        staminaButton.setPosition(20, takeDmgButton.getY() - 60);
        staminaButton.addCaptureListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
                buttonStyle.fontColor = Color.RED;
                buttonStyle.font = new BitmapFont();
                staminaButton.setStyle(buttonStyle);
                System.out.println("Stamina button clicked");
                useStamina(20);
                return true;
            }
        });

        stage.addActor(staminaButton);
    }
    /**
     * Creates a button to simulate adding a status effect for testing purposes.
     */
    public void createAddStatusButton(){
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.font = new BitmapFont();

        TextButton statusButton = new TextButton("Add Status", textButtonStyle);
        statusButton.setPosition(20, takeDmgButton.getY() - 90);
        statusButton.addCaptureListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
                buttonStyle.fontColor = Color.RED;
                buttonStyle.font = new BitmapFont();
                statusButton.setStyle(buttonStyle);
                System.out.println("Status button clicked");
                addStatus("status1");
                return true;
            }
        });

        stage.addActor(statusButton);
    }
    public void forTesting(){

        createDamageButton();
        createStaminaButton();
        createHealButton();
        createAddStatusButton();
        stage.setDebugAll(true);
    }
}
