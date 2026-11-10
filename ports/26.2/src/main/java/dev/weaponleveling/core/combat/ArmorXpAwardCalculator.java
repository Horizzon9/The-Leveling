package dev.weaponleveling.core.combat;

/** Converts real player damage into bounded per-armor XP without awarding XP for zero-damage events. */
public final class ArmorXpAwardCalculator {
    private ArmorXpAwardCalculator() {
    }

    public static long calculate(double realDamage, double xpPerDamage, long cap) {
        if (!Double.isFinite(realDamage) || !Double.isFinite(xpPerDamage)
                || realDamage <= 0 || xpPerDamage <= 0 || cap <= 0) {
            return 0;
        }
        double scaled = realDamage * xpPerDamage;
        if (!Double.isFinite(scaled) || scaled <= 0) {
            return 0;
        }
        long roundedUp = scaled >= Long.MAX_VALUE ? Long.MAX_VALUE : (long) Math.ceil(scaled);
        return Math.min(roundedUp, cap);
    }
}
