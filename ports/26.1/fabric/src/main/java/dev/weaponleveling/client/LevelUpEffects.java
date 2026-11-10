package dev.weaponleveling.client;

import dev.weaponleveling.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Client-only sound response to a server-confirmed level-up. */
public final class LevelUpEffects {
    private LevelUpEffects() {
    }

    public static void play() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        if (ClientConfig.LEVEL_UP_SOUND_ENABLED.get() && ClientConfig.LEVEL_UP_SOUND_VOLUME.get() > 0) {
            minecraft.level.playLocalSound(minecraft.player, SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS,
                    ClientConfig.LEVEL_UP_SOUND_VOLUME.get().floatValue(), 1.12F);
        }
    }
}
