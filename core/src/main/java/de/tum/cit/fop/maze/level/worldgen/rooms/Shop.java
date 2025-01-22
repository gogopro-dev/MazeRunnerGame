package de.tum.cit.fop.maze.level.worldgen.rooms;


import de.tum.cit.fop.maze.level.worldgen.CellType;
import de.tum.cit.fop.maze.level.worldgen.GeneratorCell;
import de.tum.cit.fop.maze.level.worldgen.GeneratorStrategy;

import java.util.ArrayList;
import java.util.Random;

public class Shop extends Room {
    public Shop() {
        super(5, 7, GeneratorStrategy.AROUND_CENTER);
    }


    /**
     * Fills the room space with walls and room path, then generates a door and insides
     *
     * @param mazeCells - matrix of maze
     * @param random    - random instance
     */
    @Override
    public void generate(ArrayList<ArrayList<GeneratorCell>> mazeCells, Random random) {
        super.generate(mazeCells, random);
        updateCellType(mazeCells, i + 2, j + 2, CellType.SHOP_ITEM);
        updateCellType(mazeCells, i + 2, j + 3, CellType.SHOP_ITEM);
        updateCellType(mazeCells, i + 2, j + 4, CellType.SHOP_ITEM);
    }
}
