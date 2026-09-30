package dev.weaponleveling.platform.net;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Paket gonderimi - NeoForge'un `PacketDistributor`'inin bu kod tabanindaki karsiligi.
 *
 * Paket SINIFLARI degismedi: mod, vanilla `CustomPacketPayload` + `StreamCodec` kullandigi icin
 * (NeoForge'a ozel bir paket tipi degil) tum payload dosyalari Fabric'te oldugu gibi derlenir.
 * Degisen yalnizca kayit ve gonderim cagrilaridir.
 */
public final class ModNetwork {
    private ModNetwork() {
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    /**
     * Istemciden sunucuya gonderim. Istemciye ozel siniflari ayri tutmak icin ayri bir sinifa
     * devreder - bu metot yalnizca istemciden cagirildigi icin adanmis sunucuda o sinif hic
     * yuklenmez.
     */
    public static void sendToServer(CustomPacketPayload payload) {
        ClientPacketSender.send(payload);
    }
}
