package de.tum.cit.fop.maze.entities;

/**
 * Represents an object that can be attacked and take damage within the game.
 * Classes implementing this interface must define the behavior for taking damage.
 */
public interface Attackable {
    /// Take the {@code damage} amount of damage
    void takeDamage(int damage);
}
