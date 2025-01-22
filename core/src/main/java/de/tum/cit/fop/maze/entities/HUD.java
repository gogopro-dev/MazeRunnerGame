package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HUD {
    private final Stage stage;
    private final List<List<Image>> matrixHPBar;
    private final Map<String, Image> statusBar;
    private ProgressBar staminaBar;
    private final float heartsAnimationFrameDuration = 1 / 5f;
    private final SpriteBatch spriteBatch;
    private final Label scoreLabel;
    private final Label timeLabel;
//    private final Table timeAndScoreTable;
//    private final Table healthTable;


    private TextureAtlas atlas;


    private int score = 1000;
    private final BitmapFont font;
    private final float fontScale = 1.1f;
    //    private final Instant start = Clock.systemDefaultZone().instant();
//    private Instant now;
    private final Table labelTable;
    private Label.LabelStyle labelStyle;
    public final Table spriteInventory = new Table();
    public final Table textInventory = new Table();
    private int inventoryRows = 2;
    private int inventoryCols = 5;
    private int sizeOfInvIcon = 40;
    private int invFontSize = 17;
    private int spacingBetweenIcons = 10;
    private float tableOffsetX = 20;
    private float tableOffsetY = 10;
    private float inventoryWidth = inventoryCols * sizeOfInvIcon +
        (inventoryCols - 1) * spacingBetweenIcons;
    private float inventoryHeight = inventoryRows * sizeOfInvIcon +
        (inventoryRows - 1) * spacingBetweenIcons;
    private final TextureAtlas inventoryAtlas;
    private Map<String, Label> invInfo = new HashMap<>();
    private float elapsedTime = 0f;

    private int receivedDmg;
    private int health;
    private int maxHealth;

    private float stamina = 100f;
    private final float staminaConsumptionSpeed = 0.5f;
    private final float staminaBarScaling = 1f;
    private final float fillamentAlignmentX = 32f;

    private final float statusBarSpacingFromHPBar = 10f;
    private final float statusBarInnerSpacing = 5f;
    private final float statusScale = 1.5f;


    private boolean gotHit = false;
    private Map<String, Animation<TextureRegion>> animations;

    float hitElapsedTime = 0;
    float offsetXStepHP;
    float offsetYStepHP;
    final float spacingX = 4f;
    final float spacingY = 4f;
    final float heartsScaling = 1.6f;
    final float padding = 10f;
    final float labelPadding = padding + 5f;
    final float spacingBetwHPBarAndStaminaBar = 20f;
    int amountOfHeartsInRow = 10;

    int indexXOfLastFullHalf;
    int indexYOfLastFullHalf;


    public HUD(Player player) {
        this.spriteBatch = LevelScreen.getInstance().batch;
        this.stage =
            new Stage(
                new ExtendViewport(
                    Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera()
                ), spriteBatch
            );


        matrixHPBar = new ArrayList<>();
        statusBar = new HashMap<>();

        Gdx.input.setInputProcessor(this.stage);
        this.health = player.health;
        this.maxHealth = player.maxHealth;

        labelTable = new Table();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("font/YosterIslandRegular-VqMe.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 27;
        parameter.color = new Color(0xE0E0E0FF);
        parameter.borderWidth = 1;
        parameter.borderColor = new Color(0x000000FF);
        font = generator.generateFont(parameter);
        labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        timeLabel = new Label(getLabelTime(elapsedTime), labelStyle);
        scoreLabel = new Label(getLabelScore(score), labelStyle);
        timeLabel.setFontScale(fontScale);
        scoreLabel.setFontScale(fontScale);

        setLabelTablePosition();

        inventoryAtlas = new TextureAtlas(Gdx.files.local("assets/temporary/collectables/collectables.atlas"));
        setupInventory();

        loadTextures();
        updateHPBar();
        createStaminaBar();
        createDamageButton();
    }

    private void setupInventory() {

        spriteInventory.setSize(inventoryWidth, inventoryHeight);
        spriteInventory.setPosition(stage.getViewport().getWorldWidth() - inventoryWidth - tableOffsetX - padding,
            stage.getViewport().getWorldHeight() - inventoryHeight - padding);
        stage.addActor(spriteInventory);

        textInventory.setSize(inventoryWidth, inventoryHeight);
        textInventory.setPosition(stage.getViewport().getWorldWidth() - inventoryWidth - padding,
            stage.getViewport().getWorldHeight() - inventoryHeight - tableOffsetY - padding);
        stage.addActor(textInventory);
    }

    public void updateInventory(String collectableType, String textureName) {
        if (invInfo.get(collectableType) == null) {
            ;
            Drawable drawable = new TextureRegionDrawable(inventoryAtlas.findRegion(textureName));
            drawable.setMinWidth(sizeOfInvIcon);
            drawable.setMinHeight(sizeOfInvIcon);
            spriteInventory.add(new Image(drawable)).width(sizeOfInvIcon).height(sizeOfInvIcon).center()
                .padRight(spacingBetweenIcons);

            Label.LabelStyle labelStyle = new Label.LabelStyle();
            labelStyle.font = createFont(invFontSize, Color.WHITE);
            Label label = new Label("x1", labelStyle);
            textInventory.add(label).width(sizeOfInvIcon).height(sizeOfInvIcon).center()
                .padRight(spacingBetweenIcons);

            invInfo.put(collectableType, label);
            if (spriteInventory.getChildren().size % inventoryCols == 0) {
                spriteInventory.row().padTop(spacingBetweenIcons);
                textInventory.row().padTop(spacingBetweenIcons);
            }
            return;
        }
        Label label = invInfo.get(collectableType);
        String[] text = label.getText().toString().split("x");
        int amount = Integer.parseInt(text[1]);
        amount++;
        label.setText("x" + amount);


    }

    private void setLabelTablePosition() {
        labelTable.add(timeLabel);
        labelTable.row();
        labelTable.add(scoreLabel);
        labelTable.setSize(timeLabel.getWidth(),
            timeLabel.getHeight() + scoreLabel.getHeight());
        labelTable.setPosition((stage.getViewport().getWorldWidth() - labelTable.getWidth()) / 2,
            stage.getViewport().getWorldHeight() - labelTable.getHeight() - labelPadding);
        labelTable.align(Align.center);
//        labelTable.debug();
        stage.addActor(labelTable);
    }

    private String getLabelTime(float elapsedTime) {
        long seconds = (long) elapsedTime;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("Time: %02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

    private String getLabelScore(int score) {
        return String.format("Score: %06d", score);
    }

    public void loadTextures() {
        atlas = new TextureAtlas(Gdx.files.local("assets/temporary/HUDv2/HUDv2.atlas"));
        animations = new HashMap<>();

        Animation<TextureRegion> lHFtoE = new Animation<>(heartsAnimationFrameDuration,
            atlas.findRegions("HealthL_DMG")); // lHalfFullToEmpty
        lHFtoE.setPlayMode(Animation.PlayMode.LOOP);

        Animation<TextureRegion> rHFtoE = new Animation<>(heartsAnimationFrameDuration,
            atlas.findRegions("HealthR_DMG")); // rHalfFullToEmpty
        rHFtoE.setPlayMode(Animation.PlayMode.LOOP);

        Array<TextureRegion> HealthL_noDMG = new Array<>();
        HealthL_noDMG.add(atlas.findRegion("HealthL_noDMG", 1)); // lHalfFullToFull
        HealthL_noDMG.add(atlas.findRegion("HealthL_DMG", 1)); // lHalfFullToEmpty
        HealthL_noDMG.add(atlas.findRegion("HealthL_DMG", 0)); // lHalfFullToFull
        HealthL_noDMG.add(atlas.findRegion("HealthL_noDMG", 1)); // lHalfFullToEmpty

        Animation<TextureRegion> lHFtoF = new Animation<>(heartsAnimationFrameDuration, HealthL_noDMG);
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
        eachHeart.setPosition(offsetX, stage.getViewport().getWorldHeight() - offsetY - eachHeart.getHeight());
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

        while (target != count) {
            addImageToRow(row, x, y, count % 2 == 0 ? lHalfTexture : rHalfTexture);
            x += count % 2 == 0 ? offsetXStepHP : offsetXStepHP + spacingX;
            count++;
            if (count % (amountOfHeartsInRow * 2) == 0) {
                matrixHPBar.add(row);
                row = new ArrayList<>();
                y += offsetYStepHP + spacingY;
                x = padding + fillamentAlignmentX;
            }
        }
        if (!row.isEmpty()) {
            matrixHPBar.add(row);
        }
        if (lHalfTexture == atlas.findRegion("HealthL_noDMG", 1)) {
            indexXOfLastFullHalf = matrixHPBar.get(matrixHPBar.size() - 1).size() - 1;
            indexYOfLastFullHalf = matrixHPBar.size() - 1;
            System.out.println(indexXOfLastFullHalf + " " + indexYOfLastFullHalf);
        }
        return List.of(x, y, count);
    }

    public void updateHPBar() {
        if (health <= 0) {
            health = 2;
        }
        clearHPBar();

        TextureRegion lHFull = atlas.findRegion("HealthL_noDMG", 1);
        TextureRegion rHFull = atlas.findRegion("HealthR_noDMG", 1);
        TextureRegion lHEmpty = atlas.findRegion("HealthL_DMG", 2);
        TextureRegion rHEmpty = atlas.findRegion("HealthR_DMG", 2);

        List<Float> resultOfFirstFill =
            fillHPBarWithHearts(health, padding + fillamentAlignmentX, offsetYStepHP / 2 + padding,
                lHFull, rHFull, 0
            );
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

    private void drawHeartAnimation(String animationName, int y, int x) {
        TextureRegion currentFrame = animations.get(animationName).getKeyFrame(hitElapsedTime);
        Image heart = matrixHPBar.get(y).get(x);
        float width = heart.getImageWidth() * heartsScaling;
        float height = heart.getImageHeight() * heartsScaling;
        spriteBatch.draw(currentFrame, heart.getX(), heart.getY(), width, height);
    }


    public void createStaminaBar() {
        Image lastHeart = matrixHPBar.get(matrixHPBar.size() - 1).
            get(matrixHPBar.get(matrixHPBar.size() - 1).size() - 1);
        int width = (int) ((lastHeart.getX() - padding - fillamentAlignmentX
            + lastHeart.getWidth() * heartsScaling) * staminaBarScaling);
        int height = (int) (20 * staminaBarScaling);

        staminaBar = new ProgressBar(
            0f, stamina, staminaConsumptionSpeed, false, new ProgressBar.ProgressBarStyle()
        );

        staminaBar.getStyle().background = Utils.getColoredDrawable(width, height + 1, Color.DARK_GRAY);
        staminaBar.getStyle().knob = Utils.getColoredDrawable(0, height, Color.GOLD);
        staminaBar.getStyle().knobBefore = Utils.getColoredDrawable(width, height, Color.GOLD);

        staminaBar.setWidth(width);
        staminaBar.setHeight(height);
        staminaBar.setValue(stamina);
        staminaBar.setAnimateDuration(0.25f);


        float stBarX = padding;
        float stBarY = matrixHPBar.get(matrixHPBar.size() - 1).get(0).getY() - spacingBetwHPBarAndStaminaBar - height;


        staminaBar.setPosition(stBarX + fillamentAlignmentX * staminaBarScaling, stBarY);


        stage.addActor(staminaBar);
        setStaminaBarBorder(
            (int) (width / staminaBarScaling), (int) (height / staminaBarScaling), stBarX, stBarY + height
        );
    }

    private void setStaminaBarBorder(int width, int height, float stBarX, float stBarY) {
        //TODO repack Atlas with proper name and get rid of "magic numbers"
        Image staminaBarBorder = new Image(atlas.findRegion("staminaBarBorder"));
        float borderWidth = (width + 36) * staminaBarScaling;
        float borderHeight = (height + 20) * staminaBarScaling;
        float borderAlignmentY = (height + 11) * staminaBarScaling;

        staminaBarBorder.setSize(borderWidth, borderHeight);
        staminaBarBorder.setPosition(stBarX, stBarY - borderAlignmentY);
        stage.addActor(staminaBarBorder);
    }

    public void drainStamina(float deltaStamina) {
        staminaBar.setValue(staminaBar.getValue() - deltaStamina);
    }

    public void restoreStamina(float deltaStamina) {
        staminaBar.setValue(staminaBar.getValue() + deltaStamina);
    }

    public void renderStatusBar() {
        float x = matrixHPBar.get(0).get(amountOfHeartsInRow * 2 - 1).getX()
            + offsetXStepHP + statusBarSpacingFromHPBar;
        float y = matrixHPBar.get(0).get(0).getY();

        for (Map.Entry<String, Image> entry : statusBar.entrySet()) {
            Image statusImg = entry.getValue();
            statusImg.setScale(statusScale);
            statusImg.setPosition(x, y);
            stage.addActor(statusImg);
            x -= statusImg.getWidth() + statusBarInnerSpacing;
        }
    }

    private void damageAnimation(float deltaTime) {
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

        while (heartsToAnimate > 0) {
            if (tempIndexX % 2 == 0) {
                drawHeartAnimation("lHalfFullToEmpty", tempIndexY, tempIndexX);
            } else {
                drawHeartAnimation("rHalfFullToEmpty", tempIndexY, tempIndexX);
            }
            tempIndexX++;
            heartsToAnimate--;
            if (tempIndexX >= amountOfHeartsInRow * 2) {
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

    private void updateLabels(float elapsedTime) {
        timeLabel.setText(getLabelTime(elapsedTime));
        scoreLabel.setText(getLabelScore(score - (int) elapsedTime));
    }

    private BitmapFont createFont(int size, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.local("font/YosterIslandRegular-VqMe.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.borderWidth = 1;
        parameter.borderColor = new Color(0x000000FF);
        return generator.generateFont(parameter);
    }

    public void dispose() {
        atlas.dispose();
        stage.dispose();
    }


    public void render(float deltaTime) {
        elapsedTime += deltaTime;
        updateLabels(elapsedTime);
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

    public void show() {
        Gdx.input.setInputProcessor(this.stage);
    }

    public void resize() {
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }
}

