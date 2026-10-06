package ants.sim;

import io.jbotsim.core.Node;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationTest {

    private static Simulation run(long seed, int ticks) {
        Simulation sim = Simulation.manual(seed);
        sim.start();
        for (int i = 0; i < ticks; i++) sim.step();
        return sim;
    }

    private static String fingerprint(Simulation sim) {
        StringBuilder sb = new StringBuilder(sim.stats().toString());
        sim.environment().forEachCell(c -> sb
                .append(c.isDug() ? '1' : '0')
                .append(String.format(Locale.ROOT, "%.6f/%.6f;",
                        c.getFoodPheromoneIntensity(), c.getQueenPheromoneIntensity())));
        for (Node n : sim.topology().getNodes())
            sb.append(n.getClass().getSimpleName())
              .append(String.format(Locale.ROOT, "@%.3f,%.3f;", n.getX(), n.getY()));
        return sb.toString();
    }

    @Test
    void sameSeedGivesSameWorld() {
        assertEquals(fingerprint(run(42L, 1500)), fingerprint(run(42L, 1500)));
    }

    @Test
    void coloniesGrowDigAndLayTrails() {
        Simulation sim = run(42L, 1500);
        AtomicInteger dug = new AtomicInteger();
        double[] queenPheromone = {0};
        sim.environment().forEachCell(c -> {
            if (c.isDug()) dug.incrementAndGet();
            queenPheromone[0] += c.getQueenPheromoneIntensity();
        });
        assertTrue(sim.stats().tick() > 1400, "tick was " + sim.stats().tick());
        assertTrue(dug.get() > 1, "ants should have dug tunnels, dug=" + dug.get());
        assertTrue(queenPheromone[0] > 0, "ants should have laid queen pheromone");
    }

    @Test
    void stepRequiresManualClock() {
        Simulation sim = Simulation.interactive(1L);
        assertThrows(IllegalStateException.class, sim::step);
    }
}
