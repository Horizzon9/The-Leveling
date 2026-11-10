package dev.weaponleveling.mixin;

import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.LevelableItemEligibility;
import dev.weaponleveling.server.DurabilityWarningService;
import dev.weaponleveling.server.ShieldService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/** Keeps levelable weapons at zero durability rather than allowing vanilla to remove their stack. */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @ModifyVariable(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"), argsOnly = true)
    private int weaponleveling$savedShieldDurability(int damage) {
        return ShieldService.reducedDurabilityDamage((ItemStack) (Object) this, damage);
    }

    @Unique
    private int weaponleveling$remainingBefore = -1;

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"))
    private void weaponleveling$rememberDurability(int damage, ServerLevel level, LivingEntity entity, Consumer<ItemStack> onBroken, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        weaponleveling$remainingBefore = entity instanceof ServerPlayer && self.isDamageableItem()
                ? self.getMaxDamage() - self.getDamageValue() : -1;
    }

    // After the hit, so Unbreaking and the shield saving have already decided how much durability was really lost.
    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("RETURN"))
    private void weaponleveling$warnLowDurability(int damage, ServerLevel level, LivingEntity entity, Consumer<ItemStack> onBroken, CallbackInfo ci) {
        int before = weaponleveling$remainingBefore;
        weaponleveling$remainingBefore = -1;
        if (before >= 0 && entity instanceof ServerPlayer player) {
            DurabilityWarningService.checkAndWarn(player, (ItemStack) (Object) this, before);
        }
    }

    @Redirect(method = "applyDamage(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void weaponleveling$preserveBrokenWeapon(ItemStack stack, int amount, int damage, LivingEntity entity,
                                                      Consumer<ItemStack> onBroken) {
        if (!BrokenWeaponHandler.preserveOnBreak(stack, entity)) {
            stack.shrink(amount);
        }
    }

    @Redirect(method = "applyDamage(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private void weaponleveling$skipVanillaBreakCallback(Consumer<Object> callback, Object rawItem) {
        // Vanilla's callback grants break statistics/advancements. The weapon was retained, so do not fire it.
        if (!(rawItem instanceof ItemStack broken) || !dev.weaponleveling.config.ServerConfig.PRESERVE_BROKEN_ITEMS.get()
                || !LevelableItemEligibility.isEligible(broken)) {
            callback.accept(rawItem);
        }
    }
}
