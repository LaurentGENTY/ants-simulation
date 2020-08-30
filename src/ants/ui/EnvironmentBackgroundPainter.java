package ants.ui;

import ants.environment.Environment;
import ants.environment.Cell;
import io.jbotsim.core.Topology;
import io.jbotsim.ui.painting.JBackgroundPainter;
import io.jbotsim.ui.painting.UIComponent;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.List;
import java.util.*;

public class EnvironmentBackgroundPainter extends JBackgroundPainter {

    private Topology topology;
    private Environment environment;

    private Map<Cell, List<Point>> areas = new HashMap<>();
    public EnvironmentBackgroundPainter(Topology topology, Environment environment) {
        this.topology = topology;

        this.environment = environment;

        initAreas();
    }

    private void initAreas() {
        Vector<Vector> grid = environment.getGrid();
        for (Vector vector : grid)
            for (Cell cell : (Vector<Cell>)vector)
                areas.put(cell, createArea(cell));
    }
    private List<Point> createArea(Cell n) {
        List<Point> points = new ArrayList<>();

        int centerX = (int) n.getX();
        int centerY = (int) n.getY();
        int halfSpacingX = (int) (environment.getElementWidth() / 2);
        int halfSpacingY = (int) (environment.getElementHeight() / 2);
        points.add(new Point(centerX - halfSpacingX, centerY - halfSpacingY));
        points.add(new Point(centerX + halfSpacingX, centerY - halfSpacingY));
        points.add(new Point(centerX + halfSpacingX, centerY + halfSpacingY));
        points.add(new Point(centerX - halfSpacingX, centerY + halfSpacingY));

        return points;
    }

    @Override
    public void paintBackground(UIComponent c, Topology topology) {
        Graphics2D g = (Graphics2D) c.getComponent();
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image image = tk.getImage(getClass().getResource("/resources/images/ant-soil.jpg"));
        g.drawImage(image, 0, 0, null);

        drawMap(g);
    }

    private void drawMap(Graphics2D g2d) {
        for (Map.Entry<Cell, List<Point>> listEntry : areas.entrySet())
            drawEntry(g2d, listEntry.getKey(), listEntry.getValue());
    }

    private void drawEntry(Graphics2D g2d, Cell key, List<Point> points) {

        double wallValue = key.getCost();

        Color color = Color.WHITE;
        if (wallValue != Cell.MIN_COST_VALUE) {
            int minAlpha = 10;
            int alpha = minAlpha + (int) (wallValue / Cell.MAX_COST_VALUE * 255);
            alpha = alpha > 255 ? 255 : alpha;
            Color initialColor = Color.BLACK;
            color = new Color(initialColor.getRed(), initialColor.getGreen(), initialColor.getBlue(), alpha);
        }
        Point firstP = points.get(0);

        GeneralPath filledPolygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.size());
        filledPolygon.moveTo(firstP.getX(), firstP.getY());
        for(Point p: points)
            if(p != firstP)
                filledPolygon.lineTo(p.getX(), p.getY());

        filledPolygon.closePath();
        g2d.setColor(color);
        g2d.fill(filledPolygon);
        g2d.setColor(g2d.getColor().brighter());
        g2d.draw(filledPolygon);
    }


}
