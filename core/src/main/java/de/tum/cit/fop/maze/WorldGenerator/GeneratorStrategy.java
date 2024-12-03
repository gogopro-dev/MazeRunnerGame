package de.tum.cit.fop.maze.WorldGenerator;

/**
 * <p>Enum for the different strategies for generating rooms.</p>
 * <ol>
 *     <li>{@code CENTER} - Generates a room in the very center of the maze.</li>
 *     <li>{@code SIDES} - Generates a room in a random location.</li>
 *     <li>{@code CORNERS} - Places BFS points in the corners</li>
 *     <li>{@code RANDOM} - Places BFS points on the sides</li>
 *     <li>{@code AROUND_CENTER} - Places BFS points around the center randomly</li>
 * </ol>
 */
public enum GeneratorStrategy {
    CENTER,
    SIDES,
    CORNERS,
    RANDOM,
    AROUND_CENTER,
}
