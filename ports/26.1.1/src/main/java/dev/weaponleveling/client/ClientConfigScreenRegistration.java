package dev.weaponleveling.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.function.Supplier;

/** Adds Weapon Settings to NeoForge's built-in Mod List configuration action. */
public final class ClientConfigScreenRegistration {
    private ClientConfigScreenRegistration() {
    }

    public static void register(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (Supplier<IConfigScreenFactory>) () -> (ignored, parent) -> new WeaponSettingsScreen(parent));
    }
}
