package ants.actors;

import ants.sim.SimConfig;
import ants.sim.SimRandom;

public class QueenNode extends CellLocatedNode {

    /* stock de nourriture (pas de TTL, une reine fourmi peut vivre jusqua 37 ans !) */
    private int foodStock;
    private int foodDelivered;

    public QueenNode() {
        this(SimConfig.QUEEN_INITIAL_STOCK);
    }

    public QueenNode(int initialStock) {
        super();
        foodStock = initialStock;

        setIcon("/images/ant-queen.png");
        setIconSize(getIconSize() * 2);
    }

    @Override
    /* production recurrent de fourmis */
    public void onClock() {
        if (shouldProduceOffspring())
            produceOffspring();
    }

    private boolean shouldProduceOffspring() {
        return SimRandom.get().nextDouble() < SimConfig.QUEEN_SPAWN_PROBABILITY;
    }

    public void produceOffspring() {
        if (foodStock <= 0) {
            die();
            // A dead queen must not lay: the original fell through and spawned anyway.
            return;
        }
        foodStock--;

        AntNode babyAnt = new AntNode(this);
        babyAnt.setCurrentCell(getCurrentCell());
        getTopology().addNode(babyAnt);
    }

    public void increaseFoodStock(int value) {
        // die() only flags the node; ants processed later in the same tick still reach her.
        if (isDying())
            return;
        this.foodStock += value;
        this.foodDelivered += value;
    }

    public int getFoodStock() {
        return foodStock;
    }

    public int getFoodDelivered() {
        return foodDelivered;
    }
}
