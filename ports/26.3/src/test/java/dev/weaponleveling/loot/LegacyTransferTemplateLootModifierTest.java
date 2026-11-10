package dev.weaponleveling.loot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class LegacyTransferTemplateLootModifierTest {
    @Test
    void keepsEndCityTemplateChanceAtFivePercent() {
        assertChance("chests/end_city_treasure", 0.05F);
    }

    @Test
    void usesHalfTheNetheriteUpgradeTemplateChanceInCommonBastionChests() {
        assertChance("chests/bastion_bridge", 0.05F);
        assertChance("chests/bastion_hoglin_stable", 0.05F);
        assertChance("chests/bastion_other", 0.05F);
    }

    @Test
    void usesHalfTheGuaranteedNetheriteUpgradeTemplateChanceInTreasureBastions() {
        assertChance("chests/bastion_treasure", 0.50F);
    }

    @Test
    void doesNotAddTemplatesToOtherLootTables() {
        assertChance("chests/nether_bridge", 0.0F);
    }

    private static void assertChance(String lootTablePath, float expectedChance) {
        Identifier lootTableId = Identifier.withDefaultNamespace(lootTablePath);
        assertEquals(expectedChance, LegacyTransferTemplateLootModifier.templateChance(lootTableId));
    }
}
