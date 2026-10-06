package ants.comparators;

import ants.environment.Cell;
import java.util.Comparator;

/* Orders cells by queen pheromone intensity, strongest first. */
public class QueenPheromoneComparator implements Comparator<Cell> {

    @Override
    public int compare(Cell c1, Cell c2) {
        return Double.compare(c2.getQueenPheromoneIntensity(), c1.getQueenPheromoneIntensity());
    }
}
