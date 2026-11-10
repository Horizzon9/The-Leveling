package dev.weaponleveling.server;

import dev.weaponleveling.WeaponLevelingMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = WeaponLevelingMod.MOD_ID)
public final class LegacyIdentityEvents {
    private LegacyIdentityEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LegacyIdentityService.refreshInventory(player);
        }
    }
}
