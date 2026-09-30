package dev.weaponleveling.server;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.network.NetworkHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerLifecycleEvents {
    private PlayerLifecycleEvents() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                NetworkHandler.sendServerSettings(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                KillStreakTracker.forget(handler.player.getUUID()));
    }
}
