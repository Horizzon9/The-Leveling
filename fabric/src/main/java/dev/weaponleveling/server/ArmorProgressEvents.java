package dev.weaponleveling.server;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.combat.ArmorXpAwardCalculator;
import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.core.xp.ProgressionEngine;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.WeaponComponents;
import dev.weaponleveling.integration.ArmorEligibility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import dev.weaponleveling.platform.event.DamageEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Awards each equipped armor piece XP from damage that remains after armor, enchantment, and effect mitigation.
 * The pre-damage stage intentionally runs before absorption is consumed, so a fully absorbed hit still trains armor.
 */
public final class ArmorProgressEvents {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private ArmorProgressEvents() {
    }

    public static void register() {
        DamageEvents.registerMitigated(ArmorProgressEvents::onPlayerDamageCalculated);
        // Armor mastery: worn pieces shield the wearer from creature types they have learned to withstand.
        DamageEvents.registerModifyIncoming((target, source, amount) -> target instanceof ServerPlayer player
                ? amount * (float) ChronicleService.armorMasteryMultiplier(player, source) : amount);
    }

    private static void onPlayerDamageCalculated(LivingEntity target, DamageSource source, float mitigatedDamage) {
        if (!(target instanceof ServerPlayer player)
                || !ServerConfig.ARMOR_LEVELING_ENABLED.get()
                || (player.isCreative() && !ServerConfig.ALLOW_CREATIVE_PROGRESS.get())) {
            return;
        }
        long earnedXp = armorXpFor(mitigatedDamage);
        if (earnedXp <= 0) {
            return;
        }

        boolean changed = false;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!ArmorEligibility.isEligible(armor) || BrokenWeaponHandler.isBroken(armor)) {
                continue;
            }
            WeaponProgress progress = armor.get(WeaponComponents.PROGRESS.get());
            if (progress == null) {
                progress = WeaponProgress.create(0);
            }
            // Armor remembers the real post-mitigation pressure it endured. The existing combat-stat
            // field is shared intentionally so old items remain schema-compatible.
            WeaponProgress withStats = progress.withCombatStats(mitigatedDamage, false);
            var result = ProgressionEngine.addXp(withStats, earnedXp, ServerConfig.xpCurve(),
                    ServerConfig.legacyPolicy());
            armor.set(WeaponComponents.PROGRESS.get(), result.progress());
            changed = true;
            boolean identityUnlocked = result.progress().legacyTier().ordinal() > progress.legacyTier().ordinal();
            int masteryTier = ChronicleService.recordArmorHit(armor, player, source,
                    identityUnlocked ? result.progress().legacyTier() : LegacyTier.NONE);
            if (result.levelsGained() > 0) {
                LevelUpNotifier.notify(player, armor, result.progress().level(), result.progress().legacyTier(),
                        identityUnlocked);
            }
            ModAdvancements.onProgress(player, result);
            if (masteryTier > 0) {
                ChronicleService.announceArmorMastery(player, armor, source, masteryTier);
                ModAdvancements.grant(player, ModAdvancements.ARMOR_MASTERY);
            }
        }
        if (changed) {
            player.inventoryMenu.broadcastChanges();
        }
    }

    private static long armorXpFor(float actualDamage) {
        return ArmorXpAwardCalculator.calculate(actualDamage, ServerConfig.ARMOR_XP_PER_DAMAGE.get(),
                ServerConfig.ARMOR_MAX_XP_PER_DAMAGE_EVENT.get());
    }

}
