package dev.weaponleveling.core.combat;

/** Server-authoritative Armor contribution earned from one armor item's level. */
public record ArmorLevelBonus(double armor) {
    public static final ArmorLevelBonus NONE = new ArmorLevelBonus(0);

    /**
     * Guaranteed floor for every levelable armor piece: +1 Armor per 100 levels, so a level-100
     * piece always earns at least +1 Armor. The floor is independent of the configured per-level
     * rate and cap, so weak or legacy config values can never shrink armor below it again.
     */
    public static final double GUARANTEED_ARMOR_PER_100_LEVELS = 1.0;

    public static ArmorLevelBonus calculate(int armorLevel, double flatPerLevel, double maxArmor) {
        int gainedLevels = Math.max(0, armorLevel);
        if (gainedLevels == 0) {
            return NONE;
        }
        double guaranteed = gainedLevels * GUARANTEED_ARMOR_PER_100_LEVELS / 100.0;
        if (!Double.isFinite(flatPerLevel) || !Double.isFinite(maxArmor)
                || flatPerLevel <= 0 || maxArmor <= 0) {
            return new ArmorLevelBonus(guaranteed);
        }
        double configured = Math.min(gainedLevels * flatPerLevel, maxArmor);
        return new ArmorLevelBonus(Math.max(configured, guaranteed));
    }
}
