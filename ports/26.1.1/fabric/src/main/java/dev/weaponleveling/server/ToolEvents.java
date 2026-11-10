package dev.weaponleveling.server;

import dev.weaponleveling.platform.event.ItemAttributeEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Fabric wiring for {@link ToolService}. BEFORE stops a broken tool from breaking anything; AFTER only
 * fires for blocks that were really broken, which matches NeoForge's uncancelled BreakEvent.
 */
public final class ToolEvents {
    private ToolEvents() {
    }

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
                !ToolService.blocksBreaking(player));
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) ->
                ToolService.onBlockBroken(player, level, pos, state));
        ItemAttributeEvents.register((stack, out) -> {
            AttributeModifier bonus = ToolService.miningBonus(stack);
            if (bonus != null) {
                out.accept(new ItemAttributeEvents.Extra(Attributes.MINING_EFFICIENCY, bonus, EquipmentSlotGroup.MAINHAND));
            }
        });
    }
}
