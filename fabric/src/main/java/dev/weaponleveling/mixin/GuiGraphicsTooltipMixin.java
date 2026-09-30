package dev.weaponleveling.mixin;

import dev.weaponleveling.platform.event.TooltipEvents;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NeoForge'un `RenderTooltipEvent.GatherComponents` olayinin vanilla karsiligi.
 *
 * Vanilla `renderTooltip(Font, ItemStack, int, int)` govdesi tek bir cagri yapar:
 * `renderTooltip(font, Screen.getTooltipFromItem(...), stack.getTooltipImage(), x, y)`.
 * Buradaki 3. argumani (gorsel bilesen) degistiriyoruz - esyanin kendi resim bileseni yoksa
 * panelimiz oraya girer.
 *
 * Esya, ayni metodun basinda yakalanir: `@ModifyArg` yalnizca degistirdigi argumani gorur, panelin
 * HANGI esya icin uretilecegini bilemez. Ipucu cizimi tek is parcaciginda (render thread) ve ic
 * ice gecmeden calistigi icin bu alan guvenlidir; ayrica HEAD her zaman `@ModifyArg`in bagli
 * oldugu cagridan once calisir (program sirasi).
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsTooltipMixin {
    @Unique
    private ItemStack weaponleveling$tooltipStack = ItemStack.EMPTY;

    @Inject(
        method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
        at = @At("HEAD")
    )
    private void weaponleveling$captureTooltipStack(
        Font font,
        ItemStack stack,
        int mouseX,
        int mouseY,
        CallbackInfo callback
    ) {
        weaponleveling$tooltipStack = stack;
    }

    @ModifyArg(
        method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;"
                + "Ljava/util/List;Ljava/util/Optional;II)V"
        ),
        index = 2
    )
    private Optional<TooltipComponent> weaponleveling$addLevelPanel(Optional<TooltipComponent> visualComponent) {
        return TooltipEvents.panel(weaponleveling$tooltipStack, visualComponent);
    }
}
