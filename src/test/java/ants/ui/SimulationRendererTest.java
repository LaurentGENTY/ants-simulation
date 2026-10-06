package ants.ui;

import ants.environment.Cell;
import ants.sim.SimConfig;
import ants.sim.Simulation;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationRendererTest {

    private static Simulation startedSim() {
        Simulation sim = Simulation.manual(42L);
        sim.start();
        return sim;
    }

    private static BufferedImage background(SimulationRenderer renderer) {
        BufferedImage img = new BufferedImage(SimConfig.TOPOLOGY_WIDTH, SimConfig.TOPOLOGY_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        renderer.paintBackground(g);
        g.dispose();
        return img;
    }

    @Test
    void frameHasTopologySize() {
        BufferedImage frame = new SimulationRenderer(startedSim()).renderFrame();
        assertEquals(SimConfig.TOPOLOGY_WIDTH, frame.getWidth());
        assertEquals(SimConfig.TOPOLOGY_HEIGHT, frame.getHeight());
    }

    @Test
    void foodPheromoneTintsItsCellGreenOnlyWhenVisible() {
        Simulation sim = startedSim();
        Cell cell = sim.environment().getElement(25, 20);
        cell.incrementFoodPheromoneIntensity(SimConfig.PHEROMONE_MAX);
        int x = (int) cell.getX(), y = (int) cell.getY();

        SimulationRenderer renderer = new SimulationRenderer(sim);
        renderer.setPheromonesVisible(false);
        Color hidden = new Color(background(renderer).getRGB(x, y));
        renderer.togglePheromones();
        assertTrue(renderer.pheromonesVisible());
        Color shown = new Color(background(renderer).getRGB(x, y));

        // Compare green relative to red: the cell may be white (dug) or dark soil underneath.
        assertTrue(shown.getGreen() - shown.getRed() > hidden.getGreen() - hidden.getRed() + 40,
                "expected a green tint, hidden=" + hidden + " shown=" + shown);
    }

    @Test
    void rendersWhenQueenIsDead() {
        Simulation sim = startedSim();
        sim.queen().die();
        sim.step();
        assertFalse(sim.stats().queenAlive());
        assertDoesNotThrow(() -> new SimulationRenderer(sim).renderFrame());
    }
}
