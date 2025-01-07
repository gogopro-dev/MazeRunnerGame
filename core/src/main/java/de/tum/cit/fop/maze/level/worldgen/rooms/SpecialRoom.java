package de.tum.cit.fop.maze.level.worldgen.rooms;


import de.tum.cit.fop.maze.level.worldgen.GeneratorStrategy;

public class SpecialRoom extends Room {
    public SpecialRoom() {
        super(5, 5, GeneratorStrategy.RANDOM);
    }
}
