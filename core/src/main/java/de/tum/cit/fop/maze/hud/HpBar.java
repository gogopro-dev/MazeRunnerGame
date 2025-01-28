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

import java.util.List;

public class HpBar {
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
    private float spacing = 4*scaling;


    public HpBar(float x, float y, TextureAtlas textureAtlas, Stage stage) {

        this.x = x + staminaIconWidth;
        this.y = y;
        this.stage = stage;

        healthLDMG = new Animation<>(animationDuration, textureAtlas.findRegions("HealthL_DMG"));
        healthLDMG.setPlayMode(Animation.PlayMode.LOOP);
        healthRDMG = new Animation<>(animationDuration, textureAtlas.findRegions("HealthR_DMG"));
        healthRDMG.setPlayMode(Animation.PlayMode.LOOP);
        healthLNoDMG = new Animation<>(animationDuration, textureAtlas.findRegions("HealthL_noDMG"));
        healthLNoDMG.setPlayMode(Animation.PlayMode.LOOP);


        staticHealthLEmpty = textureAtlas.findRegion("HealthL_DMG", 2);
        staticHealthLFull = textureAtlas.findRegion("HealthL_noDMG", 1);
        staticHealthREmpty = textureAtlas.findRegion("HealthR_DMG", 2);
        staticHealthRFull = textureAtlas.findRegion("HealthR_noDMG", 1);

        stage.addActor(healthBar);
    }

    public void updateHpBar(int health, int maxHealth) {
        this.health = health;
        this.maxHealth = maxHealth;

        if (healthBar.getChildren().size > 0) {
            healthBar.clear();
        }

        fillWithHearts(1, health, staticHealthLFull, staticHealthRFull, scaling);

        if (maxHealth != health) {
            fillWithHearts(health + 1, maxHealth, staticHealthLEmpty, staticHealthREmpty, scaling);
        }
        healthBar.setSize(healthBar.getPrefWidth(), healthBar.getPrefHeight());
        healthBar.setPosition(x, y - healthBar.getPrefHeight());
        stage.addActor(healthBar);
    }

    private void fillWithHearts(int start, int end, TextureRegion staticHealthLFull, TextureRegion staticHealthRFull,
                                float scaling) {
        for (int i = start; i <= end; i++) {
            if (i % 2 == 1) {
                Drawable heart = new TextureRegionDrawable(staticHealthLFull);
                heart.setMinWidth(heart.getMinWidth() * scaling);
                heart.setMinHeight(heart.getMinHeight() * scaling);
                healthBar.add(new Image(heart));
            } else {
                Drawable heart = new TextureRegionDrawable(staticHealthRFull);
                heart.setMinWidth(heart.getMinWidth() * scaling);
                heart.setMinHeight(heart.getMinHeight() * scaling);
                healthBar.add(new Image(heart)).padRight(spacing);
            }

            if (i % heartsInRow == 0) {
                healthBar.row().padTop(spacing);
            }
        }
    }

    public void heal(int value){
        health += value;
        if (health > maxHealth) {
            health = maxHealth;
        }
        updateHpBar(health, maxHealth);
    }

    public void takeDmg(int damage) {

        receivedDamage = damage;
        health = Math.max( health - receivedDamage, 0);
        updateHpBar(health, maxHealth);
        hitElapsedTime = 0f;
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

    public void render(float deltaTime, SpriteBatch batch) {
        elapsedTime += deltaTime;
        if (gotHit) {
            damageAnimation(deltaTime, batch);
            if (getAllAnimationsFinished()) {
                gotHit = false;
            }
        }
    }
    public int getHealth() {
        return health;
    }

    public int getSize() {
        return healthBar.getChildren().size;
    }

    public List<Integer> coordinates() {
        return List.of((int) x, (int) y);
    }

    public void removeHpBar() {
        healthBar.clear();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y - healthBar.getPrefHeight();
    }
    public void setScaling(float scaling) {
        this.scaling = scaling;
        spacing = 5*scaling;
    }
    public float getWidth() {
        return healthBar.getPrefWidth();
    }
    public float getHeight() {
        return healthBar.getPrefHeight();
    }
}
