package ants.capture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void parsesAllOptions() {
        CaptureOptions o = CaptureOptions.parse(new String[]{"--seed", "7", "--ticks", "100", "--every", "10", "--out", "/tmp/x"});
        assertEquals(new CaptureOptions(7L, 100, 10, Path.of("/tmp/x")), o);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "--every 0", "--ticks 0", "--ticks abc", "--seed 1.5",
            "--ticks", "--bogus 1", "--ticks 10 --every 20"})
    void rejectsInvalidArguments(String line) {
        assertThrows(IllegalArgumentException.class, () -> CaptureOptions.parse(line.split(" ")));
    }
}
