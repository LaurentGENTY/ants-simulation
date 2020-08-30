package ants.environment;

import ants.actors.CellLocatedNode;

import java.util.Random;

public class RockNode extends CellLocatedNode {

    public RockNode(){
        super();
        /* initialisation de l'icone */
        setIcon("/resources/images/rock.png");
        setWirelessStatus(false);
    }
}
