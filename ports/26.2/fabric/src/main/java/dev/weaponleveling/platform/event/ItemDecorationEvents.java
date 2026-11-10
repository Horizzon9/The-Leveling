package dev.weaponleveling.platform.event;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.ExtractItemDecorationsCallback;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/**
 * Envanter/hotbar ikonlarinin uzerine cizim - NeoForge'un `IItemDecorator` +
 * `RegisterItemDecorationsEvent` ikilisinin bu kod tabanindaki karsiligi.
 *
 * Fabric API 26.x, vanilla cizim yoluna dogrudan baglanan
 * {@link ExtractItemDecorationsCallback} olayini saglar; 1.21.x surumlerindeki
 * `GuiGraphics.renderItemDecorations` mixin'ine artik gerek yoktur.
 */
public final class ItemDecorationEvents {
    private static final List<Decorator> DECORATORS = new ArrayList<>();
    private static boolean registered;

    private ItemDecorationEvents() {
    }

    @FunctionalInterface
    public interface Decorator {
        void render(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y);
    }

    public static void register(Decorator decorator) {
        if (!registered) {
            ExtractItemDecorationsCallback.EVENT.register((graphics, font, stack, x, y) ->
                    render(graphics, font, stack, x, y));
            registered = true;
        }
        DECORATORS.add(decorator);
    }

    /** Olay tarafindan cagirilir. */
    public static void render(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        for (Decorator decorator : DECORATORS) {
            decorator.render(graphics, font, stack, x, y);
        }
    }
}
