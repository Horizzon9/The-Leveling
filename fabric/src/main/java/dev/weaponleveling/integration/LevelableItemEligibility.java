package dev.weaponleveling.integration;

import net.minecraft.world.item.ItemStack;

/** Shared gate for systems that apply equally to levelable weapons and armor. */
public final class LevelableItemEligibility {
    private LevelableItemEligibility() {
    }

    public static boolean isEligible(ItemStack stack) {
        return WeaponEligibility.isEligible(stack) || ArmorEligibility.isEligible(stack);
    }

    /** Client-side variant using the automatic-detection setting received from the server. */
    public static boolean isEligible(ItemStack stack, boolean autoDetectExternalWeapons) {
        return WeaponEligibility.isEligible(stack, autoDetectExternalWeapons) || ArmorEligibility.isEligible(stack);
    }

    public static boolean isEligibleOnServer(ItemStack stack) {
        return WeaponEligibility.isEligibleOnServer(stack) || ArmorEligibility.isEligible(stack);
    }
}
