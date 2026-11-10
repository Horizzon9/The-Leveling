package dev.weaponleveling.mixin;

import dev.weaponleveling.server.CombatEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Settle combat rewards only after vanilla and modded death protection have run. Server-side damage
 * goes through {@code hurtServer}; {@code Player} overrides it but finishes through {@code super}.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityCombatMixin {
    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void weaponleveling$finishAttack(ServerLevel level, DamageSource source, float amount,
                                            CallbackInfoReturnable<Boolean> callback) {
        CombatEvents.finishAttack((LivingEntity) (Object) this, source);
    }
}
