package dev.weaponleveling.config;

import dev.weaponleveling.core.model.LegacyTier;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue EXPANDED_TOOLTIP_ENABLED = BUILDER
            .define("tooltip.expandedTooltipEnabled", true);
    public static final ModConfigSpec.BooleanValue HOLD_KEY_TO_EXPAND = BUILDER
            .define("tooltip.holdKeyToExpand", true);
    public static final ModConfigSpec.BooleanValue SHOW_XP_NUMBERS = BUILDER
            .define("tooltip.showXpNumbers", true);
    public static final ModConfigSpec.BooleanValue SHOW_XP_PERCENTAGE = BUILDER
            .define("tooltip.showXpPercentage", false);
    public static final ModConfigSpec.BooleanValue CUSTOM_TOOLTIP_PANEL = BUILDER
            .comment("Renders item level and XP with a dedicated pixel panel instead of text glyph decorations.")
            .define("tooltip.customTooltipPanel", true);
    public static final ModConfigSpec.ConfigValue<String> TOOLTIP_MODE = BUILDER
            .comment("Tooltip density: compact, standard, or detailed.")
            .define("tooltip.mode", "standard", ClientConfig::isTooltipMode);
    public static final ModConfigSpec.BooleanValue SHOW_BROKEN_SLOT_OVERLAY = BUILDER
            .comment("Draws a red border and pixel cracks over broken levelable items.")
            .define("tooltip.showBrokenSlotOverlay", true);
    public static final ModConfigSpec.BooleanValue SHOW_LEGACY_IDENTITY = BUILDER
            .comment("Shows permanent Relic and Legacy identity in item tooltips and Jade.")
            .define("tooltip.showLegacyIdentity", true);
    public static final ModConfigSpec.BooleanValue SHOW_PROGRESS_BAR = BUILDER
            .comment("Shows a compact text progress bar under the level line.")
            .define("tooltip.showProgressBar", true);
    public static final ModConfigSpec.BooleanValue SHOW_KEY_HINTS = BUILDER
            .comment("Shows the expand/open key hints in eligible weapon tooltips.")
            .define("tooltip.showKeyHints", true);
    public static final ModConfigSpec.BooleanValue SHOW_COMBAT_STATISTICS = BUILDER
            .comment("Shows total damage and kill statistics in the expanded tooltip.")
            .define("tooltip.showCombatStatistics", true);
    public static final ModConfigSpec.BooleanValue SHOW_CHRONICLE = BUILDER
            .comment("Shows the weapon chronicle (first bearer, favored foes, mastery and milestones) in the expanded tooltip.")
            .define("tooltip.showChronicle", true);
    public static final ModConfigSpec.BooleanValue MILESTONE_NAME_COLORS = BUILDER
            .comment("Colors an item's name as it approaches the level cap: green at 10%, aqua at 25%, purple at 50% and gold at 100%.")
            .define("visual.milestoneNameColors", true);
    public static final ModConfigSpec.BooleanValue RELIC_GLINT = BUILDER
            .comment("Relic and Legacy items shimmer like enchanted items.")
            .define("visual.relicGlint", true);
    public static final ModConfigSpec.IntValue PROGRESS_BAR_SEGMENTS = BUILDER
            .comment("Number of segments in the tooltip progress bar.")
            .defineInRange("tooltip.progressBarSegments", 16, 4, 40);
    public static final ModConfigSpec.ConfigValue<String> TOOLTIP_COLORS = BUILDER
            .comment("Display-only preset: default, high_contrast")
            .define("tooltip.colors", "default", ClientConfig::isColorPreset);
    public static final ModConfigSpec.ConfigValue<String> LEVEL_COLOR = color("tooltip.levelColor", "#FFD166");
    public static final ModConfigSpec.ConfigValue<String> MAX_LEVEL_COLOR = color("tooltip.maxLevelColor", "#FF8A24");
    public static final ModConfigSpec.ConfigValue<String> RELIC_COLOR = color("tooltip.relicColor", "#F0A04B");
    public static final ModConfigSpec.ConfigValue<String> LEGACY_COLOR = color("tooltip.legacyColor", "#FFD76A");
    public static final ModConfigSpec.ConfigValue<String> PROGRESS_FILLED_COLOR = color("tooltip.progressFilledColor", "#45D6A5");
    public static final ModConfigSpec.ConfigValue<String> PROGRESS_EMPTY_COLOR = color("tooltip.progressEmptyColor", "#3D4A5C");
    public static final ModConfigSpec.ConfigValue<String> DETAIL_COLOR = color("tooltip.detailColor", "#A9B7C8");
    public static final ModConfigSpec.DoubleValue LEVEL_UP_SOUND_VOLUME = BUILDER
            .defineInRange("notification.levelUpSoundVolume", 1.0, 0.0, 1.0);
    public static final ModConfigSpec.BooleanValue LEVEL_UP_OVERLAY_ENABLED = BUILDER
            .comment("Shows the compact level seal above the hotbar when the server notification mode is seal.")
            .define("notification.levelUpOverlayEnabled", true);
    public static final ModConfigSpec.BooleanValue LEVEL_UP_SOUND_ENABLED = BUILDER
            .comment("Plays the client-side level-up sound.")
            .define("notification.levelUpSoundEnabled", true);
    public static final ModConfigSpec.BooleanValue KILL_XP_OVERLAY_ENABLED = BUILDER
            .comment("Shows the exact item XP awarded by a kill above the hotbar.")
            .define("notification.killXpOverlayEnabled", true);
    public static final ModConfigSpec.IntValue KILL_XP_OVERLAY_DURATION_MS = BUILDER
            .comment("How long the kill XP notification stays on screen.")
            .defineInRange("notification.killXpOverlayDurationMs", 1_200, 400, 5_000);
    public static final ModConfigSpec.BooleanValue CHAT_SHARE_ENABLED = BUILDER
            .comment("Allows Shift + middle click on an eligible item to share it in chat.")
            .define("interaction.chatShareEnabled", true);
    public static final ModConfigSpec.IntValue LEVEL_UP_OVERLAY_DURATION_MS = BUILDER
            .comment("How long the compact level seal stays on screen. Values are in milliseconds.")
            .defineInRange("notification.levelUpOverlayDurationMs", 5_000, 3_000, 10_000);
    public static final ModConfigSpec.BooleanValue UPDATE_VANILLA_ATTACK_DAMAGE_LINE = BUILDER
            .comment("Replaces the eligible weapon's green vanilla Attack Damage line with its level-adjusted value.")
            .define("tooltip.updateVanillaAttackDamageLine", true);
    public static final ModConfigSpec.BooleanValue SHOW_RANGED_DAMAGE_MULTIPLIER = BUILDER
            .comment("Shows the level-adjusted damage preview for every eligible weapon. Ranged previews use a fully drawn standard-arrow shot.")
            .define("tooltip.showRangedDamageMultiplier", true);
    public static final ModConfigSpec.BooleanValue UPDATE_VANILLA_ARMOR_LINE = BUILDER
            .comment("Replaces the eligible armor's green vanilla Armor line with its level-adjusted value.")
            .define("tooltip.updateVanillaArmorLine", true);
    public static final ModConfigSpec.ConfigValue<String> ATTACK_DAMAGE_DISPLAY_FORMAT = BUILDER
            .comment("Display-only format: base_plus_bonus or total.")
            .define("tooltip.attackDamageDisplayFormat", "total", ClientConfig::isAttackDamageDisplayFormat);
    public static final ModConfigSpec.BooleanValue SHOW_PAUSE_SETTINGS_BUTTON = BUILDER
            .comment("Adds the compact Weapon Leveling button to the pause menu.")
            .define("menu.showPauseSettingsButton", true);
    public static final ModConfigSpec.IntValue PAUSE_SETTINGS_BUTTON_RIGHT_OFFSET = BUILDER
            .comment("Distance from the pause menu's right edge for the compact settings button.")
            .defineInRange("menu.pauseSettingsButtonRightOffset", 8, 0, 100);
    public static final ModConfigSpec.IntValue PAUSE_SETTINGS_BUTTON_TOP_OFFSET = BUILDER
            .comment("Distance from the pause menu's top edge for the compact settings button.")
            .defineInRange("menu.pauseSettingsButtonTopOffset", 8, 0, 100);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClientConfig() {
    }

    public static int levelColor() {
        return highContrast() ? 0xFFFFFF : parseColor(LEVEL_COLOR.get(), 0xFFD166);
    }

    public static int maxLevelColor() {
        return highContrast() ? 0xFFFF55 : parseColor(MAX_LEVEL_COLOR.get(), 0xFF8A24);
    }

    public static int progressFilledColor() {
        return highContrast() ? 0x55FF55 : parseColor(PROGRESS_FILLED_COLOR.get(), 0x45D6A5);
    }

    public static int progressEmptyColor() {
        return highContrast() ? 0x777777 : parseColor(PROGRESS_EMPTY_COLOR.get(), 0x3D4A5C);
    }

    public static int detailColor() {
        return highContrast() ? 0xFFFF55 : parseColor(DETAIL_COLOR.get(), 0xA9B7C8);
    }

    public static int identityColor(LegacyTier legacyTier, int fallback) {
        if (!SHOW_LEGACY_IDENTITY.get()) {
            return fallback;
        }
        return switch (legacyTier) {
            case RELIC -> highContrast() ? 0xFFFF55 : parseColor(RELIC_COLOR.get(), 0xF0A04B);
            case LEGACY -> highContrast() ? 0xFFFFFF : parseColor(LEGACY_COLOR.get(), 0xFFD76A);
            default -> fallback;
        };
    }

    private static ModConfigSpec.ConfigValue<String> color(String path, String defaultValue) {
        return BUILDER.comment("Hex RGB display color, for example #FFD166.")
                .define(path, defaultValue, ClientConfig::isHexColor);
    }

    private static boolean isHexColor(Object value) {
        return value instanceof String string && string.matches("#[0-9a-fA-F]{6}");
    }

    private static boolean isColorPreset(Object value) {
        return value instanceof String string && (string.equals("default") || string.equals("high_contrast"));
    }

    private static boolean isAttackDamageDisplayFormat(Object value) {
        return value instanceof String string && (string.equals("base_plus_bonus") || string.equals("total"));
    }

    private static boolean isTooltipMode(Object value) {
        return value instanceof String string
                && (string.equals("compact") || string.equals("standard") || string.equals("detailed"));
    }

    private static boolean highContrast() {
        return TOOLTIP_COLORS.get().equals("high_contrast");
    }

    private static int parseColor(String value, int fallback) {
        try {
            return Integer.parseInt(value.substring(1), 16);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }
}
