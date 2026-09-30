package dev.weaponleveling.integration;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/** Every ordinary player armor item is levelable unless a datapack explicitly excludes it. */
public final class ArmorEligibility {
    private ArmorEligibility() {
    }

    public static boolean isEligible(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(ArmorTags.EXCLUDED_ARMOR)
                && (stack.is(ArmorTags.LEVELABLE_ARMOR) || stack.getItem() instanceof ArmorItem);
    }
}
