package de.tum.cit.fop.maze.entities.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public enum TrapType {
    SPIKES,
    FLAMETHROWER_H,
    FLAMETHROWER_V;


    public static TrapType pickRandomVerticalTrap(Random random) {
        List<TrapType> candidtaes = Arrays.stream(TrapType.values()).filter(
            (trap) -> !trap.name().contains("_V")
        ).collect(Collectors.toUnmodifiableList());

        return candidtaes.get(random.nextInt(candidtaes.size()));
    }

    public static TrapType pickRandomHorizontalTrap(Random random) {
        List<TrapType> candidtaes = Arrays.stream(TrapType.values()).filter(
            (trap) -> !trap.name().contains("_H")
        ).collect(Collectors.toUnmodifiableList());

        return candidtaes.get(random.nextInt(candidtaes.size()));
    }
}
