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
        Cell location = environment.getRandomLocationDepth(0.8,8);
        /* on accepte la nourriture que si la case n'est pas déjà creusée ou alors s'il y a deja de la nourriture ou une pierrre */
        while(location.isFood() || location.isRock() || location.isDug())
            location = environment.getRandomLocationDepth(0.8,8);

        location.setFood(true);

        n.setLocation(location);
        n.setCurrentCell(location);
        tp.addNode(n);
    }

    private boolean shouldSpawn() {
        return SimRandom.get().nextDouble() < SimConfig.FOOD_SPAWN_PROBABILITY;
    }
}
