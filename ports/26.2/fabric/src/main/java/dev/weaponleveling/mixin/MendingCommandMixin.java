package dev.weaponleveling.mixin;

import java.util.Collection;
import net.minecraft.core.Holder;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Reapplying Mending is harmless; only other enchantments can conflict with it. */
@Mixin(EnchantCommand.class)
public abstract class MendingCommandMixin {
    @Redirect(method = "enchant", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;isEnchantmentCompatible(Ljava/util/Collection;Lnet/minecraft/core/Holder;)Z"))
    private static boolean weaponleveling$allowExistingMending(Collection<Holder<Enchantment>> existing,
                                                              Holder<Enchantment> requested) {
        if (requested.is(Enchantments.MENDING)) {
            existing = existing.stream().filter(enchantment -> !enchantment.equals(requested)).toList();
        }
        return EnchantmentHelper.isEnchantmentCompatible(existing, requested);
    }
}
