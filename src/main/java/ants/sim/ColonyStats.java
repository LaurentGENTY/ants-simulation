package ants.sim;

import ants.actors.AntNode;
import ants.actors.QueenNode;
import io.jbotsim.core.Node;
import io.jbotsim.core.Topology;

/** Immutable per-tick snapshot, safe to read from the Swing thread while the simulation runs. */
public record ColonyStats(int tick, int ants, int queenStock, int foodDelivered, boolean queenAlive) {

    static ColonyStats snapshot(Topology topology, QueenNode queen) {
        int ants = 0;
        for (Node node : topology.getNodes())
            if (node instanceof AntNode)
                ants++;
        boolean queenAlive = topology.getNodes().contains(queen);
        return new ColonyStats(topology.getTime(), ants, queen.getFoodStock(), queen.getFoodDelivered(), queenAlive);
    }
}
