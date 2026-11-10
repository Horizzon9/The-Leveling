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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Awards each equipped armor piece XP from damage that remains after armor, enchantment, and effect mitigation.
 * The pre-damage stage intentionally runs before absorption is consumed, so a fully absorbed hit still trains armor.
 */
@EventBusSubscriber(modid = WeaponLevelingMod.MOD_ID)
public final class ArmorProgressEvents {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private ArmorProgressEvents() {
    }

    /** Armor mastery: worn pieces shield the wearer from creature types they have learned to withstand. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            event.setAmount(event.getAmount() * (float) ChronicleService.armorMasteryMultiplier(player,
                    event.getSource()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDamageCalculated(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !ServerConfig.ARMOR_LEVELING_ENABLED.get()
                || (player.isCreative() && !ServerConfig.ALLOW_CREATIVE_PROGRESS.get())) {
            return;
        }
        // /kill and the void bypass invulnerability with absurd damage values; they must never train armor.
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        long earnedXp = armorXpFor(Math.min(event.getNewDamage(), player.getMaxHealth()));
        if (earnedXp <= 0) {
            return;
        }

        EquipmentSlot target = pickTargetSlot(player, event.getSource());
        double targetMultiplier = ServerConfig.ARMOR_TARGET_XP_MULTIPLIER.get();
        long cap = ServerConfig.ARMOR_MAX_XP_PER_DAMAGE_EVENT.get();
        boolean changed = false;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!ArmorEligibility.isEligible(armor) || BrokenWeaponHandler.isBroken(armor)) {
                continue;
            }
            // Every worn piece trains; the piece that took the hit trains faster.
            long pieceXp = slot == target
                    ? Math.min(cap, (long) Math.ceil(earnedXp * targetMultiplier)) : earnedXp;
            WeaponProgress progress = armor.get(WeaponComponents.PROGRESS.get());
            if (progress == null) {
                progress = WeaponProgress.create(0);
            }
            // Armor remembers the real post-mitigation pressure it endured. The existing combat-stat
            // field is shared intentionally so old items remain schema-compatible.
            WeaponProgress withStats = progress.withCombatStats(event.getNewDamage(), false);
            var result = ProgressionEngine.addXp(withStats, pieceXp, ServerConfig.xpCurve(),
                    ServerConfig.legacyPolicy());
            armor.set(WeaponComponents.PROGRESS.get(), result.progress());
            changed = true;
            boolean identityUnlocked = result.progress().legacyTier().ordinal() > progress.legacyTier().ordinal();
            int masteryTier = ChronicleService.recordArmorHit(armor, player, event.getSource(),
                    identityUnlocked ? result.progress().legacyTier() : LegacyTier.NONE);
            if (result.levelsGained() > 0) {
                LevelUpNotifier.notify(player, armor, result.progress().level(), result.progress().legacyTier(),
                        identityUnlocked);
            }
            ModAdvancements.onProgress(player, result);
            if (masteryTier > 0) {
                ChronicleService.announceArmorMastery(player, armor, event.getSource(), masteryTier);
                ModAdvancements.grant(player, ModAdvancements.ARMOR_MASTERY);
            }
        }
        if (changed) {
            player.inventoryMenu.broadcastChanges();
        }
    }

    /** Gives every worn armor piece a configurable share (default 5%) of the XP the player's weapon just earned. */
    public static void shareWeaponXp(ServerPlayer player, long weaponXp) {
        double share = ServerConfig.ARMOR_KILL_XP_SHARE.get();
        if (weaponXp <= 0 || share <= 0 || !ServerConfig.ARMOR_LEVELING_ENABLED.get()
                || (player.isCreative() && !ServerConfig.ALLOW_CREATIVE_PROGRESS.get())) {
            return;
        }
        long pieceXp = Math.max(1, (long) Math.ceil(weaponXp * share));
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
            var result = ProgressionEngine.addXp(progress, pieceXp, ServerConfig.xpCurve(),
                    ServerConfig.legacyPolicy());
            armor.set(WeaponComponents.PROGRESS.get(), result.progress());
            changed = true;
            boolean identityUnlocked = result.progress().legacyTier().ordinal() > progress.legacyTier().ordinal();
            if (result.levelsGained() > 0) {
                LevelUpNotifier.notify(player, armor, result.progress().level(), result.progress().legacyTier(),
                        identityUnlocked);
            }
            ModAdvancements.onProgress(player, result);
        }
        if (changed) {
            player.inventoryMenu.broadcastChanges();
        }
    }

    /**
     * Minecraft has no hit-location, so it is estimated: projectiles by the height they struck at, falls by the
     * feet, elytra crashes by the chest, everything else by a weighted roll over the worn pieces.
     */
    private static EquipmentSlot pickTargetSlot(ServerPlayer player, DamageSource source) {
        if (source.is(DamageTypes.FALL)) {
            return EquipmentSlot.FEET;
        }
        if (source.is(DamageTypes.FLY_INTO_WALL)) {
            return EquipmentSlot.CHEST;
        }
        Entity direct = source.getDirectEntity();
        if (direct != null && direct != player && source.is(DamageTypeTags.IS_PROJECTILE)) {
            double relative = (direct.getY() - player.getY()) / Math.max(0.1, player.getBbHeight());
            if (relative >= 0.85) {
                return EquipmentSlot.HEAD;
            }
            return relative >= 0.45 ? EquipmentSlot.CHEST : relative >= 0.2 ? EquipmentSlot.LEGS : EquipmentSlot.FEET;
        }
        double[] weights = {0.2, 0.4, 0.25, 0.15};
        double total = 0;
        for (int index = 0; index < ARMOR_SLOTS.length; index++) {
            ItemStack armor = player.getItemBySlot(ARMOR_SLOTS[index]);
            if (ArmorEligibility.isEligible(armor) && !BrokenWeaponHandler.isBroken(armor)) {
                total += weights[index];
            }
        }
        double roll = player.getRandom().nextDouble() * total;
        for (int index = 0; index < ARMOR_SLOTS.length; index++) {
            ItemStack armor = player.getItemBySlot(ARMOR_SLOTS[index]);
            if (ArmorEligibility.isEligible(armor) && !BrokenWeaponHandler.isBroken(armor)) {
                roll -= weights[index];
                if (roll <= 0) {
                    return ARMOR_SLOTS[index];
                }
            }
        }
        return EquipmentSlot.CHEST;
    }

    private static long armorXpFor(float actualDamage) {
        return ArmorXpAwardCalculator.calculate(actualDamage, ServerConfig.ARMOR_XP_PER_DAMAGE.get(),
                ServerConfig.ARMOR_MAX_XP_PER_DAMAGE_EVENT.get());
    }

}
