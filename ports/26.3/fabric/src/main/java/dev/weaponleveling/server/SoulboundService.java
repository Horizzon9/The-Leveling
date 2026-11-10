package dev.weaponleveling.server;

import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.integration.WeaponComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.ArrayList;
import java.util.List;

/**
 * Soulbound items: Relic or Legacy equipment that stays with its owner through death.
 *
 * Items are taken out of the inventory just before the death drops are made and are written to
 * {@link SoulboundStash} at once, so they survive a server stop between death and respawn. On
 * respawn each item returns to the slot it came from; if that slot is taken it goes to any free
 * slot, and if the inventory is full it drops at the player's feet rather than being lost.
 */
public final class SoulboundService {
    private SoulboundService() {
    }

    /** Called right before a dying player's inventory is dropped. */
    public static void stashOnDeath(ServerPlayer player) {
        LegacyTier minimum = minimumTier();
        if (minimum == null || player.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
            return;
        }
        Inventory inventory = player.getInventory();
        List<SoulboundStash.Entry> kept = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (isSoulbound(stack, minimum)) {
                kept.add(new SoulboundStash.Entry(slot, stack.copy()));
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
        if (!kept.isEmpty()) {
            SoulboundStash.get(player.level().getServer()).add(player.getUUID(), kept);
        }
    }

    /** Gives back everything stashed for the player. Safe to call at any time; does nothing when empty. */
    public static void restore(ServerPlayer player) {
        if (!player.isAlive()) {
            return;
        }
        List<SoulboundStash.Entry> kept = SoulboundStash.get(player.level().getServer()).take(player.getUUID());
        Inventory inventory = player.getInventory();
        for (SoulboundStash.Entry entry : kept) {
            ItemStack stack = entry.stack();
            if (entry.slot() < inventory.getContainerSize() && inventory.getItem(entry.slot()).isEmpty()) {
                inventory.setItem(entry.slot(), stack);
            } else if (!inventory.add(stack)) {
                player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        }
        if (!kept.isEmpty()) {
            player.inventoryMenu.broadcastChanges();
        }
    }

    private static boolean isSoulbound(ItemStack stack, LegacyTier minimum) {
        WeaponProgress progress = stack.get(WeaponComponents.PROGRESS.get());
        return progress != null && progress.legacyTier().ordinal() >= minimum.ordinal();
    }

    /** Lowest identity that is soulbound, or null when the feature is off. */
    private static LegacyTier minimumTier() {
        return switch (ServerConfig.SOULBOUND_TIER.get()) {
            case "relic" -> LegacyTier.RELIC;
            case "legacy" -> LegacyTier.LEGACY;
            default -> null;
        };
    }
}
