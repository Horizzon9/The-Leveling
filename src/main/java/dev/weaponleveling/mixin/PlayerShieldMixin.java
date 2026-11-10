package dev.weaponleveling.mixin;

import dev.weaponleveling.server.ShieldService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla calls {@code hurtCurrentlyUsedShield} with the full damage of every blow a raised shield
 * absorbs, on both loaders, so one hook feeds shield XP and ends a block when the shield breaks.
 */
@Mixin(Player.class)
public abstract class PlayerShieldMixin {
    @Inject(method = "hurtCurrentlyUsedShield", at = @At("HEAD"))
    private void weaponleveling$shieldBlocked(float damage, CallbackInfo callback) {
        if ((Object) this instanceof ServerPlayer player) {
            ShieldService.onBlocked(player, damage);
        }
    }

    @Inject(method = "hurtCurrentlyUsedShield", at = @At("TAIL"))
    private void weaponleveling$stopBrokenShield(float damage, CallbackInfo callback) {
        if ((Object) this instanceof ServerPlayer player) {
            ShieldService.stopIfBroken(player);
        }
    }
}
