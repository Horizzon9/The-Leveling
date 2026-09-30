package dev.weaponleveling.core.combat;

/** Server-authoritative Armor contribution earned from one armor item's level. */
public record ArmorLevelBonus(double armor) {
    public static final ArmorLevelBonus NONE = new ArmorLevelBonus(0);

    public static ArmorLevelBonus calculate(int armorLevel, double flatPerLevel, double maxArmor) {
        int gainedLevels = Math.max(0, armorLevel);
        if (gainedLevels == 0 || !Double.isFinite(flatPerLevel) || !Double.isFinite(maxArmor)
                || flatPerLevel <= 0 || maxArmor <= 0) {
            return NONE;
        }
        double bonus = gainedLevels >= maxArmor / flatPerLevel ? maxArmor : gainedLevels * flatPerLevel;
        return new ArmorLevelBonus(Math.min(bonus, maxArmor));
    }
}
