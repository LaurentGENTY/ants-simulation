package ants.actors;

import ants.sim.ManualClock;
import io.jbotsim.core.Topology;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class QueenNodeTest {

    @Test
    void starvingQueenDiesWithoutOffspring() {
        Topology tp = new Topology(100, 100);
        tp.setClockModel(ManualClock.class);
        QueenNode queen = new QueenNode(0);
        tp.addNode(50, 50, queen);
        tp.start();

        queen.produceOffspring();
        ManualClock.last().tick();

        assertFalse(tp.getNodes().contains(queen));
        assertEquals(0, tp.getNodes().stream().filter(n -> n instanceof AntNode).count());
    }

    @Test
    void deliveriesAreCounted() {
        QueenNode queen = new QueenNode(10);
        queen.increaseFoodStock(2);
        queen.increaseFoodStock(1);
        assertEquals(13, queen.getFoodStock());
        assertEquals(3, queen.getFoodDelivered());
    }
}
