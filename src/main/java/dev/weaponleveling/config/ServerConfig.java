package dev.weaponleveling.config;

import dev.weaponleveling.core.chronicle.FoeMastery;
import dev.weaponleveling.core.chronicle.KillStreak;
import dev.weaponleveling.core.xp.XpCurve;
import dev.weaponleveling.core.xp.LegacyPolicy;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_LEVEL = BUILDER
            .comment("Maximum weapon level.")
            .defineInRange("progression.maxLevel", 500, 1, 10_000);
    public static final ModConfigSpec.LongValue BASE_XP = BUILDER
            .comment("Default requirement to move from level 0 to 1. The 500-level curve starts at 100 XP.")
            .defineInRange("progression.baseXp", 100L, 1L, Long.MAX_VALUE);
    public static final ModConfigSpec.DoubleValue XP_REQUIREMENT_MULTIPLIER = BUILDER
            .comment("Scales the XP every level needs, for the formula and the exact table alike. 0.5 halves the cost of every level, 2 doubles it.")
            .defineInRange("progression.xpRequirementMultiplier", 0.5, 0.01, 100.0);
    public static final ModConfigSpec.DoubleValue GROWTH_FACTOR = BUILDER
            .comment("Default exponential XP growth. 1.02432611896515 raises each next level by about 2.433%; level 0 to 500 needs about 680.8 million total XP.")
            .defineInRange("progression.growthFactor", 1.02432611896515, 1.0, 100.0);
    public static final ModConfigSpec.BooleanValue SCALE_CURVE_WITH_MAX_LEVEL = BUILDER
            .comment("Stretches the XP curve to the configured maxLevel so higher caps stay reachable: each level costs baseXp * growthFactor^(level * 500 / maxLevel). At maxLevel 500 nothing changes; at 1000 the last level costs the same as at 500 and total XP roughly doubles instead of growing exponentially. Set false to apply growthFactor per level regardless of maxLevel. Ignored while xpTable is used.")
            .define("progression.scaleCurveWithMaxLevel", true);
    public static final ModConfigSpec.ConfigValue<List<? extends Long>> XP_TABLE = BUILDER
            .comment("Optional exact per-level requirements. Entry 1 is the XP required to move from level 0 to 1. Empty uses the formula.")
            .defineListAllowEmpty("progression.xpTable", List.of(), () -> 1L,
                    value -> value instanceof Number number && number.longValue() > 0);
    public static final ModConfigSpec.BooleanValue ALLOW_CREATIVE_PROGRESS = BUILDER
            .comment("Allows weapons and armor to gain progression while their holder is in Creative mode.")
            .define("progression.allowCreativeProgress", false);
    public static final ModConfigSpec.IntValue RELIC_LEVEL_PERCENT = BUILDER
            .comment("Percentage of the configured maximum level that permanently grants Relic identity.")
            .defineInRange("identity.relicLevelPercent", 25, 1, 100);
    public static final ModConfigSpec.IntValue LEGACY_LEVEL_PERCENT = BUILDER
            .comment("Percentage of the configured maximum level that permanently grants Legacy identity."
                    + " Values below the Relic threshold are safely treated as equal to it.")
            .defineInRange("identity.legacyLevelPercent", 100, 1, 100);

    public static final ModConfigSpec.BooleanValue KILL_XP_ENABLED = BUILDER
            .define("xp.killXpEnabled", true);
    public static final ModConfigSpec.BooleanValue PLAYER_KILL_XP_ENABLED = BUILDER
            .comment("Awards the normal kill XP for killing another player. Disabled by default; hits and damage never award XP.")
            .define("xp.playerKillXpEnabled", false);
    public static final ModConfigSpec.DoubleValue KILL_BONUS = BUILDER
            .defineInRange("xp.killBonus", 5.0, 0.0, 1_000_000.0);
    public static final ModConfigSpec.DoubleValue MOB_HEALTH_BASELINE = BUILDER
            .comment("Maximum health that awards exactly the configured kill bonus."
                    + " Every vanilla and modded living entity scales linearly from this value.")
            .defineInRange("xp.mobHealthBaseline", 20.0, 0.1, 1_000_000.0);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITY_MULTIPLIERS = BUILDER
            .comment("Optional entity XP overrides in namespace:id=value form."
                    + " Without an override, every vanilla and modded living entity scales directly from maximum health."
                    + " With the default baseline, 20 maximum health = 1x the configured kill bonus.")
            .defineListAllowEmpty("xp.entityMultipliers", List.of(), () -> "minecraft:zombie=1.0",
                    ServerConfig::isEntityMultiplier);
    public static final ModConfigSpec.LongValue MAX_XP_PER_ATTACK = BUILDER
            .comment("Maximum XP awarded for one eligible kill.")
            .defineInRange("xp.maxXpPerAttack", 10_000L, 0L, Long.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue ENABLE_ANTI_ABUSE = BUILDER
            .comment("Caps actual damage tracking at the target's pre-hit health.")
            .define("xp.enableAntiAbuse", true);

    public static final ModConfigSpec.BooleanValue TOOL_LEVELING_ENABLED = BUILDER
            .comment("Lets pickaxes, shovels and hoes gain XP by breaking blocks they are the right tool for, and mine faster as they level. Axes stay weapons and level by fighting.")
            .define("tools.levelingEnabled", true);
    public static final ModConfigSpec.DoubleValue TOOL_XP_PER_HARDNESS = BUILDER
            .comment("Tool XP per point of block hardness (stone 1.5, ores 3, obsidian 50). Mining is much faster than fighting, so the default is low: stone averages 0.15 XP, about 33 stone per zombie kill. Fractions are paid out by chance. Blocks that break instantly give nothing.")
            .defineInRange("tools.xpPerHardness", 0.1, 0.0, 1000.0);
    public static final ModConfigSpec.LongValue TOOL_MAX_XP_PER_BLOCK = BUILDER
            .comment("Maximum tool XP from one broken block.")
            .defineInRange("tools.maxXpPerBlock", 50L, 0L, 1_000_000L);
    public static final ModConfigSpec.DoubleValue TOOL_MAX_MINING_BONUS = BUILDER
            .comment("Mining efficiency a tool at the level cap adds in the main hand, growing linearly with level. Uses the vanilla mining_efficiency attribute: Efficiency I adds 2, III adds 10, V adds 26.")
            .defineInRange("tools.maxMiningEfficiencyBonus", 10.0, 0.0, 100.0);
    public static final ModConfigSpec.BooleanValue PRESERVE_BROKEN_ITEMS = BUILDER
            .comment("When a levelable item reaches zero durability it becomes Broken (unusable, kept, repairable) instead of vanishing. Off restores vanilla: the item is destroyed and its level is lost.")
            .define("broken.preserveItems", true);
    public static final ModConfigSpec.BooleanValue AUTO_REPLACE_BROKEN = BUILDER
            .comment("When a levelable item breaks, swap a matching unbroken item from the inventory into its place and say so in chat. Needs broken.preserveItems for the broken item to be kept.")
            .define("broken.autoReplace", false);
    public static final ModConfigSpec.BooleanValue SHIELD_LEVELING_ENABLED = BUILDER
            .comment("Lets shields gain XP from the damage they block. A levelled shield also loses durability more slowly. Needs no armor settings to be on.")
            .define("shield.levelingEnabled", true);
    public static final ModConfigSpec.DoubleValue SHIELD_MAX_DURABILITY_SAVING = BUILDER
            .comment("Share of the durability a block would cost that a shield at the level cap saves. Grows linearly with level; 0.5 means a max-level shield loses half as much durability per block.")
            .defineInRange("shield.maxDurabilitySaving", 0.5, 0.0, 0.9);
    public static final ModConfigSpec.BooleanValue ARMOR_LEVELING_ENABLED = BUILDER
            .comment("Lets equipped player armor gain XP from damage remaining after armor, enchantment, and effect mitigation."
                    + " Absorption does not prevent armor XP.")
            .define("armor.levelingEnabled", true);
    public static final ModConfigSpec.DoubleValue ARMOR_XP_PER_DAMAGE = BUILDER
            .comment("XP awarded to each equipped levelable armor piece per point of mitigated damage before absorption.")
            .defineInRange("armor.xpPerDamage", 1.0, 0.0, 1_000_000.0);
    public static final ModConfigSpec.LongValue ARMOR_MAX_XP_PER_DAMAGE_EVENT = BUILDER
            .comment("Maximum XP that one equipped armor item can gain from one damage event.")
            .defineInRange("armor.maxXpPerDamageEvent", 10_000L, 0L, Long.MAX_VALUE);
    public static final ModConfigSpec.DoubleValue ARMOR_TARGET_XP_MULTIPLIER = BUILDER
            .comment("All equipped armor pieces gain damage XP, but the piece that was hit gets this many times more.")
            .defineInRange("armor.targetedPieceXpMultiplier", 2.0, 1.0, 100.0);
    public static final ModConfigSpec.DoubleValue ARMOR_KILL_XP_SHARE = BUILDER
            .comment("Share of the XP a weapon earns that every equipped armor piece also receives (0.05 = 5%). 0 disables it.")
            .defineInRange("armor.weaponXpShare", 0.05, 0.0, 1.0);
    public static final ModConfigSpec.BooleanValue ARMOR_LEVEL_BONUS_ENABLED = BUILDER
            .comment("Applies an Armor attribute bonus for every level gained after level zero.")
            .define("armor.levelBonusEnabled", true);
    public static final ModConfigSpec.DoubleValue ARMOR_FLAT_BONUS_PER_LEVEL = BUILDER
            .comment("Armor added per gained level. The default 0.02 gives +1 Armor for every 50 levels per piece (+10 at level 500)."
                    + " Whatever this says, a piece always earns at least +1 Armor per 100 levels.")
            .defineInRange("armor.flatBonusPerLevel", 0.02, 0.0, 1_000_000.0);
    public static final ModConfigSpec.DoubleValue ARMOR_MAX_LEVEL_BONUS = BUILDER
            .comment("Maximum Armor bonus per piece from levels. Never cuts below the guaranteed +1 Armor per 100 levels.")
            .defineInRange("armor.maxLevelBonus", 20.0, 0.0, 1_000_000.0);

    public static final ModConfigSpec.BooleanValue LEVEL_DAMAGE_BONUS_ENABLED = BUILDER
            .comment("Applies the bounded level damage curve after level zero.")
            .define("damage.levelBonusEnabled", true);
    public static final ModConfigSpec.DoubleValue LEVEL_DAMAGE_MAX_MULTIPLIER = BUILDER
            .comment("Damage multiplier reached at the configured maximum level. Default: 11.0 makes a 7-damage weapon deal about 77 damage at level 500.")
            .defineInRange("damage.levelBonusMaxMultiplier", 11.0, 1.0, 1_000_000.0);
    public static final ModConfigSpec.DoubleValue LEVEL_DAMAGE_CURVE_EXPONENT = BUILDER
            .comment("Curve shape for level damage. The 500-level default of 1.05 remains gently exponential while keeping an early visible bonus.")
            .defineInRange("damage.levelBonusCurveExponent", 1.05, 0.1, 10.0);

    public static final ModConfigSpec.BooleanValue AUTO_DETECT_EXTERNAL_WEAPONS = BUILDER
            .comment("Automatically levels non-vanilla, damageable items with a main-hand Attack Damage attribute."
                    + " Vanilla pickaxes, shovels, and hoes stay excluded.")
            .define("compatibility.autoDetectExternalWeapons", true);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ADDITIONAL_WEAPON_ITEMS = BUILDER
            .comment("Item IDs that must level even without an Attack Damage attribute.")
            .defineListAllowEmpty("compatibility.additionalWeaponItems", List.of(), () -> "examplemod:example_weapon",
                    ServerConfig::isItemId);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_WEAPON_ITEMS = BUILDER
            .comment("Item IDs excluded from leveling even when automatic compatibility detects them.")
            .defineListAllowEmpty("compatibility.excludedWeaponItems", List.of(), () -> "examplemod:example_tool",
                    ServerConfig::isItemId);
    public static final ModConfigSpec.ConfigValue<String> LEVEL_UP_NOTIFICATION = BUILDER
            .comment("One of: seal, actionbar, chat, off. Seal and actionbar use the compact level-up toast; chat posts a message.")
            .define("notification.levelUp", "seal", ServerConfig::isNotificationMode);

    public static final ModConfigSpec.IntValue COMMAND_PERMISSION_LEVEL = BUILDER
            .comment("Vanilla permission level required for /weaponlevel commands. 2 is the normal operator level.")
            .defineInRange("commands.permissionLevel", 2, 0, 4);
    public static final ModConfigSpec.BooleanValue ALLOW_IN_GAME_CONFIGURATION = BUILDER
            .comment("Allows operators to change the supported server settings from The Leveling menu.")
            .define("commands.allowInGameConfiguration", true);
    public static final ModConfigSpec.BooleanValue ALLOW_LEVEL_COMMANDS = BUILDER
            .comment("Allows exact level changes and relative level changes through /weaponlevel.")
            .define("commands.allowLevelCommands", true);
    public static final ModConfigSpec.BooleanValue ALLOW_LEVEL_REDUCTION = BUILDER
            .comment("Allows /weaponlevel set and addlevel to reduce a weapon level to zero, but never below zero.")
            .define("commands.allowLevelReduction", true);
    public static final ModConfigSpec.BooleanValue ALLOW_XP_COMMANDS = BUILDER
            .comment("Allows absolute and additive XP commands through /weaponlevel.")
            .define("commands.allowXpCommands", true);
    public static final ModConfigSpec.BooleanValue ALLOW_XP_REDUCTION = BUILDER
            .comment("Allows /weaponlevel setxp to lower total weapon XP. This may return an item to level zero, but never below it.")
            .define("commands.allowXpReduction", true);
    public static final ModConfigSpec.LongValue MAX_COMMAND_XP = BUILDER
            .comment("Maximum XP accepted by one /weaponlevel setxp or addxp command. The 500-level default allows the 680.8 million XP cap while keeping accidental values bounded.")
            .defineInRange("commands.maxXp", 1_000_000_000L, 0L, Long.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue CHRONICLE_ENABLED = BUILDER
            .comment("Keeps a chronicle on every weapon: its first bearer, the creatures it hunts most, and notable moments such as its first kill, first boss kill and the day it became a Relic or Legacy. Foe mastery needs the chronicle to count kills.")
            .define("chronicle.enabled", true);
    public static final ModConfigSpec.BooleanValue FOE_MASTERY_ENABLED = BUILDER
            .comment("Weapons earn mastery tiers against a creature type after defeating it many times. Each tier adds a damage bonus against that type only. Never applies against players.")
            .define("chronicle.foeMastery.enabled", true);
    public static final ModConfigSpec.IntValue FOE_MASTERY_TIER_ONE_KILLS = BUILDER
            .comment("Kills of one creature type needed for mastery tier I.")
            .defineInRange("chronicle.foeMastery.tierOneKills", 100, 1, 10_000_000);
    public static final ModConfigSpec.IntValue FOE_MASTERY_TIER_TWO_KILLS = BUILDER
            .comment("Kills of one creature type needed for mastery tier II. Values not above tier I are ignored.")
            .defineInRange("chronicle.foeMastery.tierTwoKills", 500, 1, 10_000_000);
    public static final ModConfigSpec.IntValue FOE_MASTERY_TIER_THREE_KILLS = BUILDER
            .comment("Kills of one creature type needed for mastery tier III. Values not above tier II are ignored.")
            .defineInRange("chronicle.foeMastery.tierThreeKills", 2000, 1, 10_000_000);
    public static final ModConfigSpec.DoubleValue FOE_MASTERY_DAMAGE_BONUS_PER_TIER = BUILDER
            .comment("Extra damage per mastery tier against the mastered creature type, applied after the level multiplier. 0.05 means +5% per tier.")
            .defineInRange("chronicle.foeMastery.damageBonusPerTier", 0.05, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue TITLES_ENABLED = BUILDER
            .comment("Lets players give a Relic or Legacy item a permanent name with /leveling title. Once named, only operators can rename it.")
            .define("chronicle.titlesEnabled", true);
    public static final ModConfigSpec.BooleanValue ARMOR_MASTERY_ENABLED = BUILDER
            .comment("Armor earns mastery tiers against a creature type after enduring many of its hits. Each tier of each worn piece reduces damage from that type only. Never applies against players.")
            .define("chronicle.armorMastery.enabled", true);
    public static final ModConfigSpec.IntValue ARMOR_MASTERY_TIER_ONE_HITS = BUILDER
            .comment("Hits from one creature type an armor piece must endure for mastery tier I.")
            .defineInRange("chronicle.armorMastery.tierOneHits", 200, 1, 10_000_000);
    public static final ModConfigSpec.IntValue ARMOR_MASTERY_TIER_TWO_HITS = BUILDER
            .comment("Hits needed for armor mastery tier II. Values not above tier I are ignored.")
            .defineInRange("chronicle.armorMastery.tierTwoHits", 1000, 1, 10_000_000);
    public static final ModConfigSpec.IntValue ARMOR_MASTERY_TIER_THREE_HITS = BUILDER
            .comment("Hits needed for armor mastery tier III. Values not above tier II are ignored.")
            .defineInRange("chronicle.armorMastery.tierThreeHits", 4000, 1, 10_000_000);
    public static final ModConfigSpec.DoubleValue ARMOR_MASTERY_REDUCTION_PER_TIER = BUILDER
            .comment("Damage reduction per mastery tier of each worn piece against the mastered type. 0.01 means a full set at tier III takes 12% less damage from it. The total never exceeds 50%.")
            .defineInRange("chronicle.armorMastery.damageReductionPerTier", 0.01, 0.0, 0.125);

    public static final ModConfigSpec.ConfigValue<String> SOULBOUND_TIER = BUILDER
            .comment("Identity from which items are soulbound and stay with their owner through death: legacy, relic or off. Has no effect while keepInventory is on.")
            .define("soulbound.tier", "legacy", ServerConfig::isSoulboundTier);
    public static final ModConfigSpec.BooleanValue LEGACY_KILL_EFFECT = BUILDER
            .comment("Shows an amber particle burst and a chime where a creature falls to a Legacy weapon.")
            .define("effects.legacyKillEffect", true);

    public static final ModConfigSpec.BooleanValue KILL_STREAK_ENABLED = BUILDER
            .comment("Rapid consecutive kills with the same weapon build a streak that raises the kill XP of each following kill.")
            .define("streak.enabled", true);
    public static final ModConfigSpec.IntValue KILL_STREAK_WINDOW_SECONDS = BUILDER
            .comment("Longest pause between two kills that keeps a streak alive.")
            .defineInRange("streak.windowSeconds", 6, 1, 60);
    public static final ModConfigSpec.DoubleValue KILL_STREAK_BONUS_PER_KILL = BUILDER
            .comment("Extra kill XP added by every streak kill after the first. 0.1 means the second kill gives +10%, the third +20%.")
            .defineInRange("streak.xpBonusPerKill", 0.10, 0.0, 1.0);
    public static final ModConfigSpec.DoubleValue KILL_STREAK_MAX_BONUS = BUILDER
            .comment("Cap on the total streak XP bonus. 0.5 means a streak never gives more than +50% XP. xp.maxXpPerAttack still applies.")
            .defineInRange("streak.maxXpBonus", 0.50, 0.0, 5.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ServerConfig() {
    }

    public static XpCurve xpCurve() {
        double growth = SCALE_CURVE_WITH_MAX_LEVEL.get()
                ? XpCurve.scaledGrowthFactor(GROWTH_FACTOR.get(), MAX_LEVEL.get())
                : GROWTH_FACTOR.get();
        return new XpCurve(MAX_LEVEL.get(), effectiveBaseXp(), growth, effectiveXpTable());
    }

    /** Base XP after {@link #XP_REQUIREMENT_MULTIPLIER}; the client curve is built from the same numbers. */
    public static long effectiveBaseXp() {
        return XpCurve.scaleRequirement(BASE_XP.get(), XP_REQUIREMENT_MULTIPLIER.get());
    }

    public static java.util.List<Long> effectiveXpTable() {
        double multiplier = XP_REQUIREMENT_MULTIPLIER.get();
        return XP_TABLE.get().stream().map(value -> XpCurve.scaleRequirement(value.longValue(), multiplier)).toList();
    }

    public static LegacyPolicy legacyPolicy() {
        int legacyPercent = LEGACY_LEVEL_PERCENT.get();
        return new LegacyPolicy(Math.min(RELIC_LEVEL_PERCENT.get(), legacyPercent), legacyPercent);
    }

    public static FoeMastery foeMastery() {
        return FoeMastery.fromConfig(CHRONICLE_ENABLED.get() && FOE_MASTERY_ENABLED.get(),
                List.of(FOE_MASTERY_TIER_ONE_KILLS.get(), FOE_MASTERY_TIER_TWO_KILLS.get(),
                        FOE_MASTERY_TIER_THREE_KILLS.get()),
                FOE_MASTERY_DAMAGE_BONUS_PER_TIER.get());
    }

    public static FoeMastery armorMastery() {
        return FoeMastery.fromConfig(CHRONICLE_ENABLED.get() && ARMOR_MASTERY_ENABLED.get(),
                List.of(ARMOR_MASTERY_TIER_ONE_HITS.get(), ARMOR_MASTERY_TIER_TWO_HITS.get(),
                        ARMOR_MASTERY_TIER_THREE_HITS.get()),
                ARMOR_MASTERY_REDUCTION_PER_TIER.get());
    }

    public static KillStreak.Rules killStreakRules() {
        if (!KILL_STREAK_ENABLED.get()) {
            return KillStreak.Rules.DISABLED;
        }
        return new KillStreak.Rules(KILL_STREAK_WINDOW_SECONDS.get() * 20L, KILL_STREAK_BONUS_PER_KILL.get(),
                KILL_STREAK_MAX_BONUS.get());
    }

    private static boolean isSoulboundTier(Object value) {
        return value instanceof String string && (string.equals("legacy") || string.equals("relic")
                || string.equals("off"));
    }

    private static boolean isNotificationMode(Object value) {
        return value instanceof String string && (string.equals("seal") || string.equals("actionbar")
                || string.equals("chat") || string.equals("off"));
    }

    private static boolean isEntityMultiplier(Object value) {
        if (!(value instanceof String string)) {
            return false;
        }
        int separator = string.lastIndexOf('=');
        if (separator <= 0 || net.minecraft.resources.ResourceLocation.tryParse(
                string.substring(0, separator)) == null) {
            return false;
        }
        try {
            double multiplier = Double.parseDouble(string.substring(separator + 1));
            return Double.isFinite(multiplier) && multiplier >= 0 && multiplier <= 1000;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static boolean isItemId(Object value) {
        return value instanceof String string && net.minecraft.resources.ResourceLocation.tryParse(string) != null;
    }
}
