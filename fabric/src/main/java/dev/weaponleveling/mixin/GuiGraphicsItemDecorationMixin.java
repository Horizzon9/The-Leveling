package dev.weaponleveling.mixin;

import dev.weaponleveling.platform.event.ItemDecorationEvents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NeoForge'un `IItemDecorator` yuzeyinin vanilla karsiligi.
 *
 * Bes parametreli asiri yukleme hedeflenir: dort parametreli surum vanilla'da dogrudan buna
 * devreder, yani tek kanca her iki cagri yolunu da kapsar. TAIL secimi, dayaniklilik cubugu ve
 * adet yazisi cizildikten SONRA calismayi garanti eder - NeoForge'da dekoratorler de en son
 * calisirdi, yani katman sirasi korunur.
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsItemDecorationMixin {

    @Inject(
        method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;"
            + "IILjava/lang/String;)V",
        at = @At("TAIL")
    )
    private void weaponleveling$renderItemDecorations(
        Font font,
        ItemStack stack,
        int x,
        int y,
        String text,
        CallbackInfo callback
    ) {
        ItemDecorationEvents.render((GuiGraphics) (Object) this, font, stack, x, y);
    }
}
