package dev.weaponleveling.server;

import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.chronicle.KillStreak;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Live kill streak of every online player. Streaks are short-lived combat state, so they are kept
 * in memory only and dropped when the player leaves. Accessed from the server thread only.
 */
public final class KillStreakTracker {
    private static final Map<UUID, KillStreak> STREAKS = new HashMap<>();

    private KillStreakTracker() {
    }

    /** Records a kill and returns the streak it extends or starts, or null while streaks are disabled. */
    public static KillStreak recordKill(ServerPlayer player, UUID weaponId) {
        KillStreak.Rules rules = ServerConfig.killStreakRules();
        if (!rules.isEnabled()) {
            STREAKS.remove(player.getUUID());
            return null;
        }
        KillStreak streak = KillStreak.advance(STREAKS.get(player.getUUID()), weaponId,
                player.level().getServer().getTickCount(), rules);
        STREAKS.put(player.getUUID(), streak);
        return streak;
    }

    public static double xpMultiplier(KillStreak streak) {
        return streak == null ? 1.0 : ServerConfig.killStreakRules().xpMultiplier(streak.count());
    }

    public static void forget(UUID playerId) {
        STREAKS.remove(playerId);
    }
}
