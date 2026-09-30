package dev.weaponleveling.client;

import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;

/**
 * Ozel ipucu verisini cizilebilir bilesene cevirir - NeoForge'un
 * `RegisterClientTooltipComponentFactoriesEvent` karsiligi Fabric'te `TooltipComponentCallback`tir.
 */
public final class ClientTooltipComponents {
    private ClientTooltipComponents() {
    }

    public static void register() {
        TooltipComponentCallback.EVENT.register(data ->
                data instanceof LevelProgressTooltip tooltip ? new ClientLevelProgressTooltip(tooltip) : null);
    }
}
