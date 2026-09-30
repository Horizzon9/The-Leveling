package dev.weaponleveling.network;

import dev.weaponleveling.client.ClientPayloadHandler;
import dev.weaponleveling.server.ServerSettingsService;
import dev.weaponleveling.server.ChatShareService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;


public final class NetworkHandler {
    private NetworkHandler() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("10")
                .playToClient(ServerSettingsPayload.TYPE, ServerSettingsPayload.STREAM_CODEC,
                        NetworkHandler::handleServerSettings)
                .playToClient(LevelUpSealPayload.TYPE, LevelUpSealPayload.STREAM_CODEC,
                        NetworkHandler::handleLevelUpSeal)
                .playToClient(KillXpPayload.TYPE, KillXpPayload.STREAM_CODEC,
                        NetworkHandler::handleKillXp)
                .playToServer(RequestServerSettingsPayload.TYPE, RequestServerSettingsPayload.STREAM_CODEC,
                        NetworkHandler::handleServerSettingsRequest)
                .playToServer(UpdateServerSettingsPayload.TYPE, UpdateServerSettingsPayload.STREAM_CODEC,
                        NetworkHandler::handleServerSettingsUpdate)
                .playToServer(ShareItemPayload.TYPE, ShareItemPayload.STREAM_CODEC,
                        NetworkHandler::handleShareItem);
    }

    private static void handleServerSettings(ServerSettingsPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientPayloadHandler.handleServerSettings(payload);
        }
    }

    private static void handleLevelUpSeal(LevelUpSealPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientPayloadHandler.handleLevelUpSeal(payload);
        }
    }

    private static void handleKillXp(KillXpPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientPayloadHandler.handleKillXp(payload);
        }
    }

    private static void handleServerSettingsRequest(RequestServerSettingsPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            context.reply(ServerSettingsService.snapshot(player));
        }
    }

    private static void handleServerSettingsUpdate(UpdateServerSettingsPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        String rejection = ServerSettingsService.apply(player, payload.changes());
        if (rejection != null) {
            player.displayClientMessage(Component.translatable("message.weaponleveling.settings_rejected." + rejection), true);
            sendServerSettings(player);
            return;
        }
        broadcastServerSettings(player.server);
        player.displayClientMessage(Component.translatable("message.weaponleveling.settings_saved"), true);
    }

    private static void handleShareItem(ShareItemPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ChatShareService.share(player, payload.containerId(), payload.menuSlot());
        }
    }

    public static void sendServerSettings(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, ServerSettingsService.snapshot(player));
    }

    public static void broadcastServerSettings(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendServerSettings(player);
        }
    }

}
