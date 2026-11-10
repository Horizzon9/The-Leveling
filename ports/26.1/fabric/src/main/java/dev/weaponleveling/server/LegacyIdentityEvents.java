package dev.weaponleveling.server;

import dev.weaponleveling.WeaponLevelingMod;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class LegacyIdentityEvents {
    private LegacyIdentityEvents() {
    }

    public static void register() {
        // NeoForge `PlayerEvent.PlayerLoggedInEvent` karsiligi: oyuncu oyun asamasina girdiginde.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                LegacyIdentityService.refreshInventory(handler.player));
    }
}
