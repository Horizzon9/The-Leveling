package dev.weaponleveling.core.xp;

import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.core.model.WeaponProgress;

/** Configurable percentage thresholds used to grant permanent item identities. */
public record LegacyPolicy(int relicLevelPercent, int legacyLevelPercent) {
    public static final LegacyPolicy DEFAULT = new LegacyPolicy(25, 100);

    public LegacyPolicy {
        if (relicLevelPercent < 1 || relicLevelPercent > 100
                || legacyLevelPercent < 1 || legacyLevelPercent > 100
                || relicLevelPercent > legacyLevelPercent) {
            throw new IllegalArgumentException("Legacy thresholds must be ordered percentages from 1 to 100");
        }
    }

    public LegacyTier earnedTier(int level, int maximumLevel) {
        if (level >= thresholdLevel(maximumLevel, legacyLevelPercent)) {
            return LegacyTier.LEGACY;
        }
        if (level >= thresholdLevel(maximumLevel, relicLevelPercent)) {
            return LegacyTier.RELIC;
        }
        return LegacyTier.NONE;
    }

    public WeaponProgress promote(WeaponProgress progress, int maximumLevel) {
        return progress.withLegacyTier(LegacyTier.highest(progress.legacyTier(),
                earnedTier(progress.level(), maximumLevel)));
    }

    private static int thresholdLevel(int maximumLevel, int percentage) {
        int safeMaximum = Math.max(1, maximumLevel);
        return Math.max(1, (int) Math.ceil(safeMaximum * (percentage / 100.0)));
    }
}
