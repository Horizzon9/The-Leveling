package dev.weaponleveling.platform.net;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Istemci->sunucu gonderimini izole eder. `ClientPlayNetworking` istemciye ozel Minecraft
 * siniflarina baglidir; bu sinif yalnizca {@link ModNetwork#sendToServer} cagrildiginda yuklenir,
 * yani adanmis sunucuda hicbir zaman yuklenmez.
 */
final class ClientPacketSender {
    private ClientPacketSender() {
    }

    static void send(CustomPacketPayload payload) {
        // Sunucu bu payload turunu tanimiyorsa (orn. mod yalnizca istemcide kuruluysa) Fabric
        // gonderimde istisna atar. Mod her iki tarafta da kuruluyken `canSend` daima true'dur.
        if (ClientPlayNetworking.canSend(payload.type())) {
            ClientPlayNetworking.send(payload);
        }
    }
}
