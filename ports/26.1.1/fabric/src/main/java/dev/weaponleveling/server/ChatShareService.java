package dev.weaponleveling.server;

import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.combat.ArmorLevelBonus;
import dev.weaponleveling.core.combat.LevelDamageBonus;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.core.model.LegacyPersona;
import dev.weaponleveling.integration.ArmorEligibility;
import dev.weaponleveling.integration.ArmorProtection;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.WeaponComponents;
import dev.weaponleveling.integration.WeaponDamage;
import dev.weaponleveling.integration.WeaponEligibility;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Validates inventory share requests and broadcasts a hoverable, server-derived item summary. */
public final class ChatShareService {
    private static final long COOLDOWN_TICKS = 20;
    private static final Map<UUID, Long> LAST_SHARE_TICK = new HashMap<>();
    private static final DecimalFormat NUMBER = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private ChatShareService() {
    }

    public static void share(ServerPlayer player, int containerId, int menuSlot) {
        if (player.containerMenu.containerId != containerId || menuSlot < 0
                || menuSlot >= player.containerMenu.slots.size()) {
            return;
        }
        long now = player.level().getGameTime();
        long previous = LAST_SHARE_TICK.getOrDefault(player.getUUID(), Long.MIN_VALUE / 2);
        if (now - previous < COOLDOWN_TICKS) {
            return;
        }

        ItemStack stack = player.containerMenu.getSlot(menuSlot).getItem();
        boolean weapon = WeaponEligibility.isEligibleOnServer(stack);
        boolean armor = ArmorEligibility.isEligible(stack);
        if ((!weapon && !armor) || stack.isEmpty()) {
            return;
        }
        LAST_SHARE_TICK.put(player.getUUID(), now);

        WeaponProgress progress = stack.get(WeaponComponents.PROGRESS.get());
        int level = progress == null ? 0 : progress.level();
        long currentXp = progress == null ? 0 : progress.currentXp();
        long requiredXp = ServerConfig.xpCurve().requiredXp(level);
        Component statistic = weapon ? weaponStatistic(stack, level) : armorStatistic(stack, level);
        Component message = Component.translatable("message.weaponleveling.shared_item",
                        player.getDisplayName(), stack.getDisplayName(), level, ServerConfig.MAX_LEVEL.get(),
                        currentXp, requiredXp, statistic)
                .withStyle(ChatFormatting.GRAY);
        if (progress != null && progress.legacyTier() != LegacyTier.NONE) {
            LegacyPersona persona = LegacyPersona.resolve(progress, armor, weapon && WeaponEligibility.isRanged(stack));
            message = message.copy().append(Component.literal(" · ")).append(Component.translatable(
                            "tooltip.weaponleveling.identity_persona",
                            Component.translatable("tooltip.weaponleveling.identity." + progress.legacyTier().serializedName()),
                            Component.translatable("tooltip.weaponleveling.persona." + persona.path().serializedName()),
                            persona.renownRank())
                    .withStyle(progress.legacyTier() == LegacyTier.LEGACY
                            ? ChatFormatting.GOLD : ChatFormatting.YELLOW));
        }
        player.level().getServer().getPlayerList().broadcastSystemMessage(message, false);
    }

    private static Component weaponStatistic(ItemStack stack, int level) {
        double baseDamage = WeaponEligibility.isRanged(stack) && WeaponDamage.baseAttackDamage(stack) <= 0
                ? WeaponDamage.standardRangedDamage(stack) : WeaponDamage.mainHandDamage(stack);
        double damage = BrokenWeaponHandler.isBroken(stack) ? 0 : baseDamage;
        if (damage > 0 && ServerConfig.LEVEL_DAMAGE_BONUS_ENABLED.get()) {
            damage = LevelDamageBonus.calculate(level, ServerConfig.MAX_LEVEL.get(),
                    ServerConfig.LEVEL_DAMAGE_MAX_MULTIPLIER.get(),
                    ServerConfig.LEVEL_DAMAGE_CURVE_EXPONENT.get()).applyTo(damage);
        }
        return Component.translatable("message.weaponleveling.shared_damage", format(damage));
    }

    private static Component armorStatistic(ItemStack stack, int level) {
        double armor = BrokenWeaponHandler.isBroken(stack) ? 0 : ArmorProtection.baseArmor(stack);
        if (armor > 0 && ServerConfig.ARMOR_LEVEL_BONUS_ENABLED.get()) {
            armor += ArmorLevelBonus.calculate(level, ServerConfig.ARMOR_FLAT_BONUS_PER_LEVEL.get(),
                    ServerConfig.ARMOR_MAX_LEVEL_BONUS.get()).armor();
        }
        return Component.translatable("message.weaponleveling.shared_armor", format(armor));
    }

    private static String format(double value) {
        synchronized (NUMBER) {
            return NUMBER.format(value);
        }
    }
}
