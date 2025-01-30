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

    public AbsolutePoint(Vector2 point) {
        this(point.x, point.y);
    }

    public float distance(AbsolutePoint other) {
        return (float) Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }

    public float angle(AbsolutePoint other) {
        double theta = Math.atan2(other.y - y, other.x - x);
        float angle = (float) Math.toDegrees(theta);
        angle -= 90;
        if (angle < 0) angle += 360;
        return angle;
    }

    /**
     * Determines if this point is located to the right of another point.
     *
     * @param other The other point to compare against.
     * @return {@code true} if this point is located to the right of the other point, {@code false} otherwise.
     */
    public boolean onTheRightFrom(AbsolutePoint other) {
        return x > other.x;
    }


    /**
     * Determines if this point is located to the left of another point.
     *
     * @param other The other point to compare against.
     * @return {@code true} if this point is located to the left of the other point, {@code false} otherwise.
     */
    public boolean onTheLeftFrom(AbsolutePoint other) {
        return x < other.x;
    }

    /**
     * Determines if this point is located above another point.
     *
     * @param other The other point to compare against.
     * @return {@code true} if this point is located above the other point, {@code false} otherwise.
     */
    public boolean above(AbsolutePoint other) {
        return y < other.y;
    }

    /**
     * Determines if this point is located below another point.
     *
     * @param other The other point to compare against.
     * @return {@code true} if this point is located below the other point, {@code false} otherwise.
     */
    public boolean below(AbsolutePoint other) {
        return y > other.y;
    }

    /**
     * Converts the current AbsolutePoint instance to a Vector2 object.
     *
     * @return A Vector2 representation of this AbsolutePoint, using its x and y coordinates.
     */
    public Vector2 toVector2() {
        return new Vector2(x, y);
    }

    /**
     * Converts the current point, represented in cell units, to meters.
     * The conversion is performed using the global constant for cell size in meters.
     *
     * @return A new AbsolutePoint instance where the x and y coordinates are scaled
     * from cell units to meters using Globals.CELL_SIZE_METERS.
     */
    public AbsolutePoint toMetersFromCells() {
        return new AbsolutePoint(x * Globals.CELL_SIZE_METERS, y * Globals.CELL_SIZE_METERS);
    }

    /**
     * Adds a specified value to the x-coordinate of this AbsolutePoint and returns a new AbsolutePoint instance
     * with the updated x-coordinate, while keeping the y-coordinate unchanged.
     *
     * @param x The value to be added to the x-coordinate of this AbsolutePoint.
     * @return A new AbsolutePoint instance with the updated x-coordinate.
     */
    public AbsolutePoint addX(float x) {
        return new AbsolutePoint(this.x + x, this.y);
    }

    /**
     * Adds a specified value to the y-coordinate of this AbsolutePoint and returns
     * a new AbsolutePoint instance with the updated y-coordinate, while keeping the
     * x-coordinate unchanged.
     *
     * @param y The value to be added to the y-coordinate of this AbsolutePoint.
     * @return A new AbsolutePoint instance with the updated y-coordinate.
     */
    public AbsolutePoint addY(float y) {
        return new AbsolutePoint(this.x, this.y + y);
    }

    /**
     * Returns the orientation of this point relative to another point.
     *
     * @param other The other point.
     * @return The orientation of this point relative to the other point.
     * @throws IllegalArgumentException If the points are not aligned.
     */
    public Direction orientationTo(AbsolutePoint other) {
        if (this.x == other.x) {
            if (this.y < other.y) {
                return Direction.UP;
            } else {
                return Direction.DOWN;
            }
        } else if (this.y == other.y) {
            if (this.x < other.x) {
                return Direction.RIGHT;
            } else {
                return Direction.LEFT;
            }
        } else {
            throw new IllegalArgumentException("Points are not aligned");
        }
    }


    /// Locale does not matter for numbers
    @SuppressWarnings("DefaultLocale")
    @Override
    public String toString() {
        return String.format("(%f, %f)", x, y);
    }
}
