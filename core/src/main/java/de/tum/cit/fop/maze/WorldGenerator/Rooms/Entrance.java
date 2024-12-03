package de.tum.cit.fop.maze.WorldGenerator.Rooms;


import de.tum.cit.fop.maze.WorldGenerator.CellType;
import de.tum.cit.fop.maze.WorldGenerator.GeneratorStrategy;
import de.tum.cit.fop.maze.WorldGenerator.MazeCell;

import java.util.List;
import java.util.Random;

public class Entrance extends Room {
    public Entrance() {
        super(3, 3, GeneratorStrategy.CENTER);
    }

    @Override
    public void generate(List<List<MazeCell>> mazeCells, Random random) {
        for (MazeCell cell : this.roomCells) {
            updateCellType(mazeCells, cell.i, cell.j, CellType.ROOM_PATH);
        }
    }

}
