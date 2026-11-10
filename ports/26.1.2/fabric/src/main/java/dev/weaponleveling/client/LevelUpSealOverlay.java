package dev.weaponleveling.client;

import dev.weaponleveling.config.ClientConfig;
import dev.weaponleveling.core.model.LegacyTier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
// Fabric: HUD cizimi `HudRenderCallback` uzerinden gelir; GuiGraphics dogrudan parametredir.

/** Compact, hotbar-adjacent level-up presentation. It is client-only and never changes progression. */
public final class LevelUpSealOverlay {
    private static final long FADE_IN_MS = 120;
    private static final long FADE_OUT_MS = 220;
    private static final Identifier TOAST_BACKGROUND = Identifier.withDefaultNamespace("toast/advancement");
    private static final int WIDTH = 160;
    private static final int HEIGHT = 32;
    private static final int TEXT_WIDTH = 120;
    private static ActiveSeal activeSeal;

    private LevelUpSealOverlay() {
    }

    public static void show(String itemId, String itemName, int level, LegacyTier legacyTier,
                            boolean identityUnlocked) {
        if (!ClientConfig.LEVEL_UP_OVERLAY_ENABLED.get()) {
            return;
        }
        Identifier resourceLocation = Identifier.tryParse(itemId);
        Item item = resourceLocation == null ? null : BuiltInRegistries.ITEM.getValue(resourceLocation);
        ItemStack icon = item == null ? ItemStack.EMPTY : new ItemStack(item);
        long now = System.currentTimeMillis();
        // Trimmed once here instead of on every render call: the seal stays on screen for a couple
        // of seconds, and neither the name nor the font metrics change while it is displayed.
        activeSeal = new ActiveSeal(icon, trimToWidth(itemName, TEXT_WIDTH), level, legacyTier,
                identityUnlocked, now,
                now + ClientConfig.LEVEL_UP_OVERLAY_DURATION_MS.get());
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (activeSeal == null) {
            return;
        }
        long now = System.currentTimeMillis();
        long remaining = activeSeal.expiresAt() - now;
        if (remaining <= 0) {
            activeSeal = null;
            return;
        }
        float alpha = Math.min(1.0F, Math.min((now - activeSeal.startedAt()) / (float) FADE_IN_MS,
                remaining / (float) FADE_OUT_MS));

        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int left = (width - WIDTH) / 2;
        int top = height - 128;
        int alphaChannel = Math.max(0, Math.min(255, Math.round(alpha * 255)));

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TOAST_BACKGROUND, left, top, WIDTH, HEIGHT,
                withAlpha(0xFFFFFFFF, alphaChannel));
        if (!activeSeal.icon().isEmpty()) {
            graphics.item(activeSeal.icon(), left + 8, top + 8);
        }
        int accent = switch (activeSeal.legacyTier()) {
            case RELIC -> 0xFFFFA94D;
            case LEGACY -> 0xFFFFE08A;
            default -> 0xFFFFD77A;
        };
        net.minecraft.network.chat.Component title = activeSeal.identityUnlocked()
                ? net.minecraft.network.chat.Component.translatable("overlay.weaponleveling.identity_unlocked."
                        + activeSeal.legacyTier().serializedName())
                : net.minecraft.network.chat.Component.translatable("overlay.weaponleveling.level_reached",
                        activeSeal.level());
        graphics.text(Minecraft.getInstance().font, title, left + 30, top + 6,
                withAlpha(accent, alphaChannel), true);
        graphics.text(Minecraft.getInstance().font, activeSeal.itemName(), left + 30, top + 18,
                withAlpha(0xFFF2F2F2, alphaChannel), true);
    }

    private static String trimToWidth(String value, int maxWidth) {
        var font = Minecraft.getInstance().font;
        if (font.width(value) <= maxWidth) {
            return value;
        }
        return font.plainSubstrByWidth(value, maxWidth - font.width("…")) + "…";
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private record ActiveSeal(ItemStack icon, String itemName, int level, LegacyTier legacyTier,
                              boolean identityUnlocked, long startedAt, long expiresAt) {
    }
}
