package ants.environment;

import io.jbotsim.core.Point;
import io.jbotsim.core.Topology;

import java.util.Random;
import java.util.Vector;

public class Environment {
    private Vector<Vector> env;
    private Topology tp;
    private final int nbColumn;
    private final int nbRow;
    private Point gridStartLocation;
    private final double elementWidth;
    private final double elementHeight;
    private final Random locationRandom;

    public Environment(Topology tp, int nbColumn, int nbRow) {
        this.tp = tp;
        this.nbColumn = nbColumn;
        this.nbRow = nbRow;
        elementWidth = tp.getWidth()/nbColumn;
        double usedHeight = tp.getHeight() * 2. / 3;
        elementHeight = usedHeight / nbRow;

        locationRandom = new Random();

        createEnvironment();
    }

    private void createEnvironment() {

        env = new Vector<>(nbColumn);

        double gridStartY = tp.getHeight() * 1. / 3 + elementHeight /2;
        double gridStartX = 0 + elementWidth /2;
        gridStartLocation = new Point(gridStartX, gridStartY);

        createCells(elementWidth, elementHeight);

        updateNeighborhood();
    }

    private void createCells(double width, double height) {
        for(int x = 0; x < nbColumn; x++)
            for(int y = 0; y < nbRow; y++)
                addCell(x, y, createCell(x, y, width, height));
    }

    private void updateNeighborhood() {
        for(int x = 0; x < nbColumn; x++)
            for(int y = 0; y < nbRow; y++)
                updateNeighbors(x, y);
    }

    private void updateNeighbors(int x, int y) {
        Cell current = getElement(x, y);

        if(x>0)
            current.setLefNeighbor(getElement(x-1, y));

        if(x>0 && y>0)
            current.setTopLefNeighbor(getElement(x-1, y-1));

        if(x>0 && y<nbRow-1)
            current.setBottomLeftNeighbor(getElement(x-1, y+1));

        if(y>0)
            current.setTopNeighbor(getElement(x, y-1));

        if(x<nbColumn-1)
            current.setRightNeighbor(getElement(x+1, y));

        if(x<nbColumn-1 && y>0)
            current.setTopRightNeighbor(getElement(x+1, y-1));

        if(x<nbColumn-1 && y<nbRow-1)
            current.setBottomRightNeighbor(getElement(x+1, y+1));

        if(y<nbRow-1)
            current.setBottomNeighbor(getElement(x, y+1));
    }

    public Cell getElement(int x, int y) {
        return (Cell) env.get(x).get(y);
    }

    private void addCell(int x, int y, Cell value) {
        Vector column = null;
        if(env.size()<=x) {
            column = new Vector<Cell>(nbRow);
            env.add(x, column);
        }
        column = env.get(x);

        column.add(y, value);

    }

    private Cell createCell(int x, int y, double width, double height) {
        Point location = new Point(gridStartLocation.getX() + x * width, gridStartLocation.getY() + y * height);
        Cell cell = new Cell(location);
        return cell;
    }

    public Vector<Vector> getGrid() {
        return env;
    }

    public double getElementWidth() {
        return elementWidth;
    }
    public double getElementHeight() {
        return elementHeight;
    }

    public Cell getRandomLocation() {
        return getElement(locationRandom.nextInt(nbColumn-2)+1, locationRandom.nextInt(nbRow-2)+1);
    }

    /* renvoit une cellule aleatoire compte tenu de la profondeur */
    public Cell getRandomLocationDepth(double height, int derivative) {
        Random r = new Random();
        double a = (nbRow - 2) * height;
        int b = (int) Math.floor(a);
        int rows = (int) Math.round(r.nextGaussian() * derivative) + b;
        if (rows > nbRow - 2)
            rows = nbRow - 2;
        else if (rows < 1)
            rows = 1;
        return getElement(locationRandom.nextInt(nbColumn-2)+1, rows + 1);
    }

    public int getNbColumn() {
        return nbColumn;
    }

    public int getNbRow() {
        return nbRow;
    }

}
