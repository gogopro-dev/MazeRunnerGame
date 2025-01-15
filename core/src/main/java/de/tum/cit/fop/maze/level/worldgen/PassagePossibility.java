package de.tum.cit.fop.maze.level.worldgen;

/**
 * <p>Represents a possible passage between two disjoint components in the maze.</p>
 * @param a first cell
 * @param wall wall we can break to get to the other cell
 * @param b second cell
 */
public record PassagePossibility(GeneratorCell a, GeneratorCell wall, GeneratorCell b) {
    public PassagePossibility {
        if (a.i == b.i && a.j == b.j) {
            throw new IllegalArgumentException("a and b cannot be the same cell");
        }
        if (a.i != wall.i && a.j != wall.j) {
            throw new IllegalArgumentException("wall must be in the same row or column as a");
        }
        if (b.i != wall.i && b.j != wall.j) {
            throw new IllegalArgumentException("wall must be in the same row or column as b");
        }
    }
}
