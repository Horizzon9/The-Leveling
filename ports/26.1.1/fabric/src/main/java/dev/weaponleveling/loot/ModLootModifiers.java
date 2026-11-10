package dev.weaponleveling.loot;

import dev.weaponleveling.content.ModContent;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Ganimet tablosu eklemeleri.
 *
 * NeoForge tarafinda bu is bir "Global Loot Modifier" idi: ayri bir serializer registry'si, bir
 * codec ve `data/.../loot_modifiers/*.json` dosyalari gerekiyordu. Fabric'te karsiligi
 * `LootTableEvents.MODIFY`'dir - tablo yuklenirken dogrudan bir havuz eklenir, yani ek kayit
 * turune ve veri dosyalarina gerek kalmaz (o JSON'lar bu surumde SILINDI).
 *
 * Davranis ayni: hedef tablolar ve sanslar {@link LegacyTransferTemplateLootModifier} icindedir;
 * tek atislik bir havuz, verilen sansla sablonu ekler.
 */
public final class ModLootModifiers {
    private ModLootModifiers() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            float chance = LegacyTransferTemplateLootModifier.templateChance(key.identifier());
            if (chance <= 0.0F) {
                return;
            }
            tableBuilder.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(chance))
                    .add(LootItem.lootTableItem(ModContent.LEGACY_TRANSFER_TEMPLATE.get())));
        });
    }
}
