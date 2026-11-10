package dev.weaponleveling.integration;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Reads an armor item's intrinsic Armor value without including this mod's level modifier. */
public final class ArmorProtection {
    private ArmorProtection() {
    }

    public static double baseArmor(ItemStack stack) {
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null || modifiers.modifiers().isEmpty()) {
            // Per-stack defaults cover mods that compute armor from the stack's own data.
            modifiers = stack.getItem().getDefaultAttributeModifiers(stack);
        }
        final double[] baseArmor = {0};
        modifiers.modifiers().forEach(entry -> {
            if (entry.attribute().equals(Attributes.ARMOR)
                    && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                baseArmor[0] += entry.modifier().amount();
            }
        });
        return baseArmor[0];
    }
}
