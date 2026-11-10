package dev.weaponleveling.integration;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

/**
 * Every ordinary player armor item and every shield is levelable unless a datapack explicitly excludes it.
 * Shields count as armor here so tooltips, auras, identity, transfers and soulbound apply to them unchanged.
 */
public final class ArmorEligibility {
    private ArmorEligibility() {
    }

    public static boolean isEligible(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(ArmorTags.EXCLUDED_ARMOR)
                && (stack.is(ArmorTags.LEVELABLE_ARMOR) || stack.getItem() instanceof ArmorItem
                || stack.getItem() instanceof ElytraItem || isShield(stack));
    }

    public static boolean isShield(ItemStack stack) {
        return stack.getItem() instanceof ShieldItem;
    }

    public static boolean isArmor(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof ArmorItem
                || stack.getItem() instanceof ElytraItem || stack.is(ArmorTags.LEVELABLE_ARMOR));
    }
}
