package dev.weaponleveling.mixin;

import dev.weaponleveling.platform.event.DamageEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * `LivingEntityDamageMixin`in `actuallyHurt` kancalarinin `Player` karsiligi. `Player`
 * `actuallyHurt`u kendi govdesiyle ezer ve `super`i cagirmaz; bu yuzden oyuncu hedefler (zirh XP'si,
 * oyuncuya vurus XP'si) icin o kancalar buraya da baglanmak zorundadir. Govde `LivingEntity`ninkiyle
 * ayni dizilimdedir (bytecode ile dogrulandi): arg slotuna 1. store `getDamageAfterArmorAbsorb`,
 * 2. store `getDamageAfterMagicAbsorb`, 3. store absorption cikarimi; tek son return `gameEvent`ten
 * sonradir. Kancalarin anlamlari birebir `LivingEntityDamageMixin`daki ile aynidir.
 */
@Mixin(Player.class)
public abstract class PlayerDamageMixin {

    @ModifyVariable(method = "actuallyHurt", at = @At(value = "STORE", ordinal = 1), argsOnly = true)
    private float weaponleveling$observePlayerMitigatedDamage(
        float value,
        ServerLevel level,
        DamageSource source,
        float damageAmount
    ) {
        DamageEvents.fireMitigated((LivingEntity) (Object) this, source, value);
        return value;
    }

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void weaponleveling$playerAppliedDamage(
        ServerLevel level,
        DamageSource source,
        float damageAmount,
        CallbackInfo callback
    ) {
        DamageEvents.fireApplied((LivingEntity) (Object) this, source, damageAmount);
    }
}
