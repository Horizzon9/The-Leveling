package dev.weaponleveling.server;

import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.chronicle.ChronicleRecords;
import dev.weaponleveling.core.chronicle.FoeMastery;
import dev.weaponleveling.core.chronicle.RomanNumerals;
import dev.weaponleveling.core.chronicle.WeaponChronicle;
import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.integration.ArmorEligibility;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.WeaponComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Server-side bridge between combat and the {@link WeaponChronicle} stored on each item. */
public final class ChronicleService {
    private static final long TICKS_PER_DAY = 24_000L;
    /** Hard ceiling on armor mastery, whatever the config says: a full set can never become immune. */
    private static final double MAX_ARMOR_MASTERY_REDUCTION = 0.5;
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private ChronicleService() {
    }

    public static WeaponChronicle read(ItemStack stack) {
        WeaponChronicle chronicle = stack.get(WeaponComponents.CHRONICLE.get());
        return chronicle == null ? WeaponChronicle.EMPTY : chronicle;
    }

    public static long currentDay(ServerPlayer player) {
        return Math.max(0, player.level().getLevelData().getGameTime() / TICKS_PER_DAY);
    }

    /** Damage multiplier the weapon has earned against the target's creature type; 1 when none applies. */
    public static double masteryMultiplier(ItemStack weapon, LivingEntity target) {
        WeaponChronicle chronicle = weapon.get(WeaponComponents.CHRONICLE.get());
        if (chronicle == null || target instanceof Player || !ServerConfig.CHRONICLE_ENABLED.get()) {
            return 1.0;
        }
        return ServerConfig.foeMastery().damageMultiplier(chronicle.killsOf(foeId(target)));
    }

    /**
     * Writes one kill, and an identity earned by it, into the weapon's chronicle.
     *
     * @return the mastery tier this kill reached, or zero when it reached none or chronicles are disabled
     */
    public static int recordKill(ItemStack weapon, RangedWeaponTracker.RangedAttack rangedAttack,
                                 Entity directEntity, ServerPlayer attacker, LivingEntity target,
                                 LegacyTier earnedIdentity) {
        if (!ServerConfig.CHRONICLE_ENABLED.get()) {
            return 0;
        }
        long day = currentDay(attacker);
        // Players are chronicled as foes but never mastered: mastery must not tilt PvP balance.
        FoeMastery mastery = target instanceof Player ? FoeMastery.DISABLED : ServerConfig.foeMastery();
        WeaponChronicle.KillOutcome outcome = read(weapon)
                .withBearer(attacker.getGameProfile().name())
                .recordKill(foeId(target), day, mastery);
        persist(weapon, rangedAttack, directEntity, attacker, outcome.chronicle().recordIdentity(earnedIdentity, day));
        return outcome.reachedTier();
    }

    /**
     * Updates the weapon's personal bests with one hit. Writes nothing unless a record improved,
     * so ordinary hits cost no item update.
     */
    public static void recordFeats(ItemStack weapon, RangedWeaponTracker.RangedAttack rangedAttack,
                                   Entity directEntity, ServerPlayer attacker, LivingEntity target,
                                   double hitDamage, int streak) {
        if (!ServerConfig.CHRONICLE_ENABLED.get()) {
            return;
        }
        WeaponChronicle chronicle = read(weapon);
        double shotDistance = rangedAttack == null ? 0 : attacker.distanceTo(target);
        ChronicleRecords improved = chronicle.records().withFeat(hitDamage, shotDistance, streak);
        if (improved != chronicle.records()) {
            persist(weapon, rangedAttack, directEntity, attacker,
                    chronicle.withBearer(attacker.getGameProfile().name()).withRecords(improved));
        }
    }

    private static void persist(ItemStack weapon, RangedWeaponTracker.RangedAttack rangedAttack,
                                Entity directEntity, ServerPlayer attacker, WeaponChronicle chronicle) {
        if (rangedAttack == null) {
            weapon.set(WeaponComponents.CHRONICLE.get(), chronicle);
        } else {
            RangedWeaponTracker.forEachMatchingStack(rangedAttack, directEntity, attacker,
                    stack -> stack.set(WeaponComponents.CHRONICLE.get(), chronicle));
        }
    }

