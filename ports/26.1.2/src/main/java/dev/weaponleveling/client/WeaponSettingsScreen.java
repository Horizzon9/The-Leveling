package dev.weaponleveling.client;

import dev.weaponleveling.config.ClientConfig;
import dev.weaponleveling.network.ServerSettingsPayload;
import dev.weaponleveling.network.RequestServerSettingsPayload;
import dev.weaponleveling.network.UpdateServerSettingsPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/** Compact settings screen shared by the pause-menu shortcut and NeoForge's mod-list config entry. */
public final class WeaponSettingsScreen extends Screen {
    private static final int MAX_PANEL_WIDTH = 360;
    private static final int HORIZONTAL_MARGIN = 10;
    private static final int TAB_Y = 30;
    private static final int CONTENT_Y = 62;
    private final Screen parent;
    private final List<Field> fields = new ArrayList<>();
    private int page;
    private ServerSettingsPayload settings;
    private int panelLeft;
    private int panelWidth;
    private int panelBottom;

    public WeaponSettingsScreen(Screen parent) {
        super(Component.translatable("screen.weaponleveling.settings.title"));
        this.parent = parent;
        this.settings = ClientServerSettings.current();
    }

    @Override
    protected void init() {
        if (minecraft != null && minecraft.player != null) {
            ClientPacketDistributor.sendToServer(new RequestServerSettingsPayload());
        }
        fields.clear();
        panelWidth = Math.max(220, Math.min(MAX_PANEL_WIDTH, width - HORIZONTAL_MARGIN * 2));
        panelLeft = (width - panelWidth) / 2;
        int tabGap = 4;
        int tabWidth = (panelWidth - tabGap * 5) / 6;
        addRenderableWidget(Button.builder(Component.translatable("screen.weaponleveling.settings.progression"),
                ignored -> setPage(0)).bounds(panelLeft, TAB_Y, tabWidth, 20).build()).active = page != 0;
        addRenderableWidget(Button.builder(Component.translatable("screen.weaponleveling.settings.xp"),
                ignored -> setPage(1)).bounds(panelLeft + tabWidth + tabGap, TAB_Y, tabWidth, 20).build()).active = page != 1;
        addRenderableWidget(Button.builder(Component.translatable("screen.weaponleveling.settings.display"),
                ignored -> setPage(2)).bounds(panelLeft + (tabWidth + tabGap) * 2, TAB_Y,
                tabWidth, 20).build()).active = page != 2;
        addRenderableWidget(Button.builder(Component.translatable("screen.weaponleveling.settings.stats"),
                ignored -> setPage(3)).bounds(panelLeft + (tabWidth + tabGap) * 3, TAB_Y,
                tabWidth, 20).build()).active = page != 3;
        addRenderableWidget(Button.builder(Component.translatable("screen.weaponleveling.settings.effects"),
                ignored -> setPage(4)).bounds(panelLeft + (tabWidth + tabGap) * 4, TAB_Y,
                tabWidth, 20).build()).active = page != 4;
        addRenderableWidget(Button.builder(Component.translatable("screen.weaponleveling.settings.identity"),
                ignored -> setPage(5)).bounds(panelLeft + (tabWidth + tabGap) * 5, TAB_Y,
                panelWidth - (tabWidth + tabGap) * 5, 20).build()).active = page != 5;

        int contentBottom = switch (page) {
            case 0 -> addProgressionPage(panelLeft, CONTENT_Y);
            case 1 -> addXpPage(panelLeft, CONTENT_Y);
            case 2 -> addDisplayPage(panelLeft, CONTENT_Y);
            case 3 -> addStatisticsPage(panelLeft, CONTENT_Y);
            case 4 -> addEffectsPage(panelLeft, CONTENT_Y);
            default -> addIdentityPage(panelLeft, CONTENT_Y);
        };
        int footerY = Math.min(height - 28, contentBottom + 12);
        addRenderableWidget(Button.builder(Component.translatable("screen.weaponleveling.settings.apply"),
                ignored -> applyPage()).bounds(panelLeft, footerY, (panelWidth - 6) / 2, 20).build())
                .active = settings.canConfigure() && (page < 2 || page == 5);
        addRenderableWidget(Button.builder(Component.translatable("screen.weaponleveling.settings.done"),
                ignored -> onClose()).bounds(panelLeft + (panelWidth + 6) / 2, footerY,
                (panelWidth - 6) / 2, 20).build());
        panelBottom = footerY + 28;
    }

