package dev.weaponleveling.client;

import dev.weaponleveling.config.ClientConfig;
import dev.weaponleveling.network.LowDurabilityWarningPayload;
import dev.weaponleveling.network.ServerSettingsPayload;
import dev.weaponleveling.network.LevelUpSealPayload;
import dev.weaponleveling.network.KillXpPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public final class ClientPayloadHandler {
    private ClientPayloadHandler() {
    }

    public static void handleServerSettings(ServerSettingsPayload payload) {
        ClientServerSettings.update(payload);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() instanceof WeaponSettingsScreen settingsScreen) {
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

    public static void handleLowDurabilityWarning(LowDurabilityWarningPayload payload) {
        Minecraft.getInstance().execute(() -> {
            if (ClientConfig.SHOW_LOW_DURABILITY_WARNING.get()) {
                Minecraft mc = Minecraft.getInstance();
                Component msg = Component.translatable("message.weaponleveling.low_durability",
                        payload.itemName(), payload.remaining()).withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
                mc.gui.hud.setOverlayMessage(msg, false);
                if (mc.player != null) {
                    mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8f, 0.5f);
                }
            }
        });
    }
}
