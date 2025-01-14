package de.tum.cit.fop.maze.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.commonFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HUDv2 {
    private Stage stage;
    private List<List<Image>> matrixHPBar;
    private List<TextureRegion> statusBar;
    private float heartsAnimationFrameDuration;
    private SpriteBatch spriteBatch;
    private boolean gotHit;
    private TextureAtlas atlas;
    int amountOfHeartsInRow;
    private ProgressBar staminaBar;

    private int receivedDmg;
    private int health;
    private int maxHealth;

    private Map<String, Animation<TextureRegion>> animations;
    private Animation<TextureRegion> leftHalfFtoE;
    private Animation<TextureRegion> rightHalfFtoE;
    private Animation<TextureRegion> leftHalfFtoF;
    float hitElapsedTime;
    float offsetXStep;
    float offsetYStep;
    float spacingX;
    float spacingY;
    float heartsScaling;
    float padding;
    int indexXOfLastFullHalf;
    int indexYOfLastFullHalf;

    float spacingBetwHPBarAndStaminaBar;

    public HUDv2(int health, int maxHealth, SpriteBatch spriteBatch) {
        stage = new Stage();
        matrixHPBar = new ArrayList<>();
        statusBar = new ArrayList<>();

        heartsAnimationFrameDuration = 1f;
        this.spriteBatch = spriteBatch;
        this.stage = new Stage();
        Gdx.input.setInputProcessor(this.stage);
        gotHit = false;
        hitElapsedTime = 0f;
        this.health = health;
        this.maxHealth = maxHealth;

        init();


//            this.health = health;
//            this.maxHealth = maxHealth;

        updateHPBar();
        createStaminaBar();
//            createStaminaBar();
    }

    public void init() {
        atlas = new TextureAtlas(Gdx.files.internal("temporary\\HUDv2\\HUD_v2_1.atlas"));
        animations = new HashMap<>();

        Animation<TextureRegion> lHFtoE = new Animation<TextureRegion>(heartsAnimationFrameDuration, atlas.findRegions("lHFtoE"));
        lHFtoE.setPlayMode(Animation.PlayMode.LOOP);

        Animation<TextureRegion> rHFtoE = new Animation<TextureRegion>(heartsAnimationFrameDuration, atlas.findRegions("rHFtoE"));
        rHFtoE.setPlayMode(Animation.PlayMode.LOOP);

        Array <TextureRegion> lHFtoFTextureRegions = new Array<>();
        lHFtoFTextureRegions.add(atlas.findRegion("lHFtoF"));
        lHFtoFTextureRegions.add(atlas.findRegion("lHFtoE", 0));
        lHFtoFTextureRegions.add(atlas.findRegion("lHFtoF"));
        lHFtoFTextureRegions.add(atlas.findRegion("lHFtoE", 0));

        Animation<TextureRegion> lHFtoF = new Animation<TextureRegion>(heartsAnimationFrameDuration, lHFtoFTextureRegions);
        lHFtoF.setPlayMode(Animation.PlayMode.LOOP);

        animations.put("lHalfFullToFull", lHFtoF);
        animations.put("lHalfFullToEmpty", lHFtoE);
        animations.put("rHalfFullToEmpty", rHFtoE);

//        leftHalfFtoE = new Animation<TextureRegion>(heartsAnimationFrameDuration, atlas.findRegions("lHFtoE"));
//        rightHalfFtoE = new Animation<TextureRegion>(heartsAnimationFrameDuration, atlas.findRegions("rHFtoE"));
//
////        leftHalfFtoF.setPlayMode(Animation.PlayMode.LOOP);
//        leftHalfFtoE.setPlayMode(Animation.PlayMode.LOOP);
//        rightHalfFtoE.setPlayMode(Animation.PlayMode.LOOP);

        heartsScaling = 1.5f;
        offsetXStep = animations.get("lHalfFullToEmpty").getKeyFrame(0).getRegionWidth() * heartsScaling;
        offsetYStep = animations.get("lHalfFullToEmpty").getKeyFrame(0).getRegionHeight() * heartsScaling;
        spacingX = 2.5f;
        spacingY = 2.5f;
        padding = 10f;
        amountOfHeartsInRow = 20;

        spacingBetwHPBarAndStaminaBar = 15f;


    }
    private void addImageToRow(List<Image> row, float offsetX, float offsetY, TextureRegion regionName) {
        Image eachHeart = new Image(regionName);
        eachHeart.setScale(heartsScaling);
        eachHeart.setPosition(offsetX, Gdx.graphics.getHeight() - offsetY - eachHeart.getHeight());
        row.add(eachHeart);
        stage.addActor(eachHeart);
    }

    // for info: lHFull, rHFull, lHEmpty, rHEmpty

    private List<Float> fillHPBarWithHearts(int target, float StartX, float StartY, TextureRegion lHalfTexture, TextureRegion rHalfTexture, float count){
        float x = StartX;
        float y = StartY;
        if (count % (amountOfHeartsInRow*2) == 0) {
            matrixHPBar.add(new ArrayList<>());
        }
        List<Image> row = matrixHPBar.remove(matrixHPBar.size()-1);
//        drawn += startsWith;
//        target += startsWith;

        while (target != count){
            addImageToRow(row, x, y, count%2 == 0 ? lHalfTexture : rHalfTexture);
            x += count%2==0 ? offsetXStep: offsetXStep + spacingX;
            count++;
            if (count%(amountOfHeartsInRow*2)==0){
                matrixHPBar.add(row);
                row = new ArrayList<>();
                y += offsetYStep + spacingY;
                x = padding;
            }
        }
        if (!row.isEmpty()) {
            matrixHPBar.add(row);
        }
        if (lHalfTexture == atlas.findRegion("lHFtoE", 0)) {
            indexXOfLastFullHalf = matrixHPBar.get(matrixHPBar.size()-1).size()-1;
            indexYOfLastFullHalf = matrixHPBar.size()-1;
            System.out.println(indexXOfLastFullHalf + " " + indexYOfLastFullHalf);
        }
        return List.of(x, y, (float) count);
    }

    public void updateHPBar(){
        if (health <= 0) {
            health = 2;
        }
        clearHPBar();

        TextureRegion lHFull = atlas.findRegion("lHFtoE", 0);
        TextureRegion rHFull = atlas.findRegion("rHFtoE", 0);
        TextureRegion lHEmpty = atlas.findRegion("lHFtoE", 3);
        TextureRegion rHEmpty = atlas.findRegion("rHFtoE", 3);

        List<Float> resultOfFirstFill = fillHPBarWithHearts(health, padding, offsetYStep/2 + padding, lHFull, rHFull, 0);
        fillHPBarWithHearts(maxHealth, resultOfFirstFill.get(0), resultOfFirstFill.get(1), lHEmpty, rHEmpty, resultOfFirstFill.get(2));
        System.out.println(matrixHPBar.size());


    }

    public void clearHPBar() {
        for (List<Image> row : matrixHPBar) {
            for (Image heart : row) {
                heart.remove();
            }
        }
        matrixHPBar.clear();
    }

    public void takeDmg(int receivedDmg) {
        if (receivedDmg <= 0) {
            return;
        }
        this.receivedDmg = receivedDmg;
        gotHit = true;
        health -= receivedDmg;
        hitElapsedTime = 0f;
        updateHPBar();
    }

    private void drawHeartAnimation(String animationName, int y, int x)  {
        TextureRegion currentFrame = animations.get(animationName).getKeyFrame(hitElapsedTime);
        Image heart = matrixHPBar.get(y).get(x);
        float width = heart.getImageWidth()*heartsScaling;
        float height = heart.getImageHeight()*heartsScaling;
        spriteBatch.draw(currentFrame, heart.getX(), heart.getY(), width, height);
        }



    public void createStaminaBar() {
        int width = 300;
        int height = 15;

        staminaBar = new ProgressBar(0f, 100f, 0.01f, false, new ProgressBar.ProgressBarStyle());
        staminaBar.getStyle().background = commonFunctions.getColoredDrawable(width, height, Color.WHITE);
        staminaBar.getStyle().knob = commonFunctions.getColoredDrawable(0, height, Color.GOLD);
        staminaBar.getStyle().knobBefore = commonFunctions.getColoredDrawable(width, height, Color.GOLD);

        staminaBar.setWidth(width);
        staminaBar.setHeight(height);

        staminaBar.setValue(100f);
        staminaBar.setAnimateDuration(0.25f);


        float stBarX = matrixHPBar.get(matrixHPBar.size()-1).get(0).getX();
        float stBarY = matrixHPBar.get(matrixHPBar.size()-1).get(0).getY() - spacingBetwHPBarAndStaminaBar;


        staminaBar.setPosition(stBarX, stBarY);
        stage.addActor(staminaBar);
    }

//    public void drawStaminaBarBackground(float width, float height) {
//        Image staminaBarBackground = new Image(atlas.findRegion("staminaBarBackground"));
//        float stBarWidth = width + 192/2f;
//        float stBarHeight = height + 16;
//
//        stage.addActor(staminaBarBackground);
//        staminaBarBackground.setSize(stBarWidth, stBarHeight);
//        staminaBarBackground.setPosition(stBarX - 192/4f, stBarY - 8);
//    }

    public void updateStaminaBar(float currentStamina, float maxStamina) {
        staminaBar.setRange(0f, maxStamina);
        staminaBar.setValue(currentStamina);
    }
    public void drainStamina(float stamina) {
        staminaBar.setValue(staminaBar.getValue() - stamina);
    }

    private void damageAnimation(float deltaTime){
        // receivedDmg, IndexX, IndexY
        int heartsToAnimate = receivedDmg;
        if (receivedDmg <= 0) {
            return;
        }
        int tempIndexX = indexXOfLastFullHalf%(amountOfHeartsInRow*2 -1) == 0 ? 0 : indexXOfLastFullHalf +1;
//        int tempIndexX = indexXOfLastHeartToAnimate;
        int tempIndexY = indexXOfLastFullHalf%(amountOfHeartsInRow*2 -1) == 0 ? indexYOfLastFullHalf + 1 : indexYOfLastFullHalf;

        if (indexXOfLastFullHalf % 2==0) {
            drawHeartAnimation("lHalfFullToFull", indexYOfLastFullHalf, indexXOfLastFullHalf);
//            tempIndexX++;
        }

        while(heartsToAnimate > 0) {
            if (tempIndexX % 2 == 0) {
                drawHeartAnimation("lHalfFullToEmpty", tempIndexY, tempIndexX);
            } else {
                drawHeartAnimation("rHalfFullToEmpty", tempIndexY, tempIndexX);
            }
            tempIndexX++;
            heartsToAnimate--;
            if (tempIndexX >= amountOfHeartsInRow*2) {
                tempIndexY++;
                tempIndexX = 0;
            }
        }


    }

    private boolean areAllAnimationsFinished() {
        for (Map.Entry<String, Animation<TextureRegion>> entry : animations.entrySet()) {
            if (!entry.getValue().isAnimationFinished(hitElapsedTime)) {
                return false;
            }
        }
        return true;
    }

    public void createDamageButton() {

        Button takeDmgButton = new Button();
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.font = new BitmapFont();

        takeDmgButton = new TextButton("Take Damage", textButtonStyle);
        takeDmgButton.setPosition(20, Gdx.graphics.getHeight() / 2f);
        takeDmgButton.addCaptureListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("Damage button clicked");
                takeDmg(20);
                return true;
            }
        });
        stage.addActor(takeDmgButton);
    }



    public void render(float deltaTime) {
        stage.act();
        stage.draw();
        if (gotHit) {
            damageAnimation(deltaTime);
            hitElapsedTime += deltaTime;
            if (areAllAnimationsFinished()) {
                gotHit = false;
                hitElapsedTime = 0f;
            }
        }
    }
}

