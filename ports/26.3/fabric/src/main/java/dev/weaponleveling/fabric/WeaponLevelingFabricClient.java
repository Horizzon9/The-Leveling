package dev.weaponleveling.fabric;

import dev.weaponleveling.client.ClientContentRegistration;
import dev.weaponleveling.client.ClientEvents;
import dev.weaponleveling.client.ClientItemDecorations;
import dev.weaponleveling.client.ClientKeyMappings;
import dev.weaponleveling.client.ClientTooltipComponents;
import dev.weaponleveling.network.NetworkHandler;
import net.fabricmc.api.ClientModInitializer;

/**
 * Istemci giris noktasi. Fabric "main" giris noktasini "client"tan once calistirir, yani buraya
 * gelindiginde icerik kaydi ve paket turleri hazirdir.
 */
public final class WeaponLevelingFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NetworkHandler.registerClientReceivers();
        ClientContentRegistration.registerMenuScreens();
        ClientTooltipComponents.register();
        ClientItemDecorations.register();
        ClientKeyMappings.register();
        ClientEvents.register();
    }
}
