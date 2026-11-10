package dev.weaponleveling.client;

import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

public final class ClientTooltipComponents {
    private ClientTooltipComponents() {
    }

    public static void register(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(LevelProgressTooltip.class, ClientLevelProgressTooltip::new);
    }
}