    /**
     * Counts a hit a worn armor piece endured from a creature, and an identity the hit earned.
     *
     * @return the armor mastery tier the hit reached, or zero
     */
    public static int recordArmorHit(ItemStack armor, ServerPlayer wearer, DamageSource source,
                                     LegacyTier earnedIdentity) {
        if (!ServerConfig.CHRONICLE_ENABLED.get()) {
            return 0;
        }
        long day = currentDay(wearer);
        WeaponChronicle chronicle = read(armor).withBearer(wearer.getGameProfile().name())
                .recordIdentity(earnedIdentity, day);
        int reachedTier = 0;
        LivingEntity attacker = creatureAttacker(source);
        if (attacker != null) {
            WeaponChronicle.KillOutcome outcome = chronicle.recordHit(foeId(attacker), day,
                    ServerConfig.armorMastery());
            chronicle = outcome.chronicle();
            reachedTier = outcome.reachedTier();
        }
        armor.set(WeaponComponents.CHRONICLE.get(), chronicle);
        return reachedTier;
    }

    /**
     * Damage multiplier a player's worn armor has earned against the attacking creature type.
     * Each piece contributes its own tier, so a fully mastered set protects more than one piece.
     */
    public static double armorMasteryMultiplier(ServerPlayer player, DamageSource source) {
        LivingEntity attacker = creatureAttacker(source);
        if (attacker == null || !ServerConfig.CHRONICLE_ENABLED.get()) {
            return 1.0;
        }
        FoeMastery mastery = ServerConfig.armorMastery();
        if (!mastery.isEnabled()) {
            return 1.0;
        }
        String foeId = foeId(attacker);
        double reduction = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armor = player.getItemBySlot(slot);
            WeaponChronicle chronicle = armor.get(WeaponComponents.CHRONICLE.get());
            if (chronicle != null && ArmorEligibility.isEligible(armor) && !BrokenWeaponHandler.isBroken(armor)) {
                reduction += mastery.bonusForTier(mastery.tierFor(chronicle.killsOf(foeId)));
            }
        }
        return 1.0 - Math.min(MAX_ARMOR_MASTERY_REDUCTION, reduction);
    }

    public static void announceMastery(ServerPlayer player, ItemStack weapon, LivingEntity target, int tier) {
        long percent = Math.round(ServerConfig.foeMastery().bonusForTier(tier) * 100);
        announce(player, Component.translatable("message.weaponleveling.mastery",
                weapon.getHoverName(), target.getType().getDescription(), RomanNumerals.of(tier), percent));
    }

    public static void announceArmorMastery(ServerPlayer player, ItemStack armor, DamageSource source, int tier) {
        LivingEntity attacker = creatureAttacker(source);
        if (attacker == null) {
            return;
        }
        long percent = Math.round(ServerConfig.armorMastery().bonusForTier(tier) * 100);
        announce(player, Component.translatable("message.weaponleveling.armor_mastery",
                armor.getHoverName(), attacker.getType().getDescription(), RomanNumerals.of(tier), percent));
    }

    /** A fully reset item starts a new history, exactly like its level and identity. */
    public static void clear(ItemStack stack) {
        stack.remove(WeaponComponents.CHRONICLE.get());
    }

    public static String foeId(LivingEntity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    /** The creature responsible for the damage, including a shooter behind a projectile; never a player. */
    private static LivingEntity creatureAttacker(DamageSource source) {
        return source != null && source.getEntity() instanceof LivingEntity attacker && !(attacker instanceof Player)
                ? attacker : null;
    }

    private static void announce(ServerPlayer player, Component message) {
        player.sendSystemMessage(message.copy().withStyle(ChatFormatting.GOLD));
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE),
                SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6F, 1.25F,
                player.getRandom().nextLong()));
    }
}
