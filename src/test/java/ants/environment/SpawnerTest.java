package ants.environment;

import ants.sim.ManualClock;
import io.jbotsim.core.Point;
import io.jbotsim.core.Topology;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class SpawnerTest {

    private static Environment fullyDugEnvironment(Topology tp) {
        Environment env = new Environment(tp, 30, 25);
        env.forEachCell(c -> c.setDug(true));
        return env;
    }

    @Test
    void foodSpawnerGivesUpWhenNoCellIsFree() {
        Topology tp = new Topology(1000, 800);
        FoodSpawner spawner = new FoodSpawner(tp, fullyDugEnvironment(tp));
        assertTimeoutPreemptively(Duration.ofSeconds(2), spawner::spawnRandomFood);
        assertEquals(0, tp.getNodes().size());
    }

    @Test
    void rockSpawnerGivesUpWhenNoCellIsFree() {
        Topology tp = new Topology(1000, 800);
        RockSpawner spawner = new RockSpawner(tp, fullyDugEnvironment(tp));
        assertTimeoutPreemptively(Duration.ofSeconds(2), spawner::spawnRandomRocks);
        assertEquals(0, tp.getNodes().size());
    }

    @Test
    void exhaustedFoodFreesItsCell() {
        Topology tp = new Topology(1000, 800);
        tp.setClockModel(ManualClock.class);
        Cell cell = new Cell(new Point(100, 100));
        cell.setFood(true);
        FoodNode food = new FoodNode();
        food.setCurrentCell(cell);
        tp.addNode(100, 100, food);
        tp.start();

        food.setQuantity(0);

        assertFalse(cell.isFood());
    }
}
