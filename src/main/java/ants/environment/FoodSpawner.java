package ants.environment;

import ants.sim.SimConfig;
import ants.sim.SimRandom;

import io.jbotsim.core.Topology;


public class FoodSpawner{

    private Topology tp;
    private Environment environment;

    public FoodSpawner(Topology topology, Environment environment) {
        tp = topology;
        this.environment = environment;
    }

    public void tick() {
        if (shouldSpawn())
            spawnRandomFood();
    }

    public void spawnRandomFood() {
        FoodNode n = new FoodNode();

        /* on créé une case random avec de la nourriture */
        /* on accepte la nourriture que si la case n'est pas déjà creusée ou alors s'il y a deja de la nourriture ou une pierrre */
        Cell location = null;
        for (int attempt = 0; attempt < SimConfig.MAX_SPAWN_ATTEMPTS && location == null; attempt++) {
            Cell candidate = environment.getRandomLocationDepth(0.8,8);
            if (!candidate.isFood() && !candidate.isRock() && !candidate.isDug())
                location = candidate;
        }
        // Tunnels and rocks can fill the grid: skip this spawn rather than spin forever.
        if (location == null)
            return;

        location.setFood(true);

        n.setLocation(location);
        n.setCurrentCell(location);
        tp.addNode(n);
    }

    private boolean shouldSpawn() {
        return SimRandom.get().nextDouble() < SimConfig.FOOD_SPAWN_PROBABILITY;
    }
}
