package dev.weaponleveling.integration;

import dev.weaponleveling.WeaponLevelingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Pickaxes, shovels and hoes level by mining. Axes stay weapons: they level by fighting, so one axe
 * never mixes combat and mining progress. Datapacks add modded tools with #weaponleveling:levelable_tools.
 */
public final class ToolEligibility {
    public static final TagKey<Item> LEVELABLE_TOOLS = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, "levelable_tools"));

    private ToolEligibility() {
    }

    public static boolean isEligible(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(WeaponTags.EXCLUDED_WEAPONS)
                && (stack.is(LEVELABLE_TOOLS) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HOES));
    }
}
