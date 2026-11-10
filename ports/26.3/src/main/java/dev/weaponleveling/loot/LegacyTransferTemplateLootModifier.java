package dev.weaponleveling.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.weaponleveling.content.ModContent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Adds the non-craftable base template to End City and Bastion treasure chests. */
public final class LegacyTransferTemplateLootModifier extends LootModifier {
    private static final float END_CITY_CHANCE = 0.05F;
    private static final float COMMON_BASTION_CHANCE = 0.05F;
    private static final float BASTION_TREASURE_CHANCE = 0.50F;
    private static final Map<Identifier, Float> TEMPLATE_CHANCES = Map.of(
            vanillaChest("end_city_treasure"), END_CITY_CHANCE,
            vanillaChest("bastion_bridge"), COMMON_BASTION_CHANCE,
            vanillaChest("bastion_hoglin_stable"), COMMON_BASTION_CHANCE,
            vanillaChest("bastion_other"), COMMON_BASTION_CHANCE,
            vanillaChest("bastion_treasure"), BASTION_TREASURE_CHANCE);

    public static final MapCodec<LegacyTransferTemplateLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, LegacyTransferTemplateLootModifier::new));

    private LegacyTransferTemplateLootModifier(Optional<Holder<LootItemCondition>> conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        float chance = templateChance(context.getQueriedLootTableId());
        if (chance > 0.0F && context.getRandom().nextFloat() < chance) {
            generatedLoot.add(new ItemStack(ModContent.LEGACY_TRANSFER_TEMPLATE.get()));
        }
        return generatedLoot;
    }

    static float templateChance(Identifier lootTableId) {
        return TEMPLATE_CHANCES.getOrDefault(lootTableId, 0.0F);
    }

    private static Identifier vanillaChest(String name) {
        return Identifier.withDefaultNamespace("chests/" + name);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
