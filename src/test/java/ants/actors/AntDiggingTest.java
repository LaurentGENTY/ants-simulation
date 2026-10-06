package ants.actors;

import ants.environment.Cell;
import ants.environment.Environment;
import ants.sim.ManualClock;
import ants.sim.SimRandom;
import io.jbotsim.core.Topology;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntDiggingTest {

    @Test
    void antGivesUpDiggingWhenARockAppearsOnItsTarget() {
        SimRandom.reseed(1L);
        Topology tp = new Topology(1000, 800);
        tp.setClockModel(ManualClock.class);
        Environment env = new Environment(tp, 30, 25);

        Cell nest = env.getElement(25, 20);
        nest.setDug(true);
        QueenNode queen = new QueenNode(5);
        queen.setCurrentCell(nest);
        tp.addNode(nest.getX(), nest.getY(), queen);

        // The ant's only way out is `target`.
        Cell start = env.getElement(5, 5);
        start.setDug(true);
        Cell target = env.getElement(6, 5);
        for (Cell neighbor : start.getAllNeighbors())
            if (neighbor != target)
                neighbor.setRock(true);
        AntNode ant = new AntNode(queen);
        ant.setCurrentCell(start);
        tp.addNode(start.getX(), start.getY(), ant);

        tp.start();
        assertTrue(ant.isDigging(), "the ant should start digging its only exit");

        target.setRock(true);
        ManualClock clock = ManualClock.last();
        for (int i = 0; i < 100; i++) clock.tick();

        assertFalse(target.isDug(), "nobody may dig through a rock");
        assertEquals(start.getX(), ant.getX(), 1e-9);
        assertEquals(start.getY(), ant.getY(), 1e-9);
    }
}
