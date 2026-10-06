package ants.capture;

import java.nio.file.Files;
import java.nio.file.Path;

/** Validated command-line options of {@link CaptureMain}. */
public record CaptureOptions(long seed, int ticks, int every, Path out) {

    public static final String USAGE =
            "Usage: CaptureMain [--seed <long>] [--ticks <n>] [--every <n>] [--out <dir>]";

    public static CaptureOptions parse(String[] args) {
        long seed = 42L;
        int ticks = 3000;
        int every = 5;
        Path out = Path.of("build/capture/frames");
        for (int i = 0; i < args.length; i += 2) {
            String flag = args[i];
            if (i + 1 >= args.length)
                throw new IllegalArgumentException("Missing value for " + flag);
            String value = args[i + 1];
            switch (flag) {
                case "--seed" -> seed = parseLong(flag, value);
                case "--ticks" -> ticks = parseInt(flag, value);
                case "--every" -> every = parseInt(flag, value);
                case "--out" -> out = Path.of(value);
                default -> throw new IllegalArgumentException("Unknown option: " + flag);
            }
        }
        if (ticks <= 0)
            throw new IllegalArgumentException("--ticks must be > 0");
        if (every <= 0)
            throw new IllegalArgumentException("--every must be > 0");
        if (every > ticks)
            throw new IllegalArgumentException("--every must be <= --ticks");
        if (Files.exists(out) && !Files.isDirectory(out))
            throw new IllegalArgumentException("--out must be a directory: " + out);
        return new CaptureOptions(seed, ticks, every, out);
    }

    private static long parseLong(String flag, String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(flag + " expects an integer, got \"" + value + "\"");
        }
    }

    private static int parseInt(String flag, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(flag + " expects an integer, got \"" + value + "\"");
        }
    }
}
