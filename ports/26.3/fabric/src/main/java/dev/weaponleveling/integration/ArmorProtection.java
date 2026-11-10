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
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);
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
