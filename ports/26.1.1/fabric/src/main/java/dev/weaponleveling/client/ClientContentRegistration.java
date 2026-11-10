package dev.weaponleveling.client;

import dev.weaponleveling.content.LegacyTransferScreen;
import dev.weaponleveling.content.ModContent;
import net.minecraft.client.gui.screens.MenuScreens;

/**
 * Menu -> ekran eslemesi. NeoForge'un `RegisterMenuScreensEvent`'i yerine vanilla
 * `MenuScreens.register` dogrudan istemci baslatmada cagirilir.
 */
public final class ClientContentRegistration {
    private ClientContentRegistration() {
    }

    public static void registerMenuScreens() {
        MenuScreens.register(ModContent.LEGACY_TRANSFER_MENU.get(), LegacyTransferScreen::new);
    }
}
