package de.tum.cit.fop.maze.level.worldgen.rooms;

import de.tum.cit.fop.maze.level.worldgen.CellType;
import de.tum.cit.fop.maze.level.worldgen.GeneratorStrategy;
import de.tum.cit.fop.maze.level.worldgen.MazeCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KeyRoom extends Room {
    public KeyRoom() {
        super(3, 3, GeneratorStrategy.AROUND_CENTER);
    }


    @Override
    public void generate(ArrayList<ArrayList<MazeCell>> mazeCells, Random random) {
        for (MazeCell cell : this.roomCells) {
            updateCellType(mazeCells, cell.i, cell.j, CellType.ROOM_PATH);
            if (cell.i == i + 1 && cell.j == j + 1) {
                updateCellType(mazeCells, cell.i, cell.j, CellType.KEY_OBELISK);
            }
        }
    }
}
