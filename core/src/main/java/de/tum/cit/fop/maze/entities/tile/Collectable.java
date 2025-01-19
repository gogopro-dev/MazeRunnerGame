package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.World;
import com.google.gson.Gson;
import de.tum.cit.fop.maze.level.LevelScreen;

public class Collectable extends TileEntity {
    /*Итак мои предложения к коллектблам:
Сердечки = выпадают с каким то шансом с убитых мобов. Восстанавливают 1(?) фулл сердце
Монетки = выпадают с мобов в количестве от 1 до 4(рандом). За них можно купить бафы в магазе
"Монетка дэмэджа" = могут выпасть с бочки с шансом 1%. +15% к общему дамагу по врагам
"Монетка Защиты" = могут выпасть с бочки с шансом 1%. -15% к получаемому урону от врагов
Магазин:
"Амулет воскресения": если вы умерли и есть амулет, то вы воскресните с фулл хп и станете неуязвимым на 3 секунды. После испольщования пропадает. Стоимость: 75 монет
"Амулет вампира": после каждого убитого врага есть шанс 10% восстановить себе здоровье (рандом от 1 сердца до 3, включая половинчатые значения). Стоимость 100 монет
"Сапоги скорохода": Увеличивает вашу скорость на 10%. Стоимость: 50 монет */

    /// Enum for Collectable types
    public enum CollectableType {
        HEART, GOLD_COIN, DAMAGE_COIN, DEFENSE_COIN, RESURRECTION_AMULET, VAMPIRE_AMULET, SPEED_BOOTS
    }
    protected CollectableType type  = null;
    private Animation<TextureRegion> idleAnimation;
    //TODO: add pickupAnimation
    private Animation<TextureRegion> pickupAnimation;
    private boolean pickedUp;
    /// Player attributes which will be changed after Collectable pickup
    protected record CollectableBuffs(int health, int gold, int damage, int resist, boolean resurrection, boolean vampireBuff, boolean speedBuff) {
        public CollectableBuffs() {
            this(0, 0, 0, 0, false, false, false);
        }
        public CollectableBuffs(int health, int gold, int damage, int resist, boolean resurrection, boolean vampireBuff, boolean speedBuff) {
            this.health = health;
            this.gold = gold;
            this.damage = damage;
            this.resist = resist;
            this.resurrection = resurrection;
            this.vampireBuff = vampireBuff;
            this.speedBuff = speedBuff;
        }
    }
    protected CollectableBuffs collectableBuffs = new CollectableBuffs();
    private int height;
    private int width;
    private int id;
    private String textureName;
    private String tempTextureName = "key";
    private SpriteBatch batch;
    private float frameDuration = 1f;
    private float elapsedTime;
    private float pickupElapsedTime;
    /** Constructor for testing purposes */

    public Collectable() {
        super(1, 1);
        batch = LevelScreen.getInstance().batch;
        TextureAtlas atlas = new TextureAtlas(Gdx.files.local("temporary/collectables/collectables.atlas"));
        idleAnimation = new Animation<>(frameDuration, atlas.findRegions(tempTextureName));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
//        pickupAnimation = new Animation<>(frameDuration, atlas.findRegions(idleAnimName));
    }
    /** constructor for testing purposes */
    public Collectable(String type, int health, int gold, int damage, int resist) {
        super(1, 1);
        batch = LevelScreen.getInstance().batch;
        TextureAtlas atlas = new TextureAtlas(Gdx.files.local("temporary/collectables/collectables.atlas"));
        idleAnimation = new Animation<>(frameDuration, atlas.findRegions(tempTextureName));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
        switch (type){
            case "HEART":
                this.type = CollectableType.HEART;
                break;
            case "GOLD_COIN":
                this.type = CollectableType.GOLD_COIN;
                break;
            case "DAMAGE_COIN":
                this.type = CollectableType.DAMAGE_COIN;
                break;
            case "DEFENSE_COIN":
                this.type = CollectableType.DEFENSE_COIN;
                break;
        }
        collectableBuffs = new CollectableBuffs(health, gold, damage, resist, false, false, false);
    }
/** Constructor to test Unique Collectables*/
    public Collectable(String uniqueType){
        super(1, 1);
        batch = LevelScreen.getInstance().batch;
        TextureAtlas atlas = new TextureAtlas(Gdx.files.local("temporary/collectables/collectables.atlas"));
        idleAnimation = new Animation<>(frameDuration, atlas.findRegions(tempTextureName));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
        switch (uniqueType){
            case "RESURRECTION_AMULET":
                this.type = CollectableType.RESURRECTION_AMULET;
                collectableBuffs = new CollectableBuffs(0, 0, 0, 0, true, false, false);
                break;
            case "VAMPIRE_AMULET":
                this.type = CollectableType.VAMPIRE_AMULET;
                collectableBuffs = new CollectableBuffs(0, 0, 0, 0, false, true, false);
                break;
            case "SPEED_BOOTS":
                this.type = CollectableType.SPEED_BOOTS;
                collectableBuffs = new CollectableBuffs(0, 0, 0, 0, false, false, true);
                break;
        }
    }

    /** Json Construcor */
    public Collectable fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Collectable.class);

    }


    @Override
    public void render(float delta) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        TextureRegion currentFrame;
        elapsedTime+=delta;
        if (!pickedUp){
            currentFrame = idleAnimation.getKeyFrame(elapsedTime, true);
            batch.draw(currentFrame, getSpriteDrawPosition().x(), getSpriteDrawPosition().y(),
                    getSpriteDrawWidth(), getSpriteDrawHeight());
        }
        // TODO after creating TextureAtlas for pickup animation delete else and uncomment code below (not tested yet)
        else{
            //TODO: despawn collectable after picking it up
            dispose();
        }
//        else {
//            currentFrame = pickupAnimation.getKeyFrame(pickupElapsedTime);
//            pickupElapsedTime += delta;
//            if(pickupAnimation.isAnimationFinished(pickupElapsedTime)){
//                dispose();
//            }
//        }
    }

    public void dispose() {
    }

    @Override
    public String toString() {
        return "Picked up";
    }
    /** Method which executes on Collectable pickup */
    @Override
    public void onPlayerStartContact(Contact c) {
        super.onPlayerStartContact(c);
        if (!pickedUp){
            toDestroy = true;
            pickedUp = true;
            pickupElapsedTime = 0f;
            ///  Collectable pickup logic is done in Player class
            LevelScreen.getInstance().player.collect(this);
        }

    }

    public void setBatch(SpriteBatch batch) {
        this.batch = batch;
    }


    public SpriteBatch getBatch() {
        return batch;
    }

    public CollectableType getType() {
        return type;
    }
    public int getNonUniqueAttribute(CollectableType type){
        return switch (type) {
            case HEART -> collectableBuffs.health;
            case GOLD_COIN -> collectableBuffs.gold;
            case DAMAGE_COIN -> collectableBuffs.damage;
            case DEFENSE_COIN -> collectableBuffs.resist;
            default -> 0;
        };

    }
    public boolean getUniqueAttribute(CollectableType type){
        return switch (type) {
            case RESURRECTION_AMULET -> collectableBuffs.resurrection;
            case VAMPIRE_AMULET -> collectableBuffs.vampireBuff;
            case SPEED_BOOTS -> collectableBuffs.speedBuff;
            default -> false;
        };
    }
}
