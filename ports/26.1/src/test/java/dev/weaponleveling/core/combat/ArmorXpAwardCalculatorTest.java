package dev.weaponleveling.core.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArmorXpAwardCalculatorTest {
    @Test
    void awardsXpOnlyForRealDamage() {
        assertEquals(0, ArmorXpAwardCalculator.calculate(0, 1, 100));
        assertEquals(0, ArmorXpAwardCalculator.calculate(-1, 1, 100));
        assertEquals(3, ArmorXpAwardCalculator.calculate(2.1, 1, 100));
    }

    @Test
    void appliesTheConfiguredPerPieceCap() {
        assertEquals(25, ArmorXpAwardCalculator.calculate(100, 1, 25));
    }

    @Test
    void awardsXpForTinyPositiveDamageAfterMitigation() {
        assertEquals(1, ArmorXpAwardCalculator.calculate(0.01, 1, 10));
    }
}
