package ants.actors;

import ants.environment.Cell;
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

    double speed = 8;

    @Override
    public void onClock() {
        if(!destinations.isEmpty()) {
            Point dest = destinations.peek();

            /* si on est pas sur la cellule : on sy dirige */
            if (distance(dest) > speed) {
                setDirection(dest);
                move(speed);
            } else {
                /* on est arrive a destination : on la retire de la queue */
                setLocation(dest);
                destinations.poll();
                onArrival();
            }
        }
        onArrival();
    }

    abstract public void onArrival();

    public void addDestination(Cell destination){ destinations.add(destination);}
}
