package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.tum.cit.fop.maze.essentials.Utils;

import java.util.ArrayList;
import java.util.List;

public class HUD {

    private int health;
    private int maxHealth;
    private int maxStamina;

    Stage stage;
    Skin skin;

    List<Image> hpBar;
    Button takeDmgButton;
    int receivedDmg;

    Animation<TextureRegion> animation_heartFullToHalf;
    Animation<TextureRegion> animation_heartHalfToEmpty;
    Animation<TextureRegion> animation_heartFullToEmpty;
    float heartsAnimationFrameDuration;
    SpriteBatch animations;

    ProgressBar staminaBar;
    boolean staminaBarIsEmpty;
    int currentSeconds;

    float elapsedTime;
    float hitElapsedTime;


    public HUD(SpriteBatch sb) {

        health = 14;
        maxHealth = 14;
        hpBar = new ArrayList<Image>();
        elapsedTime = 0;
        /// @param currentSeconds for the stamina bar testing
        currentSeconds = 0;
        staminaBarIsEmpty = false;

        heartsAnimationFrameDuration = 0.25f;
        animations = sb;
        this.stage = new Stage();
        Gdx.input.setInputProcessor(this.stage);

        initSkins();
        setAnimations();
        createHPBar(stage);
        createDamageButton(stage);
        createStaminaBar();
    }

    public void disposeHPBar(Stage stage) {

        for (Image image : hpBar) {
            image.remove();
        }
        hpBar.clear();
    }

    public boolean hpCheck_heartIsHalf(){

        return health % 2 != 0;
    }

    public void createHPBar(Stage stage) {

        int hearts = health / 2;

        int emptyHearts = (maxHealth-health)/2;
        if (health<0){
            return;
        }

        float offset = 0;

        // create full hearts

        Texture heart = skin.get("full", Texture.class);
        for (int i = 0; i < hearts; i++) {
            Image eachHeart = new Image(heart);
            hpBar.add(eachHeart);
            eachHeart.setPosition(offset, Gdx.graphics.getHeight() - eachHeart.getHeight());
            offset += eachHeart.getWidth();
            eachHeart.setName("full");
            stage.addActor(eachHeart);
        }

        //create half hearts

        if (hpCheck_heartIsHalf()) {

            Image halfHeartImg = new Image(skin.get("half", Texture.class));
            this.hpBar.add(halfHeartImg);
            halfHeartImg.setPosition(offset, Gdx.graphics.getHeight() - halfHeartImg.getHeight());
            stage.addActor(halfHeartImg);
            halfHeartImg.setName("half");
            offset += halfHeartImg.getWidth();
        }

        // create empty hearts

        if(emptyHearts!=0){

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

    //Initializes the stamina bar
    public void createStaminaBar(){

        int width = 100;
        int height = 10;


        staminaBar = new ProgressBar(0f, 1f, 0.01f, false, new ProgressBar.ProgressBarStyle());
        staminaBar.getStyle().background = Utils.getColoredDrawable(width, height, Color.BLACK);
        staminaBar.getStyle().knob = Utils.getColoredDrawable(0, height, Color.GOLD);
        staminaBar.getStyle().knobBefore = Utils.getColoredDrawable(width, height, Color.GOLD);

        staminaBar.setWidth(width);
        staminaBar.setHeight(height);

        staminaBar.setAnimateDuration(0.0f);
        staminaBar.setValue(1f);

        staminaBar.setAnimateDuration(0.25f);

        staminaBar.setPosition(5, hpBar.get(0).getY() - 15);
        stage.addActor(staminaBar);
    }

    public void animateHeartsFullToEmpty(){

        int dmg = this.receivedDmg;
        int even = (dmg % 2 + (health + dmg) % 2) % 2;

        for(int i = health/2+even; i < (health + this.receivedDmg)/2; i++){
            TextureRegion currentFrame = (TextureRegion) animation_heartFullToEmpty.getKeyFrame(hitElapsedTime);
            Image currentHeart = hpBar.get(i);
            int frameWidth = currentFrame.getRegionWidth();
            int frameHeight = currentFrame.getRegionHeight();
            animations.draw(currentFrame,currentHeart.getX(), currentHeart.getY(), frameWidth, frameHeight);
        }
    }

//TODO: merge functions to one & change register to camelCase
    public void animateHeartFullToHalf(int indexOfHeartToAnimate){

        TextureRegion currentFrame = (TextureRegion) animation_heartFullToHalf.getKeyFrame(hitElapsedTime);
        Image currentHeart = hpBar.get(indexOfHeartToAnimate);
        int frameWidth = currentFrame.getRegionWidth();
        int frameHeight = currentFrame.getRegionHeight();
        animations.draw(currentFrame, currentHeart.getX(), currentHeart.getY(), frameWidth, frameHeight);
    }

    public void animateHeartHalfToEmpty(int indexOfHeartToAnimate){

        TextureRegion currentFrame = (TextureRegion) animation_heartHalfToEmpty.getKeyFrame(hitElapsedTime);
        Image currentHeart = hpBar.get(indexOfHeartToAnimate);
        int frameWidth = currentFrame.getRegionWidth();
        int frameHeight = currentFrame.getRegionHeight();
        animations.draw(currentFrame, currentHeart.getX(), currentHeart.getY(), frameWidth, frameHeight);
    }

    public boolean getAllAnimationsFinished(){
        return animation_heartHalfToEmpty.isAnimationFinished(hitElapsedTime) && animation_heartFullToHalf.isAnimationFinished(hitElapsedTime) && animation_heartFullToEmpty.isAnimationFinished(hitElapsedTime);
    }


    public void takeDmg(Stage stage) {
        health -= receivedDmg;
        /// health must be >= 0 in order for hpBar to work properly
        if (health < 0){
            health = 0;
        }
        disposeHPBar(stage);
        createHPBar(stage);
        hitElapsedTime = 0f;
    }

    public void drawHeartsAnimations() {
        int dmg = this.receivedDmg;
        if (dmg == 0) {
            return;
        }
        if(health == 0){
            animateHeartsFullToEmpty();
            return;
        }
        // If the player's prev health was odd, then the last heart was half
        if((health + dmg) % 2 != 0){
            animateHeartHalfToEmpty((dmg + health) / 2);
        }
        // if the player's current health is odd, then the last heart is half
        if(health % 2 != 0){
            animateHeartFullToHalf(health / 2);
        }
        animateHeartsFullToEmpty();

        if (getAllAnimationsFinished()) {
            hitElapsedTime = 0f;
        }
    }

    /// to test how health works
    public void createDamageButton(Stage stage) {
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.font = new BitmapFont();

        takeDmgButton = new TextButton("Take Damage", textButtonStyle);
        takeDmgButton.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        takeDmgButton.addCaptureListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
                buttonStyle.fontColor = Color.RED;
                buttonStyle.font = new BitmapFont();
                takeDmgButton.setStyle(buttonStyle);
                receivedDmg=3;
                takeDmg(stage);
            }
        });

