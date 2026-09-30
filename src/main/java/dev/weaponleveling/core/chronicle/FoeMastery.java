package dev.weaponleveling.core.chronicle;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Rules for the damage a weapon earns against a creature type it has defeated many times.
 *
 * @param thresholds   strictly increasing kill counts; reaching the n-th one grants tier n
 * @param bonusPerTier extra damage fraction per tier, applied after the level multiplier
 */
public record FoeMastery(List<Integer> thresholds, double bonusPerTier) {
    public static final FoeMastery DISABLED = new FoeMastery(List.of(), 0);

    public FoeMastery {
        thresholds = List.copyOf(thresholds);
        if (!Double.isFinite(bonusPerTier) || bonusPerTier < 0) {
            throw new IllegalArgumentException("Mastery bonus must be a finite, non-negative number");
        }
        for (int index = 0; index < thresholds.size(); index++) {
            int threshold = thresholds.get(index);
            if (threshold <= 0 || (index > 0 && threshold <= thresholds.get(index - 1))) {
                throw new IllegalArgumentException("Mastery thresholds must be positive and strictly increasing");
            }
        }
    }

    /** Builds rules from loosely ordered config values: non-positive and repeated thresholds are ignored. */
    public static FoeMastery fromConfig(boolean enabled, List<Integer> rawThresholds, double bonusPerTier) {
        if (!enabled) {
            return DISABLED;
        }
        List<Integer> normalized = rawThresholds.stream()
                .filter(threshold -> threshold != null && threshold > 0)
                .distinct()
                .sorted()
                .toList();
        return new FoeMastery(normalized, Math.max(0, bonusPerTier));
    }

    public boolean isEnabled() {
        return !thresholds.isEmpty();
    }

    public int maxTier() {
        return thresholds.size();
    }

    public int tierFor(int kills) {
        return (int) IntStream.range(0, thresholds.size())
                .filter(index -> kills >= thresholds.get(index))
                .count();
    }

    /** Kills still needed for the next tier, or zero once the final tier is reached. */
    public int killsToNextTier(int kills) {
        int tier = tierFor(kills);
        return tier >= thresholds.size() ? 0 : thresholds.get(tier) - Math.max(0, kills);
    }

    public double damageMultiplier(int kills) {
        return 1.0 + tierFor(kills) * bonusPerTier;
    }

    public double bonusForTier(int tier) {
        return Math.max(0, Math.min(tier, maxTier())) * bonusPerTier;
    }
}
