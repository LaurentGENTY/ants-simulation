package main.java.ants;

import ants.environment.*;
import ants.ui.EnvironmentBackgroundPainter;
import ants.actors.AntNode;
import ants.actors.QueenNode;

import io.jbotsim.core.Topology;
import io.jbotsim.ui.JViewer;

public class AntHillMain {

    private Topology tp;
    private QueenNode queen;

    public static Environment environment;

    public static void main(String[] args) {
        new AntHillMain();
    }

    public AntHillMain() {
        tp = new Topology(1000,800);

        tp.setNodeModel("ant", AntNode.class);
        tp.setNodeModel("queen", QueenNode.class);
        tp.setNodeModel("food", FoodNode.class);
        tp.setNodeModel("rock", RockNode.class);

        environment = new Environment(tp, 30, 25);

        initializeQueen();
        initializeFood(15);
        initializeRocks(5);

        JViewer jv = new JViewer(tp);
        EnvironmentBackgroundPainter painter = new EnvironmentBackgroundPainter(tp, environment);
        jv.getJTopology().setDefaultBackgroundPainter(painter);

        tp.start();
    }

    private void initializeFood(int nb) {
        FoodSpawner foodSpawner = new FoodSpawner(tp, environment);
        for(int i = 0; i<nb;i++)
            foodSpawner.spawnRandomFood();
    }

    private void initializeRocks(int nb) {
        RockSpawner rockSpawner = new RockSpawner(tp, environment);
        for(int i = 0; i<nb;i++)
            rockSpawner.spawnRandomRocks();
    }

    public void initializeQueen() {

        queen = new QueenNode();

        Cell queenCell = environment.getRandomLocation();
        queen.setCurrentCell(queenCell);
        queen.setLocation(queenCell);

        queenCell.setCost(Cell.MIN_COST_VALUE);
        queenCell.setDug(true);

        tp.addNode(queen);

    }

}
