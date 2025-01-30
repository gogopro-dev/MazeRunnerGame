package de.tum.cit.fop.maze.entities.tile;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Represents the type of trap that can be placed on a tile
 * <p>
 *     The trap types are:
 *     <ul>
 *         <li>Spikes</li>
 *         <li>Flamethrower Horizontal</li>
 *         <li>Flamethrower Vertical</li>
 *     </ul>
 * </p>
 */
public enum TrapType {
    SPIKES,
    FLAMETHROWER_H,
    FLAMETHROWER_V;

    /**
     * Picks a random horizontal trap
     * @param random the random number generator
     * @return a random horizontal trap
     */
    public static TrapType pickRandomHorizontalTrap(Random random) {
        /// Asks for .toList() which would not compile on Android target
        @SuppressWarnings("SimplifyStreamApiCallChains")
        List<TrapType> candidates = Arrays.stream(TrapType.values()).filter(
            (trap) -> !trap.name().contains("_V")
        ).collect(Collectors.toUnmodifiableList());

        return candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * Picks a random vertical trap
     * @param random the random number generator
     * @return a random vertical trap
     */
    public static TrapType pickRandomVerticalTrap(Random random) {

        /// Asks for .toList() which would not compile on Android target
        @SuppressWarnings("SimplifyStreamApiCallChains")
        List<TrapType> candidates = Arrays.stream(TrapType.values()).filter(
            (trap) -> !trap.name().contains("_H")
        ).collect(Collectors.toUnmodifiableList());

        return candidates.get(random.nextInt(candidates.size()));
    }
}
