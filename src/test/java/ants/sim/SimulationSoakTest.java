package ants.sim;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class SimulationSoakTest {

    // Spawners used to spin forever once stale food flags, rocks and tunnels filled the grid
    // (seed 3 froze before tick 25,000).
    @Test
    void longRunNeverFreezes() {
        assertTimeoutPreemptively(Duration.ofSeconds(90), () -> {
            Simulation sim = Simulation.manual(3L);
            sim.start();
            for (int i = 0; i < 40_000; i++) sim.step();
        });
    }
}
