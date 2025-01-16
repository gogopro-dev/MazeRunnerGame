package de.tum.cit.fop.maze.essentials;

import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.Globals;

/**
 * Represents a point in the world with absolute coordinates with some handy utility methods.
 *
 * @param x The x coordinate.
 * @param y The y coordinate.
 */
public record AbsolutePoint(float x, float y) {

    public float distance(AbsolutePoint other) {
        return (float) Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }

    public float angle(AbsolutePoint other) {
        return (float) Math.atan2(other.y - y, other.x - x);
    }

    public boolean onTheRightFrom(AbsolutePoint other) {
        return x > other.x;
    }

    public boolean onTheLeftFrom(AbsolutePoint other) {
        return x < other.x;
    }

    public boolean above(AbsolutePoint other) {
        return y < other.y;
    }

    public boolean below(AbsolutePoint other) {
        return y > other.y;
    }

    public Vector2 toVector2() {
        return new Vector2(x, y);
    }


    public AbsolutePoint toMetersFromCells() {
        return new AbsolutePoint(x * Globals.CELL_SIZE_METERS, y * Globals.CELL_SIZE_METERS);
    }

    public AbsolutePoint addX(float x) {
        return new AbsolutePoint(this.x + x, this.y);
    }

    public AbsolutePoint addY(float y) {
        return new AbsolutePoint(this.x, this.y + y);
    }


    // Locale does not matter for numbers
    @SuppressWarnings("DefaultLocale")
    @Override
    public String toString() {
        return String.format("(%f, %f)", x, y);
    }
}
