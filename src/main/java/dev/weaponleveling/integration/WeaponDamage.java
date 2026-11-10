package dev.weaponleveling.integration;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Reads an item's intrinsic main-hand attack damage without using the holder's attributes. */
public final class WeaponDamage {
    private WeaponDamage() {
    }

    public static double baseAttackDamage(ItemStack stack) {
        // Some mods (e.g. Silent Gear) build attack damage per stack in getDefaultAttributeModifiers(stack)
        // instead of the item's data component. Asking the item keeps that damage visible, and still reads the
        // component when a stack overrides it. It never goes through the attribute event, so level bonuses stay out.
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null || modifiers.modifiers().isEmpty()) {
            modifiers = stack.getItem().getDefaultAttributeModifiers(stack);
        }
        final double[] baseDamage = {0};
        modifiers.forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.equals(Attributes.ATTACK_DAMAGE)
                    && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                baseDamage[0] += modifier.amount();
            }
        });
        return Math.max(0, baseDamage[0]);
    }

    /**
     * The green vanilla main-hand tooltip includes the player's one-point base
     * attack attribute in addition to the item's attack-damage modifier.
     */
    public static double mainHandDamage(ItemStack stack) {
        double modifierDamage = baseAttackDamage(stack);
        return modifierDamage > 0 ? modifierDamage + 1.0 : 0;
    }

    /**
     * Baseline damage for a fully drawn standard-arrow shot. The live damage
     * event still supplies the authoritative result, so enchantments, critical
     * arrows, special ammunition, and modded projectile effects remain intact.
     */
    public static double standardRangedDamage(ItemStack stack) {
        if (stack.is(Items.BOW)) {
            return 6.0;
        }
        if (stack.is(Items.CROSSBOW)) {
            return 7.0;
        }
        // Compatible projectile weapons without a known vanilla base use the
        // normal fully drawn-arrow baseline rather than a misleading zero.
        return 6.0;
    }
}
