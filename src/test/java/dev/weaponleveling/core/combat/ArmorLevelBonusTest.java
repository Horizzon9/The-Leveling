package dev.weaponleveling.core.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArmorLevelBonusTest {
    @Test
    void levelZeroDoesNotReceiveArmorBonus() {
        assertEquals(ArmorLevelBonus.NONE, ArmorLevelBonus.calculate(0, 0.02, 2));
    }

    @Test
    void retainsFineGrainedArmorValues() {
        assertEquals(0.1, ArmorLevelBonus.calculate(5, 0.02, 2).armor(), 0.000_001);
    }

    @Test
    void capsBonusForEachArmorPiece() {
        assertEquals(2, ArmorLevelBonus.calculate(100, 0.03, 2).armor(), 0.000_001);
    }

    @Test
    void defaultsGiveOneArmorForEveryFiftyLevels() {
        assertEquals(1, ArmorLevelBonus.calculate(50, 0.02, 20).armor(), 0.000_001);
        assertEquals(2, ArmorLevelBonus.calculate(100, 0.02, 20).armor(), 0.000_001);
        assertEquals(10, ArmorLevelBonus.calculate(500, 0.02, 20).armor(), 0.000_001);
    }

    @Test
    void guaranteesOneArmorAtLevelOneHundred() {
        assertEquals(1, ArmorLevelBonus.calculate(100, 0.004, 2).armor(), 0.000_001);
        assertEquals(1, ArmorLevelBonus.calculate(100, 0, 0).armor(), 0.000_001);
    }

    @Test
    void weakLegacyRatesStillScaleWithLevelAboveTheFloor() {
        assertEquals(1.2, ArmorLevelBonus.calculate(120, 0.004, 2).armor(), 0.000_001);
    }

    @Test
    void capCannotCutBelowTheGuaranteedFloor() {
        assertEquals(5, ArmorLevelBonus.calculate(500, 0.004, 2).armor(), 0.000_001);
    }
}
