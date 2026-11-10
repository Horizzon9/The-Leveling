package dev.weaponleveling.client;

import dev.weaponleveling.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
// Fabric: HUD cizimi `HudRenderCallback` uzerinden gelir; GuiGraphics dogrudan parametredir.

/** Compact client-only kill-XP toast rendered with Minecraft's own advancement background. */
public final class KillXpOverlay {
    private static final Identifier TOAST_BACKGROUND = Identifier.withDefaultNamespace("toast/advancement");
    private static final long FADE_MS = 180;
    private static final int MIN_WIDTH = 120;
    private static final int TEXT_PADDING = 16;
    private static final int HEIGHT = 24;
    private static ActiveNotice activeNotice;

    private KillXpOverlay() {
    }

    public static void show(long amount, int streak) {
        if (!ClientConfig.KILL_XP_OVERLAY_ENABLED.get() || amount <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        long combinedAmount = activeNotice != null && activeNotice.expiresAt() > now
                ? saturatingAdd(activeNotice.amount(), amount) : amount;
        Component text = streak >= 2
                ? Component.translatable("overlay.weaponleveling.kill_xp_streak", combinedAmount, streak)
                : Component.translatable("overlay.weaponleveling.kill_xp", combinedAmount);
        // Measured once per notice instead of every render frame it stays on screen.
        int textWidth = Minecraft.getInstance().font.width(text);
        activeNotice = new ActiveNotice(combinedAmount, text, textWidth, Math.max(MIN_WIDTH, textWidth + TEXT_PADDING), now,
                now + ClientConfig.KILL_XP_OVERLAY_DURATION_MS.get());
    }

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (activeNotice == null) {
            return;
        }
        long now = System.currentTimeMillis();
        long remaining = activeNotice.expiresAt() - now;
        if (remaining <= 0) {
            activeNotice = null;
            return;
        }
        float alpha = Math.min(1.0F, Math.min((now - activeNotice.startedAt()) / (float) FADE_MS,
                remaining / (float) FADE_MS));
        float lifetimeProgress = 1.0F - remaining / (float) ClientConfig.KILL_XP_OVERLAY_DURATION_MS.get();
        int width = activeNotice.width();
        int left = (minecraft.getWindow().getGuiScaledWidth() - width) / 2;
        int top = minecraft.getWindow().getGuiScaledHeight() - 92
                - Math.round(Math.min(1.0F, lifetimeProgress) * 4.0F);
        int alphaChannel = Math.max(0, Math.min(255, Math.round(alpha * 255)));

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TOAST_BACKGROUND, left, top, width, HEIGHT,
                withAlpha(0xFFFFFFFF, alphaChannel));

        int textX = left + (width - activeNotice.textWidth()) / 2;
        int textY = top + (HEIGHT - minecraft.font.lineHeight) / 2;
        graphics.text(minecraft.font, activeNotice.text(), textX, textY,
                withAlpha(0xFFFFD77A, alphaChannel), true);
    }

    private static long saturatingAdd(long left, long right) {
        if (Long.MAX_VALUE - left < right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private record ActiveNotice(long amount, Component text, int textWidth, int width, long startedAt,
                                long expiresAt) {
    }
}
