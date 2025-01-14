package de.tum.cit.fop.maze.level.worldgen.rooms;


import de.tum.cit.fop.maze.level.worldgen.GeneratorStrategy;

public class SpecialRoom extends Room {
    public SpecialRoom() {
        super(3, 3, GeneratorStrategy.RANDOM);
    }
}
