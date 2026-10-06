package ants.environment;

import io.jbotsim.core.Point;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CellNeighborTest {

    private static Cell rock() {
        Cell c = new Cell(new Point(0, 0));
        c.setRock(true);
        return c;
    }

    @Test
    void enclosedCellHasNoWalkableNeighbor() {
        // Corner cell: 3 real neighbours, all rocks, 5 null slots (outside the grid).
        Cell corner = new Cell(new Point(0, 0));
        corner.setRightNeighbor(rock());
        corner.setBottomNeighbor(rock());
        corner.setBottomRightNeighbor(rock());
        assertNull(corner.randomWalkableNeighbor(new Random(1)));
    }

    @Test
    void onlyFreeNeighborIsAlwaysChosen() {
        Cell center = new Cell(new Point(0, 0));
        Cell free = new Cell(new Point(1, 0));
        for (int i = 0; i < 8; i++) center.setNeighBor(i, rock());
        center.setNeighBor(Cell.RIGHT, free);
        Random random = new Random(7);
        for (int i = 0; i < 50; i++) assertSame(free, center.randomWalkableNeighbor(random));
    }
}
