package de.tum.cit.fop.maze.level.worldgen.rooms;

import de.tum.cit.fop.maze.level.worldgen.CellType;
import de.tum.cit.fop.maze.level.worldgen.GeneratorStrategy;
import de.tum.cit.fop.maze.level.worldgen.GeneratorCell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * <p>Represents a room in the maze.</p>
 * <p>Rooms are used to generate the maze. They are placed inside maze and filled with walls and paths.</p>
 */
public abstract class Room {
    ///  The height of the room
    public final int height;
    ///  The width of the room
    public final int width;

    /// The row index of the room
    protected int i = -1;
    /// The column index of the room
    protected int j = -1;

    /// The {@link GeneratorStrategy} to generate the room
    public final GeneratorStrategy generatorStrategy;

    /// The list of cells that are part of the room
    protected List<GeneratorCell> roomCells = null;

    protected Room(int height, int width, GeneratorStrategy generatorStrategy) {
        this.height = height;
        this.width = width;
        this.generatorStrategy = generatorStrategy;
    }

    public List<GeneratorCell> getRoomCells() {
        return roomCells;
    }

    /**
     * Sets the location of the room
     * @param i row index
     * @param j column index
     */
    public void setLocation(int i, int j) {
        this.i = i;
        this.j = j;
    }


    /**
     * Sets the list of cells that are part of the room
     * @param roomCells the list of cells that are part of the room
     */
    public void setRoomCells(List<GeneratorCell> roomCells) {
        this.roomCells = roomCells;
    }

    public void generateInsides() {

    }

    /**
     * Generates a door for the room
     * @param maze the maze
     * @param random the random instance to use
     */
    public void generateDoor(ArrayList<ArrayList<GeneratorCell>> maze, Random random) {
        if (i == -1 || j == -1) {
            throw new IllegalStateException("Room location is not set");
        }
        /// Door can't be generated at corners so we chose only walls and filter out the corner cells
        List<GeneratorCell> candidates = this.roomCells.stream().filter(
            cell -> (
                cell.getCellType() == CellType.ROOM_WALL && (
                    cell.getI() != i || cell.getJ() != j
                ) && (
                    cell.getI() != i + height - 1 || cell.getJ() != j
                ) && (
                    cell.getI() != i || cell.getJ() != j + width - 1
                ) && (
                    cell.getI() != i + height - 1 || cell.getJ() != j + width - 1
                )
            )
        ).collect(Collectors.toList());
        Collections.shuffle(candidates, random);
        for (GeneratorCell candidate : candidates) {
            int cellI = candidate.getI();
            int cellJ = candidate.getJ();
            if (cellI - 1 > 0 && maze.get(cellI - 1).get(cellJ).getCellType() == CellType.PATH) {
                updateCellType(maze, cellI, cellJ, CellType.DOOR);
                return;
            } else if (cellI + 1 < maze.size() && maze.get(cellI + 1).get(cellJ).getCellType() == CellType.PATH) {
                updateCellType(maze, cellI, cellJ, CellType.DOOR);
                return;
            } else if (cellJ - 1 > 0 && maze.get(cellI).get(cellJ - 1).getCellType() == CellType.PATH) {
                updateCellType(maze, cellI, cellJ, CellType.DOOR);
                return;
            } else if (cellJ + 1 < maze.get(cellI).size() && maze.get(cellI).get(cellJ + 1).getCellType() == CellType.PATH) {
                updateCellType(maze, cellI, cellJ, CellType.DOOR);
                return;
            }
        }

        /// Remove a wall next to the door as well if there is no other options to generate a reachable door
        for (GeneratorCell candidate : candidates) {
            int cellI = candidate.getI();
            int cellJ = candidate.getJ();
            if (cellI - 1 > 0 && maze.get(cellI - 1).get(cellJ).getCellType() == CellType.WALL) {
                updateCellType(maze, cellI, cellJ, CellType.DOOR);
                updateCellType(maze, cellI - 1, cellJ, CellType.PATH);
                return;
            } else if (cellI + 1 < maze.size() && maze.get(cellI + 1).get(cellJ).getCellType() == CellType.WALL) {
                updateCellType(maze, cellI, cellJ, CellType.DOOR);
                updateCellType(maze, cellI + 1, cellJ, CellType.PATH);
                return;
            } else if (cellJ - 1 > 0 && maze.get(cellI).get(cellJ - 1).getCellType() == CellType.WALL) {
                updateCellType(maze, cellI, cellJ, CellType.DOOR);
                updateCellType(maze, cellI, cellJ - 1, CellType.PATH);
                return;
            } else if (cellJ + 1 < maze.get(cellI).size() && maze.get(cellI).get(cellJ + 1).getCellType() == CellType.WALL) {
                updateCellType(maze, cellI, cellJ, CellType.DOOR);
                updateCellType(maze, cellI, cellJ + 1, CellType.PATH);
                return;
            }
        }
        throw new IllegalStateException("Door could not be generated");

    }


    /**
     * Fills the room space with walls and room path, then generates a door and insides
     * @param mazeCells - matrix of maze
     * @param random - random instance
     */
    public void generate(ArrayList<ArrayList<GeneratorCell>> mazeCells, Random random) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i == 0 || i == height - 1 || j == 0 || j == width - 1) {
                    updateCellType(mazeCells, this.i + i, this.j + j, CellType.ROOM_WALL);

                } else {
                    updateCellType(mazeCells, this.i + i, this.j + j, CellType.ROOM_PATH);
                }
            }
        }
        this.generateDoor(mazeCells, random);
        this.generateInsides();
    }

    /**
     * Updates the cell type of the room cell and the corresponding cell in the maze
     * @param maze the maze
     * @param i the row of the cell
     * @param j the column of the cell
     * @param cellType the new cell type
     */
    public void updateCellType(ArrayList<ArrayList<GeneratorCell>> maze, int i, int j, CellType cellType) {
        if (this.i == -1 || this.j == -1) {
            throw new IllegalStateException("Room location is not set");
        }
        maze.get(i).get(j).setCellType(cellType);
    }

}
