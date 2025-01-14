package de.tum.cit.fop.maze.level.worldgen.rooms;


import de.tum.cit.fop.maze.level.worldgen.CellType;
import de.tum.cit.fop.maze.level.worldgen.GeneratorStrategy;
import de.tum.cit.fop.maze.level.worldgen.MazeCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Entrance extends Room {
    public Entrance() {
        super(5, 5, GeneratorStrategy.CENTER);
    }

    @Override
    public void generate(ArrayList<ArrayList<MazeCell>> mazeCells, Random random) {
        for (MazeCell cell : this.roomCells) {
            updateCellType(mazeCells, cell.i, cell.j, CellType.ROOM_PATH);
        }
    }

}
