package dev.weaponleveling.platform.event;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * "Kullanilan esya birakildi" kancasi - NeoForge'un `ArrowLooseEvent` ve
 * `LivingEntityUseItemEvent.Stop` olaylarinin ORTAK karsiligi.
 *
 * NEDEN TEK KANCA IKISININ YERINE GECIYOR: vanilla'da `BowItem.releaseUsing(...)` zaten
 * `LivingEntity.releaseUsingItem()` icinden cagirilir (yay da, trident de ayni yoldan gecer).
 * Dolayisiyla `releaseUsingItem`in BASINA baglanmak her iki olayin da kapsadigi ani yakalar - ve
 * kritik olarak mermi HENUZ OLUSTURULMADAN once calisir, ki mermi dogdugunda bekleyen firlatma
 * kaydi hazir olsun.
 */
public final class ItemUseEvents {
    private static final List<ReleaseUsing> LISTENERS = new ArrayList<>();

    private ItemUseEvents() {
    }

    @FunctionalInterface
    public interface ReleaseUsing {
        void onRelease(LivingEntity entity, ItemStack stack);
    }

    public static void registerReleaseUsing(ReleaseUsing listener) {
        LISTENERS.add(listener);
    }

    /** Mixin tarafindan cagirilir. */
    public static void fireReleaseUsing(LivingEntity entity, ItemStack stack) {
        for (ReleaseUsing listener : LISTENERS) {
            listener.onRelease(entity, stack);
        }
    }
}
