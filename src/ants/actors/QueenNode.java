package ants.actors;

import java.util.Random;

public class QueenNode extends CellLocatedNode {

    /* stock de nourriture (pas de TTL, une reine fourmi peut vivre jusqua 37 ans !) */
    private int foodStock;

    public QueenNode(){
        super();
        foodStock = 10;

        setIcon("/resources/images/ant-queen.png");
        setIconSize(getIconSize()*2);
    }

    @Override
    /* production recurrent de fourmis */
    public void onClock() {
        if (shouldProduceOffspring())
            produceOffspring();
    }

    @Override
    public void onPostClock() {
        super.onPostClock();
    }

    private boolean shouldProduceOffspring() {
        /* FAIRE SPAWN PEU DE FOURMIS  :::::
        if(getTime() % 1000 == 0)
            return true;
        return false; */
        return new Random().nextDouble() < 0.01;
    }

    public void produceOffspring(){
        if(foodStock <= 0)
            die();
        foodStock--;

        AntNode babyAnt = new AntNode(this);
        babyAnt.setCurrentCell(getCurrentCell());
        getTopology().addNode(babyAnt);
    }

    /* stock de nourriture */
    public void increaseFoodStock(int value) {
        this.foodStock += value;
    }
}
