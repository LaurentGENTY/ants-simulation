package ants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntHillMainTest {

    @Test
    void noArgumentUsesFallbackSeed() {
        assertEquals(99L, AntHillMain.parseSeed(new String[0], () -> 99L));
    }

    @Test
    void seedFlagIsParsed() {
        assertEquals(42L, AntHillMain.parseSeed(new String[]{"--seed", "42"}, () -> 99L));
    }

    @Test
    void invalidSeedNamesTheFlag() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> AntHillMain.parseSeed(new String[]{"--seed", "abc"}, () -> 99L));
        assertTrue(e.getMessage().contains("--seed"), e.getMessage());
    }

    @Test
    void unknownArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> AntHillMain.parseSeed(new String[]{"--speed", "2"}, () -> 99L));
    }
}
