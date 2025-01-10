package de.tum.cit.fop.maze.Entity;

/**
 * Represents an entity in the game world.
 */
public abstract class Entity {
    protected int health;
    protected int maxHealth;
    protected int stamina;

    /**
     * Creates a new entity with default values.
     */
    public Entity() {
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

}
