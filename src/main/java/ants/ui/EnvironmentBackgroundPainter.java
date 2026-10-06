package ants.ui;

import io.jbotsim.core.Topology;
import io.jbotsim.ui.painting.JBackgroundPainter;
import io.jbotsim.ui.painting.UIComponent;

import java.awt.Graphics2D;

/** JBotSim adapter: the window paints background and HUD, JBotSim paints the sprites on top. */
public class EnvironmentBackgroundPainter extends JBackgroundPainter {

    private final SimulationRenderer renderer;

    public EnvironmentBackgroundPainter(SimulationRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void paintBackground(UIComponent c, Topology topology) {
        Graphics2D g = (Graphics2D) c.getComponent();
        renderer.paintBackground(g);
        renderer.paintHud(g);
    }
}
