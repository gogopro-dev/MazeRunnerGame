package de.tum.cit.fop.maze.entities.tile;

import box2dLight.Light;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.utils.Array;
import com.google.gson.*;
import de.tum.cit.fop.maze.Assets;
import de.tum.cit.fop.maze.BodyBits;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.essentials.DebugRenderer;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.level.LevelScreen;
import games.rednblack.miniaudio.MASound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;

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
        EMPTY, HEART, GOLD_COIN, DAMAGE_COIN, DEFENSE_COIN, RESURRECTION_AMULET, VAMPIRE_AMULET, SPEED_BOOTS,
        KEY, FIREBALL
    }


    private CollectableAttributes collectableAttributes;
    private transient Animation<TextureRegion> idleAnimation;
    public transient boolean pickedUp = false;
    public transient float elapsedTime;
    public transient float pickupElapsedTime;
    private transient final ArrayList<AbsolutePoint> dropPositions = new ArrayList<>();
    private transient float dropPathLength = 0f;
    private transient float dropElapsedTime = 0f;
    private transient PrismaticJoint joint;
    private transient boolean descriptionIsShown = false;
    private transient CircleShape circleShape;
    private transient FixtureDef circleFixtureDef = new FixtureDef();
    private transient Light light;
    private transient float lightAnimationElapsedTime = 10f;
    private transient MASound pickupSound;

    private boolean hasBeenDropped = true;

    /**
     * Constructor for testing purposes
     */

    private Collectable() {
        super(1, 1);
        circleShape = new CircleShape();
        circleShape.setRadius(Globals.COLLECTABLE_DESCRIPTION_RANGE * Globals.CELL_SIZE_METERS);
        circleFixtureDef.shape = circleShape;
        circleFixtureDef.isSensor = true;
        circleFixtureDef.filter.categoryBits = BodyBits.TILE_ENTITY;
        circleFixtureDef.filter.maskBits = BodyBits.TILE_ENTITY_MASK;
    }

    public Collectable(CollectableType type) {
        this();
        collectableAttributes = Assets.getInstance().getCollectables().stream().filter(
                collectableAttributes -> collectableAttributes.type == type).findFirst()
            .orElse(null);
        if (collectableAttributes == null) {
            throw new JsonSyntaxException("Collectable type not found in collectables.json");
        }
        init();
    }

    public void init() {
        TextureAtlas atlas = Assets.getInstance().getAssetManager().get(
            "assets/temporary/collectables/collectables.atlas", TextureAtlas.class
        );
        idleAnimation = new Animation<>(
            collectableAttributes.frameDuration, atlas.findRegions(collectableAttributes.textureName)
        );
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
        if (this.collectableAttributes.pickupSound != null) {
            pickupSound = Assets.getInstance().getSound(this.collectableAttributes.pickupSound);
            pickupSound.setSpatialization(false);
        }
    }

    public Collectable(CollectableAttributes attributes) {
        this();
        collectableAttributes = attributes;
        init();
    }

    public Collectable(CollectableAttributes attributes, boolean drop) {
        this(attributes);
        this.hasBeenDropped = !drop;
    }


    public Collectable(CollectableType type, boolean drop) {
        this(type);
        this.hasBeenDropped = !drop;
    }

    @Override
    public void render(float delta) {
        AbsolutePoint position = this.getSpriteDrawPosition();
        this.render(delta, position.x(), position.y());
        ///  If at any point the pickup animation would be introduced, move the toDestroy assignment
    }

    public void render(float delta, float x, float y) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        TextureRegion currentFrame;
        elapsedTime += delta;
        if (!pickedUp) {
            currentFrame = idleAnimation.getKeyFrame(elapsedTime, true);
            batch.draw(currentFrame, x, y,
                getSpriteDrawWidth(), getSpriteDrawHeight());
        }
        if (this.dropPositions.isEmpty() && !hasBeenDropped) {
            hasBeenDropped = true;
            updateDropPositions();
        }
        if (this.collectableAttributes.emitsLight && this.isSpawned()) {
            lightAnimationElapsedTime += delta;
            if (this.isOnPlayer) {
                this.light.setDistance(
                    Utils.easeOutCirc(lightAnimationElapsedTime) * Globals.TORCH_LIGHT_RADIUS
                );
            } else {
                this.light.setDistance(
                    Globals.TORCH_LIGHT_RADIUS -
                        Utils.easeOutCirc(lightAnimationElapsedTime) * Globals.TORCH_LIGHT_RADIUS
                );
            }
        }
        animateDrop(delta);
        ///  If at any point the pickup animation would be introduced, move the toDestroy assignment
    }

    @Override
    public String toString() {
        return "Collectable{" +
            "collectableAttributes=" + collectableAttributes +
            ", idleAnimation=" + idleAnimation +
            ", pickedUp=" + pickedUp +
            ", hasBeenDropped=" + hasBeenDropped +
            "} " + super.toString();
    }

    /**
     * Method which executes on Collectable pickup
     */

    @Override
    public void contactTick(float delta) {
        if (!pickedUp && this.getPosition().distance(LevelScreen.getInstance().player.getPosition()) <
            Globals.COLLECTABLE_PICKUP_RADIUS && this.dropPositions.isEmpty()) {
            if (pickupSound != null) {
                this.pickupSound.stop();
                this.pickupSound.setLooping(false);
                this.pickupSound.play();
            }
            toDestroy = true;
            pickedUp = true;
            pickupElapsedTime = 0f;
            ///  Collectable pickup logic is hasBeenDropped in Player class
            LevelScreen.getInstance().player.collect(this);
        }
        if (!this.collectableAttributes.isConsumable && !descriptionIsShown && Utils.isPlayerExposed(getPosition())) {
            descriptionIsShown = true;
            LevelScreen.getInstance().hud.setItemDescription(
                this.collectableAttributes.toPrettyDescription()
            );
        }
    }

    @Override
    public void onPlayerEndContact(Contact c) {
        descriptionIsShown = false;
        if (!this.collectableAttributes.isConsumable &&
            Objects.equals(LevelScreen.getInstance().hud.getItemDescription(),
                this.collectableAttributes.toPrettyDescription())) {
            lightAnimationElapsedTime = 0f;
            LevelScreen.getInstance().hud.deleteDescription();
        }
    }

    @Override
    public void onPlayerStartContact(Contact c) {
        lightAnimationElapsedTime = 0f;
    }


    public void setBatch(SpriteBatch batch) {
        this.batch = batch;
    }


    public SpriteBatch getBatch() {
        return batch;
    }

    public CollectableType getType() {
        return collectableAttributes.type;
    }

    private void updateDropPositions() {
        Random random = LevelScreen.getInstance().map.random;
        float x = body.getPosition().x;
        float y = body.getPosition().y;

        float x_end = x + random.nextFloat() * 3 - 1.5f;
        float y_end = y + random.nextFloat() * 1.5f - 0.85f;

        float x_control = (x_end - x) / 2 + x;
        float y_control = y_end + 1.5f;
        Bezier<Vector2> bezier = new Bezier<>();
        bezier.set(new Vector2(x, y), new Vector2(x_control, y_control), new Vector2(x_end, y_end));
        AbsolutePoint prev = new AbsolutePoint(x, y);
        for (int i = 0; i < 15; i++) {
            float t = i / 15f;
            Vector2 point = new Vector2();
            bezier.valueAt(point, t);
            AbsolutePoint absolutePoint = new AbsolutePoint(point);
            dropPositions.add(absolutePoint);
            dropPathLength += prev.distance(absolutePoint);
            prev = absolutePoint;
        }

    }

    @Override
    public void spawn(float x, float y) {
        super.spawn(x, y);
        if (!this.collectableAttributes.isConsumable) {
            body.createFixture(circleFixtureDef);
            circleShape.dispose();
        }
        if (this.collectableAttributes.emitsLight) {
            this.light = new PointLight(
                LevelScreen.getInstance().rayHandler,
                Globals.RAY_AMOUNT
            );
            this.light.attachToBody(body);
            this.light.setSoft(true);
            this.light.setStaticLight(false);
            Filter lightFilter = new Filter();
            lightFilter.categoryBits = BodyBits.LIGHT;
            lightFilter.maskBits = BodyBits.LIGHT_MASK;
            this.light.setContactFilter(lightFilter);
            this.light.setColor(Globals.TORCH_LIGHT_COLOR);
            // Active does require extra ops, but it makes the code way easier
            this.light.setActive(true);
            this.light.setDistance(0f);
        }
        /// Must be last
        if (hasBeenDropped) {
            return;
        }
        Filter filter = new Filter();
        filter.categoryBits = BodyBits.ENTITY;
        filter.maskBits = BodyBits.ENTITY_MASK;
        Fixture fixture = body.getFixtureList().get(0);
        fixture.setSensor(false);
        fixture.setFilterData(filter);
    }

    public void setHasBeenDropped(boolean hasBeenDropped) {
        this.hasBeenDropped = hasBeenDropped;
    }

    private Body createTempBody(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        return body.getWorld().createBody(bodyDef);

    }

    public void animateDrop(float delta) {
        if (dropPositions.isEmpty()) {
            return;
        }
        AbsolutePoint prev = this.getPosition();
        for (AbsolutePoint point : dropPositions) {
            DebugRenderer.getInstance().drawLine(prev, point, Color.PURPLE);
            prev = point;
        }
        dropElapsedTime += delta;
        if (joint == null) {
            PrismaticJointDef jointDef = new PrismaticJointDef();
            jointDef.bodyA = body;
            jointDef.bodyB = createTempBody(dropPositions.get(0).x(), dropPositions.get(0).y());
            jointDef.collideConnected = true;
            jointDef.enableMotor = true;
            jointDef.referenceAngle = 0;
            jointDef.motorSpeed = 3f * (getPosition().onTheLeftFrom(dropPositions.get(dropPositions.size() - 1)) ? -1 : 1);
            jointDef.maxMotorForce = 1000f;
            jointDef.enableLimit = false;
            joint = (PrismaticJoint) body.getWorld().createJoint(jointDef);
        }
        if (dropElapsedTime > dropPathLength / 60f) {
            dropElapsedTime = 0f;
            dropPositions.remove(0);
            if (dropPositions.isEmpty()) {
                Body b = joint.getBodyB();
                body.getWorld().destroyJoint(joint);
                body.getWorld().destroyBody(b);
                body.setLinearVelocity(0, 0);
                Filter filter = new Filter();
                filter.categoryBits = BodyBits.TILE_ENTITY;
                filter.maskBits = BodyBits.TILE_ENTITY_MASK;
                Iterator<Fixture> iter = new Array.ArrayIterator<>(body.getFixtureList());
                while (iter.hasNext()) {
                    var fixture = iter.next();
                    fixture.setFilterData(filter);
                    fixture.setSensor(true);
                }
                joint = null;
                return;
            }
            joint.getBodyB().setTransform(dropPositions.get(0).x(), dropPositions.get(0).y(), 0);
        }
    }

    public Animation<TextureRegion> getIdleAnimation() {
        return idleAnimation;
    }


    @Override
    public void dispose() {
        super.dispose();
        if (this.light != null) this.light.remove();
    }

    public CollectableAttributes getCollectableAttributes() {
        return collectableAttributes;
    }


}
