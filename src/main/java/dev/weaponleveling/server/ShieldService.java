package dev.weaponleveling.server;

import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.combat.ArmorXpAwardCalculator;
import dev.weaponleveling.core.combat.ShieldDurability;
import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.core.xp.ProgressionEngine;
import dev.weaponleveling.integration.ArmorEligibility;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.WeaponComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Levelling for shields: the damage a shield blocks trains it, and a levelled shield wears out more slowly. */
public final class ShieldService {
    private ShieldService() {
    }

    /** Called with the full damage of a blow the player's raised shield just absorbed. */
    public static void onBlocked(ServerPlayer player, float blockedDamage) {
        if (!ServerConfig.SHIELD_LEVELING_ENABLED.get() || blockedDamage <= 0
                || (player.isCreative() && !ServerConfig.ALLOW_CREATIVE_PROGRESS.get())) {
            return;
        }
        ItemStack shield = player.getUseItem();
        if (!ArmorEligibility.isShield(shield) || !ArmorEligibility.isEligible(shield)
                || BrokenWeaponHandler.isBroken(shield)) {
            return;
        }
        long earnedXp = ArmorXpAwardCalculator.calculate(blockedDamage, ServerConfig.ARMOR_XP_PER_DAMAGE.get(),
                ServerConfig.ARMOR_MAX_XP_PER_DAMAGE_EVENT.get());
        if (earnedXp <= 0) {
            return;
        }
        WeaponProgress progress = shield.get(WeaponComponents.PROGRESS.get());
        if (progress == null) {
            progress = WeaponProgress.create(0);
        }
        // Blocked damage is stored in the same combat-stat field armor uses for damage endured.
        var result = ProgressionEngine.addXp(progress.withCombatStats(blockedDamage, false), earnedXp,
                ServerConfig.xpCurve(), ServerConfig.legacyPolicy());
        shield.set(WeaponComponents.PROGRESS.get(), result.progress());
        boolean identityUnlocked = result.progress().legacyTier().ordinal() > progress.legacyTier().ordinal();
        ChronicleService.recordArmorHit(shield, player, null,
                identityUnlocked ? result.progress().legacyTier() : LegacyTier.NONE);
        if (result.levelsGained() > 0) {
            LevelUpNotifier.notify(player, shield, result.progress().level(), result.progress().legacyTier(),
                    identityUnlocked);
        }
        ModAdvancements.onProgress(player, result);
        player.inventoryMenu.broadcastChanges();
    }

    /** A shield that broke while raised cannot keep blocking until the player lowers it. */
    public static void stopIfBroken(ServerPlayer player) {
        ItemStack shield = player.getUseItem();
        if (ArmorEligibility.isShield(shield) && BrokenWeaponHandler.isBroken(shield)) {
            player.stopUsingItem();
        }
    }

    /** Durability a block costs the stack: unchanged unless it is a levelled shield. */
    public static int reducedDurabilityDamage(ItemStack stack, int damage) {
        if (!ServerConfig.SHIELD_LEVELING_ENABLED.get() || !ArmorEligibility.isShield(stack)
                || !ArmorEligibility.isEligible(stack)) {
            return damage;
        }
        WeaponProgress progress = stack.get(WeaponComponents.PROGRESS.get());
        if (progress == null) {
            return damage;
        }
        return ShieldDurability.reduce(damage, progress.level(), ServerConfig.MAX_LEVEL.get(),
                ServerConfig.SHIELD_MAX_DURABILITY_SAVING.get());
    }
}
