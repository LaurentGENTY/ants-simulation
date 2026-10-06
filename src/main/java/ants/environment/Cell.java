package ants.environment;

import ants.sim.SimConfig;
import ants.sim.SimRandom;

import io.jbotsim.core.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Cell extends Point {

    /* cout de la cell */
    public static final int MAX_COST_VALUE = 40;
    public static final int MIN_COST_VALUE = 1;
    private int cost;
    /* cout initial */
    private final int initialCost;

    /* environnement direct */
    Map<Integer, Cell> neighBor = new HashMap<>();

    /* pose de pheromones par les fourmis */
    private double foodPheromoneIntensity;
    private double queenPheromoneIntensity;

    /* nombre de onClock pour creuser la case */
    private int timeDigging;

    /* booleans d'états de la case */
    private boolean dug;
    private boolean isFood;
    private boolean isRock;

    public Cell(Point location){
        super(location);

        /* couleur de la case (cout) */
        this.cost = SimRandom.get().nextInt(MAX_COST_VALUE - MIN_COST_VALUE+1) + MIN_COST_VALUE+1;
        this.initialCost = cost;

        /* le temps restant pour creuser une case depend de son cout initial (de sa profondeur) */
        this.timeDigging = initialCost;

        /* initialisation des pheromones */
        this.foodPheromoneIntensity = 0;
        this.queenPheromoneIntensity = 0;

        /* initialisation de l'état */
        this.dug = false;
        this.isFood = false;
        this.isRock = false;
    }

    /** One tick of exponential decay; values under the floor are cleared so trails really end. */
    public void evaporate() {
        foodPheromoneIntensity = decay(foodPheromoneIntensity, SimConfig.FOOD_EVAPORATION_FACTOR);
        queenPheromoneIntensity = decay(queenPheromoneIntensity, SimConfig.QUEEN_EVAPORATION_FACTOR);
    }

    private static double decay(double intensity, double factor) {
        double next = intensity * factor;
        return next < SimConfig.PHEROMONE_FLOOR ? 0 : next;
    }

    /* methode pour les voisins */

    public Cell getNeighBor(int index) {
        return neighBor.get(index);
    }
    public void setNeighBor(int index, Cell value) {
        neighBor.put(index, value);
    }

    public static final int TOP = 0;
    public static final int TOP_RIGHT = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM_RIGHT = 3;
    public static final int BOTTOM = 4;
    public static final int BOTTOM_LEFT = 5;
    public static final int LEFT = 6;
    public static final int TOP_LEFT = 7;

    public Cell getRightNeighbor() {
        return getNeighBor(RIGHT);
    }
    public void setRightNeighbor(Cell neighbor) {
        setNeighBor(RIGHT, neighbor);
    }
    public Cell getLeftNeighbor() {
        return getNeighBor(LEFT);
    }
    public void setLefNeighbor(Cell neighbor) {
        setNeighBor(LEFT, neighbor);
    }
    public Cell getTopNeighbor() {
        return getNeighBor(TOP);
    }
    public void setTopNeighbor(Cell neighbor) {
        setNeighBor(TOP, neighbor);
    }
    public Cell getBottomNeighbor() {
        return getNeighBor(BOTTOM);
    }
    public void setBottomNeighbor(Cell neighbor) {
        setNeighBor(BOTTOM, neighbor);
    }
    public Cell getTopRightNeighbor() {
        return getNeighBor(TOP_RIGHT);
    }
    public void setTopRightNeighbor(Cell neighbor) {
        setNeighBor(TOP_RIGHT, neighbor);
    }
    public Cell getTopLeftNeighbor() {
        return getNeighBor(TOP_LEFT);
    }
    public void setTopLefNeighbor(Cell neighbor) {
        setNeighBor(TOP_LEFT, neighbor);
    }
    public Cell getBottomRightNeighbor() {
        return getNeighBor(BOTTOM_RIGHT);
    }
    public void setBottomRightNeighbor(Cell neighbor) {
        setNeighBor(BOTTOM_RIGHT, neighbor);
    }
    public Cell getBottomLeftNeighbor() {
        return getNeighBor(BOTTOM_LEFT);
    }
    public void setBottomLeftNeighbor(Cell neighbor) {
        setNeighBor(BOTTOM_LEFT, neighbor);
    }

    public ArrayList<Cell> getAllNeighbors() {
        /* recuperer tous les voisins non null (en dehors de l'environnement) de la cell dans un array list */
        ArrayList<Cell> neighbors = new ArrayList<Cell>();
        for(int i = 0; i < 8; i++) {
            if(getNeighBor(i) != null)
                neighbors.add(getNeighBor(i));
        }
        return neighbors;
    }

    /** Uniform pick among neighbours an ant may enter, or null when the cell is boxed in. */
    public Cell randomWalkableNeighbor(Random random) {
        ArrayList<Cell> walkable = new ArrayList<>();
        for (Cell neighbor : getAllNeighbors())
            if (!neighbor.isRock())
                walkable.add(neighbor);
        if (walkable.isEmpty())
            return null;
        return walkable.get(random.nextInt(walkable.size()));
    }

    /* getters setters cout */
    public int getCost() {
        return cost;
    }
    public void setCost(int cost) {
        if(cost < MIN_COST_VALUE) this.cost = MIN_COST_VALUE;
        else if(cost > initialCost) this.cost = initialCost;
        else this.cost = cost;
        return;
    }

    /* getters setters sur les pheromones */
    public double getFoodPheromoneIntensity() {
        return foodPheromoneIntensity;
    }
    public void incrementFoodPheromoneIntensity(double value) {
        foodPheromoneIntensity = Math.min(SimConfig.PHEROMONE_MAX, foodPheromoneIntensity + value);
    }

    public double getQueenPheromoneIntensity() {
        return queenPheromoneIntensity;
    }
    public void incrementQueenPheromoneIntensity(double value) {
        queenPheromoneIntensity = Math.min(SimConfig.PHEROMONE_MAX, queenPheromoneIntensity + value);
    }

    /* getter setters booleans d'états */
    public boolean isDug() {
        return dug;
    }
    public void setDug(boolean d) { this.dug = d;}
    public boolean isFood() {
        return isFood;
    }
    public void setFood(boolean rock) {
        isFood = rock;
    }
    public boolean isRock() {
        return isRock;
    }
    public void setRock(boolean rock) {
        isRock = rock;
    }

    /* temps de creusage */
    public void decrementTimeDigging() {
        this.timeDigging--;

        /* si le temps de creusage a été atteint alors la cell est creusée */
        if(this.timeDigging <= 0)
            this.dug = true;
        return;
    }

    /* convertir le temps de creusage en couleur (cout) */
    public int convertTimeDiggingCost() {
        return this.timeDigging + MIN_COST_VALUE;
    }
}
