package dev.weaponleveling.platform.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * Bir esyanin ipucuna gorsel bilesen ekler - NeoForge'un `RenderTooltipEvent.GatherComponents`
 * olayinin bu kod tabanindaki karsiligi. Fabric API'de karsiligi yoktur; olay
 * {@code GuiGraphicsTooltipMixin} tarafindan vanilla ipucu cizim yolundan dagitilir.
 *
 * NEDEN "resim bileseni" SLOTU KULLANILIYOR: vanilla `renderTooltip(font, stack, x, y)` cagrisi
 * esyanin `getTooltipImage()` sonucunu ipucuna gorsel bilesen olarak gecirir. Seviyelenebilir bir
 * silah/zirhta bu slot HER ZAMAN bostur (yalnizca bohca gibi esyalar doldurur), bu yuzden orayi
 * kendi panelimizle doldurmak hicbir vanilla bileseni gizlemez - NeoForge surumunde panelin
 * listeye eklenmesiyle ayni goruntuyu verir.
 */
public final class TooltipEvents {
    private static final List<PanelProvider> PROVIDERS = new ArrayList<>();

    private TooltipEvents() {
    }

    @FunctionalInterface
    public interface PanelProvider {
        /** @return eklenecek panel, ya da bu esya icin panel yoksa null. */
        TooltipComponent panelFor(ItemStack stack);
    }

    public static void register(PanelProvider provider) {
        PROVIDERS.add(provider);
    }

    /**
     * Mixin tarafindan cagirilir.
     *
     * @param original vanilla'nin kendi gorsel bileseni
     * @return vanilla zaten bir bilesen saglamissa O KORUNUR; aksi halde saglayicilarin urettigi
     *     ilk panel kullanilir.
     */
    public static Optional<TooltipComponent> panel(ItemStack stack, Optional<TooltipComponent> original) {
        if (original.isPresent() || PROVIDERS.isEmpty() || stack.isEmpty()) {
            return original;
        }
        for (PanelProvider provider : PROVIDERS) {
            TooltipComponent panel = provider.panelFor(stack);
            if (panel != null) {
                return Optional.of(panel);
            }
        }
        return original;
    }
}
