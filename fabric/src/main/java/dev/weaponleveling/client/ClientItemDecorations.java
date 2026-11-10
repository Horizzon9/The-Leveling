package dev.weaponleveling.client;

import dev.weaponleveling.config.ClientConfig;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.LevelableItemEligibility;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import dev.weaponleveling.platform.event.ItemDecorationEvents;
import net.minecraft.world.item.ItemStack;

/** Draws small, texture-free progression marks directly over inventory and hotbar item icons. */
public final class ClientItemDecorations {
    private ClientItemDecorations() {
    }

    public static void register() {
        ItemDecorationEvents.register(ClientItemDecorations::render);
    }

    private static void render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        var settings = ClientServerSettings.current();
        if (!LevelableItemEligibility.isEligible(stack, settings.autoDetectExternalWeapons())) {
            return;
        }

        // Eligibility is already confirmed above, so this only needs the broken flag itself.
        if (ClientConfig.SHOW_BROKEN_SLOT_OVERLAY.get() && BrokenWeaponHandler.isMarkedBroken(stack)) {
            drawBrokenOverlay(graphics, x, y);
        }
        IdentityAura.drawSlot(graphics, stack, x, y);
    }

    private static void drawBrokenOverlay(GuiGraphics graphics, int x, int y) {
        int edge = 0xE8E43E4F;
        int crack = 0xFFF36A74;
        graphics.fill(x, y, x + 16, y + 1, edge);
        graphics.fill(x, y + 15, x + 16, y + 16, edge);
        graphics.fill(x, y, x + 1, y + 16, edge);
        graphics.fill(x + 15, y, x + 16, y + 16, edge);

        // Two deliberately irregular one-pixel crack paths; these are geometry, not font symbols or textures.
        graphics.fill(x + 5, y + 1, x + 7, y + 5, crack);
        graphics.fill(x + 6, y + 4, x + 10, y + 6, crack);
        graphics.fill(x + 8, y + 5, x + 10, y + 10, crack);
        graphics.fill(x + 9, y + 9, x + 13, y + 11, crack);
        graphics.fill(x + 3, y + 10, x + 7, y + 12, crack);
        graphics.fill(x + 5, y + 11, x + 7, y + 15, crack);
    }
}
