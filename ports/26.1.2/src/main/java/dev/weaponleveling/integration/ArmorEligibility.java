package dev.weaponleveling.integration;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Every ordinary player armor item and every shield is levelable unless a datapack explicitly excludes it.
 * Shields count as armor here so tooltips, auras, identity, transfers and soulbound apply to them unchanged.
 */
public final class ArmorEligibility {
    private ArmorEligibility() {
    }

    public static boolean isEligible(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(ArmorTags.EXCLUDED_ARMOR)
                && (stack.is(ArmorTags.LEVELABLE_ARMOR) || armorSlot(stack) != null || isElytra(stack) || isShield(stack));
    }

    public static boolean isShield(ItemStack stack) {
        // Modded shields carry the blocks-attacks component without extending ShieldItem.
        return stack.getItem() instanceof ShieldItem || stack.getItem().components().has(DataComponents.BLOCKS_ATTACKS);
    }

    public static boolean isArmor(ItemStack stack) {
        return !stack.isEmpty() && (armorSlot(stack) != null || isElytra(stack) || stack.is(ArmorTags.LEVELABLE_ARMOR));
    }

    /** Elytra are no longer their own item class; they are recognized by the glider component. */
    public static boolean isElytra(ItemStack stack) {
        return stack.getItem().components().has(DataComponents.GLIDER);
    }

    /**
     * The slot a built-in armor piece is worn in, or null when the item is not one. An armor piece
     * declares an armor slot through its equippable component and carries an Armor modifier, which
     * keeps elytra, carved pumpkins and mob heads out. Only the item's own components are read, so
     * the result never depends on per-stack component changes or on attribute modifier events.
     */
    public static EquipmentSlot armorSlot(ItemStack stack) {
        Item item = stack.getItem();
        Equippable equippable = item.components().get(DataComponents.EQUIPPABLE);
        if (equippable == null || !equippable.slot().isArmor() || !hasArmorModifier(item)) {
            return null;
        }
        return equippable.slot();
    }

    private static boolean hasArmorModifier(Item item) {
        ItemAttributeModifiers modifiers = item.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().equals(Attributes.ARMOR)) {
                return true;
            }
        }
        return false;
    }
}
