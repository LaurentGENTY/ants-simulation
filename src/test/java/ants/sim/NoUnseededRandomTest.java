package ants.sim;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoUnseededRandomTest {

    // Any private Random breaks reproducible captures: everything must go through SimRandom.
    @Test
    void onlySimRandomCreatesRandom() throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            List<Path> offenders = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.endsWith("SimRandom.java"))
                    .filter(p -> read(p).contains("new Random("))
                    .toList();
            assertEquals(List.of(), offenders);
        }
    }

    private static String read(Path p) {
        try {
            return Files.readString(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
