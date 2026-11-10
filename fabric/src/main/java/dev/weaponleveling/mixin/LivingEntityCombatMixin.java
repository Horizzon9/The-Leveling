package dev.weaponleveling.mixin;

import dev.weaponleveling.server.CombatEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Settle combat rewards only after vanilla and modded death protection have run. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityCombatMixin {
    @Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("RETURN"))
    private void weaponleveling$finishAttack(DamageSource source, float amount,
                                            CallbackInfoReturnable<Boolean> callback) {
        LivingEntity target = (LivingEntity) (Object) this;
        if (!target.level().isClientSide()) {
            CombatEvents.finishAttack(target, source);
        }
    }
}
