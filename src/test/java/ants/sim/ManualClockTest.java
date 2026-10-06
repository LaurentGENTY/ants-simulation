package ants.sim;

import io.jbotsim.core.Topology;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManualClockTest {

    @Test
    void ticksOnlyWhenAsked() {
        Topology tp = new Topology(100, 100);
        tp.setClockModel(ManualClock.class);
        int[] calls = {0};
        tp.addClockListener(() -> calls[0]++);
        tp.start();
        ManualClock clock = ManualClock.last();
        for (int i = 0; i < 10; i++) clock.tick();
        assertEquals(10, calls[0]);
    }
}
