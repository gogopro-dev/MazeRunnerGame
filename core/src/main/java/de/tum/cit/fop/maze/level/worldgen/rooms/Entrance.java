package de.tum.cit.fop.maze.level.worldgen.rooms;


import de.tum.cit.fop.maze.level.worldgen.CellType;
import de.tum.cit.fop.maze.level.worldgen.GeneratorStrategy;
import de.tum.cit.fop.maze.level.worldgen.GeneratorCell;

import java.util.ArrayList;
import java.util.Random;

public class Entrance extends Room {
    public Entrance() {
        super(5, 5, GeneratorStrategy.CENTER);
    }

    @Override
    public void generate(ArrayList<ArrayList<GeneratorCell>> mazeCells, Random random) {
        for (GeneratorCell cell : this.roomCells) {
            updateCellType(mazeCells, cell.getI(), cell.getJ(), CellType.ROOM_PATH);
        }
        /// Spawn player in the center of the start
        updateCellType(mazeCells, i + 2, j + 2, CellType.PLAYER);
    }

}
