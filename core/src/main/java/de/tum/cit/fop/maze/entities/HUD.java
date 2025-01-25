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
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import de.tum.cit.fop.maze.entities.tile.Collectable;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.*;
import java.util.List;

public class HUD {
    private final Stage stage;
    private final List<List<Image>> matrixHPBar;
    private final Map<String, Image> statusBar;
    private ProgressBar staminaBar;
    private final float heartsAnimationFrameDuration = 1 / 5f;
    private final SpriteBatch spriteBatch;
    private final Label scoreLabel;
    private final Label time;

    private final Label coins;
    private final Table coinsAndKeysTable = new Table();
//    private final Table healthTable;


    private TextureAtlas atlas;


    private int currentScore = 1000;
    private final float fontScale = 1.1f;
    //    private final Instant start = Clock.systemDefaultZone().instant();
//    private Instant now;
    private final Table timeAndScoreTable;
    private Label.LabelStyle descriptionStyle;
    public final Table spriteInventory = new Table();
    public final Table textInventory = new Table();
    public final Table descriptionTable = new Table();
    public final Container<Table> descriptionContainer = new Container<>(descriptionTable);
    private int inventoryRows = 2;
    private int inventoryCols = 5;
    private int sizeOfInvIcon = 40;
    private int invFontSize = 17;
    private int spacingBetweenIcons = 10;
    private float tableOffsetX = 20;
    private float tableOffsetY = 10;
    private final float inventoryWidth = inventoryCols * sizeOfInvIcon +
        (inventoryCols - 1) * spacingBetweenIcons;
    private final float inventoryHeight = inventoryRows * sizeOfInvIcon +
        (inventoryRows - 1) * spacingBetweenIcons;
    private final TextureAtlas inventoryAtlas;
    private Map<String, Label> invInfo = new HashMap<>();
    private float elapsedTime = 0f;

    private Sprite exitArrow;

    private int receivedDmg;
    private int health;
    private int maxHealth;


    private float staminaRecoveryElapsedTime = 0f;
    private float staminaDrainElapsedTime = 0f;
    private float maxStamina = 100f;
    private float currentStamina = 100f;
    private float staminaPerSecond = 10f;
    private boolean staminaDrain = false;
    private boolean staminaRecovery = false;

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

        timeAndScoreTable = new Table();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("font/YosterIslandRegular-VqMe.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 27;
        parameter.color = new Color(0xE0E0E0FF);
        parameter.borderWidth = 1;
        parameter.borderColor = new Color(0x000000FF);
        Label.LabelStyle timeAndScoreStyle = new Label.LabelStyle();
        timeAndScoreStyle.font = generator.generateFont(parameter);
        time = new Label(getLabelTime(elapsedTime), timeAndScoreStyle);
        scoreLabel = new Label(getLabelScore(currentScore), timeAndScoreStyle);
        time.setFontScale(fontScale);
        scoreLabel.setFontScale(fontScale);

        Label.LabelStyle coinsAndKeysLabelStyle = new Label.LabelStyle();
        coinsAndKeysLabelStyle.font = generator.generateFont(parameter);

        descriptionStyle = new Label.LabelStyle();
        parameter.size = 17; // font size
        parameter.borderWidth = 0;
        parameter.color = new Color(1, 1, 1, 0.7f);
        parameter.gamma = 4f;
        descriptionStyle.font = generator.generateFont(parameter);
        //TODO change background to png

        descriptionContainer.setBackground(Utils.getColoredDrawable(200, 200,
            new Color(0, 0, 0, 0.7f)));


        generator.dispose();
        initTimeAndScoreTable();

        // setItemDescription("LOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOng description");


        inventoryAtlas = new TextureAtlas(Gdx.files.local("assets/temporary/collectables/collectables.atlas"));
        setupInventory();

        loadTextures();
        updateHPBar();
        createStaminaBar();

        coins = new Label(String.format(": " + LevelScreen.getInstance().player.getGold()), coinsAndKeysLabelStyle);

        initCoinsAndKeysTable();

        exitArrow = new Sprite(atlas.findRegion("arrow"));
        initExitArrow();
    }

