package ants.comparators;

import ants.environment.Cell;
import java.util.Comparator;

/* classe permettant de comaprer deux cellules selon la quantité de pheromones de nourriture */
public class FoodPheromoneComparator implements Comparator<Cell> {

    @Override
    public int compare(Cell c1, Cell c2) {
        return Double.compare(c1.getFoodPheromoneIntensity(),c2.getFoodPheromoneIntensity());
    }
}
