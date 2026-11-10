package dev.weaponleveling.core.chronicle;

import java.util.Objects;
import java.util.UUID;

/**
 * Consecutive kills made with one weapon, each within a short window of the previous one.
 * Switching weapons or pausing longer than the window starts a new streak.
 *
 * @param weaponId     instance id of the weapon that owns the streak
 * @param count        kills in the streak, starting at one
 * @param lastKillTick server tick of the most recent kill
 */
public record KillStreak(UUID weaponId, int count, long lastKillTick) {

    public KillStreak {
        Objects.requireNonNull(weaponId, "weaponId");
        if (count < 1) {
            throw new IllegalArgumentException("A streak always contains at least one kill");
        }
    }

    public static KillStreak advance(KillStreak previous, UUID weaponId, long tick, Rules rules) {
        boolean continues = previous != null
                && previous.weaponId.equals(weaponId)
                && tick >= previous.lastKillTick
                && tick - previous.lastKillTick <= rules.windowTicks();
        if (!continues) {
            return new KillStreak(weaponId, 1, tick);
        }
        int count = previous.count == Integer.MAX_VALUE ? previous.count : previous.count + 1;
        return new KillStreak(weaponId, count, tick);
    }

    /**
     * @param windowTicks  longest pause between two kills that keeps the streak alive
     * @param bonusPerKill extra XP fraction added by every kill after the first
     * @param maxBonus     cap on the total extra XP fraction
     */
    public record Rules(long windowTicks, double bonusPerKill, double maxBonus) {
        public static final Rules DISABLED = new Rules(0, 0, 0);

        public Rules {
            if (windowTicks < 0 || !Double.isFinite(bonusPerKill) || bonusPerKill < 0
                    || !Double.isFinite(maxBonus) || maxBonus < 0) {
                throw new IllegalArgumentException("Kill streak rules contain an invalid value");
            }
        }

        public boolean isEnabled() {
            return windowTicks > 0 && bonusPerKill > 0 && maxBonus > 0;
        }

        public double xpMultiplier(int streakCount) {
            if (!isEnabled() || streakCount <= 1) {
                return 1.0;
            }
            return 1.0 + Math.min(maxBonus, (streakCount - 1) * bonusPerKill);
        }
    }
}