    public void initExitArrow() {

        float arrowSize = 50;
        exitArrow.setSize(arrowSize, arrowSize);
        updateArrowPosition();
    }

    private void updateArrowPosition() {
        float arrowPadding = 20;
        exitArrow.setPosition(stage.getViewport().getWorldWidth() - exitArrow.getWidth() - arrowPadding,
            arrowPadding);
    }

    public void drawExitArrow(float angle) {
        exitArrow.setRotation(angle);
        exitArrow.draw(spriteBatch);
    }

    private void pickUpCoin(int value){
        float spacingFromStaminaBar = 10;

        LevelScreen.getInstance().player.addGold(value);
        coins.setText(": " + LevelScreen.getInstance().player.getGold());
        coinsAndKeysTable.setPosition(padding + coinsAndKeysTable.getPrefWidth() / 2,
            staminaBar.getY() - spacingFromStaminaBar - coinsAndKeysTable.getPrefHeight() / 2);

    }

    private void pickUpKey() {

        float spacingFromStaminaBar = 10;
        float keyWidth = 45;
        float keyHeight = 35;

        Drawable keysIcon = new TextureRegionDrawable(inventoryAtlas.findRegion("key", 1));
        keysIcon.setMinHeight(keyHeight);
        keysIcon.setMinWidth(keyWidth);
        coinsAndKeysTable.add(new Image(keysIcon)).width(keyWidth).height(keyHeight);
        coinsAndKeysTable.setPosition(padding + coinsAndKeysTable.getPrefWidth() / 2,
            staminaBar.getY() - spacingFromStaminaBar - coinsAndKeysTable.getPrefHeight() / 2);
    }

    private void initCoinsAndKeysTable() {

        Drawable coinsIcon = new TextureRegionDrawable(inventoryAtlas.findRegion("coin"));
        float iconSize = 40;
        coinsIcon.setMinWidth(iconSize);
        coinsIcon.setMinHeight(iconSize);
        coinsAndKeysTable.add(new Image(coinsIcon)).width(iconSize).height(iconSize);
        coinsAndKeysTable.add(coins);
        coinsAndKeysTable.row();
        float spacingFromStaminaBar = 10;
        coinsAndKeysTable.setPosition(padding + coinsAndKeysTable.getPrefWidth() / 2,
            staminaBar.getY() - spacingFromStaminaBar - coinsAndKeysTable.getPrefHeight() / 2);
        stage.addActor(coinsAndKeysTable);
    }

    private void setItemDescription(String description) {
        float labelWidth = 300;
//        for (int i = 0; i < description.length()/lenOfCharsInRow; i ++) {
//            Label itemDescription = new Label(description.substring(i * lenOfCharsInRow,
//                (i + 1) * lenOfCharsInRow - 1), descriptionStyle);
//            descriptionTable.add(itemDescription);
//            descriptionTable.row();
//        }
//        if (description.length() % lenOfCharsInRow > 0) {
//            Label itemDescription = new Label(description.substring(description.length()
//                - description.length() % lenOfCharsInRow), descriptionStyle);
//            descriptionTable.add(itemDescription);
//            descriptionTable.row();
//        }
//        descriptionTable.setPosition(padding + descriptionTable.getWidth()/2,
//            padding + descriptionTable.getHeight()/2);
//        stage.addActor(descriptionTable);
        Label itemDescription = new Label(description, descriptionStyle);
        itemDescription.setWrap(true);
        itemDescription.setWidth(labelWidth);
        itemDescription.setBounds(0, 0, labelWidth, itemDescription.getPrefHeight());
        itemDescription.setAlignment(Align.topRight);
//        itemDescription.setAlignment(Align.bottomLeft);
        descriptionTable.add(itemDescription).width(labelWidth).pad(padding);
        descriptionTable.setSize(labelWidth, itemDescription.getPrefHeight());

        updateDescriptionPosition();

        spriteInventory.setVisible(false);
        textInventory.setVisible(false);
        stage.addActor(descriptionContainer);
    }

