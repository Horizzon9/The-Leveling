package dev.weaponleveling.core.model;

/**
 * A presentation-only identity shaped by how an item has actually been used.
 * It deliberately grants no combat bonus: history gives an item character without
 * creating a second, hidden balance system.
 */
public record LegacyPersona(Path path, int renownRank) {
    public enum Path {
        UNFORGED("unforged"),
        VETERAN("veteran"),
        SLAYER("slayer"),
        MARKSMAN("marksman"),
        BULWARK("bulwark");

        private final String serializedName;

        Path(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }
    }

    public static LegacyPersona resolve(WeaponProgress progress, boolean armor, boolean ranged) {
        if (progress == null || progress.legacyTier() == LegacyTier.NONE) {
            return new LegacyPersona(Path.UNFORGED, 0);
        }

        Path path;
        if (armor) {
            path = Path.BULWARK;
        } else if (ranged) {
            path = Path.MARKSMAN;
        } else if (progress.totalKills() >= 25) {
            path = Path.SLAYER;
        } else {
            path = Path.VETERAN;
        }

        long renown = armor
                ? (long) Math.floor(progress.totalDamageDealt() / 20.0)
                : saturatedAdd(progress.totalKills(), (long) Math.floor(progress.totalDamageDealt() / 100.0));
        return new LegacyPersona(path, rankFor(renown));
    }

    private static int rankFor(long renown) {
        if (renown >= 750) return 5;
        if (renown >= 300) return 4;
        if (renown >= 100) return 3;
        if (renown >= 25) return 2;
        return 1;
    }

    private static long saturatedAdd(long first, long second) {
        if (first > Long.MAX_VALUE - second) {
            return Long.MAX_VALUE;
        }
        return first + second;
    }
}
