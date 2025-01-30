package de.tum.cit.fop.maze.hud;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Health bar class for the player.
 */
public class HpBar {

    private List<Image> healthImages;
    private final float x;
    private final float y;
    private int health;
    private int maxHealth;
    private final int heartsInRow = 20;
    private int receivedDamage;
    private final Animation<TextureRegion> healthLNoDMG;
    private final Animation<TextureRegion> healthRDMG;
    private final Animation<TextureRegion> healthLDMG;
    private boolean gotHit;
    private float elapsedTime = 0;
    private float hitElapsedTime = 0;
    private final float animationDuration = 1 / 5f;
    private final Table healthBar = new Table();
    private TextureRegion staticHealthRFull;
    private TextureRegion staticHealthLFull;
    private TextureRegion staticHealthREmpty;
    private TextureRegion staticHealthLEmpty;
    private final Stage stage;
    private float scaling = 1.6f;
    private float staminaIconWidth = 35;
    private float spacing = 4 * scaling;


    /**
     * Instantiates a new Hp bar.
     *
     * @param x            the x coordinate
     * @param y            the y coordinate
     * @param textureAtlas the texture atlas
     * @param stage        the stage
     */
    public HpBar(float x, float y, TextureAtlas textureAtlas, Stage stage) {

        this.x = x + staminaIconWidth;
        this.y = y;
        this.stage = stage;
        healthImages = new ArrayList<>();

        healthLDMG = new Animation<>(animationDuration, textureAtlas.findRegions("HealthL_DMG"));
        healthLDMG.setPlayMode(Animation.PlayMode.NORMAL);
        healthRDMG = new Animation<>(animationDuration, textureAtlas.findRegions("HealthR_DMG"));
        healthRDMG.setPlayMode(Animation.PlayMode.NORMAL);
        healthLNoDMG = new Animation<>(animationDuration, textureAtlas.findRegions("HealthL_noDMG"));
        healthLNoDMG.setPlayMode(Animation.PlayMode.NORMAL);


        staticHealthLEmpty = textureAtlas.findRegion("HealthL_DMG", 2);
        staticHealthLFull = textureAtlas.findRegion("HealthL_noDMG", 1);
        staticHealthREmpty = textureAtlas.findRegion("HealthR_DMG", 2);
        staticHealthRFull = textureAtlas.findRegion("HealthR_noDMG", 1);

        stage.addActor(healthBar);
    }

    /**
     * Create hp bar.
     *
     * @param health    the health
     * @param maxHealth the max health
     */
    public void createHpBar(int health, int maxHealth) {
        this.health = health;
        this.maxHealth = maxHealth;

        if (healthBar.getChildren().size > 0) {
            healthBar.clear();
            healthImages.clear();
        }

        fillWithHearts(1, health, staticHealthLFull, staticHealthRFull, scaling);

        if (maxHealth != health) {
            fillWithHearts(health + 1, maxHealth, staticHealthLEmpty, staticHealthREmpty, scaling);
        }
        healthBar.setSize(healthBar.getPrefWidth(), healthBar.getPrefHeight());
        healthBar.setPosition(x, y - healthBar.getPrefHeight());
        stage.addActor(healthBar);
    }

    /**
     * Update existing hp bar.
     *
     * @param health    the health
     * @param maxHealth the max health
     * @param isDamage  the is damage
     * @param value     the value
     */
    public void updateHpBar(int health, int maxHealth, Boolean isDamage, int value){

        if (isDamage == null){
            createHpBar(health, maxHealth);
            return;
        }

        if (value == 0){
            return;
        }

        TextureRegionDrawable left = new TextureRegionDrawable(isDamage ? staticHealthLEmpty : staticHealthLFull);
        TextureRegionDrawable right = new TextureRegionDrawable(isDamage ? staticHealthREmpty : staticHealthRFull);
        left.setMinWidth(left.getMinWidth() * scaling);
        left.setMinHeight(left.getMinHeight() * scaling);
        right.setMinWidth(right.getMinWidth() * scaling);
        right.setMinHeight(right.getMinHeight() * scaling);

        for (int i = health; i < health + value; i++) {
            if (i % 2 == 0) {
                healthImages.get(i).setDrawable(left);
            } else {
                healthImages.get(i).setDrawable(right);
            }
        }


    }