    private int addProgressionPage(int left, int y) {
        y = addField(left, y, "screen.weaponleveling.settings.max_level", "max_level", Integer.toString(settings.maxLevel()));
        y = addField(left, y, "screen.weaponleveling.settings.base_xp", "base_xp", Long.toString(settings.baseXp()));
        y = addField(left, y, "screen.weaponleveling.settings.growth", "growth_factor", format(settings.growthFactor()));
        addToggle(left, y, "screen.weaponleveling.settings.creative_progress", settings.allowCreativeProgress(),
                "allow_creative_progress");
        y += 28;
        addToggle(left, y, "screen.weaponleveling.settings.level_bonus", settings.levelBonusEnabled(),
                "level_bonus_enabled");
        y += 24;
        y = addField(left, y, "screen.weaponleveling.settings.damage_max", "level_max_multiplier",
                format(settings.levelMaxMultiplier()));
        y = addField(left, y, "screen.weaponleveling.settings.damage_curve", "level_curve_exponent",
                format(settings.levelCurveExponent()));
        addToggle(left, y, "screen.weaponleveling.settings.auto_detect_external_weapons",
                settings.autoDetectExternalWeapons(), "auto_detect_external_weapons");
        return y + 20;
    }

    private int addXpPage(int left, int y) {
        addToggle(left, y, "screen.weaponleveling.settings.kill_xp", settings.killXpEnabled(), "kill_xp_enabled");
        y += 28;
        addToggle(left, y, "screen.weaponleveling.settings.player_kill_xp", settings.playerKillXpEnabled(),
                "player_kill_xp_enabled");
        y += 28;
        y = addField(left, y, "screen.weaponleveling.settings.kill_bonus", "kill_bonus", format(settings.killBonus()));
        addToggle(left, y, "screen.weaponleveling.settings.armor_xp", settings.armorLevelingEnabled(),
                "armor_leveling_enabled");
        y += 28;
        y = addField(left, y, "screen.weaponleveling.settings.armor_xp_per_damage", "armor_xp_per_damage",
                format(settings.armorXpPerDamage()));
        y = addField(left, y, "screen.weaponleveling.settings.armor_xp_cap", "armor_max_xp_per_damage_event",
                Long.toString(settings.armorMaxXpPerDamageEvent()));
        addToggle(left, y, "screen.weaponleveling.settings.armor_level_bonus", settings.armorLevelBonusEnabled(),
                "armor_level_bonus_enabled");
        y += 28;
        y = addField(left, y, "screen.weaponleveling.settings.armor_flat", "armor_flat_bonus_per_level",
                format(settings.armorFlatBonusPerLevel()));
        return addField(left, y, "screen.weaponleveling.settings.armor_bonus_cap", "armor_max_level_bonus",
                format(settings.armorMaxLevelBonus()));
    }

    private int addDisplayPage(int left, int y) {
        addClientTooltipModeToggle(left, y);
        y += 28;
        addClientToggle(left, y, "screen.weaponleveling.settings.custom_tooltip_panel",
                ClientConfig.CUSTOM_TOOLTIP_PANEL.get(), ClientConfig.CUSTOM_TOOLTIP_PANEL::set);
        y += 28;
        addClientToggle(left, y, "screen.weaponleveling.settings.broken_slot_overlay",
                ClientConfig.SHOW_BROKEN_SLOT_OVERLAY.get(), ClientConfig.SHOW_BROKEN_SLOT_OVERLAY::set);
        y += 28;
        addClientToggle(left, y, "screen.weaponleveling.settings.show_legacy_identity",
                ClientConfig.SHOW_LEGACY_IDENTITY.get(), ClientConfig.SHOW_LEGACY_IDENTITY::set);
        return y + 20;
    }

