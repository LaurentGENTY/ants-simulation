package ants.sim;

import java.util.Random;

/**
 * The one random source of the simulation. JBotSim instantiates nodes itself, so a global
 * holder is the only way to give every actor the same seeded stream.
 */
public final class SimRandom {

    private static Random random = new Random();

    private SimRandom() {
    }

    public static Random get() {
        return random;
    }

    public static void reseed(long seed) {
        random = new Random(seed);
    }
}
