package ants.comparators;

import ants.environment.Cell;
import io.jbotsim.core.Point;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

class PheromoneComparatorTest {

    private static Cell cell(double food, double queen) {
        Cell c = new Cell(new Point(0, 0));
        c.incrementFoodPheromoneIntensity(food);
        c.incrementQueenPheromoneIntensity(queen);
        return c;
    }

    @Test
    void strongestFoodTrailComesFirst() {
        Cell weak = cell(0.1, 0), strong = cell(0.8, 0), medium = cell(0.4, 0);
        List<Cell> cells = new ArrayList<>(List.of(weak, strong, medium));
        cells.sort(new FoodPheromoneComparator());
        assertSame(strong, cells.get(0));
        assertSame(weak, cells.get(2));
    }

    @Test
    void strongestQueenTrailComesFirst() {
        Cell weak = cell(0, 0.1), strong = cell(0, 0.8), medium = cell(0, 0.4);
        List<Cell> cells = new ArrayList<>(List.of(weak, strong, medium));
        cells.sort(new QueenPheromoneComparator());
        assertSame(strong, cells.get(0));
        assertSame(weak, cells.get(2));
    }
}
