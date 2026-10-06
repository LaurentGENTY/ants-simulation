package ants.capture;

import ants.sim.ColonyStats;
import ants.sim.Simulation;
import ants.ui.SimulationRenderer;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/** Runs the simulation headless with a fixed seed and writes one PNG every N ticks. */
public final class CaptureMain {

    private CaptureMain() {
    }

    public static void main(String[] args) throws IOException {
        CaptureOptions options;
        try {
            options = CaptureOptions.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(CaptureOptions.USAGE);
            System.exit(1);
            return;
        }
        int frames = capture(options);
        System.out.println("Wrote " + frames + " frames to " + options.out());
    }

    public static int capture(CaptureOptions options) throws IOException {
        Files.createDirectories(options.out());
        deleteOldFrames(options.out());

        Simulation simulation = Simulation.manual(options.seed());
        simulation.start();
        SimulationRenderer renderer = new SimulationRenderer(simulation);

        int written = 0;
        for (int tick = 1; tick <= options.ticks(); tick++) {
            simulation.step();
            if (tick % options.every() == 0) {
                Path file = options.out().resolve(String.format("frame_%05d.png", written));
                ImageIO.write(renderer.renderFrame(), "png", file.toFile());
                written++;
            }
        }
        ColonyStats s = simulation.stats();
        // Printed to compare seeds when picking the showcase run.
        System.out.printf("seed=%d ants=%d delivered=%d queenAlive=%b%n",
                options.seed(), s.ants(), s.foodDelivered(), s.queenAlive());
        return written;
    }

    // A shorter re-run must not leave frames from a longer one behind (ffmpeg would include them).
    private static void deleteOldFrames(Path dir) throws IOException {
        List<Path> old;
        try (Stream<Path> files = Files.list(dir)) {
            old = files.filter(p -> p.getFileName().toString().matches("frame_\\d+\\.png")).toList();
        }
        for (Path p : old)
            Files.delete(p);
    }
}
