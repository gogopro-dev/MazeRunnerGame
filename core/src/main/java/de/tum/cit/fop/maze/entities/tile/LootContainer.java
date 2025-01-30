package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.entities.Attackable;
import de.tum.cit.fop.maze.essentials.*;
import de.tum.cit.fop.maze.level.LevelScreen;
import games.rednblack.miniaudio.MASound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static de.tum.cit.fop.maze.essentials.Globals.CELL_SIZE_METERS;
import static de.tum.cit.fop.maze.essentials.Globals.IMMUNITY_FRAME_DURATION;

/**
 * Represents a loot container entity within the game, which can be attacked, destroyed,
 * and drops collectible loot upon destruction.
 * A loot container can be of a specific type (e.g., barrel, crate, vase) and has attributes
 * such as health, animations, and sounds associated with it.
 * Implements the {@link Attackable} interface.
 */
public class LootContainer extends TileEntity implements Attackable {
    private final ArrayList<Collectable> loot = new ArrayList<>();
    private LootContainerAttributes attributes;
    private boolean destroyed = false;
    private float destroyedTime = 0;
    private float idleTime = 0;
    private boolean hasBeenHit = false;
    private float timeSinceLastHit = 0;
    private transient Body lightBlockingBody;
    private transient MASound hitSound;
    private transient MASound destroySound;
    private transient boolean collisionDisabled = false;
    private transient Animation<TextureRegion> idleAnimation;
    private transient Animation<TextureRegion> destroyedAnimation;

    /**
     * This constructor will ONLY be used by GSON
     */
    private LootContainer() {
        super(1, 2, new BodyDef(), new FixtureDef());
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.fixedRotation = true;
        bodyDef.allowSleep = false;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(CELL_SIZE_METERS / 2, CELL_SIZE_METERS / 8f);
        fixtureDef.shape = shape;
        fixtureDef.isSensor = false;

        fixtureDef.filter.categoryBits = BodyBits.WALL;
        fixtureDef.filter.maskBits = BodyBits.WALL_MASK;
    }

    /**
     * Constructs a new LootContainer with the specified type. Initializes the container by
     * determining its attributes, setting up its loot pool, and randomly populating it with
     * items based on drop chances and maximum loot constraints defined in its attributes.
     *
     * @param type The type of the LootContainer (e.g., BARREL, CRATE, VASE) which determines its
     *             attributes and behavior.
     */
    public LootContainer(LootContainerType type) {
        this();
        this.attributes = Assets.getInstance().getLootContainerConfig().stream()
            .filter(attribute -> attribute.type.equals(type)).findFirst().orElseThrow();
        init();
        int passes = 0;
        Random random = LevelScreen.getInstance().getRandom();
        while (passes < this.attributes.maxLootAmount && loot.size() < this.attributes.maxLootAmount) {
            for (CollectableAttributes collectableAttributes : Assets.getInstance().getLootContainerPool()) {
                if (random.nextFloat() <= collectableAttributes.dropChance) {
                    loot.add(new Collectable(collectableAttributes, true));
                    ++passes;
                    break;
                }

            }
            ++passes;
        }

    }

    /**
     * Initializes the LootContainer by setting up animations, sounds, and loot items.
     */
    protected void init() {
        TextureAtlas atlas =
            new TextureAtlas(Gdx.files.internal("anim/tileEntities/tile_entities.atlas"));
        this.idleAnimation = new Animation<>(
            this.attributes.frameDuration,
            atlas.findRegions(this.attributes.textureName),
            Animation.PlayMode.LOOP
        );
        this.destroyedAnimation = new Animation<>(
            this.attributes.frameDuration,
            atlas.findRegions(this.attributes.textureName + "_destroyed"),
            Animation.PlayMode.NORMAL
        );
        for (Collectable collectable : loot) {
            collectable.init();
        }
        if (this.attributes.hitSound != null) {
            this.hitSound = Assets.getInstance().getSound(this.attributes.hitSound);
        }
        if (this.attributes.destroySound != null) {
            this.destroySound = Assets.getInstance().getSound(this.attributes.destroySound);
        }
    }

    @Override
    public void takeDamage(int damage) {
        if (this.destroyed || hasBeenHit) {
            return;
        }
        hasBeenHit = true;
        timeSinceLastHit = 0;
        this.attributes.health -= damage;
        if (this.hitSound != null) {
            this.hitSound.stop();
            this.hitSound.setPosition(this.getPosition().x(), this.getPosition().y(), 0);
            this.hitSound.play();
        }
        if (this.attributes.health <= 0) {
            destroyed = true;
        }
    }

