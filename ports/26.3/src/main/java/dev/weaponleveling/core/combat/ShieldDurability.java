package dev.weaponleveling.core.combat;

/** How much of the durability a block would normally cost a levelled shield is saved. */
public final class ShieldDurability {
    private ShieldDurability() {
    }

    /**
     * Durability the shield loses for one block. The saving grows linearly with the level and reaches
     * {@code maxSaving} at the level cap. A block that would cost any durability always costs at least one.
     */
    public static int reduce(int damage, int level, int maxLevel, double maxSaving) {
        if (damage <= 0 || level <= 0 || maxLevel <= 0 || !Double.isFinite(maxSaving) || maxSaving <= 0) {
            return damage;
        }
        double saving = Math.min(1.0, maxSaving) * Math.min(1.0, level / (double) maxLevel);
        return Math.max(1, (int) Math.round(damage * (1.0 - saving)));
    }
}