    private void updateDescriptionPosition() {

        float containerWidth = descriptionTable.getWidth() + 15;
        float containerHeight = descriptionTable.getHeight() + 15;
        descriptionContainer.setSize(containerWidth, containerHeight);

        float containerX = stage.getViewport().getWorldWidth() - containerWidth - padding;
        float containerY = stage.getViewport().getWorldHeight() - containerHeight - padding;
        descriptionContainer.setPosition(containerX, containerY);
    }

    private void deleteDescription() {

        descriptionTable.clear();
        spriteInventory.setVisible(true);
        textInventory.setVisible(true);
    }

    private void heal(int value) {
        health = Math.min(health + value, maxHealth);
        updateHPBar();
    }

    private void setupInventory() {

        spriteInventory.setSize(inventoryWidth, inventoryHeight);
        textInventory.setSize(inventoryWidth, inventoryHeight);

        updateInventoryPosition();


        stage.addActor(textInventory);
    }

    private void updateInventoryPosition() {
        spriteInventory.setPosition(stage.getViewport().getWorldWidth() - inventoryWidth - tableOffsetX - padding,
            stage.getViewport().getWorldHeight() - inventoryHeight - padding);
        stage.addActor(spriteInventory);
        textInventory.setPosition(stage.getViewport().getWorldWidth() - inventoryWidth - padding,
            stage.getViewport().getWorldHeight() - inventoryHeight - tableOffsetY - padding);
    }

    public void addItemToInventory(Collectable collectable) {
        Collectable.CollectableType collectableType = collectable.getType();
        String textureName = collectable.getCollectableAttributes().textureName;
        if (invInfo.get(collectableType.name()) == null) {
            if (Objects.equals(collectableType, Collectable.CollectableType.HEART)) {
                heal(collectable.getCollectableAttributes().getImmediateHealing());
                return;
            }

            if (Objects.equals(collectableType, Collectable.CollectableType.KEY)) {
                pickUpKey();
                return;
            }

            if (Objects.equals(collectableType, Collectable.CollectableType.GOLD_COIN)) {
                pickUpCoin(collectable.getCollectableAttributes().getImmediateCoins());
                return;
            }

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

            invInfo.put(collectableType.name(), label);
            if (spriteInventory.getChildren().size % inventoryCols == 0) {
                spriteInventory.row().padTop(spacingBetweenIcons);
                textInventory.row().padTop(spacingBetweenIcons);
            }
            return;
        }
        Label label = invInfo.get(collectableType.name());
        String[] text = label.getText().toString().split("x");
        int amount = Integer.parseInt(text[1]);
        amount++;
        label.setText("x" + amount);


    }

    private void initTimeAndScoreTable() {
        timeAndScoreTable.add(time);
        timeAndScoreTable.row();
        timeAndScoreTable.add(scoreLabel);
        updateLabelTablePosition();
//        labelTable.debug();
        stage.addActor(timeAndScoreTable);
    }

    private void updateLabelTablePosition() {
        timeAndScoreTable.setSize(time.getWidth(),
            time.getHeight() + scoreLabel.getHeight());
        timeAndScoreTable.setPosition((stage.getViewport().getWorldWidth() - timeAndScoreTable.getWidth()) / 2,
            stage.getViewport().getWorldHeight() - timeAndScoreTable.getHeight() - labelPadding);
        timeAndScoreTable.align(Align.center);
    }

    private String getLabelTime(float elapsedTime) {
        long seconds = (long) elapsedTime;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format(
            Locale.getDefault(), "Time: %02d:%02d:%02d", hours, minutes % 60, seconds % 60
        );
    }