    @Override
    AbsolutePoint getSpriteDrawPosition() {
        return new AbsolutePoint(
            body.getPosition().x - getSpriteDrawWidth() / 2,
            body.getPosition().y - CELL_SIZE_METERS * 1.8f
        );
    }

    @Override
    float getSpriteDrawHeight() {
        return 3 * Globals.CELL_SIZE_METERS;
    }

    @Override
    float getSpriteDrawWidth() {
        return 3 * Globals.CELL_SIZE_METERS;
    }

    @Override
    public void spawn(float x, float y) {
        super.spawn(x, y);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(CELL_SIZE_METERS / 2, CELL_SIZE_METERS / 1.5f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = false;
        fixtureDef.filter.categoryBits = BodyBits.BARRIER_NO_LIGHT;
        fixtureDef.filter.maskBits = BodyBits.BARRIER_NO_LIGHT_MASK;
        BodyDef def = new BodyDef();
        def.position.set(body.getPosition().sub(0, CELL_SIZE_METERS / 2));
        lightBlockingBody = body.getWorld().createBody(def);
        lightBlockingBody.createFixture(fixtureDef);
        lightBlockingBody.setUserData(this);

    }

    @Override
    public void render(float delta) {
        if (hasBeenHit) {
            timeSinceLastHit += delta;
            if (timeSinceLastHit > IMMUNITY_FRAME_DURATION) {
                hasBeenHit = false;
            }
            if (!destroyed) {
                float wrapScale = Utils.easeInOutQuart(timeSinceLastHit / IMMUNITY_FRAME_DURATION);
                batch.setColor(Utils.tintInterpolation(Color.TAN, wrapScale));
            }

        }
        if (destroyed) {
            if (!collisionDisabled && destroyedAnimation.isAnimationFinished(destroyedTime * 2f)) {
                Iterator<Fixture> fixtureIterator = new Array.ArrayIterator<>(body.getFixtureList());
                while (fixtureIterator.hasNext()) {
                    body.destroyFixture(fixtureIterator.next());
                }
                fixtureIterator = new Array.ArrayIterator<>(lightBlockingBody.getFixtureList());
                while (fixtureIterator.hasNext()) {
                    lightBlockingBody.destroyFixture(fixtureIterator.next());
                }
                collisionDisabled = true;
                Gdx.app.postRunnable(() -> {
                    for (Collectable collectable : loot) {
                        collectable.setHasBeenDropped(false);
                        LevelScreen.getInstance().tileEntityManager.createTileEntity(collectable, this.getPosition());
                    }
                    this.loot.clear();
                });
                if (this.attributes.destroySound != null) {
                    this.destroySound.stop();
                    this.destroySound.setPosition(this.getPosition().x(), this.getPosition().y(), 0);
                    this.destroySound.play();
                    LevelScreen.getInstance().getLevelData().addScore(50);
                }
            }
            destroyedTime += delta;
            batch.draw(
                destroyedAnimation.getKeyFrame(destroyedTime),
                getSpriteDrawPosition().x(),
                getSpriteDrawPosition().y(),
                getSpriteDrawWidth(),
                getSpriteDrawHeight()
            );
            return;
        }

        idleTime += delta;
        batch.draw(
            idleAnimation.getKeyFrame(idleTime),
            getSpriteDrawPosition().x(),
            getSpriteDrawPosition().y(),
            getSpriteDrawWidth(),
            getSpriteDrawHeight()
        );
        batch.setColor(Color.WHITE);

    }

    /**
     * Existing types of LootContainer
     */
    public enum LootContainerType {
        BARREL,
        CRATE, VASE,
    }

    /**
     * <p>Represents the attributes of a loot container {@link LootContainer} within the game.
     * These attributes determine the behavior, appearance, and sounds associated
     * with a loot container. </p>
     * <p>
     * The class includes data such as:
     * <ul>
     *     <li>The maximum loot amount the container can hold.</li>
     *     <li>The duration of each animation frame for displaying the loot container.</li>
     *     <li>The container's texture, sounds, and type.</li>
     *     <li>Additional attributes related to interaction like health.</li>
     * </ul>
     *
     * <p>Instances of this class are typically used for initialization and
     * functional configuration of {@link LootContainer} objects.</p>
     */
    public static class LootContainerAttributes {
        public final int maxLootAmount;
        public final float frameDuration;
        public final String textureName;
        public final LootContainerType type;
        public final String hitSound;
        public final String destroySound;
        public int health;

        public LootContainerAttributes(
            int maxLootAmount, float frameDuration, String textureName, LootContainerType type,
            String hitSound, String destroySound
        ) {
            this.maxLootAmount = maxLootAmount;
            this.frameDuration = frameDuration;
            this.textureName = textureName;
            this.type = type;
            this.hitSound = hitSound;
            this.destroySound = destroySound;
        }
    }
}
