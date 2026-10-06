package ants.environment;

import io.jbotsim.core.Topology;
import io.jbotsim.core.event.ClockListener;

import java.util.Random;

public class FoodSpawner implements ClockListener{

    private final Random random;
    private Topology tp;
    private Environment environment;

    public FoodSpawner(Topology topology, Environment environment) {
        tp = topology;
        this.environment = environment;
        tp.addClockListener(this);
        random = new Random();
    }

    @Override
    public void onClock() {
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
        return random.nextDouble() < 0.01;
    }
}
