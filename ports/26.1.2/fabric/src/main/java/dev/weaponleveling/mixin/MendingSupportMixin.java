package dev.weaponleveling.mixin;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps XP-repair enchantments usable when an item-support tag is incomplete. */
@Mixin(Enchantment.class)
public abstract class MendingSupportMixin {
    @Shadow
    public abstract DataComponentMap effects();

    @Inject(method = {"isSupportedItem", "canEnchant"}, at = @At("RETURN"), cancellable = true)
    private void weaponleveling$allowMendingOnDamageableItems(ItemStack stack,
                                                            CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValueZ() && effects().has(EnchantmentEffectComponents.REPAIR_WITH_XP)
                && stack.isDamageableItem() && !stack.is(Items.WOLF_ARMOR)) {
            callback.setReturnValue(true);
        }
    }
}
