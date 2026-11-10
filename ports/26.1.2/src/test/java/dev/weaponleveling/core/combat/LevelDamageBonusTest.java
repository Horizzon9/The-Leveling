package dev.weaponleveling.core.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LevelDamageBonusTest {
    @Test
    void levelZeroKeepsTheOriginalDamage() {
        assertEquals(LevelDamageBonus.NONE, LevelDamageBonus.calculate(0, 100, 11, 1.65));
    }

    @Test
    void reachesConfiguredMultiplierAtTheLevelCap() {
        assertEquals(new LevelDamageBonus(11), LevelDamageBonus.calculate(100, 100, 11, 1.65));
    }

    @Test
    void keepsTheDefaultEarlyGainVisibleForASevenDamageWeapon() {
        double multiplier = LevelDamageBonus.calculate(10, 500, 11, 1.05).multiplier();
        assertEquals(1.15, 7 * (multiplier - 1), 0.02);
    }

    @Test
    void growsFasterAtHigherLevelsWithoutExceedingTheConfiguredMultiplier() {
        double levelTen = LevelDamageBonus.calculate(10, 500, 11, 1.05).multiplier();
        double levelOneHundred = LevelDamageBonus.calculate(100, 500, 11, 1.05).multiplier();
        org.junit.jupiter.api.Assertions.assertTrue(levelOneHundred > levelTen);
        org.junit.jupiter.api.Assertions.assertTrue(levelOneHundred < 11);
    }

    @Test
    void fiveHundredLevelDefaultPreservesTheBaseDamageDifference() {
        double multiplier = LevelDamageBonus.calculate(500, 500, 11, 1.05).multiplier();
        assertEquals(77, 7 * multiplier, 0.000_001);
        assertEquals(154, 14 * multiplier, 0.000_001);
    }

    @Test
    void scalesSpecialWeaponDamageOnlyAfterItsOwnCalculation() {
        LevelDamageBonus maxLevelBonus = LevelDamageBonus.calculate(500, 500, 11, 1.05);

        // A representative mace smash has already combined the mace's base damage and its fall bonus.
        assertEquals(407, maxLevelBonus.applyTo(37), 0.000_001);
        assertEquals(88, maxLevelBonus.applyTo(8), 0.000_001); // Thrown trident base impact.
        assertEquals(110, maxLevelBonus.applyTo(10), 0.000_001); // Compatible modded spear impact.
    }

    @Test
    void scalesTheDamageAlreadyAddedByVanillaEnchantments() {
        LevelDamageBonus maxLevelBonus = LevelDamageBonus.calculate(500, 500, 11, 1.05);

        // The caller provides Minecraft's post-enchantment hit amount; level scaling must not discard it.
        assertEquals(143, maxLevelBonus.applyTo(13), 0.000_001);
    }
}
