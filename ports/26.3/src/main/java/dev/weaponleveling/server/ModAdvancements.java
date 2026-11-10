package dev.weaponleveling.server;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.chronicle.KillStreak;
import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.core.xp.ProgressionResult;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Grants the mod's advancements. They are plain data-pack advancements with a single
 * {@code minecraft:impossible} criterion, awarded from code; this needs no custom trigger
 * registration and therefore behaves identically on every loader.
 */
public final class ModAdvancements {
    public static final String ROOT = "root";
    public static final String RELIC = "relic";
    public static final String LEGACY = "legacy";
    public static final String FOE_MASTERY = "foe_mastery";
    public static final String GRANDMASTER = "grandmaster";
    public static final String KILL_STREAK = "kill_streak";
    public static final String ARMOR_MASTERY = "armor_mastery";
    public static final String NAMED_ITEM = "named_item";
    public static final int STREAK_GOAL = 10;

    private ModAdvancements() {
    }

    /** Level-ups and the identities they unlock, for weapons and armor alike. */
    public static void onProgress(ServerPlayer player, ProgressionResult result) {
        if (result.levelsGained() <= 0) {
            return;
        }
        grant(player, ROOT);
        LegacyTier tier = result.progress().legacyTier();
        if (tier.ordinal() >= LegacyTier.RELIC.ordinal()) {
            grant(player, RELIC);
        }
        if (tier == LegacyTier.LEGACY) {
            grant(player, LEGACY);
        }
    }

    public static void onWeaponKill(ServerPlayer player, int masteryTier, KillStreak streak) {
        if (masteryTier > 0) {
            grant(player, FOE_MASTERY);
            if (masteryTier >= ServerConfig.foeMastery().maxTier()) {
                grant(player, GRANDMASTER);
            }
        }
        if (streak != null && streak.count() >= STREAK_GOAL) {
            grant(player, KILL_STREAK);
        }
    }

    public static void grant(ServerPlayer player, String path) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements()
                .get(Identifier.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, path));
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) {
            return;
        }
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
