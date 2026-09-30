package dev.weaponleveling.server;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.combat.LevelDamageBonus;
import dev.weaponleveling.core.combat.MobXpBalance;
import dev.weaponleveling.core.chronicle.KillStreak;
import dev.weaponleveling.core.combat.XpAwardCalculator;
import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.core.xp.ProgressionEngine;
import dev.weaponleveling.integration.WeaponComponents;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.WeaponDamage;
import dev.weaponleveling.integration.WeaponEligibility;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import dev.weaponleveling.network.KillXpPayload;
import dev.weaponleveling.platform.event.DamageEvents;
import dev.weaponleveling.platform.net.ModNetwork;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class CombatEvents {
    private static final Map<LivingEntity, AttackContext> PENDING_ATTACKS = new WeakHashMap<>();

    private CombatEvents() {
    }

    public static void register() {
        DamageEvents.registerModifyIncoming(CombatEvents::onIncomingDamage);
        DamageEvents.registerApplied(CombatEvents::onDamageApplied);
    }

    // Run after ordinary damage modifiers so enchantment and third-party weapon bonuses enter the level curve too.
    // Iptal kontrolu ARTIK BURADA DEGIL: kirik silah iptali `hurt`in basinda, bu kancadan once
    // calisir (bkz DamageEvents ve LivingEntityDamageMixin). Iptal edilen bir vurus bu noktaya
    // hicbir zaman ulasmaz, yani eski `event.isCanceled()` erken cikisi gereksizdir.
    private static float onIncomingDamage(LivingEntity target, DamageSource source, float amount) {
        if (!(source.getEntity() instanceof ServerPlayer attacker)) {
            return amount;
        }
        if (target == attacker) {
            return amount;
        }
        AttackWeapon attackWeapon = resolveAttackWeapon(attacker, source.getDirectEntity());
        if (attackWeapon == null) {
            return amount;
        }
        ItemStack weapon = attackWeapon.weapon();
        WeaponProgress progress = attackWeapon.progress();

        float originalAmount = amount;
        if (originalAmount <= 0) {
            return amount;
        }
        LevelDamageBonus levelBonus = ServerConfig.LEVEL_DAMAGE_BONUS_ENABLED.get()
                ? LevelDamageBonus.calculate(progress.level(), ServerConfig.MAX_LEVEL.get(),
                ServerConfig.LEVEL_DAMAGE_MAX_MULTIPLIER.get(), ServerConfig.LEVEL_DAMAGE_CURVE_EXPONENT.get())
                : LevelDamageBonus.NONE;
        boolean playerKillAllowed = !(target instanceof Player) || ServerConfig.PLAYER_KILL_XP_ENABLED.get();
        boolean progressionAllowed = playerKillAllowed
                && (!attacker.isCreative() || ServerConfig.ALLOW_CREATIVE_PROGRESS.get());
        if (progressionAllowed) {
            PENDING_ATTACKS.put(target, new AttackContext(attacker.getUUID(), progress.instanceId(),
                    weapon, attackWeapon.rangedAttack(), target.getHealth()));
        }
        // The incoming amount already includes special weapon calculations. This preserves a mace's
        // fall-based smash bonus, thrown-trident impact damage, and compatible spear projectile damage.
        double scaledAmount = levelBonus.applyTo(originalAmount) * ChronicleService.masteryMultiplier(weapon, target);
        return (float) Math.min(scaledAmount, Float.MAX_VALUE);
    }

    private static void onDamageApplied(LivingEntity target, DamageSource source, float appliedDamage) {
        AttackContext attack = PENDING_ATTACKS.remove(target);
        if (attack == null || !(source.getEntity() instanceof ServerPlayer attacker)
                || !attacker.getUUID().equals(attack.attackerId())) {
            return;
        }
        Entity directEntity = source.getDirectEntity();
        ItemStack weapon = attack.weapon();
        WeaponProgress progress = attack.rangedAttack() == null
                ? weapon.get(WeaponComponents.PROGRESS.get())
                : RangedWeaponTracker.currentProgress(attack.rangedAttack(), directEntity, attacker);
        if (!WeaponEligibility.isEligibleOnServer(weapon) || progress == null
                || !progress.instanceId().equals(attack.weaponInstanceId())) {
            return;
        }

        double actualDamage = ServerConfig.ENABLE_ANTI_ABUSE.get()
                ? Math.max(0, Math.min(attack.preHitHealth(), appliedDamage))
                : Math.max(0, appliedDamage);
        if (actualDamage <= 0) {
            return;
        }
        boolean killed = target.isDeadOrDying() || target.getHealth() <= 0;
        // A player kill uses the same base kill XP as a normal zombie: no special PvP multiplier.
        double entityMultiplier = target instanceof Player ? 1.0 : entityMultiplier(target);
        // Bosses are not multiplied a second time: their maximum health already determines their reward.
        double bossMultiplier = 1.0;
        KillStreak streak = killed ? KillStreakTracker.recordKill(attacker, progress.instanceId()) : null;
        long earnedXp = XpAwardCalculator.calculate(ServerConfig.KILL_XP_ENABLED.get(), killed,
                ServerConfig.KILL_BONUS.get(), entityMultiplier, bossMultiplier,
                KillStreakTracker.xpMultiplier(streak), ServerConfig.MAX_XP_PER_ATTACK.get());

        WeaponProgress withStats = progress.withCombatStats(actualDamage, killed);
        var result = ProgressionEngine.addXp(withStats, earnedXp, ServerConfig.xpCurve(),
                ServerConfig.legacyPolicy());
        if (attack.rangedAttack() == null) {
            weapon.set(WeaponComponents.PROGRESS.get(), result.progress());
        } else {
            RangedWeaponTracker.saveProgress(attack.rangedAttack(), directEntity, attacker, result.progress());
        }
        boolean identityUnlocked = result.progress().legacyTier().ordinal() > progress.legacyTier().ordinal();
        int masteryTier = killed ? ChronicleService.recordKill(weapon, attack.rangedAttack(), directEntity, attacker,
                target, identityUnlocked ? result.progress().legacyTier() : LegacyTier.NONE) : 0;
        attacker.inventoryMenu.broadcastChanges();
        if (earnedXp > 0) {
            ModNetwork.sendToPlayer(attacker, new KillXpPayload(earnedXp, streak == null ? 0 : streak.count()));
        }
        if (result.levelsGained() > 0) {
            LevelUpNotifier.notify(attacker, weapon, result.progress().level(), result.progress().legacyTier(),
                    identityUnlocked);
        }
        if (masteryTier > 0) {
            ChronicleService.announceMastery(attacker, weapon, target, masteryTier);
        }
        ModAdvancements.onProgress(attacker, result);
        ModAdvancements.onWeaponKill(attacker, masteryTier, streak);
        if (earnedXp > 0) {
            WeaponLeaderboard.record(attacker, weapon, result.progress());
        }
    }

    private static AttackWeapon resolveAttackWeapon(ServerPlayer attacker, Entity directEntity) {
        RangedWeaponTracker.RangedAttack rangedAttack = RangedWeaponTracker.findTrackedAttack(directEntity,
                attacker.getUUID());
        if (rangedAttack != null) {
            WeaponProgress progress = RangedWeaponTracker.currentProgress(rangedAttack, directEntity, attacker);
            if (progress != null) {
                return new AttackWeapon(rangedAttack.weapon(), progress, rangedAttack);
            }
        }

        ItemStack weapon = attacker.getMainHandItem();
        if (!WeaponEligibility.isEligibleOnServer(weapon) || BrokenWeaponHandler.isBroken(weapon)) {
            return null;
        }
        WeaponProgress progress = weapon.get(WeaponComponents.PROGRESS.get());
        if (progress == null) {
            progress = WeaponProgress.create(WeaponDamage.baseAttackDamage(weapon));
            weapon.set(WeaponComponents.PROGRESS.get(), progress);
        }
        return new AttackWeapon(weapon, progress, null);
    }

    private static double entityMultiplier(LivingEntity target) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
        return MobXpBalance.multiplier(id, target.getMaxHealth(), ServerConfig.MOB_HEALTH_BASELINE.get(),
                ServerConfig.ENTITY_MULTIPLIERS.get());
    }

    private record AttackWeapon(ItemStack weapon, WeaponProgress progress, RangedWeaponTracker.RangedAttack rangedAttack) {
    }

    private record AttackContext(UUID attackerId, UUID weaponInstanceId, ItemStack weapon,
                                 RangedWeaponTracker.RangedAttack rangedAttack, float preHitHealth) {
    }
}
