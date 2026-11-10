package dev.weaponleveling.client;

import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;

/**
 * Ozel ipucu verisini cizilebilir bilesene cevirir - NeoForge'un
 * `RegisterClientTooltipComponentFactoriesEvent` karsiligi Fabric'te `ClientTooltipComponentCallback`tir.
 */
public final class ClientTooltipComponents {
    private ClientTooltipComponents() {
    }

    public static void register() {
        ClientTooltipComponentCallback.EVENT.register(data ->
                data instanceof LevelProgressTooltip tooltip ? new ClientLevelProgressTooltip(tooltip) : null);
    }
}
