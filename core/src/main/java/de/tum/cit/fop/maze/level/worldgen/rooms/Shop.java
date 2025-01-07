package de.tum.cit.fop.maze.level.worldgen.rooms;


import de.tum.cit.fop.maze.level.worldgen.GeneratorStrategy;

public class Shop extends Room {
    public Shop() {
        super(4, 7, GeneratorStrategy.AROUND_CENTER);
    }
}
