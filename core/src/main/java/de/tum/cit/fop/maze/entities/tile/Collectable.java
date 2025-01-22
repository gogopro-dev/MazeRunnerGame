package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.World;
import com.google.gson.*;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.Arrays;

import static com.badlogic.gdx.math.MathUtils.random;

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
        EMPTY, HEART, GOLD_COIN, DAMAGE_COIN, DEFENSE_COIN, RESURRECTION_AMULET, VAMPIRE_AMULET, SPEED_BOOTS
    }

    public final CollectableAttributes collAttributes;
    public Animation<TextureRegion> idleAnimation;
    public Animation<TextureRegion> pickupAnimation;
    public boolean pickedUp;
    public float affectedStat;
    public int id;
    public String textureName;
    public String tempTextureName = "key";
    public SpriteBatch batch;
    public float elapsedTime;
    public float pickupElapsedTime;

    /**
     * Constructor for testing purposes
     */

    public Collectable(CollectableType type) {
        super(1, 1);
        Gson gson = new Gson();
        collAttributes = Arrays.stream(
            gson.fromJson(Gdx.files.internal("anim/tileEntities/collectables.json").reader(),
                CollectableAttributes[].class)
        ).filter(attribute -> attribute.type.equals(type)).findFirst().get();


        batch = LevelScreen.getInstance().batch;
        TextureAtlas atlas = new TextureAtlas(Gdx.files.local("temporary/collectables/collectables.atlas"));
        idleAnimation = new Animation<>(collAttributes.frameDuration, atlas.findRegions(collAttributes.textureName));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }


    @Override
    public void render(float delta) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        TextureRegion currentFrame;
        elapsedTime += delta;
        if (!pickedUp) {
            currentFrame = idleAnimation.getKeyFrame(elapsedTime, true);
            batch.draw(currentFrame, getSpriteDrawPosition().x(), getSpriteDrawPosition().y(),
                getSpriteDrawWidth(), getSpriteDrawHeight());
        }
        ///  If at any point the pickup animation would be introduced, move the toDestroy assignment
    }

    @Override
    public String toString() {
        return "Picked up";
    }

    /**
     * Method which executes on Collectable pickup
     */
    @Override
    public void onPlayerStartContact(Contact c) {
        super.onPlayerStartContact(c);
        if (!pickedUp) {
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
        return collAttributes.type;
    }

    public float getAffectedStat() {
        return affectedStat;
    }

}
