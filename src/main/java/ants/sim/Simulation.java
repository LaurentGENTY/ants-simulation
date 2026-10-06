package ants.sim;

import ants.actors.AntNode;
import ants.actors.QueenNode;
import ants.environment.Cell;
import ants.environment.Environment;
import ants.environment.FoodNode;
import ants.environment.FoodSpawner;
import ants.environment.RockNode;
import ants.environment.RockSpawner;
import io.jbotsim.core.Clock;
import io.jbotsim.core.DefaultClock;
import io.jbotsim.core.Topology;

/** Builds the ant world and drives its per-tick updates. */
public final class Simulation {

    private final Topology topology;
    private final Environment environment;
    private final QueenNode queen;
    private final FoodSpawner foodSpawner;
    private final RockSpawner rockSpawner;
    private final boolean manualClock;
    private ManualClock clock;
    // Own counter: JBotSim skips the time increment on its first round, so getTime() lags by one.
    private int ticks;
    private volatile ColonyStats stats;

    public static Simulation interactive(long seed) {
        return new Simulation(seed, DefaultClock.class);
    }

    public static Simulation manual(long seed) {
        return new Simulation(seed, ManualClock.class);
    }

    private Simulation(long seed, Class<? extends Clock> clockModel) {
        SimRandom.reseed(seed);
        manualClock = clockModel == ManualClock.class;

        topology = new Topology(SimConfig.TOPOLOGY_WIDTH, SimConfig.TOPOLOGY_HEIGHT);
        topology.setClockModel(clockModel);
        topology.setNodeModel("ant", AntNode.class);
        topology.setNodeModel("queen", QueenNode.class);
        topology.setNodeModel("food", FoodNode.class);
        topology.setNodeModel("rock", RockNode.class);

        environment = new Environment(topology, SimConfig.GRID_COLUMNS, SimConfig.GRID_ROWS);
        queen = createQueen();
        foodSpawner = new FoodSpawner(topology, environment);
        rockSpawner = new RockSpawner(topology, environment);
        for (int i = 0; i < SimConfig.INITIAL_FOOD; i++)
            foodSpawner.spawnRandomFood();
        for (int i = 0; i < SimConfig.INITIAL_ROCKS; i++)
            rockSpawner.spawnRandomRocks();

        stats = ColonyStats.snapshot(ticks, topology, queen);
        // One listener only: JBotSim keeps listeners in a HashMap, so several RNG-consuming
        // listeners would run in an arbitrary order and break seed reproducibility.
        topology.addClockListener(this::onWorldTick);
    }

    private QueenNode createQueen() {
        QueenNode q = new QueenNode();
        Cell queenCell = environment.getRandomLocation();
        q.setCurrentCell(queenCell);
        q.setLocation(queenCell);
        queenCell.setCost(Cell.MIN_COST_VALUE);
        queenCell.setDug(true);
        topology.addNode(q);
        return q;
    }

    private void onWorldTick() {
        environment.evaporate();
        foodSpawner.tick();
        rockSpawner.tick();
        ticks++;
        stats = ColonyStats.snapshot(ticks, topology, queen);
    }

    public void start() {
        topology.start();
        if (manualClock)
            clock = ManualClock.last();
    }

    /** Advances one tick; only for simulations built with {@link #manual(long)} and started. */
    public void step() {
        if (clock == null)
            throw new IllegalStateException("step() needs a started manual simulation");
        clock.tick();
    }

    public Topology topology() {
        return topology;
    }

    public Environment environment() {
        return environment;
    }

    public QueenNode queen() {
        return queen;
    }

    public ColonyStats stats() {
        return stats;
    }
}
