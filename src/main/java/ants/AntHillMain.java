package ants;

import ants.sim.Simulation;
import ants.ui.EnvironmentBackgroundPainter;
import ants.ui.PheromoneToggle;
import ants.ui.SimulationRenderer;
import io.jbotsim.ui.JTopology;
import io.jbotsim.ui.JViewer;

import java.util.function.LongSupplier;

public class AntHillMain {

    static final String USAGE = "Usage: AntHillMain [--seed <long>]";

    public static void main(String[] args) {
        long seed;
        try {
            seed = parseSeed(args, System::nanoTime);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(USAGE);
            System.exit(1);
            return;
        }
        // Printed so a nice-looking run can be replayed with --seed.
        System.out.println("Seed: " + seed);

        Simulation simulation = Simulation.interactive(seed);
        SimulationRenderer renderer = new SimulationRenderer(simulation);
        JViewer viewer = new JViewer(simulation.topology());
        JTopology view = viewer.getJTopology();
        view.setDefaultBackgroundPainter(new EnvironmentBackgroundPainter(renderer));
        PheromoneToggle.install(view, renderer);
        simulation.start();
    }

    static long parseSeed(String[] args, LongSupplier fallback) {
        if (args.length == 0)
            return fallback.getAsLong();
        if (args.length != 2 || !args[0].equals("--seed"))
            throw new IllegalArgumentException("Unexpected arguments: " + String.join(" ", args));
        try {
            return Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("--seed expects an integer, got \"" + args[1] + "\"");
        }
    }
}
