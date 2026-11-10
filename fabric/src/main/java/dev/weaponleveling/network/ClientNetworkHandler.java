package dev.weaponleveling.network;

import dev.weaponleveling.client.ClientPayloadHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Sunucu->istemci paketlerinin alicilari. Istemciye ozel siniflara dokundugu icin ayri tutulur ve
 * yalnizca istemci giris noktasindan cagirilir - adanmis sunucuda bu sinif hic yuklenmez.
 *
 * NeoForge surumunde ayni islevi `NetworkHandler` icindeki `FMLEnvironment.dist == Dist.CLIENT`
 * kontrolleri goruyordu; Fabric'te taraf ayrimi sinif duzeyinde yapilir, bu yuzden o kontrollere
 * gerek kalmadi.
 */
final class ClientNetworkHandler {
    private ClientNetworkHandler() {
    }

    static void register() {
        // `client().execute(...)`: isleyiciler istemcinin ana is parcaciginda calismalidir -
        // hepsi HUD/ekran durumuna dokunuyor.
        ClientPlayNetworking.registerGlobalReceiver(ServerSettingsPayload.TYPE,
                (payload, context) -> context.client().execute(
                        () -> ClientPayloadHandler.handleServerSettings(payload)));
        ClientPlayNetworking.registerGlobalReceiver(LevelUpSealPayload.TYPE,
                (payload, context) -> context.client().execute(
                        () -> ClientPayloadHandler.handleLevelUpSeal(payload)));
        ClientPlayNetworking.registerGlobalReceiver(KillXpPayload.TYPE,
                (payload, context) -> context.client().execute(
                        () -> ClientPayloadHandler.handleKillXp(payload)));
        ClientPlayNetworking.registerGlobalReceiver(LowDurabilityWarningPayload.TYPE,
                (payload, context) -> context.client().execute(
                        () -> ClientPayloadHandler.handleLowDurabilityWarning(payload)));
    }
}
