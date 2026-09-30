package dev.weaponleveling.mixin;

import dev.weaponleveling.client.ItemNameStyle;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Client-only: Relic and Legacy items render with the enchantment glint. */
@Mixin(ItemStack.class)
public abstract class ItemStackFoilMixin {
    @Inject(method = "hasFoil", at = @At("RETURN"), cancellable = true)
    private void weaponleveling$identityGlint(CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValueZ() && ItemNameStyle.hasIdentityGlint((ItemStack) (Object) this)) {
            callback.setReturnValue(true);
        }
    }
}
