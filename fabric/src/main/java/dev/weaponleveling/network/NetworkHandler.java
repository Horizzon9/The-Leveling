package dev.weaponleveling.network;

import dev.weaponleveling.platform.net.ModNetwork;
import dev.weaponleveling.server.ChatShareService;
import dev.weaponleveling.server.ServerSettingsService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Paket turlerinin kaydi ve sunucu tarafi alicilari.
 *
 * NeoForge'da tek bir `registrar("10")` zinciri hem tur kaydini hem isleyiciyi hem de yonu
 * belirliyordu. Fabric'te bunlar ayrilir: tur kaydi `PayloadTypeRegistry`'ye YONE GORE yapilir,
 * alicilar ise taraf basina kaydedilir. Yonler NeoForge surumundekiyle birebir aynidir - istemciye
 * giden uc paket yalnizca S2C, sunucuya giden uc paket yalnizca C2S olarak kaydedilir; yani bir
 * istemci sunucuya yalnizca S2C olmasi gereken bir paketi gonderemez.
 *
 * Istemci alicilari {@link #registerClientReceivers()} icindedir ve YALNIZCA istemci giris
 * noktasindan cagrilir (istemciye ozel siniflara dokunduklari icin).
 */
public final class NetworkHandler {
    private NetworkHandler() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ServerSettingsPayload.TYPE, ServerSettingsPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(LevelUpSealPayload.TYPE, LevelUpSealPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(KillXpPayload.TYPE, KillXpPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(LowDurabilityWarningPayload.TYPE, LowDurabilityWarningPayload.STREAM_CODEC);

        PayloadTypeRegistry.playC2S().register(RequestServerSettingsPayload.TYPE,
                RequestServerSettingsPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateServerSettingsPayload.TYPE,
                UpdateServerSettingsPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ShareItemPayload.TYPE, ShareItemPayload.STREAM_CODEC);

        // `server().execute(...)`: isleyiciler oyun ana is parcaciginda calismalidir - NeoForge
        // surumunde de oyle calisiyorlardi (envanter/menu ve config durumuna dokunuyorlar).
        // Minecraft'in olay dongusu zaten ayni is parcacigindaysa gorevi ANINDA calistirir, yani
        // ek gecikme getirmez.
        ServerPlayNetworking.registerGlobalReceiver(RequestServerSettingsPayload.TYPE,
                (payload, context) -> context.server().execute(
                        () -> handleServerSettingsRequest(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(UpdateServerSettingsPayload.TYPE,
                (payload, context) -> context.server().execute(
                        () -> handleServerSettingsUpdate(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(ShareItemPayload.TYPE,
                (payload, context) -> context.server().execute(
                        () -> handleShareItem(context.player(), payload)));
    }

    /** Yalnizca istemci giris noktasindan cagirilir. */
    public static void registerClientReceivers() {
        ClientNetworkHandler.register();
    }

    private static void handleServerSettingsRequest(ServerPlayer player) {
        ModNetwork.sendToPlayer(player, ServerSettingsService.snapshot(player));
    }

    private static void handleServerSettingsUpdate(ServerPlayer player, UpdateServerSettingsPayload payload) {
        String rejection = ServerSettingsService.apply(player, payload.changes());
        if (rejection != null) {
            player.displayClientMessage(
                    Component.translatable("message.weaponleveling.settings_rejected." + rejection), true);
            sendServerSettings(player);
            return;
        }
        broadcastServerSettings(player.server);
        player.displayClientMessage(Component.translatable("message.weaponleveling.settings_saved"), true);
    }

    private static void handleShareItem(ServerPlayer player, ShareItemPayload payload) {
        ChatShareService.share(player, payload.containerId(), payload.menuSlot());
    }

    public static void sendServerSettings(ServerPlayer player) {
        ModNetwork.sendToPlayer(player, ServerSettingsService.snapshot(player));
    }

    public static void broadcastServerSettings(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendServerSettings(player);
        }
    }
}
