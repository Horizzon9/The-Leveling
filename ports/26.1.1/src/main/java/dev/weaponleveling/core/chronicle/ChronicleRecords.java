package dev.weaponleveling.core.chronicle;

/**
 * Personal bests a weapon has set. Each value only ever grows; a weaker feat never overwrites a record.
 *
 * @param strongestHit  highest damage dealt by a single hit
 * @param longestShot   longest distance, in blocks, of a ranged hit
 * @param longestStreak longest kill streak made with the weapon
 */
public record ChronicleRecords(double strongestHit, double longestShot, int longestStreak) {
    public static final ChronicleRecords EMPTY = new ChronicleRecords(0, 0, 0);

    public ChronicleRecords {
        strongestHit = sanitize(strongestHit);
        longestShot = sanitize(longestShot);
        longestStreak = Math.max(0, longestStreak);
    }

    public boolean isEmpty() {
        return strongestHit <= 0 && longestShot <= 0 && longestStreak <= 0;
    }

    /** Returns these records improved by one hit; unchanged values keep the same instance. */
    public ChronicleRecords withFeat(double hitDamage, double shotDistance, int streak) {
        double hit = Math.max(strongestHit, sanitize(hitDamage));
        double shot = Math.max(longestShot, sanitize(shotDistance));
        int bestStreak = Math.max(longestStreak, streak);
        if (hit == strongestHit && shot == longestShot && bestStreak == longestStreak) {
            return this;
        }
        return new ChronicleRecords(hit, shot, bestStreak);
    }

    private static double sanitize(double value) {
        return Double.isFinite(value) && value > 0 ? value : 0;
    }
}
