package dev.weaponleveling.server;

import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.integration.ArmorEligibility;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.ToolEligibility;
import dev.weaponleveling.integration.WeaponEligibility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * Optional swap-in of a matching item when a levelable item breaks (off by default). The broken item
 * is kept: it takes the replacement's inventory slot, so nothing is lost and it can be repaired later.
 */
public final class BrokenItemReplacement {
    private BrokenItemReplacement() {
    }

    /** @param slot the equipment slot the item broke in, or null when it was not worn or held by the player */
    public static void replaceIfEnabled(ServerPlayer player, ItemStack broken, EquipmentSlot slot) {
        if (slot == null || !ServerConfig.AUTO_REPLACE_BROKEN.get()) {
            return;
        }
        Inventory inventory = player.getInventory();
        int index = findReplacement(inventory, broken, true);
        if (index < 0) {
            index = findReplacement(inventory, broken, false);
        }
        if (index < 0) {
            return;
        }
        ItemStack replacement = inventory.items.get(index);
        if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            // The broken piece was already moved into the inventory; the replacement is worn instead.
            inventory.items.set(index, ItemStack.EMPTY);
            player.setItemSlot(slot, replacement);
        } else {
            inventory.items.set(index, broken);
            player.setItemSlot(slot, replacement);
        }
        player.sendSystemMessage(Component.translatable("message.weaponleveling.auto_replaced",
                broken.getHoverName(), replacement.getHoverName()));
        player.inventoryMenu.broadcastChanges();
    }

    private static int findReplacement(Inventory inventory, ItemStack broken, boolean sameItem) {
        for (int index = 0; index < inventory.items.size(); index++) {
            ItemStack candidate = inventory.items.get(index);
            if (candidate.isEmpty() || candidate == broken || BrokenWeaponHandler.isBroken(candidate)
                    || (candidate.isDamageableItem() && candidate.getDamageValue() >= candidate.getMaxDamage())) {
                continue;
            }
            if (sameItem ? candidate.getItem() == broken.getItem() : sameKind(broken, candidate)) {
                return index;
            }
        }
        return -1;
    }

    private static boolean sameKind(ItemStack broken, ItemStack candidate) {
        if (broken.getItem() instanceof ArmorItem armor) {
            return candidate.getItem() instanceof ArmorItem other && other.getEquipmentSlot() == armor.getEquipmentSlot();
        }
        if (ArmorEligibility.isShield(broken)) {
            return ArmorEligibility.isShield(candidate);
        }
        if (ToolEligibility.isEligible(broken)) {
            return ToolEligibility.isEligible(candidate);
        }
        return WeaponEligibility.isEligibleOnServer(candidate)
                && WeaponEligibility.isRanged(candidate) == WeaponEligibility.isRanged(broken);
    }
}
