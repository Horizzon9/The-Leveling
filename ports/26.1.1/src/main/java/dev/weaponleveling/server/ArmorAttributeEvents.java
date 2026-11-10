package dev.weaponleveling.server;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.combat.ArmorLevelBonus;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.integration.ArmorEligibility;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.WeaponComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/** Adds the configured level-derived Armor modifier whenever a levelable armor stack is equipped. */
@EventBusSubscriber(modid = WeaponLevelingMod.MOD_ID)
public final class ArmorAttributeEvents {
    private ArmorAttributeEvents() {
    }

    @SubscribeEvent
    public static void addLevelArmor(ItemAttributeModifierEvent event) {
        ItemStack armor = event.getItemStack();
        if (!ArmorEligibility.isEligible(armor) || BrokenWeaponHandler.isBroken(armor)
                || !ServerConfig.ARMOR_LEVEL_BONUS_ENABLED.get()) {
            return;
        }
        WeaponProgress progress = armor.get(WeaponComponents.PROGRESS.get());
        int level = progress == null ? 0 : progress.level();
        double bonus = ArmorLevelBonus.calculate(level, ServerConfig.ARMOR_FLAT_BONUS_PER_LEVEL.get(),
                ServerConfig.ARMOR_MAX_LEVEL_BONUS.get()).armor();
        if (bonus <= 0) {
            return;
        }
        EquipmentSlot armorSlot = ArmorEligibility.armorSlot(armor);
        EquipmentSlotGroup slotGroup = armorSlot != null
                ? EquipmentSlotGroup.bySlot(armorSlot)
                : event.getModifiers().stream()
                .filter(entry -> entry.attribute().equals(Attributes.ARMOR))
                .map(entry -> entry.slot())
                .findFirst()
                .orElse(null);
        if (slotGroup == null) {
            return;
        }
        Identifier itemId = BuiltInRegistries.ITEM.getKey(armor.getItem());
        Identifier modifierId = Identifier.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID,
                "armor_level_bonus/" + itemId.getNamespace() + "/" + itemId.getPath());
        event.replaceModifier(Attributes.ARMOR, new AttributeModifier(modifierId, bonus,
                AttributeModifier.Operation.ADD_VALUE), slotGroup);
    }
}
