package dev.weaponleveling.mixin;

import dev.weaponleveling.platform.event.TooltipEvents;
import java.util.Optional;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Seviye panelinin ENVANTER/SANDIK gibi container ekranlarinda gorunmesini saglar.
 *
 * NEDEN AYRI BIR MIXIN GEREKTI (bytecode ile dogrulandi): `GuiGraphicsTooltipMixin`, ipucunu
 * `renderTooltip(Font, ItemStack, int, int)` asiri yuklemesi uzerinden yakalar. Ancak
 * `AbstractContainerScreen.renderTooltip(...)` o asiri yuklemeyi HIC cagirmaz; kendi
 * `getTooltipFromContainerItem(...)` sonucuyla dogrudan bes parametreli
 * `renderTooltip(Font, List, Optional, int, int)` surumune gider. Oyuncunun esya ipucunu gordugu
 * asil yer burasi oldugu icin panel yalnizca bu kanca ile gorunur hale gelir.
 *
 * Esya, vanilla'nin ipucunu hangi slot icin cizdigi bilgisinden (`hoveredSlot`) alinir - yani
 * panel her zaman gercekten ustunde durulan esyaya aittir.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenTooltipMixin {
    @Shadow
    protected Slot hoveredSlot;

    @ModifyArg(
        method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;"
                + "Ljava/util/List;Ljava/util/Optional;II)V"
        ),
        index = 2
    )
    private Optional<TooltipComponent> weaponleveling$addLevelPanel(Optional<TooltipComponent> visualComponent) {
        if (hoveredSlot == null) {
            return visualComponent;
        }
        return TooltipEvents.panel(hoveredSlot.getItem(), visualComponent);
    }
}
