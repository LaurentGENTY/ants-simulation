package ants.environment;

import ants.sim.SimConfig;
import io.jbotsim.core.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellPheromoneTest {

    private static Cell saturatedCell() {
        Cell cell = new Cell(new Point(0, 0));
        for (int i = 0; i < 20; i++) {
            cell.incrementFoodPheromoneIntensity(SimConfig.PHEROMONE_DEPOSIT);
            cell.incrementQueenPheromoneIntensity(SimConfig.PHEROMONE_DEPOSIT);
        }
        return cell;
    }

    @Test
    void depositIsClampedToMax() {
        Cell cell = saturatedCell();
        assertEquals(SimConfig.PHEROMONE_MAX, cell.getFoodPheromoneIntensity(), 1e-9);
        assertEquals(SimConfig.PHEROMONE_MAX, cell.getQueenPheromoneIntensity(), 1e-9);
    }

    @Test
    void evaporationIsProgressive() {
        Cell cell = saturatedCell();
        cell.evaporate();
        assertEquals(SimConfig.FOOD_EVAPORATION_FACTOR, cell.getFoodPheromoneIntensity(), 1e-9);
        assertEquals(SimConfig.QUEEN_EVAPORATION_FACTOR, cell.getQueenPheromoneIntensity(), 1e-9);
    }

    @Test
    void pheromoneBelowFloorIsResetToZero() {
        Cell cell = new Cell(new Point(0, 0));
        cell.incrementFoodPheromoneIntensity(SimConfig.PHEROMONE_FLOOR * 1.0001);
        cell.evaporate();
        assertEquals(0.0, cell.getFoodPheromoneIntensity());
    }

    @Test
    void foodPheromoneFadesTwiceAsFastAsQueenPheromone() {
        Cell cell = saturatedCell();
        for (int i = 0; i <= SimConfig.FOOD_PHEROMONE_LIFETIME; i++) cell.evaporate();
        assertEquals(0.0, cell.getFoodPheromoneIntensity());
        assertTrue(cell.getQueenPheromoneIntensity() > 0.05,
                "queen pheromone should still be clearly visible, was " + cell.getQueenPheromoneIntensity());
    }
}
