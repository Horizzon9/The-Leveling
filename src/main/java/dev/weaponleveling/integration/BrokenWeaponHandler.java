package dev.weaponleveling.integration;

import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.server.BrokenItemReplacement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;

/** Shared server/client state helpers for a levelable item that has reached zero durability. */
public final class BrokenWeaponHandler {
    private BrokenWeaponHandler() {
    }

    public static boolean isBroken(ItemStack stack) {
        return LevelableItemEligibility.isEligible(stack) && isMarkedBroken(stack);
    }

    /**
     * Reads the broken flag without re-checking eligibility. Safe to use only where the caller has
     * already established the stack is a levelable weapon or armor piece; several client render paths
     * (tooltip and item-decoration rendering) run every frame and would otherwise repeat that check.
     */
    public static boolean isMarkedBroken(ItemStack stack) {
        return stack.getOrDefault(WeaponComponents.BROKEN.get(), false);
    }

    /** @return true when vanilla destruction must be suppressed for this stack. */
    public static boolean preserveOnBreak(ItemStack stack, LivingEntity owner) {
        if (!ServerConfig.PRESERVE_BROKEN_ITEMS.get() || !LevelableItemEligibility.isEligible(stack)
                || !stack.isDamageableItem()) {
            return false;
        }
        // Mob armor is not part of the player-owned progression system and must keep vanilla break behavior.
        if (ArmorEligibility.isEligible(stack) && !(owner instanceof ServerPlayer)) {
            return false;
        }
        stack.set(WeaponComponents.BROKEN.get(), true);
        stack.setDamageValue(stack.getMaxDamage());
        EquipmentSlot slot = owner instanceof ServerPlayer player ? slotHolding(player, stack) : null;
        moveBrokenArmorToInventory(stack, owner);
        if (slot != null) {
            BrokenItemReplacement.replaceIfEnabled((ServerPlayer) owner, stack, slot);
        }
        return true;
    }

    public static boolean canRepairWith(ItemStack brokenWeapon, ItemStack material) {
        return isBroken(brokenWeapon) && brokenWeapon.getItem().isValidRepairItem(brokenWeapon, material);
    }

    /** Restores exactly one durability point and keeps every other item component intact. */
    public static ItemStack repairOneDurability(ItemStack brokenWeapon) {
        ItemStack repaired = brokenWeapon.copy();
        repaired.remove(WeaponComponents.BROKEN.get());
        repaired.setDamageValue(Math.max(0, repaired.getMaxDamage() - 1));
        return repaired;
    }

    private static EquipmentSlot slotHolding(ServerPlayer player, ItemStack stack) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (player.getItemBySlot(slot) == stack) {
                return slot;
            }
        }
        return null;
    }

    private static void moveBrokenArmorToInventory(ItemStack stack, LivingEntity owner) {
        if (!(owner instanceof ServerPlayer player) || !(stack.getItem() instanceof ArmorItem armor)) {
            return;
        }
        EquipmentSlot slot = armor.getEquipmentSlot();
        if (player.getItemBySlot(slot) != stack) {
            return;
        }
        ItemStack brokenArmor = stack.copy();
        player.setItemSlot(slot, ItemStack.EMPTY);
        if (!player.getInventory().add(brokenArmor)) {
            player.drop(brokenArmor, false);
        }
        player.inventoryMenu.broadcastChanges();
    }
}
