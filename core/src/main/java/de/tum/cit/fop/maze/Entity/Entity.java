package de.tum.cit.fop.maze.Entity;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.fop.maze.BodyBits;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.essentials.BoundingRectangle;
import de.tum.cit.fop.maze.level.LevelScreen;
import org.jetbrains.annotations.NotNull;

import static de.tum.cit.fop.maze.Globals.PPM;

/**
 * Represents an entity in the game world.
 */


public abstract class Entity {
    protected String box2dUserData = null;
    protected int health;
    protected int maxHealth;
    protected int stamina;
    protected final float scale = 4 * Globals.MPP;
    /// Entity movement speed (in chicken per second)
    protected float entitySpeed = 7f;
    protected Body body;
    protected BodyDef.BodyType bodyType = BodyDef.BodyType.DynamicBody;
    protected final SpriteBatch batch;
    protected final OrthographicCamera camera;
    protected float mass = 0f;
    public final BoundingRectangle boundingRectangle =
        new BoundingRectangle(0.4f * PPM * scale, 0.24f * scale * PPM);



    /**
     * Creates a new entity with default values.
     */
    public Entity(SpriteBatch batch) {
        this.batch = batch;
        this.camera = LevelScreen.getInstance().camera;
        //TODO: Change default values
        this.health = 100;
        this.maxHealth = 100;
        this.stamina = 100;
    }

    /**
     * Returns the current health of the entity.
     */
    public boolean isDead() {
        return health <= 0;
    }

    /**
     * Returns the current health of the entity.
     * @param damage The amount of damage to take.
     */
    public void takeDamage(int damage) {
        health -= damage;
    }

    /**
     * Returns the current health of the entity.
     * @param amount The amount to heal.
     */
    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    /**
     * Returns the current health of the entity.
     * @param amount The amount of stamina to use.
     */
    public void useStamina(int amount) {
        stamina -= amount;
    }

    /**
     * Returns the current health of the entity.
     * @param amount The amount of stamina to restore.
     */
    public void restoreStamina(int amount) {
        stamina += amount;
    }

    public float getSpriteX() {
        return body.getPosition().x - 18 * scale;
    }

    public float getSpriteY() {
        return body.getPosition().y - 15.5f * scale;
    }

    public @NotNull Body getBody() {
        if (body == null) {
            throw new IllegalStateException("Entity not spawned");
        }
        return body;
    }

    public void spawn(float x, float y, World world) {
        if (body != null) {
            throw new IllegalStateException("Entity already spawned");
        }
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(boundingRectangle.width() / 2, boundingRectangle.height() / 2);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = mass;
        fixtureDef.friction = 0.3f;
        fixtureDef.filter.categoryBits = BodyBits.ENTITY;
        fixtureDef.filter.maskBits = BodyBits.ENTITY_MASK;
        body = world.createBody(bodyDef);
        body.createFixture(fixtureDef);
        body.setUserData(box2dUserData);
        shape.dispose();


        shape = new PolygonShape();
        shape.setAsBox(boundingRectangle.width() / 8f, boundingRectangle.height() / 8f);
        fixtureDef = new FixtureDef();
        fixtureDef.filter.categoryBits = BodyBits.ENEMY;
        fixtureDef.filter.maskBits = BodyBits.ENEMY_MASK;
        fixtureDef.shape = shape;
        fixtureDef.restitution = 0f;
        fixtureDef.density = mass;
        fixtureDef.friction = 0f;
        this.body.createFixture(fixtureDef);
        shape.dispose();
    }

    public void dispose() {
        body.getWorld().destroyBody(body);
    }

    public AbsolutePoint getPosition() {
        return new AbsolutePoint(body.getPosition().x, body.getPosition().y);
    }
}
