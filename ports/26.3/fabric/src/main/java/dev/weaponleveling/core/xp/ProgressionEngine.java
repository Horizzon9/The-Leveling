package dev.weaponleveling.core.xp;

import dev.weaponleveling.core.model.WeaponProgress;

public final class ProgressionEngine {
    private ProgressionEngine() {
    }

    public static ProgressionResult addXp(WeaponProgress progress, long earnedXp, XpCurve curve) {
        return addXp(progress, earnedXp, curve, LegacyPolicy.DEFAULT);
    }

    public static ProgressionResult addXp(WeaponProgress progress, long earnedXp, XpCurve curve,
                                          LegacyPolicy legacyPolicy) {
        if (earnedXp <= 0 || progress.level() >= curve.maxLevel()) {
            return new ProgressionResult(legacyPolicy.promote(progress, curve.maxLevel()), 0, 0);
        }
        long currentXp = saturatedAdd(progress.currentXp(), earnedXp);
        long totalXp = saturatedAdd(progress.totalXp(), earnedXp);
        int level = progress.level();
        int levelsGained = 0;

        while (level < curve.maxLevel()) {
            long required = curve.requiredXp(level);
            if (required <= 0 || currentXp < required) {
                break;
            }
            currentXp -= required;
            level++;
            levelsGained++;
        }
        if (level >= curve.maxLevel()) {
            currentXp = 0;
        }
        WeaponProgress updated = progress.withProgress(level, currentXp, totalXp, 0);
        return new ProgressionResult(legacyPolicy.promote(updated, curve.maxLevel()), levelsGained, 0);
    }

    /**
     * Rebuilds level and XP-within-level from an absolute earned-XP value.
     * Legacy upgrade values are discarded as this version uses level progression only.
     */
    public static WeaponProgress setTotalXp(WeaponProgress progress, long totalXp, XpCurve curve) {
        return setTotalXp(progress, totalXp, curve, LegacyPolicy.DEFAULT);
    }

    public static WeaponProgress setTotalXp(WeaponProgress progress, long totalXp, XpCurve curve,
                                            LegacyPolicy legacyPolicy) {
        if (totalXp < 0) {
            throw new IllegalArgumentException("Total XP cannot be negative");
        }
        int level = 0;
        long remainingXp = totalXp;
        while (level < curve.maxLevel()) {
            long required = curve.requiredXp(level);
            if (required <= 0 || remainingXp < required) {
                break;
            }
            remainingXp -= required;
            level++;
        }
        if (level >= curve.maxLevel()) {
            remainingXp = 0;
        }

        return legacyPolicy.promote(progress.withProgress(level, remainingXp, totalXp, 0), curve.maxLevel());
    }

    /**
     * Sets an exact valid level using the first XP value for that level.
     */
    public static WeaponProgress setLevel(WeaponProgress progress, int level, XpCurve curve) {
        return setLevel(progress, level, curve, LegacyPolicy.DEFAULT);
    }

    public static WeaponProgress setLevel(WeaponProgress progress, int level, XpCurve curve,
                                          LegacyPolicy legacyPolicy) {
        return setTotalXp(progress, curve.totalXpForLevel(level), curve, legacyPolicy);
    }

    private static long saturatedAdd(long left, long right) {
        if (right > 0 && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

}
