package dev.weaponleveling.client;

import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.core.model.LegacyPersona;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/** Data passed from the normal tooltip event to the client-side pixel renderer. */
public record LevelProgressTooltip(ItemStack item, int level, int maxLevel, long currentXp, long requiredXp,
                                   boolean maximumLevel, LegacyTier legacyTier,
                                   LegacyPersona persona) implements TooltipComponent {
}
