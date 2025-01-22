package de.tum.cit.fop.maze.level.worldgen.rooms;


import de.tum.cit.fop.maze.level.worldgen.CellType;
import de.tum.cit.fop.maze.level.worldgen.GeneratorCell;
import de.tum.cit.fop.maze.level.worldgen.GeneratorStrategy;

import java.util.ArrayList;
import java.util.Random;

public class ItemsRoom extends Room {
    public ItemsRoom() {

        super(3, 3, GeneratorStrategy.RANDOM);
    }

    @Override
    public void generate(ArrayList<ArrayList<GeneratorCell>> mazeCells, Random random) {
        super.generate(mazeCells, random);
        updateCellType(
            mazeCells, i + 1, j + 1, CellType.TREASURE_ROOM_ITEM
        );
    }
}