    private String getLabelScore(int score) {
        return String.format(
            Locale.getDefault(), "Score: %06d", score
        );
    }

    private void addScore(int val) {
        currentScore += val;
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
            0f, maxStamina, 0.001f, false, new ProgressBar.ProgressBarStyle()
        );

        staminaBar.getStyle().background = Utils.getColoredDrawable(width, height + 1, Color.DARK_GRAY);
        staminaBar.getStyle().knob = Utils.getColoredDrawable(0, height, Color.GOLD);
        staminaBar.getStyle().knobBefore = Utils.getColoredDrawable(width, height, Color.GOLD);

        staminaBar.setWidth(width);
        staminaBar.setHeight(height);
        staminaBar.setValue(maxStamina);
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

    public void beginStaminaDrain(float amount) {
        staminaDrain = true;
        staminaRecovery = false;
        setStaminaPerSecond(amount);
    }

    public void stopStaminaDrain() {
        staminaDrain = false;
    }

    public void beginStaminaRecovery(float amount) {
        staminaDrain = false;
        staminaRecovery = true;
        setStaminaPerSecond(amount);
    }

    public void setStaminaRecoveryDelay(float delay) {
    }

    public void setStaminaRecovery(boolean recovery) {
        staminaRecovery = recovery;
    }

//    public void renderStatusBar() {
//        float x = matrixHPBar.get(0).get(amountOfHeartsInRow * 2 - 1).getX()
//            + offsetXStepHP + statusBarSpacingFromHPBar;
//        float y = matrixHPBar.get(0).get(0).getY();
//
//        for (Map.Entry<String, Image> entry : statusBar.entrySet()) {
//            Image statusImg = entry.getValue();
//            statusImg.setScale(statusScale);
//            statusImg.setPosition(x, y);
//            stage.addActor(statusImg);
//            x -= statusImg.getWidth() + statusBarInnerSpacing;
//        }
//    }

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

    private void updateLabels(float elapsedTime) {
        time.setText(getLabelTime(elapsedTime));
        scoreLabel.setText(getLabelScore(currentScore - (int) elapsedTime));
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
        updateStaminaBar(deltaTime);
        drawExitArrow(0);
        stage.getBatch().end();
    }

    public void setStaminaPerSecond(float staminaPerSecond) {
        this.staminaPerSecond = staminaPerSecond;
    }

    private void updateStaminaBar(float deltaTime) {
        if (!staminaDrain && staminaDrainElapsedTime > 0) {
            currentStamina -= staminaDrainElapsedTime * staminaPerSecond;
            if (currentStamina < 0) {
                currentStamina = 0;
            }
            staminaDrainElapsedTime = 0;
        }
        if (!staminaRecovery && staminaRecoveryElapsedTime > 0) {
            if (currentStamina + staminaRecoveryElapsedTime * staminaPerSecond > maxStamina) {
                currentStamina = maxStamina;
            }
            currentStamina += staminaRecoveryElapsedTime * staminaPerSecond;
            staminaRecoveryElapsedTime = 0;
        }

        if (staminaRecovery) {
            staminaRecoveryElapsedTime += deltaTime;
            staminaBar.setValue(currentStamina + staminaRecoveryElapsedTime * staminaPerSecond);

        }
        if (staminaDrain) {
            staminaDrainElapsedTime += deltaTime;
            staminaBar.setValue(currentStamina - staminaDrainElapsedTime * staminaPerSecond);
        }
        if (currentStamina <= 0) {
            staminaDrain = false;
            currentStamina = 0;
        }
        if (currentStamina >= maxStamina) {
            staminaRecovery = false;
            currentStamina = maxStamina;
        }
    }

    public void show() {
        Gdx.input.setInputProcessor(this.stage);
    }

    public void resize() {
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        updateLabelTablePosition();
        updateDescriptionPosition();
        updateInventoryPosition();
        updateArrowPosition();
    }
}

