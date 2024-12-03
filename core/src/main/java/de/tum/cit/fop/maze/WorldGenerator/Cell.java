package de.tum.cit.fop.maze.WorldGenerator;

/**
 * Temporary class to represent a cell in the maze during the building of uniform spanning tree
 */
public record Cell(int i, int j) {
    @Override
    public boolean equals(final Object O) {
        if (!(O instanceof Cell)) return false;
        if (((Cell) O).i != i) return false;
        return ((Cell) O).j == j;
    }

    @Override
    public int hashCode() {
        return i * 31 + j;
    }
}
