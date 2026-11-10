package dev.weaponleveling.core.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XpAwardCalculatorTest {
    @Test
    void capsKillAward() {
        assertEquals(25, XpAwardCalculator.calculate(true, true, 80,
                1, 1, 1, 25));
    }

    @Test
    void disabledKillXpDoesNotAwardXp() {
        assertEquals(0, XpAwardCalculator.calculate(false, true, 8,
                1, 1, 1, 100));
    }

    @Test
    void onlyAConfirmedKillAwardsXp() {
        assertEquals(5, XpAwardCalculator.calculate(true, true, 5,
                1, 1, 1, 100));
        assertEquals(0, XpAwardCalculator.calculate(true, false, 5,
                1, 1, 1, 100));
    }
}
