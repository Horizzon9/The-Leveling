package dev.weaponleveling.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyPersonaTest {
    @Test
    void identityReflectsTheItemsActualHistory() {
        WeaponProgress relic = WeaponProgress.create(7).withLegacyTier(LegacyTier.RELIC);
        assertEquals(LegacyPersona.Path.VETERAN,
                LegacyPersona.resolve(relic, false, false).path());

        WeaponProgress slayer = relic.withCombatStats(3_000, true);
        for (int i = 1; i < 25; i++) slayer = slayer.withCombatStats(10, true);
        assertEquals(LegacyPersona.Path.SLAYER,
                LegacyPersona.resolve(slayer, false, false).path());

        assertEquals(LegacyPersona.Path.MARKSMAN,
                LegacyPersona.resolve(relic, false, true).path());
        assertEquals(LegacyPersona.Path.BULWARK,
                LegacyPersona.resolve(slayer, true, false).path());
    }

    @Test
    void renownGrowsAcrossFiveBoundedRanks() {
        WeaponProgress relic = WeaponProgress.create(7).withLegacyTier(LegacyTier.RELIC);
        assertEquals(1, LegacyPersona.resolve(relic, false, false).renownRank());
        assertEquals(5, LegacyPersona.resolve(relic.withCombatStats(100_000, false),
                false, false).renownRank());
    }
}
