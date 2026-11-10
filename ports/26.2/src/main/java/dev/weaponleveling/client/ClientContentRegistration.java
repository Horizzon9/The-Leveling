package dev.weaponleveling.client;

import dev.weaponleveling.content.LegacyTransferScreen;
import dev.weaponleveling.content.ModContent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ClientContentRegistration {
    private ClientContentRegistration() {
    }

    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModContent.LEGACY_TRANSFER_MENU.get(), LegacyTransferScreen::new);
    }
}
