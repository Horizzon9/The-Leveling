package dev.weaponleveling.core.xp;

import java.util.List;

public final class XpCurve {
    /** Level cap the default growth factor was tuned for. */
    public static final int REFERENCE_MAX_LEVEL = 500;

    private final int maxLevel;
    private final long baseXp;
    private final double growthFactor;
    private final List<Long> levelTable;

    public XpCurve(int maxLevel, long baseXp, double growthFactor, List<Long> levelTable) {
        if (maxLevel < 1 || baseXp < 1 || !Double.isFinite(growthFactor) || growthFactor < 1) {
            throw new IllegalArgumentException("Invalid XP curve configuration");
        }
        this.maxLevel = maxLevel;
        this.baseXp = baseXp;
        this.growthFactor = growthFactor;
        this.levelTable = List.copyOf(levelTable);
        if (this.levelTable.stream().anyMatch(value -> value == null || value < 1)) {
            throw new IllegalArgumentException("XP table values must be positive");
        }
    }

    /**
     * Growth factor that keeps the curve's shape for any level cap: requirement(level) becomes
     * baseXp * growthFactor^(level * 500 / maxLevel). The last level then costs the same as the last
     * level of the 500-level default, and total XP grows in proportion to the cap instead of exponentially.
     */
    public static double scaledGrowthFactor(double growthFactor, int maxLevel) {
        if (maxLevel <= 0 || !Double.isFinite(growthFactor) || growthFactor <= 1.0) {
            return growthFactor;
        }
        return Math.pow(growthFactor, REFERENCE_MAX_LEVEL / (double) maxLevel);
    }

    /** Scales one XP requirement, never below 1 XP. */
    public static long scaleRequirement(long xp, double multiplier) {
        double scaled = xp * multiplier;
        if (!Double.isFinite(scaled) || scaled >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.max(1, Math.round(scaled));
    }

    public int maxLevel() {
        return maxLevel;
    }

    public long requiredXp(int level) {
        if (level < 0 || level >= maxLevel) {
            return 0;
        }
        if (level < levelTable.size()) {
            return levelTable.get(level);
        }
        double calculated = baseXp * Math.pow(growthFactor, level);
        if (!Double.isFinite(calculated) || calculated >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.max(1, Math.round(calculated));
    }

    /**
     * Returns the cumulative XP needed to enter {@code targetLevel}. Level zero
     * intentionally costs zero XP. The result is saturated rather than allowed
     * to wrap so administrative commands remain safe with large custom curves.
     */
    public long totalXpForLevel(int targetLevel) {
        if (targetLevel < 0 || targetLevel > maxLevel) {
            throw new IllegalArgumentException("Level is outside this curve");
        }
        long total = 0;
        for (int level = 0; level < targetLevel; level++) {
            long required = requiredXp(level);
            if (total > Long.MAX_VALUE - required) {
                return Long.MAX_VALUE;
            }
            total += required;
        }
        return total;
    }
}
