package dev.weaponleveling.content;

import dev.weaponleveling.WeaponLevelingMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Client screen aligned directly to the supplied 256x256 Minecraft GUI atlas. */
public final class LegacyTransferScreen extends AbstractContainerScreen<LegacyTransferMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            WeaponLevelingMod.MOD_ID, "textures/gui/container/legacy_transfer_table.png");
    private static final int TEXTURE_SIZE = 256;

    public LegacyTransferScreen(LegacyTransferMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 166;
        titleLabelX = 44;
        titleLabelY = 15;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderSlotGuidance(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight,
                TEXTURE_SIZE, TEXTURE_SIZE);

        if (hasTransferError()) {
            graphics.drawString(font, "!", leftPos + 112, topPos + 44, 0xFFFF5555, true);
        }
    }

    private void renderSlotGuidance(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredSlot != null && hoveredSlot.getItem().isEmpty()) {
            Component hint = switch (hoveredSlot.index) {
                case LegacyTransferMenu.TEMPLATE_SLOT -> Component.translatable(
                        "container.weaponleveling.legacy_transfer.slot.template");
                case LegacyTransferMenu.SOURCE_SLOT -> Component.translatable(
                        "container.weaponleveling.legacy_transfer.slot.source");
                case LegacyTransferMenu.TARGET_SLOT -> Component.translatable(
                        "container.weaponleveling.legacy_transfer.slot.target");
                default -> null;
            };
            if (hint != null) {
                graphics.renderTooltip(font, hint, mouseX, mouseY);
                return;
            }
        }
        if (hasTransferError() && isHovering(104, 38, 25, 22, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable(
                    menu.failureReason().translationKey()), mouseX, mouseY);
        }
    }

    private boolean hasTransferError() {
        return menu.failureReason() != null
                && menu.getSlot(LegacyTransferMenu.TEMPLATE_SLOT).hasItem()
                && menu.getSlot(LegacyTransferMenu.SOURCE_SLOT).hasItem()
                && menu.getSlot(LegacyTransferMenu.TARGET_SLOT).hasItem()
                && !menu.getSlot(LegacyTransferMenu.RESULT_SLOT).hasItem();
    }
}
