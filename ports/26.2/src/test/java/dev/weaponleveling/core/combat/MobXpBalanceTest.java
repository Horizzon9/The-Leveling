package dev.weaponleveling.core.combat;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobXpBalanceTest {
    @Test
    void everyVanillaMobScalesDirectlyFromMaximumHealth() {
        assertEquals(1.0, MobXpBalance.multiplier("minecraft:zombie", 20, 20, List.of()));
        assertEquals(5.0, MobXpBalance.multiplier("minecraft:ravager", 100, 20, List.of()));
        assertEquals(25.0, MobXpBalance.multiplier("minecraft:warden", 500, 20, List.of()));
        assertEquals(10.0, MobXpBalance.multiplier("minecraft:ender_dragon", 200, 20, List.of()));
    }

    @Test
    void allowsConfigToOverrideBuiltInBalance() {
        assertEquals(7.5, MobXpBalance.multiplier("minecraft:warden", 500, 20,
                List.of("minecraft:warden=7.5")));
    }

    @Test
    void scalesUnknownModdedMobsWithTheExactSameFormula() {
        assertEquals(1.0, MobXpBalance.multiplier("example:hostile", 20, 20, List.of()));
        assertEquals(4.0, MobXpBalance.multiplier("example:armored_beast", 80, 20, List.of()));
        assertEquals(50.0, MobXpBalance.multiplier("example:hostile_boss", 1_000, 20, List.of()));
        assertEquals(2.0, MobXpBalance.multiplier("example:large_animal", 40, 20, List.of()));
        assertTrue(MobXpBalance.multiplier("example:small_animal", 2, 20, List.of()) > 0.0);
    }

    @Test
    void configurableBaselinePreservesLinearScaling() {
        assertEquals(1.0, MobXpBalance.multiplier("example:creature", 40, 40, List.of()));
        assertEquals(2.0, MobXpBalance.multiplier("example:creature", 80, 40, List.of()));
    }
}
