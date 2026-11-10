package dev.weaponleveling.server;

import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.integration.WeaponComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Applies newly earned permanent identities without running a per-tick inventory scan. */
public final class LegacyIdentityService {
    private LegacyIdentityService() {
    }

    public static void refreshInventory(ServerPlayer player) {
        boolean changed = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            WeaponProgress progress = stack.get(WeaponComponents.PROGRESS.get());
            if (progress == null) {
                continue;
            }
            WeaponProgress promoted = ServerConfig.legacyPolicy().promote(progress, ServerConfig.MAX_LEVEL.get());
            if (promoted != progress) {
                stack.set(WeaponComponents.PROGRESS.get(), promoted);
                changed = true;
            }
        }
        if (changed) {
            player.inventoryMenu.broadcastChanges();
        }
    }
}
