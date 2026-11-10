package dev.weaponleveling.client;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.config.ClientConfig;
import dev.weaponleveling.core.combat.ArmorLevelBonus;
import dev.weaponleveling.core.combat.LevelDamageBonus;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.core.model.LegacyPersona;
import dev.weaponleveling.integration.ArmorEligibility;
import dev.weaponleveling.integration.ToolEligibility;
import dev.weaponleveling.integration.ArmorProtection;
import dev.weaponleveling.integration.WeaponComponents;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.LevelableItemEligibility;
import dev.weaponleveling.integration.WeaponDamage;
import dev.weaponleveling.integration.WeaponEligibility;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import dev.weaponleveling.platform.event.TooltipEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import dev.weaponleveling.network.RequestServerSettingsPayload;
import dev.weaponleveling.network.ServerSettingsPayload;
import dev.weaponleveling.network.ShareItemPayload;
import dev.weaponleveling.platform.net.ModNetwork;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;

import java.text.NumberFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ClientEvents {
    private ClientEvents() {
    }

    /**
     * Platform eslemesi (NeoForge -> Fabric):
     * - ItemTooltipEvent                    -> ItemTooltipCallback
     * - RenderTooltipEvent.GatherComponents -> TooltipEvents (mixin ile vanilla ipucu yolu)
     * - ScreenEvent.Init.Post               -> ScreenEvents.AFTER_INIT
     * - RenderGuiEvent.Post                 -> HudRenderCallback
     * - ScreenEvent.MouseButtonPressed.Pre  -> ScreenMouseEvents.allowMouseClick
     */
    public static void register() {
        ItemTooltipCallback.EVENT.register(ClientEvents::addTooltip);
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(IdentityAura::tick);
        TooltipEvents.register(ClientEvents::levelPanelFor);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, "leveling_overlays"),
                (graphics, tickCounter) -> {
                    LevelUpSealOverlay.render(graphics);
                    KillXpOverlay.render(graphics);
                });
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            addPauseSettingsButton(screen);
            refreshSettingsWhenInventoryOpens(screen);
            ScreenMouseEvents.allowMouseClick(screen).register(
                    (clickedScreen, click) -> !shareHoveredItem(clickedScreen, click.button()));
        });
    }

    private static void addTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag flag,
                                   List<Component> tooltip) {
        var serverSettings = ClientServerSettings.current();
        boolean isWeapon = WeaponEligibility.isEligible(stack, serverSettings.autoDetectExternalWeapons());
        boolean isArmor = ArmorEligibility.isEligible(stack);
        boolean isTool = ToolEligibility.isEligible(stack);
        if (!isWeapon && !isArmor && !isTool) {
            return;
        }
        WeaponProgress progress = stack.get(WeaponComponents.PROGRESS.get());
        int level = progress == null ? 0 : progress.level();
        long currentXp = progress == null ? 0 : progress.currentXp();
        LegacyTier legacyTier = progress == null ? LegacyTier.NONE : progress.legacyTier();
        LegacyPersona persona = LegacyPersona.resolve(progress, isArmor,
                isWeapon && WeaponEligibility.isRanged(stack));
        long requiredXp = ClientServerSettings.requiredXp(level);
        // Eligibility is already established above; read the flag once and reuse it below instead
        // of every helper re-deriving it, since this whole method reruns every frame a tooltip shows.
        boolean broken = BrokenWeaponHandler.isMarkedBroken(stack);
        if (isWeapon && ClientConfig.UPDATE_VANILLA_ATTACK_DAMAGE_LINE.get()) {
            updateVanillaAttackDamageLine(tooltip, stack, level, broken);
        }
        if (isArmor && ClientConfig.UPDATE_VANILLA_ARMOR_LINE.get()) {
            updateVanillaArmorLine(tooltip, stack, level, broken);
        }

        List<Component> levelDetails = new ArrayList<>();
        String tooltipMode = ClientConfig.TOOLTIP_MODE.get();
        if (broken) {
            levelDetails.add(Component.translatable("tooltip.weaponleveling.broken").withStyle(ChatFormatting.RED));
            levelDetails.add(Component.translatable("tooltip.weaponleveling.broken_repair").withStyle(ChatFormatting.GRAY));
        }
        if (!ClientConfig.CUSTOM_TOOLTIP_PANEL.get()) {
            levelDetails.add(Component.literal("  ").append(Component.translatable("tooltip.weaponleveling.item_level", level,
                    serverSettings.maxLevel())).withStyle(style -> style.withColor(level >= serverSettings.maxLevel()
                    ? ClientConfig.maxLevelColor() : ClientConfig.levelColor())));
            if (level >= serverSettings.maxLevel()) {
                levelDetails.add(Component.literal("  ").append(Component.translatable("tooltip.weaponleveling.max_level"))
                        .withStyle(ChatFormatting.GREEN));
            } else if (!tooltipMode.equals("compact") && ClientConfig.SHOW_XP_NUMBERS.get()) {
                levelDetails.add(Component.literal("  ").append(Component.translatable("tooltip.weaponleveling.xp", currentXp, requiredXp))
                        .withStyle(style -> style.withColor(ClientConfig.detailColor())));
            }
            if (ClientConfig.SHOW_LEGACY_IDENTITY.get() && legacyTier != LegacyTier.NONE) {
                levelDetails.add(Component.literal("  ").append(Component.translatable(
                                "tooltip.weaponleveling.identity_persona",
                                Component.translatable("tooltip.weaponleveling.identity." + legacyTier.serializedName()),
                                Component.translatable("tooltip.weaponleveling.persona." + persona.path().serializedName()),
                                persona.renownRank()))
                        .withStyle(style -> style.withColor(ClientConfig.identityColor(legacyTier,
                                ClientConfig.levelColor()))));
            }
            if (!tooltipMode.equals("compact") && ClientConfig.SHOW_PROGRESS_BAR.get() && requiredXp > 0) {
                levelDetails.add(progressBar(currentXp, requiredXp));
            }
            if (tooltipMode.equals("detailed") && requiredXp > 0) {
                long percent = Math.min(100, Math.round(currentXp * 100.0 / requiredXp));
                levelDetails.add(Component.literal("  ").append(Component.translatable("tooltip.weaponleveling.xp_percent", percent))
                        .withStyle(style -> style.withColor(ClientConfig.detailColor())));
            }
        }
        if (ClientConfig.CUSTOM_TOOLTIP_PANEL.get() && tooltipMode.equals("detailed") && requiredXp > 0) {
            long percent = Math.min(100, Math.round(currentXp * 100.0 / requiredXp));
            levelDetails.add(Component.literal("  ").append(Component.translatable(
                    "tooltip.weaponleveling.xp_percent", percent))
                    .withStyle(style -> style.withColor(ClientConfig.detailColor())));
        }
        if (!tooltipMode.equals("compact") && isWeapon && ClientConfig.SHOW_RANGED_DAMAGE_MULTIPLIER.get()) {
            if (WeaponEligibility.isRanged(stack) && WeaponDamage.baseAttackDamage(stack) <= 0) {
                addRangedDamagePreview(levelDetails, stack, level, serverSettings, broken);
            } else if (!ClientConfig.UPDATE_VANILLA_ATTACK_DAMAGE_LINE.get()) {
                addMainHandDamagePreview(levelDetails, stack, level, serverSettings, broken);
            }
        }

        boolean expanded = tooltipMode.equals("detailed") || (!tooltipMode.equals("compact")
                && ClientConfig.EXPANDED_TOOLTIP_ENABLED.get()
                && (!ClientConfig.HOLD_KEY_TO_EXPAND.get() || ClientKeyMappings.isTooltipExpandHeld()));
        if (expanded) {
            if (progress != null && ClientConfig.SHOW_COMBAT_STATISTICS.get()) {
                String detailPrefix = ClientConfig.CUSTOM_TOOLTIP_PANEL.get() ? "  " : "  ▸ ";
                levelDetails.add(Component.literal(detailPrefix).append(Component.translatable("tooltip.weaponleveling.damage",
                                formatTotalDamage(progress.totalDamageDealt())))
                        .withStyle(style -> style.withColor(ClientConfig.detailColor())));
                if (isArmor) {
                    levelDetails.set(levelDetails.size() - 1, Component.literal(detailPrefix).append(Component.translatable(
                                    "tooltip.weaponleveling.damage_endured", formatTotalDamage(progress.totalDamageDealt())))
                            .withStyle(style -> style.withColor(ClientConfig.detailColor())));
                } else if (isTool) {
                    levelDetails.set(levelDetails.size() - 1, Component.literal(detailPrefix).append(Component.translatable(
                                    "tooltip.weaponleveling.mined", formatTotalDamage(progress.totalDamageDealt())))
                            .withStyle(style -> style.withColor(ClientConfig.detailColor())));
                } else {
                    levelDetails.add(Component.literal(detailPrefix).append(Component.translatable(
                                    "tooltip.weaponleveling.defeated", progress.totalKills()))
                            .withStyle(style -> style.withColor(ClientConfig.detailColor())));
                }
                levelDetails.add(Component.literal(detailPrefix).append(Component.translatable(
                                "tooltip.weaponleveling.total_xp",
                                String.format(java.util.Locale.ROOT, "%,d", progress.totalXp())))
                        .withStyle(style -> style.withColor(ClientConfig.detailColor())));
            }
            if (ClientConfig.SHOW_CHRONICLE.get()) {
                ChronicleTooltip.append(levelDetails, stack, ClientConfig.CUSTOM_TOOLTIP_PANEL.get() ? "  " : "  ▸ ",
                        isArmor && !isWeapon);
            }
        } else if (!ClientConfig.CUSTOM_TOOLTIP_PANEL.get() && tooltipMode.equals("standard")
                && ClientConfig.SHOW_KEY_HINTS.get()) {
            String hintPrefix = "  ";
            levelDetails.add(Component.literal(hintPrefix).append(Component.translatable("tooltip.weaponleveling.expand_hint",
                    ClientKeyMappings.EXPAND_TOOLTIP.getTranslatedKeyMessage()).withStyle(ChatFormatting.DARK_GRAY)));
        }
        if (!ClientConfig.CUSTOM_TOOLTIP_PANEL.get() && !tooltipMode.equals("compact")
                && ClientConfig.CHAT_SHARE_ENABLED.get()) {
            levelDetails.add(Component.literal("  ").append(Component.translatable("tooltip.weaponleveling.share_hint"))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.addAll(Math.min(1, tooltip.size()), levelDetails);
        ItemNameStyle.decorate(tooltip, stack, level, serverSettings.maxLevel());
    }

    private static TooltipComponent levelPanelFor(ItemStack stack) {
        if (!ClientConfig.CUSTOM_TOOLTIP_PANEL.get()) {
            return null;
        }
        var settings = ClientServerSettings.current();
        if (!LevelableItemEligibility.isEligible(stack, settings.autoDetectExternalWeapons())) {
            return null;
        }
        WeaponProgress progress = stack.get(WeaponComponents.PROGRESS.get());
        int level = progress == null ? 0 : progress.level();
        long currentXp = progress == null ? 0 : progress.currentXp();
        long requiredXp = ClientServerSettings.requiredXp(level);
        boolean armor = ArmorEligibility.isEligible(stack);
        LegacyPersona persona = LegacyPersona.resolve(progress, armor,
                !armor && WeaponEligibility.isRanged(stack));
        return new LevelProgressTooltip(stack.copy(), level, settings.maxLevel(), currentXp,
                requiredXp, level >= settings.maxLevel(),
                progress == null ? LegacyTier.NONE : progress.legacyTier(), persona);
    }

    // NOT - IPUCU ARKA PLAN/KENAR RENGI TASINMADI: NeoForge surumu RenderTooltipEvent.Texture olayindan
    // ipucu kutusunun konumunu alip kenari panel uzerinden cizer; Fabric'te bu konumu bildiren bir
    // olay yoktur ve vanilla ipucu kutusu sabit sprite'larla cizilir.
    // Panelin KENDISI (ClientLevelProgressTooltip) kendi renklerini cizmeye devam eder; yalnizca
    // ipucu kutusunun arka plan/kenar rengi vanilla degerinde kalir. Bkz PORT_NOTU.md.

    private static void addPauseSettingsButton(Screen screen) {
        if (!(screen instanceof PauseScreen) || !ClientConfig.SHOW_PAUSE_SETTINGS_BUTTON.get()) {
            return;
        }
        int guiWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int x = guiWidth - 20 - ClientConfig.PAUSE_SETTINGS_BUTTON_RIGHT_OFFSET.get();
        int y = ClientConfig.PAUSE_SETTINGS_BUTTON_TOP_OFFSET.get();
        Screens.getWidgets(screen).add(Button.builder(Component.literal("WL"), ignored -> Minecraft.getInstance()
                        .setScreenAndShow(new WeaponSettingsScreen(screen)))
                .bounds(x, y, 20, 20)
                .tooltip(Tooltip.create(Component.translatable("screen.weaponleveling.settings.button_tooltip"))).build());
    }

    private static void refreshSettingsWhenInventoryOpens(Screen screen) {
        if (screen instanceof InventoryScreen) {
            ModNetwork.sendToServer(new RequestServerSettingsPayload());
        }
    }

    /** true ise tiklama tuketildi ve vanilla dagitimi yapilmamalidir. */
    private static boolean shareHoveredItem(Screen clickedScreen, int button) {
        if (!ClientConfig.CHAT_SHARE_ENABLED.get() || button != InputConstants.MOUSE_BUTTON_MIDDLE
                || !Minecraft.getInstance().hasShiftDown() || !(clickedScreen instanceof AbstractContainerScreen<?> screen)) {
            return false;
        }
        var slot = ((dev.weaponleveling.mixin.AbstractContainerScreenAccessor) screen)
                .weaponleveling$getHoveredSlot();
        if (slot == null || !slot.hasItem()) {
            return false;
        }
        var settings = ClientServerSettings.current();
        if (!LevelableItemEligibility.isEligible(slot.getItem(), settings.autoDetectExternalWeapons())) {
            return false;
        }
        int menuSlot = screen.getMenu().slots.indexOf(slot);
        if (menuSlot < 0) {
            return false;
        }
        ModNetwork.sendToServer(new ShareItemPayload(screen.getMenu().containerId, menuSlot));
        return true;
    }

    private static Component progressBar(long currentXp, long requiredXp) {
        int segments = ClientConfig.PROGRESS_BAR_SEGMENTS.get();
        int filled = (int) Math.min(segments, Math.max(0,
                Math.round((double) currentXp * segments / requiredXp)));
        return Component.literal("  ┃").withStyle(style -> style.withColor(ClientConfig.detailColor()))
                .append(Component.literal("▰".repeat(filled)).withStyle(style -> style.withColor(ClientConfig.progressFilledColor())))
                .append(Component.literal("▱".repeat(segments - filled))
                        .withStyle(style -> style.withColor(ClientConfig.progressEmptyColor())))
                .append(Component.literal("┃").withStyle(style -> style.withColor(ClientConfig.detailColor())));
    }

    private static void updateVanillaAttackDamageLine(List<Component> tooltip, ItemStack stack, int level,
                                                  boolean broken) {
        var settings = ClientServerSettings.current();
        if (broken) {
            replaceAttackDamageLine(tooltip, "0");
            return;
        }
        if (!settings.levelBonusEnabled()) {
            return;
        }
        double baseDamage = WeaponDamage.mainHandDamage(stack);
        if (baseDamage <= 0) {
            return;
        }
        double multiplier = LevelDamageBonus.calculate(level, settings.maxLevel(), settings.levelMaxMultiplier(),
                settings.levelCurveExponent()).multiplier();
        if (multiplier <= 1) {
            return;
        }
        double totalDamage = baseDamage * multiplier;
        double bonus = totalDamage - baseDamage;
        String value = ClientConfig.ATTACK_DAMAGE_DISPLAY_FORMAT.get().equals("total")
                ? format(totalDamage)
                : format(baseDamage) + " + " + format(bonus);
        replaceAttackDamageLine(tooltip, value);
    }

    private static void updateVanillaArmorLine(List<Component> tooltip, ItemStack stack, int level,
                                           boolean broken) {
        var settings = ClientServerSettings.current();
        if (broken) {
            replaceArmorLine(tooltip, "0");
            return;
        }
        if (!settings.armorLevelBonusEnabled()) {
            return;
        }
        double baseArmor = ArmorProtection.baseArmor(stack);
        if (baseArmor <= 0) {
            return;
        }
        double bonus = ArmorLevelBonus.calculate(level, settings.armorFlatBonusPerLevel(),
                settings.armorMaxLevelBonus()).armor();
        if (bonus <= 0) {
            return;
        }
        replaceArmorLine(tooltip, format(baseArmor + bonus));
    }

    private static void replaceAttackDamageLine(List<Component> tooltip, String value) {
        String attackDamageName = Component.translatable(Attributes.ATTACK_DAMAGE.value().getDescriptionId()).getString();
        int index = firstFlatAttributeLine(tooltip, attackDamageName);
        if (index < 0) {
            return;
        }
        foldAttributeLines(tooltip, index, attackDamageName, Component.literal(value + " ").append(
                Component.translatable(Attributes.ATTACK_DAMAGE.value().getDescriptionId()))
                .withStyle(ChatFormatting.DARK_GREEN));
    }

    private static void addMainHandDamagePreview(List<Component> levelDetails, net.minecraft.world.item.ItemStack stack,
                                                  int level, ServerSettingsPayload settings, boolean broken) {
        double baseDamage = WeaponDamage.mainHandDamage(stack);
        if (baseDamage <= 0) {
            return;
        }
        levelDetails.add(Component.translatable("tooltip.weaponleveling.when_in_main_hand")
                .withStyle(ChatFormatting.GRAY));
        levelDetails.add(Component.literal(format(scaledDamage(baseDamage, level, settings, broken)) + " ")
                .append(Component.translatable("tooltip.weaponleveling.attack_damage"))
                .withStyle(ChatFormatting.DARK_GREEN));
    }

    private static void addRangedDamagePreview(List<Component> levelDetails, net.minecraft.world.item.ItemStack stack,
                                                int level, ServerSettingsPayload settings, boolean broken) {
        levelDetails.add(Component.translatable("tooltip.weaponleveling.when_in_main_hand")
                .withStyle(ChatFormatting.GRAY));
        levelDetails.add(Component.literal(format(scaledDamage(WeaponDamage.standardRangedDamage(stack), level, settings, broken)) + " ")
                .append(Component.translatable("tooltip.weaponleveling.ranged_damage"))
                .withStyle(ChatFormatting.DARK_GREEN));
    }

    private static double scaledDamage(double baseDamage, int level, ServerSettingsPayload settings, boolean broken) {
        if (broken) {
            return 0;
        }
        if (!settings.levelBonusEnabled()) {
            return baseDamage;
        }
        return baseDamage * LevelDamageBonus.calculate(level, settings.maxLevel(), settings.levelMaxMultiplier(),
                settings.levelCurveExponent()).multiplier();
    }

    private static void replaceArmorLine(List<Component> tooltip, String value) {
        String armorName = Component.translatable(Attributes.ARMOR.value().getDescriptionId()).getString();
        int index = firstFlatAttributeLine(tooltip, armorName);
        if (index < 0) {
            return;
        }
        foldAttributeLines(tooltip, index, armorName, Component.literal("+" + value + " ").append(
                Component.translatable(Attributes.ARMOR.value().getDescriptionId()))
                .withStyle(ChatFormatting.DARK_GREEN));
    }

    /**
     * True for a tooltip line that shows a flat value of the given attribute, such as "3 Armor"
     * or "+2 Armor". Advanced-tooltip debug blocks ("[..]") are ignored; percentage lines belong
     * to multiplicative modifiers, which the folded total does not include.
     */
    private static boolean isFlatAttributeLine(Component line, String attributeName) {
        String text = line.getString().strip();
        if (text.endsWith("]") && text.contains("[")) {
            text = text.substring(0, text.lastIndexOf('[')).strip();
        }
        return !text.contains("%") && text.endsWith(attributeName);
    }

    private static int firstFlatAttributeLine(List<Component> tooltip, String attributeName) {
        for (int index = 0; index < tooltip.size(); index++) {
            if (isFlatAttributeLine(tooltip.get(index), attributeName)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Writes the summed total over the vanilla attribute line and removes the neighbouring
     * per-modifier lines the total already includes, so a levelled item shows one value:
     * a +3 helmet with a +2 level bonus renders as a single "+5 Armor" line.
     */
    private static void foldAttributeLines(List<Component> tooltip, int index, String attributeName,
                                           Component total) {
        int start = index;
        while (start > 0 && isFlatAttributeLine(tooltip.get(start - 1), attributeName)) {
            start--;
        }
        int end = index;
        while (end + 1 < tooltip.size() && isFlatAttributeLine(tooltip.get(end + 1), attributeName)) {
            end++;
        }
        for (int removed = end; removed > index; removed--) {
            tooltip.remove(removed);
        }
        for (int removed = index - 1; removed >= start; removed--) {
            tooltip.remove(removed);
        }
        tooltip.set(start, total);
    }

    private static String format(double value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setGroupingUsed(false);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(value);
    }

    private static String formatTotalDamage(double value) {
        return BigDecimal.valueOf(Math.max(0, value)).setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
    }

    private static int darken(int argb, double factor) {
        int red = (int) (((argb >> 16) & 0xFF) * factor);
        int green = (int) (((argb >> 8) & 0xFF) * factor);
        int blue = (int) ((argb & 0xFF) * factor);
        return (argb & 0xFF000000) | red << 16 | green << 8 | blue;
    }
}
