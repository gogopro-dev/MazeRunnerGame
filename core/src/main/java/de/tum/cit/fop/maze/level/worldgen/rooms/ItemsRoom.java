package de.tum.cit.fop.maze.level.worldgen.rooms;


import de.tum.cit.fop.maze.level.worldgen.GeneratorStrategy;

public class ItemsRoom extends Room {
    public ItemsRoom() {
        super(3, 3, GeneratorStrategy.RANDOM);
    }
}
