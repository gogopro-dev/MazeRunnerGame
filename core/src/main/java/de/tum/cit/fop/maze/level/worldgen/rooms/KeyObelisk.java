package de.tum.cit.fop.maze.level.worldgen.rooms;

import de.tum.cit.fop.maze.level.worldgen.CellType;
import de.tum.cit.fop.maze.level.worldgen.GeneratorCell;
import de.tum.cit.fop.maze.level.worldgen.GeneratorStrategy;

import java.util.ArrayList;
import java.util.Random;

public class KeyObelisk extends Room {
    public KeyObelisk() {
        super(3, 3, GeneratorStrategy.AROUND_CENTER);
    }


    @Override
    public void generate(ArrayList<ArrayList<GeneratorCell>> mazeCells, Random random) {
        for (GeneratorCell cell : this.roomCells) {
            updateCellType(mazeCells, cell.getI(), cell.getJ(), CellType.ROOM_PATH);
            if (cell.getI() == i + 1 && cell.getJ() == j + 1) {
                updateCellType(mazeCells, cell.getI(), cell.getJ(), CellType.KEY_OBELISK);
            }
        }
    }
}
