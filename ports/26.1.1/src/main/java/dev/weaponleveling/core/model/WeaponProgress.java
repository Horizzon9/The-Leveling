package dev.weaponleveling.core.model;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record WeaponProgress(
        int schemaVersion,
        UUID instanceId,
        int level,
        long currentXp,
        long totalXp,
        int unspentPoints,
        Map<String, Integer> upgradeRanks,
        double totalDamageDealt,
        long totalKills,
        LegacyTier legacyTier,
        double baseAttackDamage
) {
    public static final int CURRENT_SCHEMA_VERSION = 3;

    public WeaponProgress {
        Objects.requireNonNull(instanceId, "instanceId");
        Objects.requireNonNull(legacyTier, "legacyTier");
        upgradeRanks = Map.copyOf(upgradeRanks);
        if (schemaVersion < 1 || level < 0 || currentXp < 0 || totalXp < 0 || unspentPoints < 0
                || totalDamageDealt < 0 || totalKills < 0 || !Double.isFinite(baseAttackDamage)) {
            throw new IllegalArgumentException("Weapon progress contains an invalid value");
        }
    }

    public static WeaponProgress create(double baseAttackDamage) {
        return new WeaponProgress(CURRENT_SCHEMA_VERSION, UUID.randomUUID(), 0, 0, 0, 0,
                Map.of(), 0, 0, LegacyTier.NONE, Math.max(0, baseAttackDamage));
    }

    public WeaponProgress withProgress(int newLevel, long newCurrentXp, long newTotalXp, int newUnspentPoints) {
        return new WeaponProgress(schemaVersion, instanceId, newLevel, newCurrentXp, newTotalXp,
                0, Map.of(), totalDamageDealt, totalKills, legacyTier, baseAttackDamage);
    }

    public WeaponProgress withCombatStats(double damage, boolean killed) {
        return new WeaponProgress(schemaVersion, instanceId, level, currentXp, totalXp, 0,
                Map.of(), totalDamageDealt + Math.max(0, damage), totalKills + (killed ? 1 : 0),
                legacyTier, baseAttackDamage);
    }

    public WeaponProgress withAdministrativeValues(int newLevel, long newCurrentXp, long newTotalXp,
                                                    int newUnspentPoints) {
        return new WeaponProgress(schemaVersion, instanceId, Math.max(0, newLevel), Math.max(0, newCurrentXp),
                Math.max(0, newTotalXp), 0, Map.of(), totalDamageDealt,
                totalKills, legacyTier, baseAttackDamage);
    }

    public WeaponProgress withLegacyTier(LegacyTier newTier) {
        LegacyTier promoted = LegacyTier.highest(legacyTier, Objects.requireNonNull(newTier, "newTier"));
        if (promoted == legacyTier) {
            return this;
        }
        return new WeaponProgress(schemaVersion, instanceId, level, currentXp, totalXp, 0,
                Map.of(), totalDamageDealt, totalKills, promoted, baseAttackDamage);
    }

    public WeaponProgress reset() {
        return create(baseAttackDamage);
    }

    /** Migrates old item components without applying the schema-one level correction more than once. */
    public static WeaponProgress migrateFromStoredSchema(WeaponProgress stored) {
        if (stored.schemaVersion() >= CURRENT_SCHEMA_VERSION) {
            return stored;
        }
        int migratedLevel = stored.schemaVersion() == 1 ? Math.max(0, stored.level() - 1) : stored.level();
        return new WeaponProgress(CURRENT_SCHEMA_VERSION, stored.instanceId(), migratedLevel,
                stored.currentXp(), stored.totalXp(), stored.unspentPoints(), stored.upgradeRanks(), stored.totalDamageDealt(),
                stored.totalKills(), stored.legacyTier(), stored.baseAttackDamage());
    }
}
