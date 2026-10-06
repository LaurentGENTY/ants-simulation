package ants.ui;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Binds P to show/hide the pheromone overlay. */
public final class PheromoneToggle {

    private static final String ACTION_KEY = "togglePheromones";

    private PheromoneToggle() {
    }

    public static void install(JComponent view, SimulationRenderer renderer) {
        // Window-level binding: a plain KeyListener only fires once the panel itself has focus.
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), ACTION_KEY);
        view.getActionMap().put(ACTION_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                renderer.togglePheromones();
                view.repaint();
            }
        });
    }
}