        stage.addActor(takeDmgButton);
    }

    public void initSkins(){
        skin = new Skin();
        skin.add("full", new Texture(Gdx.files.internal("temporary/heart/Health1.png")));
        skin.add("half", new Texture(Gdx.files.internal("temporary/heart/Health3.png")));
        skin.add("empty", new Texture(Gdx.files.internal("temporary/heart/Health7.png")));
    }

    public void setAnimations(){

        TextureAtlas health_fullToHalf = new TextureAtlas(Gdx.files.internal("temporary/heart/health_full.atlas"));
        animation_heartFullToHalf = new Animation<>(heartsAnimationFrameDuration, health_fullToHalf.getRegions());
        animation_heartFullToHalf.setPlayMode(Animation.PlayMode.LOOP);


        TextureAtlas health_halfToEmpty = new TextureAtlas(Gdx.files.internal("temporary/heart/health_half.atlas"));
        animation_heartHalfToEmpty = new Animation<>(heartsAnimationFrameDuration, health_halfToEmpty.getRegions());
        animation_heartHalfToEmpty.setPlayMode(Animation.PlayMode.LOOP);


        TextureAtlas health_fullToEmpty = new TextureAtlas(Gdx.files.internal("temporary/heart/fullToEmpty.atlas"));
        animation_heartFullToEmpty = new Animation<>(heartsAnimationFrameDuration, health_fullToEmpty.getRegions());
        animation_heartFullToEmpty.setPlayMode(Animation.PlayMode.LOOP);

        hitElapsedTime = Math.max(animation_heartFullToHalf.getAnimationDuration(), animation_heartFullToEmpty.getAnimationDuration());
        System.out.println(hitElapsedTime);
    }

    public void render() {
        stage.act();
        stage.draw();

        elapsedTime += Gdx.graphics.getDeltaTime();

        if (staminaBar.getValue() <= 0){
            staminaBarIsEmpty = true;
        }

        if (staminaBar.getValue() == 1){
            staminaBarIsEmpty = false;
        }

        if (staminaBarIsEmpty && currentSeconds < Math.round(elapsedTime)){
            staminaBar.setValue(staminaBar.getValue() + 0.1f);
            currentSeconds = Math.round(elapsedTime);
        }
        else if(currentSeconds < Math.round(elapsedTime)){
            staminaBar.setValue(staminaBar.getValue() - 0.1f);
            currentSeconds = Math.round(elapsedTime);
        }

        /// Check if all animations are finished and draw if not
        if (!getAllAnimationsFinished()) {
            drawHeartsAnimations();
            hitElapsedTime += Gdx.graphics.getDeltaTime();
        }

    }

//    public Pixmap takeScreenShot(){
//        /// Potential memory leak
//        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        return pixmap;
//    }

    public void resize (int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose () {

        stage.dispose();
        skin.dispose();
    }

}

