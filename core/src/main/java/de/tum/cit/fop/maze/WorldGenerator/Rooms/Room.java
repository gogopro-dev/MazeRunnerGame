package de.tum.cit.fop.maze.WorldGenerator.Rooms;

import de.tum.cit.fop.maze.WorldGenerator.CellType;
import de.tum.cit.fop.maze.WorldGenerator.GeneratorStrategy;
import de.tum.cit.fop.maze.WorldGenerator.MazeCell;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public abstract class Room {
    public final int height;
    public final int width;
    protected int i = -1;
    protected int j = -1;
    public final GeneratorStrategy generatorStrategy;

    protected List<MazeCell> roomCells = null;

    protected Room(int height, int width, GeneratorStrategy generatorStrategy) {
        this.height = height;
        this.width = width;
        this.generatorStrategy = generatorStrategy;
    }

    public List<MazeCell> getRoomCells() {
        return roomCells;
    }

    public void setLocation(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public void setRoomCells(List<MazeCell> roomCells) {
        this.roomCells = roomCells;
    }

    public void generateInsides() {

    }

    public void generateDoor(List<List<MazeCell>> maze, Random random) {
        if (i == -1 || j == -1) {
            throw new IllegalStateException("Room location is not set");
        }
        // Door can't be generated at corners so we chose only walls and filter out the corner cells
        List<MazeCell> candidates = this.roomCells.stream().filter(
            cell -> (
                cell.getCellType() == CellType.ROOM_WALL && (
                    cell.i != i || cell.j != j
                ) && (
                    cell.i != i + height - 1 || cell.j != j
                ) && (
                    cell.i != i || cell.j != j + width - 1
                ) && (
                    cell.i != i + height - 1 || cell.j != j + width - 1
                )
            )
        ).collect(Collectors.toList());
        Collections.shuffle(candidates, random);
        for (MazeCell candidate : candidates) {
            int cellI = candidate.i;
            int cellJ = candidate.j;
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

        // Remove a wall next to the door as well if there is no other options to generate a reachable door
        for (MazeCell candidate : candidates) {
            int cellI = candidate.i;
            int cellJ = candidate.j;
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
    public void generate(List<List<MazeCell>> mazeCells, Random random) {
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
    public void updateCellType(List<List<MazeCell>> maze, int i, int j, CellType cellType) {
        if (this.i == -1 || this.j == -1) {
            throw new IllegalStateException("Room location is not set");
        }
        maze.get(i).get(j).setCellType(cellType);
    }

}
