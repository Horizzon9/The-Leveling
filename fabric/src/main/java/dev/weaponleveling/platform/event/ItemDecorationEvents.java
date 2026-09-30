package dev.weaponleveling.platform.event;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * Envanter/hotbar ikonlarinin uzerine cizim - NeoForge'un `IItemDecorator` +
 * `RegisterItemDecorationsEvent` ikilisinin bu kod tabanindaki karsiligi. Fabric API'de karsiligi
 * yoktur; cizim {@code GuiGraphicsItemDecorationMixin} tarafindan vanilla
 * `GuiGraphics.renderItemDecorations(...)` sonunda yapilir.
 *
 * NeoForge surumunde dekorator TEK TEK her esya icin kaydediliyordu (`BuiltInRegistries.ITEM
 * .forEach(...)`); burada tek bir kanca tum esyalari kapsar, yani ayni sonucu daha az kurulumla
 * verir ve sonradan eklenen modlarin esyalari da kendiliginden kapsanir.
 */
public final class ItemDecorationEvents {
    private static final List<Decorator> DECORATORS = new ArrayList<>();

    private ItemDecorationEvents() {
    }

    @FunctionalInterface
    public interface Decorator {
        void render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y);
    }

    public static void register(Decorator decorator) {
        DECORATORS.add(decorator);
    }

    /** Mixin tarafindan cagirilir. */
    public static void render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        for (Decorator decorator : DECORATORS) {
            decorator.render(graphics, font, stack, x, y);
        }
    }
}
