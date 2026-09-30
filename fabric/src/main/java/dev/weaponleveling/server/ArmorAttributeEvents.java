package dev.weaponleveling.server;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.combat.ArmorLevelBonus;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.integration.ArmorEligibility;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.WeaponComponents;
import dev.weaponleveling.platform.event.ItemAttributeEvents;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Adds the configured level-derived Armor modifier whenever a levelable armor stack is equipped. */
public final class ArmorAttributeEvents {
    private ArmorAttributeEvents() {
    }

    public static void register() {
        ItemAttributeEvents.register(ArmorAttributeEvents::addLevelArmor);
    }

    private static void addLevelArmor(ItemStack armor, Consumer<ItemAttributeEvents.Extra> out) {
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
        EquipmentSlotGroup slotGroup = armor.getItem() instanceof ArmorItem armorItem
                ? EquipmentSlotGroup.bySlot(armorItem.getEquipmentSlot())
                : existingArmorSlotGroup(armor);
        if (slotGroup == null) {
            return;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(armor.getItem());
        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID,
                "armor_level_bonus/" + itemId.getNamespace() + "/" + itemId.getPath());
        out.accept(new ItemAttributeEvents.Extra(Attributes.ARMOR,
                new AttributeModifier(modifierId, bonus, AttributeModifier.Operation.ADD_VALUE), slotGroup));
    }

    /**
     * ArmorItem olmayan (mod) zirhlar icin slot grubunu esyanin KENDI Armor degistiricisinden
     * okur - kaynak surumde bu bilgi olay nesnesinin mevcut degistirici listesinden geliyordu.
     * Esyada bilesen olarak tanimli bir liste yoksa esyanin varsayilan listesine bakilir; vanilla
     * `forEachModifier` de ayni sirayi izler.
     */
    private static EquipmentSlotGroup existingArmorSlotGroup(ItemStack armor) {
        ItemAttributeModifiers modifiers = armor.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);
        if (modifiers.modifiers().isEmpty()) {
            modifiers = armor.getItem().getDefaultAttributeModifiers();
        }
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().equals(Attributes.ARMOR)) {
                return entry.slot();
            }
        }
        return null;
    }
}
