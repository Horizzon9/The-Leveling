package dev.weaponleveling.server;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.network.NetworkHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;

@EventBusSubscriber(modid = WeaponLevelingMod.MOD_ID)
public final class PlayerLifecycleEvents {
    private PlayerLifecycleEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHandler.sendServerSettings(player);
            SoulboundService.restore(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SoulboundService.restore(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        KillStreakTracker.forget(event.getEntity().getUUID());
    }
}
