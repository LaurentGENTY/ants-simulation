package ants;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResourcesTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "/images/ant.png", "/images/ant-digging.png", "/images/ant-bean.png",
            "/images/ant-queen.png", "/images/ant-worm.png", "/images/rock.png",
            "/images/ant-soil.jpg"})
    void imageIsOnClasspath(String path) {
        assertNotNull(ResourcesTest.class.getResource(path), path);
    }

    @Test
    void noLegacyResourcePathInSources() throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            List<Path> offenders = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> read(p).contains("/resources/images/"))
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
