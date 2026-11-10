package dev.weaponleveling.mixin;

import dev.weaponleveling.server.ShieldService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blocking moved from {@code Player.hurtCurrentlyUsedShield} to the blocks-attacks component, which is
 * handed the damage of every blow a raised shield absorbs. One hook feeds shield XP and ends a block
 * when the shield breaks.
 */
@Mixin(BlocksAttacks.class)
public abstract class PlayerShieldMixin {
    @Inject(method = "hurtBlockingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;F)V", at = @At("HEAD"))
    private void weaponleveling$shieldBlocked(Level level, ItemStack stack, LivingEntity entity, InteractionHand hand,
                                             float damage, CallbackInfo callback) {
        if (entity instanceof ServerPlayer player) {
            ShieldService.onBlocked(player, damage);
        }
    }

    @Inject(method = "hurtBlockingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;F)V", at = @At("TAIL"))
    private void weaponleveling$stopBrokenShield(Level level, ItemStack stack, LivingEntity entity, InteractionHand hand,
                                                float damage, CallbackInfo callback) {
        if (entity instanceof ServerPlayer player) {
            ShieldService.stopIfBroken(player);
        }
    }
}
