package dev.weaponleveling.integration.jade;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.client.ClientServerSettings;
import dev.weaponleveling.config.ClientConfig;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.core.model.LegacyPersona;
import dev.weaponleveling.integration.ArmorEligibility;
import dev.weaponleveling.integration.WeaponEligibility;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.LevelableItemEligibility;
import dev.weaponleveling.integration.WeaponComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Optional Jade overlay for levelable items lying in the world. Item progress is already part of
 * the synchronized item stack, so this works without requiring Jade on the connected server.
 */
@WailaPlugin(WeaponLevelingMod.MOD_ID)
public final class WeaponLevelingJadePlugin implements IWailaPlugin {
    public static final Identifier ITEM_PROGRESS = Identifier.fromNamespaceAndPath(
            WeaponLevelingMod.MOD_ID, "item_progress");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(ItemProgressProvider.INSTANCE, ItemEntity.class);
    }

    private enum ItemProgressProvider implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (!(accessor.getEntity() instanceof ItemEntity itemEntity)) {
                return;
            }

            ItemStack stack = itemEntity.getItem();
            var settings = ClientServerSettings.current();
            if (!LevelableItemEligibility.isEligible(stack, settings.autoDetectExternalWeapons())) {
                return;
            }

            WeaponProgress progress = stack.get(WeaponComponents.PROGRESS.get());
            int level = progress == null ? 0 : progress.level();
            if (BrokenWeaponHandler.isMarkedBroken(stack)) {
                tooltip.add(Component.translatable("tooltip.weaponleveling.broken").withStyle(ChatFormatting.RED));
            }
            LegacyTier legacyTier = progress == null ? LegacyTier.NONE : progress.legacyTier();
            if (ClientConfig.SHOW_LEGACY_IDENTITY.get() && legacyTier != LegacyTier.NONE) {
                boolean armor = ArmorEligibility.isEligible(stack);
                LegacyPersona persona = LegacyPersona.resolve(progress, armor,
                        !armor && WeaponEligibility.isRanged(stack));
                tooltip.add(Component.literal("◆ ").append(Component.translatable(
                                "tooltip.weaponleveling.identity_persona",
                                Component.translatable("tooltip.weaponleveling.identity." + legacyTier.serializedName()),
                                Component.translatable("tooltip.weaponleveling.persona." + persona.path().serializedName()),
                                persona.renownRank()))
                        .withStyle(style -> style.withColor(ClientConfig.identityColor(legacyTier,
                                ClientConfig.levelColor()))));
            }
            tooltip.add(Component.literal("◆ ").append(Component.translatable("tooltip.weaponleveling.item_level", level,
                    settings.maxLevel())).withStyle(style -> style.withColor(ClientConfig.levelColor())));
            if (level >= settings.maxLevel()) {
                tooltip.add(Component.literal("  ▸ ").append(Component.translatable("tooltip.weaponleveling.max_level"))
                        .withStyle(ChatFormatting.GREEN));
                return;
            }

            long currentXp = progress == null ? 0 : progress.currentXp();
            long requiredXp = ClientServerSettings.requiredXp(level);
            tooltip.add(Component.literal("  ▸ ").append(Component.translatable("tooltip.weaponleveling.xp", currentXp, requiredXp))
                    .withStyle(style -> style.withColor(ClientConfig.detailColor())));
            appendEnchantments(tooltip, stack);
        }

        private static void appendEnchantments(ITooltip tooltip, ItemStack stack) {
            ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (enchantments.isEmpty()) {
                return;
            }
            tooltip.add(Component.translatable("jade.weaponleveling.enchantments").withStyle(ChatFormatting.DARK_PURPLE));
            for (var entry : enchantments.entrySet()) {
                tooltip.add(Component.literal("  ▸ ").append(Enchantment.getFullname(entry.getKey(), entry.getIntValue()))
                        .withStyle(ChatFormatting.DARK_AQUA));
            }
        }

        @Override
        public Identifier getUid() {
            return ITEM_PROGRESS;
        }
    }
}