    private int addStatisticsPage(int left, int y) {
        addClientToggle(left, y, "screen.weaponleveling.settings.update_attack_damage",
                ClientConfig.UPDATE_VANILLA_ATTACK_DAMAGE_LINE.get(), ClientConfig.UPDATE_VANILLA_ATTACK_DAMAGE_LINE::set);
        y += 28;
        addClientToggle(left, y, "screen.weaponleveling.settings.show_ranged_damage_multiplier",
                ClientConfig.SHOW_RANGED_DAMAGE_MULTIPLIER.get(), ClientConfig.SHOW_RANGED_DAMAGE_MULTIPLIER::set);
        y += 28;
        addClientToggle(left, y, "screen.weaponleveling.settings.update_armor",
                ClientConfig.UPDATE_VANILLA_ARMOR_LINE.get(), ClientConfig.UPDATE_VANILLA_ARMOR_LINE::set);
        y += 28;
        addClientFormatToggle(left, y);
        return y + 20;
    }

    private int addEffectsPage(int left, int y) {
        addClientToggle(left, y, "screen.weaponleveling.settings.level_up_overlay",
                ClientConfig.LEVEL_UP_OVERLAY_ENABLED.get(), ClientConfig.LEVEL_UP_OVERLAY_ENABLED::set);
        y += 28;
        addClientToggle(left, y, "screen.weaponleveling.settings.level_up_sound",
                ClientConfig.LEVEL_UP_SOUND_ENABLED.get(), ClientConfig.LEVEL_UP_SOUND_ENABLED::set);
        y += 28;
        addClientToggle(left, y, "screen.weaponleveling.settings.kill_xp_overlay",
                ClientConfig.KILL_XP_OVERLAY_ENABLED.get(), ClientConfig.KILL_XP_OVERLAY_ENABLED::set);
        y += 28;
        addClientToggle(left, y, "screen.weaponleveling.settings.low_durability_warning",
                ClientConfig.SHOW_LOW_DURABILITY_WARNING.get(), ClientConfig.SHOW_LOW_DURABILITY_WARNING::set);
        y += 28;
        addClientToggle(left, y, "screen.weaponleveling.settings.chat_share",
                ClientConfig.CHAT_SHARE_ENABLED.get(), ClientConfig.CHAT_SHARE_ENABLED::set);
        y += 28;
        addClientToggle(left, y, "screen.weaponleveling.settings.show_pause_button",
                ClientConfig.SHOW_PAUSE_SETTINGS_BUTTON.get(), ClientConfig.SHOW_PAUSE_SETTINGS_BUTTON::set);
        return y + 20;
    }

    private int addIdentityPage(int left, int y) {
        y = addField(left, y, "screen.weaponleveling.settings.relic_level_percent", "relic_level_percent",
                Integer.toString(settings.relicLevelPercent()));
        y = addField(left, y, "screen.weaponleveling.settings.legacy_level_percent", "legacy_level_percent",
                Integer.toString(settings.legacyLevelPercent()));
        addToggle(left, y, "screen.weaponleveling.settings.preserve_broken", settings.preserveBrokenItems(),
                "preserve_broken_items");
        y += 28;
        addToggle(left, y, "screen.weaponleveling.settings.auto_replace_broken", settings.autoReplaceBroken(),
                "auto_replace_broken");
        return y + 20;
    }

    private int addField(int left, int y, String labelKey, String key, String value) {
        EditBox input = new EditBox(font, left, y + 11, panelWidth, 20, Component.translatable(labelKey));
        input.setMaxLength(32);
        input.setValue(value);
        input.active = settings.canConfigure();
        fields.add(new Field(labelKey, key, input, y));
        addRenderableWidget(input);
        return y + 37;
    }

    private void addToggle(int x, int y, String labelKey, boolean currentValue, String key) {
        String value = Boolean.toString(!currentValue);
        Component label = Component.translatable(labelKey, currentValue ? Component.translatable("screen.weaponleveling.settings.on")
                : Component.translatable("screen.weaponleveling.settings.off"));
        if (!settings.canConfigure()) {
            label = locked(label);
        }
        Button button = Button.builder(label, ignored -> ClientPacketDistributor.sendToServer(
                new UpdateServerSettingsPayload(List.of(new UpdateServerSettingsPayload.SettingChange(key, value)))))
                .bounds(x, y, panelWidth, 20).build();
        button.active = settings.canConfigure();
        addRenderableWidget(button);
    }

