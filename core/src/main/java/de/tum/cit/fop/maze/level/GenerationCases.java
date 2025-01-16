package de.tum.cit.fop.maze.level;

import de.tum.cit.fop.maze.level.worldgen.MazeGenerator;

/**
 * <p>Contains the different cases to process the generation of the maze, to distinguish different specifics</p>
 */
public final class GenerationCases {

    /**
     * Checks if the current cell is located on the edge of the maze
     * @param i row index
     * @param j column index
     * @param generator the maze generator
     * @return {@code true} if the current cell is on the edge of the maze
     */
    public static boolean isEdge(int i, int j, MazeGenerator generator) {
        return i <= 0 || i >= generator.height - 1 || j <= 0 || j >= generator.width - 1;
    }

    /**
     * Checks if the current cell is a top of a vertical wall
     * @param i row index
     * @param j column index
     * @param generator the maze generator
     * @return {@code true} if the current cell is a top of a vertical wall
     */
    public static boolean topVerticalCase(int i, int j, MazeGenerator generator) {
        if (isEdge(i, j, generator)) {
            return false;
        }
        return
            generator.grid.get(i).get(j).getCellType().isWall() &&
            generator.grid.get(i - 1).get(j).getCellType().isWalkable() &&
            generator.grid.get(i + 1).get(j).getCellType().isWall();

    }


    /** Checks if the current cell is a single wall surrounded by walkable tiles
     * @param i row index
     * @param j column index
     * @param generator the maze generator
     * @return {@code true} if the single wall case is satisfied
     */
    public static boolean singleWallCase(int i, int j, MazeGenerator generator) {
        if (isEdge(i, j, generator)) {
            return false;
        }
        return
            generator.grid.get(i).get(j).getCellType().isWall() &&
                generator.grid.get(i - 1).get(j).getCellType().isWalkable() &&
                generator.grid.get(i + 1).get(j).getCellType().isWalkable() &&
                generator.grid.get(i).get(j + 1).getCellType().isWalkable() &&
                generator.grid.get(i).get(j - 1).getCellType().isWalkable();
    }




    /**
     * Checks if the current cell is a vertical wall, i.e. the cell is a wall and the cell above and below are walls
     * @param i row index
     * @param j column index
     * @param generator the maze generator
     * @return {@code true} if the current cell is a vertical wall
     */
    public static boolean verticalWallCase(int i, int j, MazeGenerator generator) {
        /// Check if there is a wall above or wall on top and border above
        if (i == generator.height - 1 &&
            generator.grid.get(i).get(j).getCellType().isWall() &&
            generator.grid.get(i - 1).get(j).getCellType().isWall()) {
            return true;
        }
        if (i == generator.height - 1) {
            return false;
        }

        /// Check if there is a wall below or wall on top and border below
        if (i == 0 &&
            generator.grid.get(i).get(j).getCellType().isWall() &&
            generator.grid.get(i + 1).get(j).getCellType().isWall()) {
            return true;
        }
        if (i == 0) {
            return false;
        }
        ///  Check if there is a wall below and above
        return generator.grid.get(i).get(j).getCellType().isWall() &&
            generator.grid.get(i + 1).get(j).getCellType().isWall() &&
            generator.grid.get(i - 1).get(j).getCellType().isWall();
    }

    /**
     * Checks if the current cell has any surrounding walls
     *
     * @param i         row index
     * @param j         column index
     * @param generator the maze generator
     * @return {@code true} if the current cell has any surrounding walls
     */
    public static boolean hasAnySurroundingWall(int i, int j, MazeGenerator generator) {
        if (isEdge(i, j, generator)) {
            return false;
        }
        return
            generator.grid.get(i - 1).get(j).getCellType().isWall() ||
                generator.grid.get(i + 1).get(j).getCellType().isWall() ||
                generator.grid.get(i).get(j + 1).getCellType().isWall() ||
                generator.grid.get(i).get(j - 1).getCellType().isWall();
    }
}
