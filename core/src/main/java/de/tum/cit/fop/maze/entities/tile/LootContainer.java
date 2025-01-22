package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.google.gson.Gson;
import de.tum.cit.fop.maze.BodyBits;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.entities.Attackable;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.essentials.Utils;

import java.util.Arrays;
import java.util.Iterator;

import static de.tum.cit.fop.maze.Globals.*;

public class LootContainer extends TileEntity implements Attackable {
    public final transient LootContainerAttributes attributes;
    public final LootContainerType type;
    private boolean destroyed = false;
    private boolean collisionDisabled = false;
    private transient float destroyedTime = 0;
    private transient float idleTime = 0;
    private transient boolean hasBeenHit = false;
    private transient float timeSinceLastHit = 0;
    private transient Body lightBlockingBody;


    public enum LootContainerType {
        BARREL,
        CRATE, VASE,
    }

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> destroyedAnimation;

    public final class LootContainerAttributes {
        public int health;
        public final int lootAmount;
        public final float frameDuration;
        public final String textureName;
        public final LootContainerType type;

        public LootContainerAttributes(
                int lootAmount, float frameDuration, String textureName, LootContainerType type
        ) {
            this.lootAmount = lootAmount;
            this.frameDuration = frameDuration;
            this.textureName = textureName;
            this.type = type;
        }
    }

    public LootContainer(LootContainerType type) {
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

        Gson gson = new Gson();
        this.attributes = Arrays.stream(
                gson.fromJson(
                        Gdx.files.internal("configs/lootContainers.json").reader(),
                        LootContainerAttributes[].class
                )
        ).filter(attribute -> attribute.type.equals(type)).findFirst().get();
        this.type = type;
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

    }

    @Override
    public void takeDamage(int damage) {
        if (this.destroyed || hasBeenHit) {
            return;
        }
        hasBeenHit = true;
        timeSinceLastHit = 0;
        this.attributes.health -= damage;
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
    protected void spawn(float x, float y) {
        super.spawn(x, y + CELL_SIZE_METERS / 1.5f);
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
}
