package ants.sim;

/** Tuning constants of the simulation, grouped so they can be adjusted in one place. */
public final class SimConfig {

    private SimConfig() {
    }

    public static final int TOPOLOGY_WIDTH = 1000;
    public static final int TOPOLOGY_HEIGHT = 800;
    public static final int GRID_COLUMNS = 30;
    public static final int GRID_ROWS = 25;

    public static final int INITIAL_FOOD = 15;
    public static final int INITIAL_ROCKS = 5;
    public static final int QUEEN_INITIAL_STOCK = 10;

    public static final double PHEROMONE_DEPOSIT = 0.1;
    public static final double PHEROMONE_MAX = 1.0;
    /** Below this intensity a pheromone is considered gone and reset to 0. */
    public static final double PHEROMONE_FLOOR = 0.01;

    /** Ticks for a saturated cell to fade to PHEROMONE_FLOOR (subject: 1000 for food, 2000 for queen). */
    public static final int FOOD_PHEROMONE_LIFETIME = 1000;
    public static final int QUEEN_PHEROMONE_LIFETIME = 2000;
    public static final double FOOD_EVAPORATION_FACTOR =
            Math.pow(PHEROMONE_FLOOR / PHEROMONE_MAX, 1.0 / FOOD_PHEROMONE_LIFETIME);
    public static final double QUEEN_EVAPORATION_FACTOR =
            Math.pow(PHEROMONE_FLOOR / PHEROMONE_MAX, 1.0 / QUEEN_PHEROMONE_LIFETIME);

    public static final double QUEEN_SPAWN_PROBABILITY = 0.01;
    public static final double FOOD_SPAWN_PROBABILITY = 0.01;
    public static final double ROCK_SPAWN_PROBABILITY = 0.005;
    /** Random picks tried before a spawn is skipped because no free cell was found. */
    public static final int MAX_SPAWN_ATTEMPTS = 200;

    public static final double ANT_SPEED = 8;
}
