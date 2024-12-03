package de.tum.cit.fop.maze.WorldGenerator.Rooms;


import de.tum.cit.fop.maze.WorldGenerator.GeneratorStrategy;

public class Shop extends Room {
    public Shop() {
        super(4, 7, GeneratorStrategy.AROUND_CENTER);
    }
}
