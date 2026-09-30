package dev.weaponleveling.client;

import dev.weaponleveling.config.ClientConfig;
import dev.weaponleveling.core.chronicle.WeaponChronicle;
import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.WeaponComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Cosmetic rewards for long use. They change only how an item looks, never how it fights:
 * the item name takes a milestone color as the item approaches the level cap, a given title is
 * shown under the name, and Relic or Legacy items shimmer like enchanted ones.
 */
public final class ItemNameStyle {
    private static final double[] MILESTONES = {0.10, 0.25, 0.50, 1.00};
    private static final ChatFormatting[] MILESTONE_COLORS = {
            ChatFormatting.GREEN, ChatFormatting.AQUA, ChatFormatting.LIGHT_PURPLE, ChatFormatting.GOLD
    };

    private ItemNameStyle() {
    }

    /** Recolors the name line and inserts the item's title under it. Call after all other lines are added. */
    public static void decorate(List<Component> tooltip, ItemStack stack, int level, int maxLevel) {
        if (tooltip.isEmpty()) {
            return;
        }
        ChatFormatting color = ClientConfig.MILESTONE_NAME_COLORS.get() ? milestoneColor(level, maxLevel) : null;
        if (color != null) {
            tooltip.set(0, tooltip.get(0).copy().withStyle(color));
        }
        WeaponChronicle chronicle = stack.get(WeaponComponents.CHRONICLE.get());
        if (chronicle != null && chronicle.hasTitle()) {
            tooltip.add(1, Component.literal("«" + chronicle.title() + "»")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        }
    }

    /** Color of the highest milestone reached, or null below the first one. */
    public static ChatFormatting milestoneColor(int level, int maxLevel) {
        if (level <= 0 || maxLevel <= 0) {
            return null;
        }
        double fraction = level / (double) maxLevel;
        ChatFormatting reached = null;
        for (int index = 0; index < MILESTONES.length; index++) {
            if (fraction >= MILESTONES[index]) {
                reached = MILESTONE_COLORS[index];
            }
        }
        return reached;
    }

    /**
     * Whether a Relic or Legacy item should shimmer. An explicit glint override set by a data pack,
     * command or other mod always wins, and broken items stay dull.
     */
    public static boolean hasIdentityGlint(ItemStack stack) {
        if (!ClientConfig.RELIC_GLINT.get() || stack.has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)) {
            return false;
        }
        WeaponProgress progress = stack.get(WeaponComponents.PROGRESS.get());
        return progress != null && progress.legacyTier() != LegacyTier.NONE
                && !BrokenWeaponHandler.isMarkedBroken(stack);
    }
}
