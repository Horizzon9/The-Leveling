package dev.weaponleveling.client;

import dev.weaponleveling.config.ClientConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

/** Draws an actual pixel panel and progress meter instead of Unicode bar characters. */
public final class ClientLevelProgressTooltip implements ClientTooltipComponent {
    private static final int MIN_WIDTH = 158;
    private static final int STANDARD_HEIGHT = 46;
    private static final int IDENTITY_HEIGHT = 56;
    private static final int COMPACT_HEIGHT = 36;
    private final LevelProgressTooltip data;
    // Vanilla rebuilds the tooltip component (and calls getWidth/renderImage) every frame the
    // tooltip is on screen, so the translated lines and the measured panel width are computed
    // once per instance instead of on every render call.
    private final Component title;
    private final Component levelText;
    private final Component xpText;
    private final Component identityText;
    private int cachedWidth = -1;
    private boolean framed;
    private int frameTop;
    private int frameStart;
    private int frameEnd;

    public ClientLevelProgressTooltip(LevelProgressTooltip data) {
        this.data = data;
        this.title = ClientConfig.SHOW_LEGACY_IDENTITY.get() && data.legacyTier() != dev.weaponleveling.core.model.LegacyTier.NONE
                ? Component.translatable("tooltip.weaponleveling.panel.identity." + data.legacyTier().serializedName())
                : Component.translatable("tooltip.weaponleveling.panel.title");
        this.levelText = Component.translatable("tooltip.weaponleveling.panel.level", data.level(), data.maxLevel());
        this.xpText = data.maximumLevel()
                ? Component.translatable("tooltip.weaponleveling.panel.maximum")
                : Component.translatable("tooltip.weaponleveling.panel.xp", data.currentXp(), data.requiredXp());
        this.identityText = data.persona().renownRank() > 0
                ? Component.translatable("tooltip.weaponleveling.persona_line",
                Component.translatable("tooltip.weaponleveling.persona." + data.persona().path().serializedName()),
                data.persona().renownRank())
                : Component.empty();
    }

    public LevelProgressTooltip data() {
        return data;
    }

    /** Outlines the whole tooltip box this panel sits in; {@code tooltipTop} is the box's first content row. */
    public void frameTooltip(int tooltipTop, int borderStart, int borderEnd) {
        this.framed = true;
        this.frameTop = tooltipTop;
        this.frameStart = borderStart;
        this.frameEnd = borderEnd;
    }

    @Override
    public int getHeight(Font font) {
        return compact() ? COMPACT_HEIGHT : hasIdentity() ? IDENTITY_HEIGHT : STANDARD_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        return panelWidth(font);
    }

    @Override
    public void extractImage(Font font, int x, int y, int tooltipWidth, int tooltipHeight, GuiGraphicsExtractor graphics) {
        if (framed) {
            drawFrame(graphics, x, tooltipWidth, tooltipHeight);
        }
        int width = panelWidth(font);
        int height = getHeight(font);
        int baseAccent = data.maximumLevel() ? ClientConfig.maxLevelColor() : ClientConfig.levelColor();
        int accent = ClientConfig.identityColor(data.legacyTier(), baseAccent);
        int detail = ClientConfig.detailColor();
        int panel = 0xD80E121A;
        int panelEdge = 0xFF2C3442;

        graphics.fill(x, y, x + width, y + height - 2, panel);
        graphics.fill(x, y, x + 2, y + height - 2, 0xFF000000 | accent);
        graphics.fill(x + 2, y, x + width, y + 1, 0xFF394251);
        graphics.fill(x, y + height - 3, x + width, y + height - 2, panelEdge);

        graphics.fill(x + 5, y + 5, x + 26, y + 26, panelEdge);
        graphics.fill(x + 6, y + 6, x + 25, y + 25, 0xEE080B10);
        graphics.item(data.item(), x + 7, y + 7);

        graphics.text(font, title, x + 31, y + 5, 0xFF000000 | detail, false);
        graphics.text(font, levelText, x + 31, y + 15, 0xFF000000 | accent, false);

        if (!compact()) {
            graphics.text(font, xpText, x + 31, y + 25, 0xFF000000 | detail, false);
            if (hasIdentity()) {
                graphics.text(font, identityText, x + 31, y + 35, 0xFF000000 | accent, false);
            }
        }

        int barX = x + 3;
        int barY = y + height - 10;
        int barWidth = width - 6;
        graphics.fill(barX, barY, barX + barWidth, barY + 6, 0xFF253142);
        graphics.fill(barX + 1, barY + 1, barX + barWidth - 1, barY + 5,
                0xFF000000 | ClientConfig.progressEmptyColor());
        int innerWidth = barWidth - 2;
        int filled = data.maximumLevel() ? innerWidth : progressWidth(innerWidth);
        if (filled > 0) {
            graphics.fill(barX + 1, barY + 1, barX + 1 + filled, barY + 5,
                    0xFF000000 | ClientConfig.progressFilledColor());
            graphics.fill(barX + 1, barY + 1, barX + 1 + filled, barY + 2, 0x80FFFFFF);
        }

    }

    private void drawFrame(GuiGraphicsExtractor graphics, int x, int tooltipWidth, int tooltipHeight) {
        int left = x - 3;
        int right = x + tooltipWidth + 3;
        int top = frameTop - 3;
        int bottom = frameTop + tooltipHeight + 3;
        graphics.fillGradient(left, top + 1, left + 1, bottom - 1, frameStart, frameEnd);
        graphics.fillGradient(right - 1, top + 1, right, bottom - 1, frameStart, frameEnd);
        graphics.fill(left, top, right, top + 1, frameStart);
        graphics.fill(left, bottom - 1, right, bottom, frameEnd);
    }

    private int panelWidth(Font font) {
        if (cachedWidth < 0) {
            int textWidth = compact() ? Math.max(font.width(title), font.width(levelText))
                    : Math.max(font.width(title), Math.max(font.width(levelText),
                    Math.max(font.width(xpText), font.width(identityText))));
            cachedWidth = Math.max(MIN_WIDTH, 34 + textWidth);
        }
        return cachedWidth;
    }

    private int progressWidth(int width) {
        if (data.requiredXp() <= 0) {
            return 0;
        }
        return (int) Math.min(width, Math.max(0,
                Math.round(data.currentXp() * (double) width / data.requiredXp())));
    }

    private static boolean compact() {
        return ClientConfig.TOOLTIP_MODE.get().equals("compact");
    }

    private boolean hasIdentity() {
        return ClientConfig.SHOW_LEGACY_IDENTITY.get() && data.persona().renownRank() > 0;
    }
}
