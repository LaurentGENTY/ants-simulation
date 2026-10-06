package ants.sim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;

class SimRandomTest {

    private static int[] draw(long seed) {
        SimRandom.reseed(seed);
        int[] values = new int[20];
        for (int i = 0; i < values.length; i++) values[i] = SimRandom.get().nextInt(1000);
        return values;
    }

    @Test
    void sameSeedGivesSameSequence() {
        assertArrayEquals(draw(42L), draw(42L));
    }

    @Test
    void differentSeedsGiveDifferentSequences() {
        assertFalse(Arrays.equals(draw(1L), draw(2L)));
    }
}
