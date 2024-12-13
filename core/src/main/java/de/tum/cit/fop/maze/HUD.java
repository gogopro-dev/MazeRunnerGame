package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.tum.cit.fop.maze.Entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HUD implements commonFunctions {

    Player player;
    Stage stage;
    Skin skin;
    // In order for skins to load create heart dir in assets and put the textures there
    //    String assetsPath = "C:\\Users\\Legion\\Desktop\\Game proj\\Test\\assets";

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


    public HUD(SpriteBatch sb, Player player) {

        this.player = player;
        hpBar = new ArrayList<Image>();
        elapsedTime = 0;
        currentSeconds = 0;
        staminaBarIsEmpty = false;
        heartsAnimationFrameDuration = 0.25f;
        animations = sb;
        create();
    }

    public void create() {

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

        return player.getHp() % 2 != 0;
    }


    public void createHPBar(Stage stage) {

        // should not be possible later with impl. of Player class
        int playerHP = player.getHp();
        int hearts = player.getHp() / 2;
        int emptyHearts = (this.player.getFullHP()-playerHP)/2;
        if (playerHP<0){
            return;
        }

        float offset = 0;

        // create full hearts

        Texture heart = skin.get("fhp", Texture.class);
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

            Image halfHeartImg = new Image(skin.get("hhp", Texture.class));
            this.hpBar.add(halfHeartImg);
            halfHeartImg.setPosition(offset, Gdx.graphics.getHeight() - halfHeartImg.getHeight());
            stage.addActor(halfHeartImg);
            halfHeartImg.setName("half");
            offset += halfHeartImg.getWidth();
        }

        // create empty hearts

        if(emptyHearts!=0){

            heart = skin.get("lhp", Texture.class);
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


    //!Important I stole getColoredDrawable from the internet)))

//    public static Drawable getColoredDrawable(int width, int height, Color color) {
//        // create simple font for Stamina Bar
//        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
//        pixmap.setColor(color);
//        pixmap.fill();
//        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
//        pixmap.dispose();
//
//        return drawable;
//    }
    //Initializes the stamina bar
    public void createStaminaBar(){

        int width = 100;
        int height = 10;


        staminaBar = new ProgressBar(0f, 1f, 0.01f, false, new ProgressBar.ProgressBarStyle());
        staminaBar.getStyle().background = commonFunctions.getColoredDrawable(width, height, Color.BLACK);
        staminaBar.getStyle().knob = commonFunctions.getColoredDrawable(0, height, Color.GOLD);
        staminaBar.getStyle().knobBefore = commonFunctions.getColoredDrawable(width, height, Color.GOLD);

        staminaBar.setWidth(width);
        staminaBar.setHeight(height);

        staminaBar.setAnimateDuration(0.0f);
        staminaBar.setValue(1f);

        staminaBar.setAnimateDuration(0.25f);

        staminaBar.setPosition(5, hpBar.get(0).getY() - 15);
        stage.addActor(staminaBar);
    }


    public void animate_heartsFullToEmpty(){
        int hp = this.player.getHp();
        int dmg = this.receivedDmg;
        int even = (dmg % 2 + (hp + dmg) % 2) % 2;

        for(int i = hp/2+even; i < (player.getHp() + this.receivedDmg)/2; i++){
            TextureRegion currentFrame = (TextureRegion) animation_heartFullToEmpty.getKeyFrame(hitElapsedTime);
            Image currentHeart = hpBar.get(i);
            int frameWidth = currentFrame.getRegionWidth();
            int frameHeight = currentFrame.getRegionHeight();
            animations.draw(currentFrame,currentHeart.getX(), currentHeart.getY(), frameWidth, frameHeight);
        }
    }

//TODO: merge functions to one & change register to camelCase
    public void animate_heartFullToHalf(int indexOfHeartToAnimate){
//        if(player.getHp() <= 0){
//            return;
//        }
        TextureRegion currentFrame = (TextureRegion) animation_heartFullToHalf.getKeyFrame(hitElapsedTime);
        Image currentHeart = hpBar.get(indexOfHeartToAnimate);
        int frameWidth = currentFrame.getRegionWidth();
        int frameHeight = currentFrame.getRegionHeight();
        animations.draw(currentFrame, currentHeart.getX(), currentHeart.getY(), frameWidth, frameHeight);
    }


    public void animate_heartHalfToEmpty(int indexOfHeartToAnimate){
//        if(player.getHp() <= 0){
//            return;
//        }
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
        player.setHp(player.getHp() - this.receivedDmg);
        if (player.isDead()){
            player.setHp(0);
        }
        disposeHPBar(stage);
        createHPBar(stage);
        hitElapsedTime = 0f;
    }

    public void drawHeartsAnimations() {
        int dmg = this.receivedDmg;
        int hp = this.player.getHp();
        if (dmg == 0) {
            return;
        }
        if(hp == 0){
            animate_heartsFullToEmpty();
            return;
        }
        // If the player's prev health was odd, then the last heart was half
        if((player.getHp() + dmg) % 2 != 0){
            animate_heartHalfToEmpty((dmg + hp) / 2);
        }
        // if the player's current health is odd, then the last heart is half
        if(hp%2!=0){
            animate_heartFullToHalf(hp/2);
        }
        animate_heartsFullToEmpty();

        if (getAllAnimationsFinished()) {
            hitElapsedTime = 0f;
        }
    }

    // create damage button to check how health bar works
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
        skin.add("fhp", new Texture(Gdx.files.internal("heart/Health1.png")));
        skin.add("hhp", new Texture(Gdx.files.internal("heart/Health3.png")));
        skin.add("lhp", new Texture(Gdx.files.internal("heart/Health7.png")));
    }
    // initialize animations
    public void setAnimations(){

        TextureAtlas health_fullToHalf = new TextureAtlas(Gdx.files.internal("heart/"+"health_full.atlas"));
        animation_heartFullToHalf = new Animation<>(heartsAnimationFrameDuration, health_fullToHalf.getRegions());
        animation_heartFullToHalf.setPlayMode(Animation.PlayMode.LOOP);


        TextureAtlas health_halfToEmpty = new TextureAtlas(Gdx.files.internal("heart/"+"health_half.atlas"));
        animation_heartHalfToEmpty = new Animation<>(heartsAnimationFrameDuration, health_halfToEmpty.getRegions());
        animation_heartHalfToEmpty.setPlayMode(Animation.PlayMode.LOOP);


        TextureAtlas health_fullToEmpty = new TextureAtlas(Gdx.files.internal("heart/"+"fullToEmpty.atlas"));
        animation_heartFullToEmpty = new Animation<>(heartsAnimationFrameDuration, health_fullToEmpty.getRegions());
        animation_heartFullToEmpty.setPlayMode(Animation.PlayMode.LOOP);

        hitElapsedTime = Math.max(animation_heartFullToHalf.getAnimationDuration(), animation_heartFullToEmpty.getAnimationDuration());
        System.out.println(hitElapsedTime);
    }
    // I used HUD file as main so you should check if some of the stuff here is already initialized in project main file
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

    public Pixmap takeScreenShot(){
        /// Potential memory leak
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return pixmap;
    }

//    public void draw(Stage stage) {
//    }

    public void resize (int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose () {

        stage.dispose();
        skin.dispose();
    }
    // I used HUD file as main so you should check if some of the stuff here is already initialized in project main file
    //TODO : check if this is needed in the project

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Skin getSkin() {
        return skin;
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
    }

    public List<Image> getHpBar() {
        return hpBar;
    }

    public void setHpBar(List<Image> hpBar) {
        this.hpBar = hpBar;
    }

    public Button getTakeDmgButton() {
        return takeDmgButton;
    }

    public void setTakeDmgButton(Button takeDmgButton) {
        this.takeDmgButton = takeDmgButton;
    }

    public int getReceivedDmg() {
        return receivedDmg;
    }

    public void setReceivedDmg(int receivedDmg) {
        this.receivedDmg = receivedDmg;
    }

    public Animation getAnimation_heartFullToHalf() {
        return animation_heartFullToHalf;
    }

    public void setAnimation_heartFullToHalf(Animation animation_heartFullToHalf) {
        this.animation_heartFullToHalf = animation_heartFullToHalf;
    }

    public Animation getAnimation_heartHalfToEmpty() {
        return animation_heartHalfToEmpty;
    }

    public void setAnimation_heartHalfToEmpty(Animation animation_heartHalfToEmpty) {
        this.animation_heartHalfToEmpty = animation_heartHalfToEmpty;
    }

    public Animation getAnimation_heartFullToEmpty() {
        return animation_heartFullToEmpty;
    }

    public void setAnimation_heartFullToEmpty(Animation animation_heartFullToEmpty) {
        this.animation_heartFullToEmpty = animation_heartFullToEmpty;
    }

    public float getHeartsAnimationFrameDuration() {
        return heartsAnimationFrameDuration;
    }

    public void setHeartsAnimationFrameDuration(float heartsAnimationFrameDuration) {
        this.heartsAnimationFrameDuration = heartsAnimationFrameDuration;
    }

    public SpriteBatch getAnimations() {
        return animations;
    }

    public void setAnimations(SpriteBatch animations) {
        this.animations = animations;
    }

    public ProgressBar getStaminaBar() {
        return staminaBar;
    }

    public void setStaminaBar(ProgressBar staminaBar) {
        this.staminaBar = staminaBar;
    }

    public boolean isStaminaBarIsEmpty() {
        return staminaBarIsEmpty;
    }

    public void setStaminaBarIsEmpty(boolean staminaBarIsEmpty) {
        this.staminaBarIsEmpty = staminaBarIsEmpty;
    }

    public int getCurrentSeconds() {
        return currentSeconds;
    }

    public void setCurrentSeconds(int currentSeconds) {
        this.currentSeconds = currentSeconds;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(float elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public float getHitElapsedTime() {
        return hitElapsedTime;
    }

    public void setHitElapsedTime(float hitElapsedTime) {
        this.hitElapsedTime = hitElapsedTime;
    }
}

