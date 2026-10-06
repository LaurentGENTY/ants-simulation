package ants.comparators;

import ants.environment.Cell;
import java.util.Comparator;

/* Orders cells by food pheromone intensity, strongest first. */
public class FoodPheromoneComparator implements Comparator<Cell> {

    @Override
    public int compare(Cell c1, Cell c2) {
        return Double.compare(c2.getFoodPheromoneIntensity(), c1.getFoodPheromoneIntensity());
    }
}
