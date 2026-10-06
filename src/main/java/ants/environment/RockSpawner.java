package ants.environment;

import ants.sim.SimConfig;
import ants.sim.SimRandom;

import io.jbotsim.core.Topology;


public class RockSpawner{

    private Topology tp;
    private Environment environment;

    public RockSpawner(Topology topology, Environment environment) {
        tp = topology;
        this.environment = environment;
    }

    public void tick() {
        /* on part du principe que dans le temps des pierres peuvent apparaitre dans la terre */
        /* TO DO : faire en sorte que si une pierre apparait pendant le retour d'une fourmi, change son trajet*/
        if (shouldSpawn())
            spawnRandomRocks();
    }

    public void spawnRandomRocks() {
        RockNode r = new RockNode();

        /* on créé une pierre random en verifiant qu'elle ne superpose pas avec de la nourriture */
        Cell location = environment.getRandomLocationDepth(0.6,5);
        while(location.isFood() || location.isDug())
            location = environment.getRandomLocationDepth(0.6,5);
        location.setRock(true);

        /* on l'ajoute à la topologie */
        r.setLocation(location);
        r.setCurrentCell(location);
        tp.addNode(r);
    }

    private boolean shouldSpawn() {
        return SimRandom.get().nextDouble() < SimConfig.ROCK_SPAWN_PROBABILITY;
    }

}
