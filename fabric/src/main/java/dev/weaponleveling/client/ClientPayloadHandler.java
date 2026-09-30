package dev.weaponleveling.client;

import dev.weaponleveling.network.ServerSettingsPayload;
import dev.weaponleveling.network.LevelUpSealPayload;
import dev.weaponleveling.network.KillXpPayload;
import net.minecraft.client.Minecraft;

public final class ClientPayloadHandler {
    private ClientPayloadHandler() {
    }

    public static void handleServerSettings(ServerSettingsPayload payload) {
        ClientServerSettings.update(payload);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof WeaponSettingsScreen settingsScreen) {
            settingsScreen.applyServerSettings(payload);
        }
    }

    public static void handleLevelUpSeal(LevelUpSealPayload payload) {
        Minecraft.getInstance().execute(() -> {
            if (payload.showOverlay()) {
                LevelUpSealOverlay.show(payload.itemId(), payload.itemName(), payload.level(),
                        payload.legacyTier(), payload.identityUnlocked());
            }
            LevelUpEffects.play();
        });
    }

    public static void handleKillXp(KillXpPayload payload) {
        Minecraft.getInstance().execute(() -> KillXpOverlay.show(payload.amount(), payload.streak()));
    }
}
