package dev.weaponleveling.loot;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;

/**
 * Craft edilemeyen taban sablonunun End City ve Bastion sandiklarinda cikma sansi.
 *
 * Sanslar ve hedef tablolar NeoForge surumuyle BIREBIR aynidir; yalnizca uygulama mekanizmasi
 * degisti - bkz {@link ModLootModifiers}. Sabitler ayri tutuluyor ki denge degeri tek yerde kalsin.
 */
public final class LegacyTransferTemplateLootModifier {
    private static final float END_CITY_CHANCE = 0.05F;
    private static final float COMMON_BASTION_CHANCE = 0.05F;
    private static final float BASTION_TREASURE_CHANCE = 0.50F;
    private static final Map<ResourceLocation, Float> TEMPLATE_CHANCES = Map.of(
            vanillaChest("end_city_treasure"), END_CITY_CHANCE,
            vanillaChest("bastion_bridge"), COMMON_BASTION_CHANCE,
            vanillaChest("bastion_hoglin_stable"), COMMON_BASTION_CHANCE,
            vanillaChest("bastion_other"), COMMON_BASTION_CHANCE,
            vanillaChest("bastion_treasure"), BASTION_TREASURE_CHANCE);

    private LegacyTransferTemplateLootModifier() {
    }

    static float templateChance(ResourceLocation lootTableId) {
        return TEMPLATE_CHANCES.getOrDefault(lootTableId, 0.0F);
    }

    private static ResourceLocation vanillaChest(String name) {
        return ResourceLocation.withDefaultNamespace("chests/" + name);
    }
}
