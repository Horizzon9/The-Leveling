package dev.weaponleveling.core.combat;

/**
 * Server-calculated, bounded multiplier earned from a weapon's level.
 * Level zero is the baseline; the curve reaches its configured multiplier at the configured level cap.
 */
public record LevelDamageBonus(double multiplier) {
    public static final LevelDamageBonus NONE = new LevelDamageBonus(1.0);

    public static LevelDamageBonus calculate(int weaponLevel, int maxLevel, double maxMultiplier, double exponent) {
        if (weaponLevel <= 0 || maxLevel <= 0 || !Double.isFinite(maxMultiplier) || !Double.isFinite(exponent)
                || maxMultiplier <= 1 || exponent <= 0) {
            return NONE;
        }
        double normalizedLevel = Math.min(1.0, Math.max(0.0,
                weaponLevel / (double) maxLevel));
        return new LevelDamageBonus(1.0 + (maxMultiplier - 1.0) * Math.pow(normalizedLevel, exponent));
    }

    /**
     * Applies the level multiplier only after the weapon and Minecraft have calculated their own damage.
     * This deliberately preserves special values such as mace smash damage, trident damage, critical hits,
     * vanilla/modded enchantment damage, and a compatible spear projectile's impact damage. The caller supplies
     * Minecraft's already-combined hit amount, so the level multiplier applies consistently to every damage bonus.
     */
    public double applyTo(double originalDamage) {
        if (!Double.isFinite(originalDamage) || originalDamage <= 0) {
            return 0;
        }
        double scaledDamage = originalDamage * multiplier;
        return Double.isFinite(scaledDamage) ? Math.min(scaledDamage, Float.MAX_VALUE) : Float.MAX_VALUE;
    }
}
