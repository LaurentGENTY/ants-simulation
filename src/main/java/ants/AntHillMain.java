package ants;

import ants.sim.Simulation;
import ants.ui.EnvironmentBackgroundPainter;
import ants.ui.SimulationRenderer;
import io.jbotsim.ui.JTopology;
import io.jbotsim.ui.JViewer;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class AntHillMain {

    public static void main(String[] args) {
        long seed = args.length == 2 && args[0].equals("--seed") ? Long.parseLong(args[1]) : System.nanoTime();
        // Printed so a nice-looking run can be replayed with --seed.
        System.out.println("Seed: " + seed);

        Simulation simulation = Simulation.interactive(seed);
        SimulationRenderer renderer = new SimulationRenderer(simulation);
        JViewer viewer = new JViewer(simulation.topology());
        JTopology view = viewer.getJTopology();
        view.setDefaultBackgroundPainter(new EnvironmentBackgroundPainter(renderer));
        view.setFocusable(true);
        view.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    renderer.togglePheromones();
                    view.repaint();
                }
            }
        });
        view.requestFocusInWindow();
        simulation.start();
    }
}
