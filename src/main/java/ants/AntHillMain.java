package ants;

import ants.sim.Simulation;
import ants.ui.EnvironmentBackgroundPainter;
import io.jbotsim.ui.JViewer;

public class AntHillMain {

    public static void main(String[] args) {
        long seed = args.length == 2 && args[0].equals("--seed") ? Long.parseLong(args[1]) : System.nanoTime();
        // Printed so a nice-looking run can be replayed with --seed.
        System.out.println("Seed: " + seed);

        Simulation simulation = Simulation.interactive(seed);
        JViewer viewer = new JViewer(simulation.topology());
        viewer.getJTopology().setDefaultBackgroundPainter(
                new EnvironmentBackgroundPainter(simulation.topology(), simulation.environment()));
        simulation.start();
    }
}
