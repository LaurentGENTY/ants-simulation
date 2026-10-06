package ants.capture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptureOptionsTest {

    @Test
    void defaults() {
        CaptureOptions o = CaptureOptions.parse(new String[0]);
        assertEquals(42L, o.seed());
        assertEquals(3000, o.ticks());
        assertEquals(5, o.every());
        assertEquals(Path.of("build/capture/frames"), o.out());
    }

    @Test
    void parsesAllOptions(@TempDir Path dir) {
        Path out = dir.resolve("frames");
        CaptureOptions o = CaptureOptions.parse(new String[]{"--seed", "7", "--ticks", "100", "--every", "10", "--out", out.toString()});
        assertEquals(new CaptureOptions(7L, 100, 10, out), o);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "--every 0", "--ticks 0", "--ticks abc", "--seed 1.5",
            "--ticks", "--bogus 1", "--ticks 10 --every 20"})
    void rejectsInvalidArguments(String line) {
        assertThrows(IllegalArgumentException.class, () -> CaptureOptions.parse(line.split(" ")));
    }

    @Test
    void numberErrorNamesTheFlag() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> CaptureOptions.parse(new String[]{"--ticks", "abc"}));
        assertTrue(e.getMessage().contains("--ticks"), e.getMessage());
    }

    @Test
    void outMustNotBeAFile(@TempDir Path dir) throws IOException {
        Path file = Files.writeString(dir.resolve("not-a-dir"), "x");
        assertThrows(IllegalArgumentException.class,
                () -> CaptureOptions.parse(new String[]{"--out", file.toString()}));
    }
}
