package dev.weaponleveling.core.combat;

/** Mining efficiency a levelled tool adds, on the same scale as the vanilla Efficiency enchantment. */
public final class ToolMiningBonus {
    private ToolMiningBonus() {
    }

    /** Grows linearly with level and reaches {@code maxBonus} at the level cap; level zero adds nothing. */
    public static double efficiency(int level, int maxLevel, double maxBonus) {
        if (level <= 0 || maxLevel <= 0 || !Double.isFinite(maxBonus) || maxBonus <= 0) {
            return 0;
        }
        return maxBonus * Math.min(1.0, level / (double) maxLevel);
    }
}
