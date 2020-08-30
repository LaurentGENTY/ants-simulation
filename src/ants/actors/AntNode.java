package ants.actors;

import ants.environment.Cell;
import ants.environment.FoodNode;
import ants.comparators.FoodPheromoneComparator;
import ants.comparators.QueenPheromoneComparator;

import io.jbotsim.core.Node;
import io.jbotsim.core.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class AntNode extends WaypointNode {

    /* differents icones pour les etats */
    private static final String NORMAL_ICON = "/resources/images/ant.png";
    private static final String DIGGING_ICON = "/resources/images/ant-digging.png";
    private static final String HAVING_FOOD_ICON = "/resources/images/ant-bean.png";

    /* temps de survie mini et maxi de la fourmi */
    private static final int MIN_TTL = 500;
    private static final int MAX_TTL = 1000;
    private int TTL;

    /* temps de creusage de tunnel */
    private int timeDigging;

    /* booleen d'états*/
    private boolean carryingFood;
    private boolean droppedFood;
    private boolean foundFood;
    private boolean digging;

    /* quantité de nourriture portée (changement du sujet : elle peu en porter UN PEU (1 de nourriture) ou BEAUCOUP (2) */
    private int carriedQuantity;
    private static final int MAX_QUANTITY = 2;

    /* quantite de pheromone ajouté */
    private static final double FOOD_PHEROMONE_QUANTITY = 0.1;
    private static final double QUEEN_PHEROMONE_QUANTITY = 0.1;

    /* reine mere à qui deposer de la nourriture */
    private QueenNode queen;

    /* connaissance de son environnement DIRECT (dans la sensing range) */
    private ArrayList<Node> sensedNodes;
    /* voisins de la case courante (à ne pas confondre avec sensed) */
    private ArrayList<Cell> neighbors;

    /* environnement direct trié selon l'intensité des pheromones */
    private ArrayList<Cell> neighborsSortedFood;
    private ArrayList<Cell> neighborsSortedQueen;

    /* dernières positions de la fourmi : eviter le death spiral en repassant par les cases précédentes */
    private Cell nextCell;
    private Cell previousCell;
    private Cell previousPreviousCell;

    public AntNode(QueenNode mother) {
        super();

        /* initialiser position initiale */
        this.queen = mother;

        this.nextCell = null;
        this.previousCell = null;
        this.previousPreviousCell = null;

        this.sensedNodes = new ArrayList<Node>();
        this.neighbors = new ArrayList<Cell>();
        this.neighborsSortedFood = new ArrayList<Cell>();
        this.neighborsSortedQueen = new ArrayList<Cell>();

        /* temps de vie de la fourmi */
        this.TTL = new Random().nextInt(MAX_TTL) + MIN_TTL;

        /* initialisation des booleans d'états */
        this.carryingFood = false;
        this.timeDigging = 0;
        this.digging = false;
        this.carriedQuantity = 0;
        setIcon(NORMAL_ICON);

        /* portee de captation des autres noeuds */
        setSensingRange(40);
    }

    @Override
    public void onStart() {
        super.onStart();

        /* on se positionne à la cell courante */
        setLocation(currentCell);
        previousCell = currentCell;

        /* on demarre l'algo des le start : sinon nextCell est null et cela ne marche pas (TODO : corriger cela ???) */
        antAlgorithm();
    }

    @Override
    public void onClock() {
        super.onClock();

        /* si la fourmi n'a plus de temps de dispo : meurt */
        if (TTL <= 0)
            die();
        else
            TTL--;

        /* si on est pas en train de creuser */
        if (digging) {
            /* on a fini de creuser on peut aller a la destination */
            if (nextCell.isDug()) {
                /* on revient à la fourmi de base ou celle qui porte de la nourriture */
                timeDigging = 0;
                digging = false;
                if (carryingFood)
                    setIcon(HAVING_FOOD_ICON);
                else
                    setIcon(NORMAL_ICON);

                /* on se dirige vers la cellule */
                addDestination(nextCell);
            }
            /* on a pas fini (50 onClock) donc on continue de creuser */
            else
                dig(nextCell);
        }
    }

    /* fourmi creuse une cellule */
    public void dig(Cell c) {
        timeDigging++;
        /* on augmente le temps de creusage (en diminuant la variable) (si la cell vient de finir d'etre creusee alors la methode va changer son booleen d'état */
        c.decrementTimeDigging();
        /* on change la "couleur" selon le temps quon a creuse */
        c.setCost(c.convertTimeDiggingCost());
        return;
    }

    /* choisi une cellule aleatoire dans les 8 cases autour de celle courante */
    public void pickRandomDestination() {
        /* une fois arrivee a destination, la fourmi prends une case aléatoire adjacente
         * et l'ajoute a ses destinations */
        Cell cell = pickNeighBoringCell();
        addDestination(cell);
    }

    protected Cell pickNeighBoringCell() {
        Cell nextCell = null;
        Random random = new Random();
        int rInt;

        while (nextCell == null || nextCell.isRock()) {
            rInt = random.nextInt(8);

            switch (rInt) {
                case 0:
                    nextCell = getCurrentCell().getTopNeighbor();
                    break;
                case 1:
                    nextCell = getCurrentCell().getTopRightNeighbor();
                    break;
                case 2:
                    nextCell = getCurrentCell().getRightNeighbor();
                    break;
                case 3:
                    nextCell = getCurrentCell().getBottomRightNeighbor();
                    break;
                case 4:
                    nextCell = getCurrentCell().getBottomNeighbor();
                    break;
                case 5:
                    nextCell = getCurrentCell().getBottomLeftNeighbor();
                    break;
                case 6:
                    nextCell = getCurrentCell().getLeftNeighbor();
                    break;
                case 7:
                    nextCell = getCurrentCell().getTopLeftNeighbor();
                    break;
                default:
                    /* erreur */
                    break;
            }
        }
        return nextCell;
    }

    @Override
    /* arrivé à destination : on effectue les actions de la fourmis (on aurait pu mettre tout l'algo dans onArrivel) */
    public void onArrival() {
        antAlgorithm();
    }

    /* algorithme de décision d'une fourmi selon son environnement direct et son état (booleens) */
    protected void antAlgorithm() {
        /* si on est en train de creuser on continue de creuser (on ne va pas bouger et onClock va creuser) */
        if (digging)
            return;

        /* sinon cela veut dire que nous ne creusons et donc que nous pouvons rechercher de la nourriture ou alors donner a la reine */

        /* dans tous les cas on ajoute une pheromone pour la reine */
        currentCell.incrementQueenPheromoneIntensity(QUEEN_PHEROMONE_QUANTITY);

        /* on sent les noeuds autour */
        sensedNodes.clear(); //sensedNodes.removeAll(sensedNodes) pas opti
        /* obligé d'instancier une nouvelle fois snesedNodes car il s'agit d'une ArrayList et getSensedNodes de la classe Node renvoit une List */
        sensedNodes = new ArrayList<Node>(getSensedNodes());

        /* liste des cell autour de la notre afin de pouvoir aller dans la meilleure direction */
        neighbors = currentCell.getAllNeighbors();
        neighborsSortedFood = sortByFoodIntensity(neighbors);

        if (!carryingFood) {
            for (Node n : sensedNodes) {
                /* si on est sur de la nourriture on l'a prend */
                if (isOnNode(n)) {
                    if (n instanceof FoodNode) {
                        if (((FoodNode) n).hasFood()) {
                            takeFood((FoodNode) n);
                            currentCell.incrementFoodPheromoneIntensity(FOOD_PHEROMONE_QUANTITY);
                            returnToQueen();
                            return;
                        }
                    }
                }

                /* si la fourmi detecte de la nourriture a cote : depose une pheromone et va sur la case de la nourriture
                * prochain tour de boucle onArrival va verifier qu'on soit dessus et prendre la nourriture */
                if (n instanceof FoodNode && distance(n) < 39) {
                    addDestination(((FoodNode) n).getCurrentCell());
                    return;
                }
            }

            /* si je suis ici : pas de nourriture à proximite donc je suis les pheromones de nourriture les plus intenses */
            for (Cell cell : neighborsSortedFood) {
                if (cell.getFoodPheromoneIntensity() > 0) {
                    /* si elle vient de donner la nourriture à la reine elle repart en arriere */
                    if (droppedFood) {
                        droppedFood = false;
                        addDestination(cell);
                        return;
                    }
                    /* eviter le death spiral */
                    if (!cell.equals(previousCell) && !cell.equals(previousPreviousCell)) {
                        addDestination(cell);
                        return;
                    }
                }
            }

            /* si je suis ici : aucune pheromone de nourriture posée autour -> choix aléatoire de direction */
            pickRandomDestination();
            return;
        }

        /* la fourmi a de la nourriture */
        else {
            for (Node n : sensedNodes) {
                /* elle est sur la reine et depose la nourriture */
                if (n instanceof QueenNode && isOnNode(n)) {
                    dropFood();
                    /* on repart chercher de la nourriture */
                    antAlgorithm();
                    return;
                }
            }
            returnToQueen();
        }

    }

    @Override
    /* aller à la destination : verifier si la prochaine cell est creusée, si non la creuser, si oui y aller */
    public void addDestination(Cell destination) {
        /* la prochaine cellule est celle sur laquelle on souhaite aller */

        /* on se deplace (on affecte les cellules précédentes) */
        this.previousPreviousCell = this.previousCell;
        this.previousCell = this.currentCell;
        this.nextCell = destination;

        /* si la fourmi ne creuse pas : soit elle y va car la cellule est creuser soit elle doit la creuser */
        if (!digging) {
            /* si la cellule est deja creusee */
            if (destination.isDug()) {
                setCurrentCell(destination);
                super.addDestination(destination);
            } /* si la cellule n'est pas creusee il faut le faire */ else {
                setDirection(nextCell);
                digging = true;
                setIcon(DIGGING_ICON);
            }
        } else
            System.err.println("Erreur : addDestination appelé pendant le creusage");
    }

    /* methode permettant de suivre le chmin vers la reine */
    private void returnToQueen() {

        /* dans tous les cas je mets une pheromone sur ma case */
        currentCell.incrementFoodPheromoneIntensity(FOOD_PHEROMONE_QUANTITY);

        /* recupere les cellules adjacentes afin de savoir ou se diriger */
        neighbors = currentCell.getAllNeighbors();
        neighborsSortedQueen = sortByQueenIntensity(neighbors);

        /* si on a tout juste trouve de la nourriture on revient en arriere */
        if (foundFood) {
            foundFood = false;
            addDestination(previousCell);
            returnToQueen();
            return;
        }

        /* si on trouve la reine dans les cellules voisines */
        for (Node n : sensedNodes) {
            if (n instanceof QueenNode && n.equals(queen)) {
                currentCell.incrementQueenPheromoneIntensity(QUEEN_PHEROMONE_QUANTITY);
                addDestination(queen.getCurrentCell());
                return;
            }
        }

        /* trouver la reine par les pheromones de reine voisines */
        for (Cell cell : neighborsSortedQueen) {
            /* si une case possede des pheromones pour aller vers la reine et qu'il ne s'agit pas des cases précedentes (eviter le death spiral)
             * TODO : le cas du death spiral n'est pas corrigé : si on prends beaucoup de nourriture alors on posera beaucoup de pheromone et donc on bouclera sans arret */
            if (cell.getQueenPheromoneIntensity() > 0 && !cell.equals(previousCell) && !cell.equals(previousPreviousCell)) {
                addDestination(cell);
                return;
            }
        }

        /* prends une case au hasard */
        pickRandomDestination();
        return;
    }

    /* méthodes pour la nourriture (drop et take) */
    public void takeFood(FoodNode node) {
        System.out.println("Fourmi " + this.getID() + " prends de la nourriture " + node.getID() + ", reste " + node.getQuantity());
        /* la fourmi prend de la nourriture */
        carryingFood = true;
        foundFood = true;
        setIcon(HAVING_FOOD_ICON);

        /* prends SOIT un peu de nourriture (1) ou BEAUCOUP (2) (si elle est musclée elle peut en prendre beaucoup !) */
        /* et c'est plus rapide pour réduire toute la nourriture d'une cellule ! */
        this.carriedQuantity = (new Random()).nextInt(MAX_QUANTITY) + 1;
        node.setQuantity(node.getQuantity() - carriedQuantity);
    }

    public void dropFood() {
        System.out.println("Fourmi " + this.getID() + " pose " + carriedQuantity + " nourriture a la reine " + queen.getID());
        /* augmente la quantité de nourriture de ce qu'on a pris */
        queen.increaseFoodStock(carriedQuantity);

        /* quantite portee revient à 0 */
        carriedQuantity = 0;

        /* on depose la nourriture */
        carryingFood = false;
        droppedFood = true;
        setIcon(NORMAL_ICON);
        return;
    }

    /* méthodes permettant de trier les cell selon les pheromones */
    /* TODO : ne serait-ce pas plus judicieux de renvoyer une seule cellule avec phéromone > 0 (si elle existe) (car au final
    *   si elle existe on s'y dirige... donc ???) */
    private ArrayList<Cell> sortByFoodIntensity(ArrayList<Cell> neighbors) {
        ArrayList<Cell> neighborsSortedFood = new ArrayList<Cell>(neighbors);
        Collections.sort(neighborsSortedFood, new FoodPheromoneComparator());
        return neighborsSortedFood;
    }

    private ArrayList<Cell> sortByQueenIntensity(ArrayList<Cell> neighbors) {
        ArrayList<Cell> neighborsSortedQueen = new ArrayList<Cell>(neighbors);
        Collections.sort(neighborsSortedQueen, new QueenPheromoneComparator());
        return neighborsSortedQueen;
    }

    private boolean isOnNode(Node n) {
        return distance(n) <= 1;
    }
}