    private void fillWithHearts(int start, int end, TextureRegion staticHealthLFull, TextureRegion staticHealthRFull,
                                float scaling) {
        for (int i = start; i <= end; i++) {
            if (i % 2 == 1) {
                Drawable heart = new TextureRegionDrawable(staticHealthLFull);
                heart.setMinWidth(heart.getMinWidth() * scaling);
                heart.setMinHeight(heart.getMinHeight() * scaling);
                Image img = new Image(heart);
                healthBar.add(img);
                healthImages.add(img);
            } else {
                Drawable heart = new TextureRegionDrawable(staticHealthRFull);
                heart.setMinWidth(heart.getMinWidth() * scaling);
                heart.setMinHeight(heart.getMinHeight() * scaling);
                Image img = new Image(heart);
                healthBar.add(img).padRight(spacing);
                healthImages.add(img);
            }

            if (i % heartsInRow == 0) {
                healthBar.row().padTop(spacing);
            }
        }
    }

    /**
     * Heal.
     *
     * @param value the value
     */
    public void heal(int value){
        if (health + value > maxHealth){
            value = maxHealth - health;
        }
        updateHpBar(health, maxHealth, false, value);
        health += value;
    }

    /**
     * Take dmg.
     *
     * @param damage the damage
     */
    public void takeDmg(int damage) {
        if (health - damage < 0) {
            receivedDamage = health;
        }
        else {
            receivedDamage = damage;
        }
        health -= receivedDamage;
        hitElapsedTime = 0;
        updateHpBar(health, maxHealth, true, receivedDamage);
        gotHit = true;
    }

    private void damageAnimation(float deltaTime, SpriteBatch batch) {

        hitElapsedTime += deltaTime;
        TextureRegion currentFrame;
        Image heart;
        float startX = healthBar.getX();
        float startY = healthBar.getY();

        if (health % 2 == 0) {
            currentFrame = healthLNoDMG.getKeyFrame(hitElapsedTime);
            heart = (Image) healthBar.getChildren().get(health);
            batch.draw(currentFrame, startX + heart.getX(),
                    startY + heart.getY(), heart.getWidth(), heart.getHeight());
        }

        for (int i = health; i < receivedDamage + health; i++) {
            if (i % 2 == 0) {
                currentFrame = healthLDMG.getKeyFrame(hitElapsedTime);
            } else {
                currentFrame = healthRDMG.getKeyFrame(hitElapsedTime);
            }
            heart = (Image) healthBar.getChild(i);
            batch.draw(currentFrame, startX + heart.getX(),
                    startY + heart.getY(), heart.getWidth(), heart.getHeight());
        }
    }

    private boolean getAllAnimationsFinished() {
        return healthLDMG.isAnimationFinished(hitElapsedTime) && healthRDMG.isAnimationFinished(hitElapsedTime)
                && healthLNoDMG.isAnimationFinished(hitElapsedTime);
    }

    /**
     * Render.
     *
     * @param deltaTime the delta time
     * @param batch     the batch
     */
    public void render(float deltaTime, SpriteBatch batch) {
        elapsedTime += deltaTime;
        if (gotHit) {
            damageAnimation(deltaTime, batch);
            if (getAllAnimationsFinished()) {
                gotHit = false;
                hitElapsedTime = 0;
            }
        }
    }

    /**
     * Gets health.
     *
     * @return the health
     */
    public int getHealth() {
        return health;
    }

    /**
     * Gets size.
     *
     * @return the size
     */
    public int getSize() {
        return healthBar.getChildren().size;
    }

    /**
     * Coordinates list.
     *
     * @return the list
     */
    public List<Integer> coordinates() {
        return List.of((int) x, (int) y);
    }

    /**
     * Remove hp bar.
     */
    public void removeHpBar() {
        healthBar.clear();
    }

    /**
     * Gets x coordinate.
     *
     * @return the x
     */
    public float getX() {
        return x;
    }

    /**
     * Gets y coordinate.
     *
     * @return the y
     */
    public float getY() {
        return y - healthBar.getPrefHeight();
    }

    /**
     * Gets width.
     *
     * @return the width
     */
    public float getWidth() {
        return healthBar.getPrefWidth();
    }

    /**
     * Gets height.
     *
     * @return the height
     */
    public float getHeight() {
        return healthBar.getPrefHeight();
    }

    /**
     * Sets health bar.
     *
     * @param health    the health
     * @param maxHealth the max health
     */
    public void setHealthBar(int health, int maxHealth) {

        this.maxHealth = maxHealth;
        this.health = health;
        this.gotHit = false;
        this.elapsedTime = 0;
        createHpBar(health, maxHealth);
    }

    /**
     * Dispose.
     */
    public void dispose(){

        healthBar.clear();
        healthImages.clear();
        healthBar.remove();
    }
}
