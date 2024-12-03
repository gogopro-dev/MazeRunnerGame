package de.tum.cit.fop.maze.WorldGenerator.Rooms;

import de.tum.cit.fop.maze.WorldGenerator.CellType;
import de.tum.cit.fop.maze.WorldGenerator.GeneratorStrategy;
import de.tum.cit.fop.maze.WorldGenerator.MazeCell;

import java.util.List;
import java.util.Random;

public class KeyRoom extends Room {
    public KeyRoom() {
        super(3, 3, GeneratorStrategy.AROUND_CENTER);
    }


    @Override
    public void generate(List<List<MazeCell>> mazeCells, Random random) {
        for (MazeCell cell : this.roomCells) {
            updateCellType(mazeCells, cell.i, cell.j, CellType.ROOM_PATH);
            if (cell.i == i + 1 && cell.j == j + 1) {
                updateCellType(mazeCells, cell.i, cell.j, CellType.KEY_OBELISK);
            }
        }
    }
}
