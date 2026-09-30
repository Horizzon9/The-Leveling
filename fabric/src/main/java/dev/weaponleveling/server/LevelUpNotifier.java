package dev.weaponleveling.server;

import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.network.LevelUpSealPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import dev.weaponleveling.platform.net.ModNetwork;

/** Delivers one server-confirmed level-up notification to the item owner. */
public final class LevelUpNotifier {
    private LevelUpNotifier() {
    }

    public static void notify(ServerPlayer player, ItemStack item, int level, LegacyTier legacyTier,
                              boolean identityUnlocked) {
        String mode = ServerConfig.LEVEL_UP_NOTIFICATION.get();
        if (mode.equals("off")) {
            return;
        }
        // Keep old actionbar selections readable after the presentation refresh: both visual modes use the toast.
        boolean showCinematicToast = mode.equals("seal") || mode.equals("actionbar");
        ModNetwork.sendToPlayer(player, new LevelUpSealPayload(
                BuiltInRegistries.ITEM.getKey(item.getItem()).toString(), item.getHoverName().getString(), level,
                legacyTier, identityUnlocked, showCinematicToast));
        if (showCinematicToast) {
            return;
        }
        Component message = identityUnlocked
                ? Component.translatable("message.weaponleveling.identity_unlocked", item.getHoverName(),
                        Component.translatable("tooltip.weaponleveling.identity." + legacyTier.serializedName()))
                : Component.translatable("message.weaponleveling.level_up", item.getHoverName(), level);
        player.displayClientMessage(message, false);
    }
}
