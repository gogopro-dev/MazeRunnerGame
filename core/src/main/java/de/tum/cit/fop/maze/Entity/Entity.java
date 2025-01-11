package de.tum.cit.fop.maze.Entity;

import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.fop.maze.BodyBits;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.level.LevelScreen;

import static de.tum.cit.fop.maze.Globals.PPM;

/**
 * Represents an entity in the game world.
 */


public abstract class Entity {
    protected int health;
    protected int maxHealth;
    protected int stamina;
    protected final float scale = 4 * Globals.MPP;
    /// Entity movement speed (in chicken per second)
    protected float entitySpeed = 8f;
    protected Body body;


    /**
     * Creates a new entity with default values.
     */
    public Entity(float positionX, float positionY) {
        //TODO: Change default values
        this.health = 100;
        this.maxHealth = 100;
        this.stamina = 100;
        LevelScreen screen = LevelScreen.getInstance();
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = screen.world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.4f * PPM * scale / 4, 0.7f * scale * PPM / 4);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.3f;
        fixtureDef.filter.categoryBits = BodyBits.ENTITY;
        fixtureDef.filter.maskBits = BodyBits.ENTITY_MASK;
        body.createFixture(fixtureDef);
        body.setUserData(this);
        body.setFixedRotation(true);
        body.setTransform(positionX, positionY, 0);
        shape.dispose();
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
}
