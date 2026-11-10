package dev.weaponleveling.client;

import dev.weaponleveling.config.ClientConfig;
import dev.weaponleveling.core.chronicle.ChronicleEntry;
import dev.weaponleveling.core.chronicle.ChronicleRecords;
import dev.weaponleveling.core.chronicle.RomanNumerals;
import dev.weaponleveling.core.chronicle.WeaponChronicle;
import dev.weaponleveling.integration.WeaponComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Expanded-tooltip section that tells an item's story from its {@link WeaponChronicle}. */
public final class ChronicleTooltip {
    private static final int SHOWN_FOES = 3;

    // Tooltips are rebuilt every frame while shown; a chronicle only changes in combat, so the
    // lines for the last hovered chronicle are reused until the value or style changes.
    private static WeaponChronicle cachedChronicle;
    private static String cachedPrefix;
    private static int cachedColor;
    private static boolean cachedArmor;
    private static List<Component> cachedLines = List.of();

    private ChronicleTooltip() {
    }

    /**
     * @param armor whether the item is armor: its tallies count hits endured instead of kills
     */
    public static void append(List<Component> tooltip, ItemStack stack, String prefix, boolean armor) {
        WeaponChronicle chronicle = stack.get(WeaponComponents.CHRONICLE.get());
        if (chronicle == null || chronicle.isEmpty()) {
            return;
        }
        int color = ClientConfig.detailColor();
        if (chronicle != cachedChronicle || !prefix.equals(cachedPrefix) || color != cachedColor
                || armor != cachedArmor) {
            cachedLines = build(chronicle, prefix, color, armor);
            cachedChronicle = chronicle;
            cachedPrefix = prefix;
            cachedColor = color;
            cachedArmor = armor;
        }
        tooltip.addAll(cachedLines);
    }

    private static List<Component> build(WeaponChronicle chronicle, String prefix, int color, boolean armor) {
        List<Component> lines = new ArrayList<>();
        MutableComponent header = chronicle.bearer().isEmpty()
                ? Component.translatable("tooltip.weaponleveling.chronicle.title")
                : Component.translatable("tooltip.weaponleveling.chronicle.title_bearer", chronicle.bearer());
        lines.add(Component.literal("  ").append(header.withStyle(ChatFormatting.GOLD)));

        String foeKey = armor ? "tooltip.weaponleveling.chronicle.endured" : "tooltip.weaponleveling.chronicle.foe";
        for (Map.Entry<String, Integer> foe : chronicle.favoredFoes(SHOWN_FOES)) {
            MutableComponent line = Component.translatable(foeKey, foeName(foe.getKey()), foe.getValue());
            int tier = chronicle.recordedMasteryTier(foe.getKey());
            if (tier > 0) {
                line.append(Component.translatable("tooltip.weaponleveling.chronicle.mastery",
                        RomanNumerals.of(tier)).withStyle(ChatFormatting.GOLD));
            }
            lines.add(detail(prefix, line, color));
        }
        ChronicleRecords records = chronicle.records();
        if (records.strongestHit() > 0) {
            lines.add(detail(prefix, Component.translatable("tooltip.weaponleveling.chronicle.strongest_hit",
                    oneDecimal(records.strongestHit())), color));
        }
        if (records.longestShot() > 0) {
            lines.add(detail(prefix, Component.translatable("tooltip.weaponleveling.chronicle.longest_shot",
                    oneDecimal(records.longestShot())), color));
        }
        if (records.longestStreak() >= 2) {
            lines.add(detail(prefix, Component.translatable("tooltip.weaponleveling.chronicle.longest_streak",
                    records.longestStreak()), color));
        }
        chronicle.first(ChronicleEntry.Kind.FIRST_KILL).ifPresent(entry -> lines.add(detail(prefix,
                Component.translatable("tooltip.weaponleveling.chronicle.first_kill",
                        foeName(entry.subject()), entry.day()), color)));
        chronicle.first(ChronicleEntry.Kind.RELIC).ifPresent(entry -> lines.add(detail(prefix,
                Component.translatable("tooltip.weaponleveling.chronicle.relic", entry.day()), color)));
        chronicle.first(ChronicleEntry.Kind.LEGACY).ifPresent(entry -> lines.add(detail(prefix,
                Component.translatable("tooltip.weaponleveling.chronicle.legacy", entry.day()), color)));
        chronicle.first(ChronicleEntry.Kind.TITLED).ifPresent(entry -> lines.add(detail(prefix,
                Component.translatable("tooltip.weaponleveling.chronicle.titled", entry.day()), color)));
        return List.copyOf(lines);
    }

    private static String oneDecimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static Component detail(String prefix, MutableComponent content, int color) {
        return Component.literal(prefix).append(content).withStyle(style -> style.withColor(color));
    }

    private static Component foeName(String entityId) {
        return Optional.ofNullable(ResourceLocation.tryParse(entityId))
                .flatMap(BuiltInRegistries.ENTITY_TYPE::getOptional)
                .map(EntityType::getDescription)
                .orElseGet(() -> Component.literal(entityId));
    }
}
