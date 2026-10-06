package ants.environment;

import ants.sim.SimRandom;

import ants.actors.CellLocatedNode;


public class FoodNode extends CellLocatedNode {

    /* quantite que les fourmis peuvent prendre */
    public static final int MIN_QUANTITY = 10;
    private int foodQuantity;

    /* temps de survie mini et maxi de la nourriture */
    private static final int MIN_TTL = 1000;
    private static final int MAX_TTL = 5000;
    public int TTL;

    public FoodNode(){
        super();
        setWirelessStatus(false);

        setDirection(SimRandom.get().nextDouble()*2*Math.PI);

        /* initialisation de la nourriture */
        foodQuantity = SimRandom.get().nextInt(MIN_QUANTITY) + MIN_QUANTITY;
        TTL = SimRandom.get().nextInt(MAX_TTL) + MIN_TTL;

        /* initialisation de l'icone */
        setIcon("/images/ant-worm.png");
        setIconSize((int)(getIconSize()* foodQuantity /10*0.9));
    }

    @Override
    public void onPostClock() {
        /* suppresion de la nourriture au bout de TTL rounds */
        TTL--;
        if(TTL <= 0)
            die();

        super.onPostClock();
    }

    public void setQuantity(int foodQuantity) {
        this.foodQuantity = foodQuantity;
        /* suppression de la nourriture si le stock est vide */
        if (foodQuantity <= 0)
            die();

        return;
    }

    public int getQuantity(){
        return foodQuantity;
    }

    public boolean hasFood() {
        return this.getQuantity() >= 1;
    }

}
