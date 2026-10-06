package ants.ui;

import ants.environment.Cell;
import ants.environment.Environment;
import ants.sim.ColonyStats;
import ants.sim.SimConfig;
import ants.sim.Simulation;
import io.jbotsim.core.Node;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** Draws the whole scene; shared by the Swing window and the headless capture. */
public final class SimulationRenderer {

    private static final Color FOOD_PHEROMONE = new Color(60, 220, 90);
    private static final Color QUEEN_PHEROMONE = new Color(70, 140, 255);
    private static final double PHEROMONE_MAX_ALPHA = 0.6;
    private static final Color HUD_BACKGROUND = new Color(0, 0, 0, 160);
    private static final Font HUD_FONT = new Font(Font.MONOSPACED, Font.BOLD, 16);

    private final Simulation simulation;
    private final BufferedImage soil;
    private final Map<String, BufferedImage> icons = new HashMap<>();
    private volatile boolean pheromonesVisible = true;

    public SimulationRenderer(Simulation simulation) {
        this.simulation = simulation;
        this.soil = loadImage("/images/ant-soil.jpg");
    }

    public void togglePheromones() {
        pheromonesVisible = !pheromonesVisible;
    }

    public void setPheromonesVisible(boolean visible) {
        pheromonesVisible = visible;
    }

    public boolean pheromonesVisible() {
        return pheromonesVisible;
    }

    public BufferedImage renderFrame() {
        BufferedImage frame = new BufferedImage(SimConfig.TOPOLOGY_WIDTH, SimConfig.TOPOLOGY_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = frame.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        paintBackground(g);
        paintSprites(g);
        paintHud(g);
        g.dispose();
        return frame;
    }

    public void paintBackground(Graphics2D g) {
        g.drawImage(soil, 0, 0, null);
        Environment env = simulation.environment();
        env.forEachCell(cell -> paintCell(g, cell, env));
        if (pheromonesVisible)
            env.forEachCell(cell -> paintPheromones(g, cell, env));
    }

    private static Rectangle2D bounds(Cell cell, Environment env) {
        double w = env.getElementWidth(), h = env.getElementHeight();
        return new Rectangle2D.Double(cell.getX() - w / 2, cell.getY() - h / 2, w, h);
    }

    // Same shading as the 2020 painter: dug cells are white, harder soil is darker.
    private static void paintCell(Graphics2D g, Cell cell, Environment env) {
        double cost = cell.getCost();
        Color color = Color.WHITE;
        if (cost != Cell.MIN_COST_VALUE) {
            int alpha = Math.min(255, 10 + (int) (cost / Cell.MAX_COST_VALUE * 255));
            color = new Color(0, 0, 0, alpha);
        }
        Rectangle2D r = bounds(cell, env);
        g.setColor(color);
        g.fill(r);
        g.setColor(color.brighter());
        g.draw(r);
    }

    private static void paintPheromones(Graphics2D g, Cell cell, Environment env) {
        Rectangle2D r = bounds(cell, env);
        // Food on top: it is the trail worth seeing and it fades faster than the queen one.
        fillTinted(g, r, QUEEN_PHEROMONE, cell.getQueenPheromoneIntensity());
        fillTinted(g, r, FOOD_PHEROMONE, cell.getFoodPheromoneIntensity());
    }

    private static void fillTinted(Graphics2D g, Rectangle2D r, Color base, double intensity) {
        if (intensity <= 0)
            return;
        int alpha = (int) Math.round(Math.min(1.0, intensity) * PHEROMONE_MAX_ALPHA * 255);
        g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
        g.fill(r);
    }

    // Mirrors JBotSim's JNode: icon scaled to 2*iconSize, rotated by direction + PI/2 around its centre.
    public void paintSprites(Graphics2D g) {
        for (Node node : new ArrayList<>(simulation.topology().getNodes())) {
            String icon = node.getIcon();
            if (icon == null)
                continue;
            BufferedImage image = icons.computeIfAbsent(icon, SimulationRenderer::loadImage);
            int size = 2 * node.getIconSize();
            AffineTransform saved = g.getTransform();
            g.translate(node.getX(), node.getY());
            g.rotate(node.getDirection() + Math.PI / 2);
            g.drawImage(image, -size / 2, -size / 2, size, size, null);
            g.setTransform(saved);
        }
    }

    public void paintHud(Graphics2D g) {
        ColonyStats s = simulation.stats();
        String[] lines = {
                "tick           " + s.tick(),
                "ants           " + s.ants(),
                "queen stock    " + (s.queenAlive() ? String.valueOf(s.queenStock()) : "dead"),
                "food delivered " + s.foodDelivered()
        };
        g.setFont(HUD_FONT);
        FontMetrics fm = g.getFontMetrics();
        int width = 0;
        for (String line : lines)
            width = Math.max(width, fm.stringWidth(line));
        int lineHeight = fm.getHeight();
        int x = 12, y = 12, padding = 10;
        g.setColor(HUD_BACKGROUND);
        g.fillRoundRect(x, y, width + 2 * padding, lines.length * lineHeight + 2 * padding, 12, 12);
        g.setColor(Color.WHITE);
        for (int i = 0; i < lines.length; i++)
            g.drawString(lines[i], x + padding, y + padding + fm.getAscent() + i * lineHeight);
    }

    private static BufferedImage loadImage(String path) {
        URL url = SimulationRenderer.class.getResource(path);
        if (url == null)
            throw new IllegalStateException("Missing image resource: " + path);
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read image " + path, e);
        }
    }
}
