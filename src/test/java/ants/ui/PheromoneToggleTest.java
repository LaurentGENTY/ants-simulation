package ants.ui;

import ants.sim.Simulation;
import org.junit.jupiter.api.Test;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PheromoneToggleTest {

    // Bound for the whole window, so P works without first clicking the simulation panel.
    @Test
    void pKeyAnywhereInWindowTogglesPheromones() {
        Simulation sim = Simulation.manual(1L);
        SimulationRenderer renderer = new SimulationRenderer(sim);
        JPanel view = new JPanel();
        PheromoneToggle.install(view, renderer);

        Object key = view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .get(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
        assertNotNull(key, "P must be bound at window level");
        Action action = view.getActionMap().get(key);

        assertTrue(renderer.pheromonesVisible());
        action.actionPerformed(null);
        assertFalse(renderer.pheromonesVisible());
        action.actionPerformed(null);
        assertTrue(renderer.pheromonesVisible());
    }
}
