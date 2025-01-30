package de.tum.cit.fop.maze.entities.tile;

import box2dLight.Light;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.utils.Array;
import com.google.gson.JsonSyntaxException;
import de.tum.cit.fop.maze.essentials.*;
import de.tum.cit.fop.maze.level.LevelScreen;
import games.rednblack.miniaudio.MASound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;

/**
 * Represents a collectable item in the game that can be picked up by the player. I
 * The item can have various types and attributes defining its behavior, visual
 * representation, and interaction with the player.
 */
public class Collectable extends TileEntity {

    private transient final ArrayList<AbsolutePoint> dropPositions = new ArrayList<>();
    public transient boolean pickedUp = false;
    public transient float elapsedTime;
    public transient float pickupElapsedTime;
    private CollectableAttributes collectableAttributes;
    private transient Animation<TextureRegion> idleAnimation;
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

    /**
     * Constructor for Collectable
     * @param type CollectableType
     */
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

    /**
     * Constructor for Collectable
     * @param attributes CollectableAttributes
     */
    public Collectable(CollectableAttributes attributes) {
        this();
        collectableAttributes = attributes;
        init();
    }

    /**
     * Constructor for Collectable
     * @param attributes CollectableAttributes
     * @param drop boolean
     */
    public Collectable(CollectableAttributes attributes, boolean drop) {
        this(attributes);
        this.hasBeenDropped = !drop;
    }
    /**
     * Constructor for Collectable
     * @param type CollectableType
     * @param drop boolean
     */
    public Collectable(CollectableType type, boolean drop) {
        this(type);
        this.hasBeenDropped = !drop;
    }

    /**
     * Initialize the collectable
     * Load the texture atlas and set the animation
     * Load the pickup sound
     */
    public void init() {
        TextureAtlas atlas = Assets.getInstance().getAssetManager().get(
            "assets/collectables/collectables.atlas", TextureAtlas.class
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

    @Override
    public void render(float delta) {
        AbsolutePoint position = this.getSpriteDrawPosition();
        this.render(delta, position.x(), position.y());
        ///  If at any point the pickup animation would be introduced, move the toDestroy assignment
    }

    /**
     * Render the collectable
     * @param delta float
     * @param x float
     * @param y float
     */
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
            generateCollectableDropPath();
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
                this.collectableAttributes.toItemDescription()
            );
        }
    }

    @Override
    public void onPlayerEndContact(Contact c) {
        descriptionIsShown = false;
        if (!this.collectableAttributes.isConsumable &&
            Objects.equals(LevelScreen.getInstance().hud.getItemDescription(),
                this.collectableAttributes.toItemDescription())) {
            lightAnimationElapsedTime = 0f;
            LevelScreen.getInstance().hud.deleteDescription();
        }
    }

    @Override
    public void onPlayerStartContact(Contact c) {
        lightAnimationElapsedTime = 0f;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public void setBatch(SpriteBatch batch) {
        this.batch = batch;
    }

    public CollectableType getType() {
        return collectableAttributes.type;
    }

    /**
     * Generate the collectable drop path using Bézier curves
     * Randomly generate the drop positions
     * and curve which the collectable will follow
     */
    private void generateCollectableDropPath() {
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

    /**
     * Create a temporary body for the collectable
     * @param x float
     * @param y float
     * @return Body
     */
    private Body createTempBody(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        return body.getWorld().createBody(bodyDef);

    }

    /**
     * Animate the collectable drop
     * @param delta float
     */
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

    /// Enum for Collectable types
    public enum CollectableType {
        EMPTY, HEART, GOLD_COIN, DAMAGE_COIN, DEFENSE_COIN, RESURRECTION_AMULET, VAMPIRE_AMULET, SPEED_BOOTS,
        KEY, FIREBALL
    }


}
