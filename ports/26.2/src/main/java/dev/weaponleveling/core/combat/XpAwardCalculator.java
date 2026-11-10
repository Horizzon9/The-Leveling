package dev.weaponleveling.core.combat;

public final class XpAwardCalculator {
    private XpAwardCalculator() {
    }

    /**
     * XP is intentionally kill-only. Damage dealt and hit count are not inputs, so neither can award XP.
     * {@code bonusMultiplier} carries situational rewards such as the kill-streak bonus; the result is
     * still capped by {@code maxXpPerKill}.
     */
    public static long calculate(boolean killEnabled, boolean killed, double killBonus,
                                 double entityMultiplier, double bossMultiplier,
                                 double bonusMultiplier, long maxXpPerKill) {
        double earned = killEnabled && killed ? Math.max(0, killBonus) : 0;
        earned *= Math.max(0, entityMultiplier);
        earned *= Math.max(0, bossMultiplier);
        earned *= Math.max(0, bonusMultiplier);
        if (!Double.isFinite(earned)) {
            return maxXpPerKill;
        }
        return Math.max(0, Math.min(maxXpPerKill, Math.round(earned)));
    }
}
