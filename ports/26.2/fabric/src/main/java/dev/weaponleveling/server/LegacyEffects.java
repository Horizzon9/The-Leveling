package dev.weaponleveling.server;

import dev.weaponleveling.config.ServerConfig;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;

/** World effects that mark the deeds of Legacy items. Purely visual; they never affect combat. */
public final class LegacyEffects {
    private static final DustParticleOptions AMBER_DUST = new DustParticleOptions(
            ARGB.colorFromFloat(1.0F, 1.0F, 0.66F, 0.16F), 1.3F);

    private LegacyEffects() {
    }

    /** An amber burst where a creature fell to a Legacy weapon, visible to everyone nearby. */
    public static void killBurst(LivingEntity target) {
        if (!ServerConfig.LEGACY_KILL_EFFECT.get() || !(target.level() instanceof ServerLevel level)) {
            return;
        }
        double x = target.getX();
        double y = target.getY() + target.getBbHeight() * 0.5;
        double z = target.getZ();
        double spread = Math.max(0.3, target.getBbWidth() * 0.6);
        level.sendParticles(AMBER_DUST, x, y, z, 28, spread, target.getBbHeight() * 0.4, spread, 0.02);
        level.sendParticles(ParticleTypes.END_ROD, x, y, z, 8, spread * 0.5, 0.3, spread * 0.5, 0.06);
        level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.4F);
    }
}
