package dev.weaponleveling.core.chronicle;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FoeMasteryAndStreakTest {
    private static final KillStreak.Rules RULES = new KillStreak.Rules(120, 0.1, 0.5);

    @Test
    void tiersFollowThresholds() {
        FoeMastery mastery = new FoeMastery(List.of(100, 500, 2000), 0.05);

        assertEquals(0, mastery.tierFor(99));
        assertEquals(1, mastery.tierFor(100));
        assertEquals(2, mastery.tierFor(1999));
        assertEquals(3, mastery.tierFor(Integer.MAX_VALUE));
        assertEquals(1.10, mastery.damageMultiplier(500), 1e-9);
        assertEquals(400, mastery.killsToNextTier(100));
        assertEquals(0, mastery.killsToNextTier(5000));
    }

    @Test
    void configValuesAreNormalized() {
        FoeMastery mastery = FoeMastery.fromConfig(true, Arrays.asList(500, 100, 100, 0, null), 0.05);

        assertEquals(List.of(100, 500), mastery.thresholds());
        assertFalse(FoeMastery.fromConfig(false, List.of(1), 1).isEnabled());
    }

    @Test
    void rejectsUnorderedThresholds() {
        assertThrows(IllegalArgumentException.class, () -> new FoeMastery(List.of(5, 5), 0.1));
    }

    @Test
    void streakGrowsWithinTheWindowAndResetsAfterIt() {
        UUID weapon = UUID.randomUUID();
        KillStreak first = KillStreak.advance(null, weapon, 1000, RULES);
        KillStreak second = KillStreak.advance(first, weapon, 1120, RULES);
        KillStreak late = KillStreak.advance(second, weapon, 1241, RULES);

        assertEquals(1, first.count());
        assertEquals(2, second.count());
        assertEquals(1, late.count());
    }

    @Test
    void switchingWeaponsStartsANewStreak() {
        KillStreak streak = KillStreak.advance(null, UUID.randomUUID(), 0, RULES);

        assertEquals(1, KillStreak.advance(streak, UUID.randomUUID(), 1, RULES).count());
    }

    @Test
    void xpBonusIsCappedAndDisabledRulesGiveNone() {
        assertEquals(1.0, RULES.xpMultiplier(1), 1e-9);
        assertEquals(1.2, RULES.xpMultiplier(3), 1e-9);
        assertEquals(1.5, RULES.xpMultiplier(50), 1e-9);
        assertEquals(1.0, KillStreak.Rules.DISABLED.xpMultiplier(50), 1e-9);
    }

    @Test
    void shieldSavesDurabilityLinearlyAndNeverBelowOne() {
        assertEquals(4, dev.weaponleveling.core.combat.ShieldDurability.reduce(4, 0, 500, 0.5));
        assertEquals(3, dev.weaponleveling.core.combat.ShieldDurability.reduce(4, 250, 500, 0.5));
        assertEquals(2, dev.weaponleveling.core.combat.ShieldDurability.reduce(4, 500, 500, 0.5));
        assertEquals(2, dev.weaponleveling.core.combat.ShieldDurability.reduce(4, 9000, 500, 0.5));
        assertEquals(1, dev.weaponleveling.core.combat.ShieldDurability.reduce(1, 500, 500, 0.9));
        assertEquals(0, dev.weaponleveling.core.combat.ShieldDurability.reduce(0, 500, 500, 0.5));
        assertEquals(7, dev.weaponleveling.core.combat.ShieldDurability.reduce(7, 500, 500, 0));
    }

    @Test
    void toolMiningBonusGrowsToTheCap() {
        assertEquals(0, dev.weaponleveling.core.combat.ToolMiningBonus.efficiency(0, 500, 10), 1e-9);
        assertEquals(5, dev.weaponleveling.core.combat.ToolMiningBonus.efficiency(250, 500, 10), 1e-9);
        assertEquals(10, dev.weaponleveling.core.combat.ToolMiningBonus.efficiency(500, 500, 10), 1e-9);
        assertEquals(10, dev.weaponleveling.core.combat.ToolMiningBonus.efficiency(900, 500, 10), 1e-9);
        assertEquals(0, dev.weaponleveling.core.combat.ToolMiningBonus.efficiency(500, 500, 0), 1e-9);
    }

    @Test
    void toolXpPaysFractionsByChance() {
        assertEquals(0, dev.weaponleveling.core.combat.ToolXp.award(1.5, 0.1, 50, 0.20));
        assertEquals(1, dev.weaponleveling.core.combat.ToolXp.award(1.5, 0.1, 50, 0.10));
        assertEquals(5, dev.weaponleveling.core.combat.ToolXp.award(50, 0.1, 50, 0.99));
        assertEquals(3, dev.weaponleveling.core.combat.ToolXp.award(50, 1.0, 3, 0.0));
        assertEquals(0, dev.weaponleveling.core.combat.ToolXp.award(0, 0.1, 50, 0.0));
    }

    @Test
    void romanNumeralsMatchEnchantmentStyle() {
        assertEquals("I", RomanNumerals.of(1));
        assertEquals("IV", RomanNumerals.of(4));
        assertEquals("XIV", RomanNumerals.of(14));
        assertEquals("0", RomanNumerals.of(0));
    }
}
