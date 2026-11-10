package dev.weaponleveling.content;

import dev.weaponleveling.WeaponLevelingMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Client screen aligned directly to the supplied 256x256 Minecraft GUI atlas. */
public final class LegacyTransferScreen extends AbstractContainerScreen<LegacyTransferMenu> {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(
            WeaponLevelingMod.MOD_ID, "textures/gui/container/legacy_transfer_table.png");
    private static final int TEXTURE_SIZE = 256;

    public LegacyTransferScreen(LegacyTransferMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
        titleLabelX = 44;
        titleLabelY = 15;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        renderSlotGuidance(graphics, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0.0F, 0.0F,
                imageWidth, imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);

        if (hasTransferError()) {
            graphics.text(font, "!", leftPos + 112, topPos + 44, 0xFFFF5555, true);
        }
    }

    private void renderSlotGuidance(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
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
                graphics.setTooltipForNextFrame(font, hint, mouseX, mouseY);
                return;
            }
        }
        if (hasTransferError() && isHovering(104, 38, 25, 22, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font, Component.translatable(
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
