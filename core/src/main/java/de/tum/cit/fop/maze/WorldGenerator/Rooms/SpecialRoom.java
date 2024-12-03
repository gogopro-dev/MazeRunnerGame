package de.tum.cit.fop.maze.WorldGenerator.Rooms;


import de.tum.cit.fop.maze.WorldGenerator.GeneratorStrategy;

public class SpecialRoom extends Room {
    public SpecialRoom() {
        super(5, 5, GeneratorStrategy.RANDOM);
    }
}
