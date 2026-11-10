package dev.weaponleveling.core.combat;

/**
 * XP for one broken block. Mining is far faster than fighting, so a block is worth only a fraction of
 * an XP point by default; the fraction is paid out by chance, which keeps the average exact without
 * rounding every block up to a whole point.
 */
public final class ToolXp {
    private ToolXp() {
    }

    /** @param roll uniform random number in [0, 1) */
    public static long award(double hardness, double xpPerHardness, long cap, double roll) {
        if (!Double.isFinite(hardness) || !Double.isFinite(xpPerHardness)
                || hardness <= 0 || xpPerHardness <= 0 || cap <= 0) {
            return 0;
        }
        double scaled = hardness * xpPerHardness;
        if (!Double.isFinite(scaled)) {
            return cap;
        }
        long whole = (long) Math.floor(scaled);
        if (roll < scaled - whole) {
            whole++;
        }
        return Math.min(whole, cap);
    }
}
