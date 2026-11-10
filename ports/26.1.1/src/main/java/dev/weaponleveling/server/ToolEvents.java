package dev.weaponleveling.server;

import dev.weaponleveling.WeaponLevelingMod;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** NeoForge wiring for {@link ToolService}. */
@EventBusSubscriber(modid = WeaponLevelingMod.MOD_ID)
public final class ToolEvents {
    private ToolEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void preventBreakingWithBrokenTool(BlockEvent.BreakEvent event) {
        if (ToolService.blocksBreaking(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void trainToolOnBreak(BlockEvent.BreakEvent event) {
        if (!event.isCanceled()) {
            ToolService.onBlockBroken(event.getPlayer(), event.getLevel(), event.getPos(), event.getState());
        }
    }

    @SubscribeEvent
    public static void addMiningBonus(ItemAttributeModifierEvent event) {
        AttributeModifier bonus = ToolService.miningBonus(event.getItemStack());
        if (bonus != null) {
            event.replaceModifier(Attributes.MINING_EFFICIENCY, bonus, EquipmentSlotGroup.MAINHAND);
        }
    }
}