    private void addClientToggle(int left, int y, String labelKey, boolean currentValue,
                                 java.util.function.Consumer<Boolean> setter) {
        addRenderableWidget(Button.builder(Component.translatable(labelKey,
                currentValue ? Component.translatable("screen.weaponleveling.settings.on")
                        : Component.translatable("screen.weaponleveling.settings.off")), ignored -> {
            setter.accept(!currentValue);
            ClientConfig.SPEC.save();
            rebuildWidgets();
        }).bounds(left, y, panelWidth, 20).build());
    }

    private void addClientFormatToggle(int left, int y) {
        boolean basePlusBonus = ClientConfig.ATTACK_DAMAGE_DISPLAY_FORMAT.get().equals("base_plus_bonus");
        addRenderableWidget(Button.builder(Component.translatable("screen.weaponleveling.settings.attack_damage_format",
                Component.translatable(basePlusBonus ? "screen.weaponleveling.settings.base_plus_bonus"
                        : "screen.weaponleveling.settings.total")), ignored -> {
            ClientConfig.ATTACK_DAMAGE_DISPLAY_FORMAT.set(basePlusBonus ? "total" : "base_plus_bonus");
            ClientConfig.SPEC.save();
            rebuildWidgets();
        }).bounds(left, y, panelWidth, 20).build());
    }

    private void addClientTooltipModeToggle(int left, int y) {
        String current = ClientConfig.TOOLTIP_MODE.get();
        String next = switch (current) {
            case "compact" -> "standard";
            case "standard" -> "detailed";
            default -> "compact";
        };
        addRenderableWidget(Button.builder(Component.translatable("screen.weaponleveling.settings.tooltip_mode",
                Component.translatable("screen.weaponleveling.settings.tooltip_mode." + current)), ignored -> {
            ClientConfig.TOOLTIP_MODE.set(next);
            ClientConfig.SPEC.save();
            rebuildWidgets();
        }).bounds(left, y, panelWidth, 20).build());
    }

    private void applyPage() {
        if (!settings.canConfigure() || fields.isEmpty()) {
            return;
        }
        List<UpdateServerSettingsPayload.SettingChange> changes = new ArrayList<>(fields.size());
        for (Field field : fields) {
            String value = field.input().getValue().trim();
            changes.add(new UpdateServerSettingsPayload.SettingChange(field.key(), value));
        }
        ClientPacketDistributor.sendToServer(new UpdateServerSettingsPayload(changes));
    }

    public void applyServerSettings(ServerSettingsPayload updated) {
        settings = updated;
        rebuildWidgets();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(panelLeft - 5, 12, panelLeft + panelWidth + 5, panelBottom, 0xEE111827);
        graphics.fill(panelLeft - 5, 12, panelLeft + panelWidth + 5, 14, 0xFFD6A54D);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.centeredText(font, title, width / 2, 12, 0xFFFFFFFF);
        if (!settings.canConfigure() && (page < 2 || page == 5)) {
            graphics.centeredText(font, Component.translatable("screen.weaponleveling.settings.read_only"),
                    width / 2, panelBottom + 6, 0xFFFFAA00);
        }
        if (settings.usesExactXpTable() && page == 0) {
            graphics.text(font, Component.translatable("screen.weaponleveling.settings.xp_table_note"),
                    panelLeft, panelBottom + 6, 0xFFFFAA00, false);
        }
        for (Field field : fields) {
            // Labels are rendered last and on an opaque strip so pause-menu blur cannot wash them out.
            graphics.fill(panelLeft, field.y() - 1, panelLeft + panelWidth, field.y() + 10, 0xFF182235);
            Component label = settings.canConfigure() ? Component.translatable(field.labelKey())
                    : locked(Component.translatable(field.labelKey()));
            graphics.text(font, label, panelLeft + 4, field.y(), 0xFFFFFFFF, true);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreenAndShow(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private void setPage(int requestedPage) {
        page = requestedPage;
        rebuildWidgets();
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static Component locked(Component label) {
        return Component.translatable("screen.weaponleveling.settings.locked", label);
    }

    private record Field(String labelKey, String key, EditBox input, int y) {
    }
}
