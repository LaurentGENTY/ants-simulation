package ants.capture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptureMainTest {

    private static List<Path> frames(Path dir) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> p.getFileName().toString().matches("frame_\\d{5}\\.png")).sorted().toList();
        }
    }

    @Test
    void writesOneNonEmptyFramePerInterval(@TempDir Path dir) throws IOException {
        int written = CaptureMain.capture(new CaptureOptions(42L, 50, 5, dir));
        List<Path> frames = frames(dir);
        assertEquals(10, written);
        assertEquals(10, frames.size());
        assertEquals("frame_00000.png", frames.get(0).getFileName().toString());
        for (Path f : frames) assertTrue(Files.size(f) > 1000, f + " looks empty");
    }

    @Test
    void staleFramesAreRemoved(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("frame_00099.png"), "stale");
        CaptureMain.capture(new CaptureOptions(42L, 10, 5, dir));
        assertEquals(2, frames(dir).size());
    }

    @Test
    void sameSeedGivesIdenticalFrames(@TempDir Path a, @TempDir Path b) throws IOException {
        CaptureMain.capture(new CaptureOptions(42L, 200, 100, a));
        CaptureMain.capture(new CaptureOptions(42L, 200, 100, b));
        assertArrayEquals(Files.readAllBytes(a.resolve("frame_00001.png")), Files.readAllBytes(b.resolve("frame_00001.png")));
    }
}
