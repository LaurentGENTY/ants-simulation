package ants.actors;

import ants.environment.Cell;
import ants.sim.SimConfig;
import io.jbotsim.core.Node;
import io.jbotsim.core.Point;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This type of Node can move over a sequence of destinations,
 * specified through the addDestination() method.
 */
abstract public class WaypointNode extends CellLocatedNode {

    Queue<Cell> destinations = new LinkedList<Cell>();

    double speed = SimConfig.ANT_SPEED;

    @Override
    public void onClock() {
        if (destinations.isEmpty())
            return;
        Point dest = destinations.peek();
        if (distance(dest) > speed) {
            setDirection(dest);
            move(speed);
        } else {
            setLocation(dest);
            destinations.poll();
            // Only on real arrival: calling it every tick re-planned and piled up destinations.
            onArrival();
        }
    }

    abstract public void onArrival();

    public void addDestination(Cell destination){ destinations.add(destination);}
}
