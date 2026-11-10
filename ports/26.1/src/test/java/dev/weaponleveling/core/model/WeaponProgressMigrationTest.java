package dev.weaponleveling.core.model;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeaponProgressMigrationTest {
    @Test
    void schemaOneBaselineLevelMigratesToLevelZeroWithoutLosingXp() {
        WeaponProgress legacy = new WeaponProgress(1, UUID.randomUUID(), 1, 37, 100, 0,
                Map.of(), 12.5, 4, LegacyTier.NONE, 7);

        WeaponProgress migrated = WeaponProgress.migrateFromStoredSchema(legacy);

        assertEquals(WeaponProgress.CURRENT_SCHEMA_VERSION, migrated.schemaVersion());
        assertEquals(0, migrated.level());
        assertEquals(37, migrated.currentXp());
        assertEquals(100, migrated.totalXp());
        assertEquals(12.5, migrated.totalDamageDealt());
        assertEquals(4, migrated.totalKills());
    }

    @Test
    void currentProgressIsNotMigratedAgain() {
        WeaponProgress current = WeaponProgress.create(7);

        assertEquals(current, WeaponProgress.migrateFromStoredSchema(current));
    }

    @Test
    void schemaTwoProgressDoesNotLoseAnotherLevelWhenLegacyTierIsAdded() {
        WeaponProgress schemaTwo = new WeaponProgress(2, UUID.randomUUID(), 125, 37, 10_000, 0,
                Map.of(), 12.5, 40, LegacyTier.NONE, 7);

        WeaponProgress migrated = WeaponProgress.migrateFromStoredSchema(schemaTwo);

        assertEquals(WeaponProgress.CURRENT_SCHEMA_VERSION, migrated.schemaVersion());
        assertEquals(125, migrated.level());
        assertEquals(LegacyTier.NONE, migrated.legacyTier());
    }
}
