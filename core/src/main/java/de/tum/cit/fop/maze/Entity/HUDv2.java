package de.tum.cit.fop.maze.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import de.tum.cit.fop.maze.commonFunctions;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HUDv2 {
    private final Stage stage;
    private final List<List<Image>> matrixHPBar;
    private final Map<String, Image> statusBar;
    private final float heartsAnimationFrameDuration = 1/5f;
    private final SpriteBatch spriteBatch;


    private TextureAtlas atlas;


    private int receivedDmg;
    private int health;
    private int maxHealth;

    private ProgressBar staminaBar;
    private float stamina = 100f;
    private final float staminaConsumptionSpeed = 0.5f;
    private final float staminaBarPadding = 10f;
    private final float staminaBarScaling = 1.5f;

    private final float statusBarSpacingFromHPBar = 10f;
    private final float statusBarInnerSpacing = 5f;
    private final float statusScale = 1.5f;


    private boolean gotHit = false;
    private Map<String, Animation<TextureRegion>> animations;

    float hitElapsedTime = 0;
    float offsetXStepHP;
    float offsetYStepHP;
    final float spacingX = 2.5f;
    final float spacingY = 2.5f;
    final float heartsScaling = 1.5f;
    final float padding = 10f;
    final float spacingBetwHPBarAndStaminaBar = 30f;
    int amountOfHeartsInRow = 20;

    int indexXOfLastFullHalf;
    int indexYOfLastFullHalf;


    public HUDv2(Player player) {
        this.spriteBatch = LevelScreen.getInstance().batch;
        this.stage =
            new Stage(
                new ScalingViewport(
                    Scaling.stretch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera()
                ), spriteBatch
            );

        matrixHPBar = new ArrayList<>();
        statusBar = new HashMap<>();


        Gdx.input.setInputProcessor(this.stage);
        this.health = player.health;
        this.maxHealth = player.maxHealth;

        loadTextures();
        updateHPBar();
        createStaminaBar();
        createDamageButton();
        createAddStatusButton();
    }

    public void loadTextures() {
        atlas = new TextureAtlas(Gdx.files.local("temporary\\HUDv2\\HUD_v2.atlas"));
        animations = new HashMap<>();

        Animation<TextureRegion> lHFtoE = new Animation<>(heartsAnimationFrameDuration,
            atlas.findRegions("lHalfFullToEmpty")); // lHalfFullToEmpty
        lHFtoE.setPlayMode(Animation.PlayMode.LOOP);

        Animation<TextureRegion> rHFtoE = new Animation<>(heartsAnimationFrameDuration,
            atlas.findRegions("rHalfFullToEmpty")); // rHalfFullToEmpty
        rHFtoE.setPlayMode(Animation.PlayMode.LOOP);

        Array <TextureRegion> lHFtoFTextureRegions = new Array<>();
        lHFtoFTextureRegions.add(atlas.findRegion("lHalfFullToFull")); // lHalfFullToFull
        lHFtoFTextureRegions.add(atlas.findRegion("lHalfFullToEmpty", 0)); // lHalfFullToEmpty
        lHFtoFTextureRegions.add(atlas.findRegion("lHalfFullToFull")); // lHalfFullToFull
        lHFtoFTextureRegions.add(atlas.findRegion("lHalfFullToEmpty", 0)); // lHalfFullToEmpty

        Animation<TextureRegion> lHFtoF = new Animation<>(heartsAnimationFrameDuration, lHFtoFTextureRegions);
        lHFtoF.setPlayMode(Animation.PlayMode.LOOP);

        animations.put("lHalfFullToFull", lHFtoF);
        animations.put("lHalfFullToEmpty", lHFtoE);
        animations.put("rHalfFullToEmpty", rHFtoE);

        offsetXStepHP = animations.get("lHalfFullToEmpty").getKeyFrame(0).getRegionWidth() * heartsScaling;
        offsetYStepHP = animations.get("lHalfFullToEmpty").getKeyFrame(0).getRegionHeight() * heartsScaling;




    }
    private void addImageToRow(List<Image> row, float offsetX, float offsetY, TextureRegion regionName) {
        Image eachHeart = new Image(regionName);
        eachHeart.setScale(heartsScaling);
        eachHeart.setPosition(offsetX, Gdx.graphics.getHeight() - offsetY - eachHeart.getHeight());
        row.add(eachHeart);
        stage.addActor(eachHeart);
    }

    // for info: lHFull, rHFull, lHEmpty, rHEmpty

    private List<Float> fillHPBarWithHearts(
        int target, float StartX, float StartY, TextureRegion lHalfTexture, TextureRegion rHalfTexture, float count
    ) {
        float x = StartX;
        float y = StartY;
        if (count % (amountOfHeartsInRow * 2) == 0) {
            matrixHPBar.add(new ArrayList<>());
        }
        List<Image> row = matrixHPBar.remove(matrixHPBar.size() - 1);

        while (target != count){
            addImageToRow(row, x, y, count % 2 == 0 ? lHalfTexture : rHalfTexture);
            x += count % 2 == 0 ? offsetXStepHP : offsetXStepHP + spacingX;
            count++;
            if (count % (amountOfHeartsInRow * 2) == 0) {
                matrixHPBar.add(row);
                row = new ArrayList<>();
                y += offsetYStepHP + spacingY;
                x = padding;
            }
        }
        if (!row.isEmpty()) {
            matrixHPBar.add(row);
        }
        if (lHalfTexture == atlas.findRegion("lHalfFullToEmpty", 0)) {
            indexXOfLastFullHalf = matrixHPBar.get(matrixHPBar.size()-1).size()-1;
            indexYOfLastFullHalf = matrixHPBar.size()-1;
            System.out.println(indexXOfLastFullHalf + " " + indexYOfLastFullHalf);
        }
        return List.of(x, y, count);
    }

    public void updateHPBar(){
        if (health <= 0) {
            health = 2;
        }
        clearHPBar();

        TextureRegion lHFull = atlas.findRegion("lHalfFullToEmpty", 0);
        TextureRegion rHFull = atlas.findRegion("rHalfFullToEmpty", 0);
        TextureRegion lHEmpty = atlas.findRegion("lHalfFullToEmpty", 3);
        TextureRegion rHEmpty = atlas.findRegion("rHalfFullToEmpty", 3);

        List<Float> resultOfFirstFill =
            fillHPBarWithHearts(health, padding, offsetYStepHP / 2 + padding, lHFull, rHFull, 0);
        fillHPBarWithHearts(
            maxHealth, resultOfFirstFill.get(0), resultOfFirstFill.get(1), lHEmpty, rHEmpty, resultOfFirstFill.get(2)
        );
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

        int width = (int) (300 * staminaBarScaling);
        int height = (int) (20 * staminaBarScaling);

        staminaBar = new ProgressBar(
            0f, stamina, staminaConsumptionSpeed, false, new ProgressBar.ProgressBarStyle()
        );

        staminaBar.getStyle().background = commonFunctions.getColoredDrawable(width, height, Color.DARK_GRAY);
        staminaBar.getStyle().knob = commonFunctions.getColoredDrawable(0, height, Color.GOLD);
        staminaBar.getStyle().knobBefore = commonFunctions.getColoredDrawable(width, height, Color.GOLD);

        staminaBar.setWidth(width);
        staminaBar.setHeight(height);
        staminaBar.setValue(stamina);
        staminaBar.setAnimateDuration(0.25f);


        float stBarX = matrixHPBar.get(matrixHPBar.size() - 1).get(0).getX() + staminaBarPadding;
        float stBarY = matrixHPBar.get(matrixHPBar.size() - 1).get(0).getY() - spacingBetwHPBarAndStaminaBar - height;


        staminaBar.setPosition(stBarX, stBarY);


        stage.addActor(staminaBar);
        setStaminaBarBorder((int) (width/staminaBarScaling), (int) (height/staminaBarScaling), stBarX, stBarY + height);
    }

    private void setStaminaBarBorder(int width, int height, float stBarX, float stBarY) {
        //TODO repack Atlas with proper name and get rid of "magic numbers"
        Image staminaBarBorder = new Image(atlas.findRegion("staminaBarBorder"));
        float borderWidth = (width +33)*staminaBarScaling;
        float borderHeight = (height + 22)*staminaBarScaling;
        float borderSpacingX = 22*staminaBarScaling;
        float borderSpacingY = (height+10)*staminaBarScaling;

        staminaBarBorder.setSize(borderWidth, borderHeight);
        staminaBarBorder.setPosition(stBarX - borderSpacingX, stBarY - borderSpacingY);
        stage.addActor(staminaBarBorder);
    }

    public void updateStaminaBar(float currentStamina, float maxStamina) {
        stamina = maxStamina;
        staminaBar.setRange(0f, maxStamina);
        staminaBar.setValue(currentStamina);
    }
    public void drainStamina(float stamina) {
        staminaBar.setValue(staminaBar.getValue() - stamina);
    }

    public void addStatus(String statusName) {
        TextureAtlas.AtlasRegion region = atlas.findRegion(statusName);
        Image statusImg = new Image(region);
        statusBar.put(statusName, statusImg);

//        statusDurations.put(statusName, duration);
    }

    public void removeStatus(String statusName) {
        statusBar.get(statusName).remove();
        statusBar.remove(statusName);
    }

    public void renderStatusBar() {
        float x = matrixHPBar.get(0).get(amountOfHeartsInRow*2-1).getX()+ offsetXStepHP + statusBarSpacingFromHPBar;
        float y = matrixHPBar.get(0).get(0).getY();

        for (Map.Entry<String, Image> entry : statusBar.entrySet()) {
            Image statusImg = entry.getValue();
            statusImg.setScale(statusScale);
            statusImg.setPosition(x, y);
            stage.addActor(statusImg);
            x -= statusImg.getWidth() + statusBarInnerSpacing;
        }
    }

    private void damageAnimation(float deltaTime){
        // receivedDmg, IndexX, IndexY
        int heartsToAnimate = receivedDmg;
        if (receivedDmg <= 0) {
            return;
        }
        int tempIndexX = indexXOfLastFullHalf % (amountOfHeartsInRow * 2 - 1) == 0 ? 0 : indexXOfLastFullHalf + 1;
//        int tempIndexX = indexXOfLastHeartToAnimate;
        int tempIndexY = indexXOfLastFullHalf % (amountOfHeartsInRow * 2 - 1) == 0 ? indexYOfLastFullHalf + 1 :
            indexYOfLastFullHalf;

        if (indexXOfLastFullHalf % 2 == 0) {
            drawHeartAnimation("lHalfFullToFull", indexYOfLastFullHalf, indexXOfLastFullHalf);
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
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.font = new BitmapFont();

        Button takeDmgButton = new TextButton("Take Damage", textButtonStyle);
        takeDmgButton.setPosition(20, Gdx.graphics.getHeight() / 2f);
        takeDmgButton.addCaptureListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("Damage button clicked");
                takeDmg(30);
                return true;
            }
        });
        stage.addActor(takeDmgButton);
    }

    public void createAddStatusButton(){
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.font = new BitmapFont();

        Button addStatusButton = new TextButton("Add Status", textButtonStyle);
        addStatusButton.setPosition(20, Gdx.graphics.getHeight() / 2f - 50);
        addStatusButton.addCaptureListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("Add status button clicked");
                addStatus("status1");
                return true;
            }
        });
        stage.addActor(addStatusButton);
    }

    public void dispose() {
        atlas.dispose();
        stage.dispose();
    }


    public void render(float deltaTime) {
        stage.act();
        stage.draw();
        stage.getBatch().begin();
        if (gotHit) {
            damageAnimation(deltaTime);
            hitElapsedTime += deltaTime;
            if (areAllAnimationsFinished()) {
                gotHit = false;
                hitElapsedTime = 0f;
            }
        }
        renderStatusBar();
        stage.getBatch().end();
    }
}

