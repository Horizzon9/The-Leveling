package dev.weaponleveling.platform.event;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * Ors sonucu ozellestirmesi - NeoForge'un `AnvilUpdateEvent`'inin bu kod tabanindaki karsiligi.
 * Fabric API'de karsiligi yoktur; olay {@code AnvilMenuMixin} tarafindan vanilla
 * `AnvilMenu.createResult()` icinden dagitilir.
 */
public final class AnvilEvents {
    private static final List<Update> LISTENERS = new ArrayList<>();

    private AnvilEvents() {
    }

    /** Ors ciktisi, deneyim maliyeti ve tuketilecek malzeme adedi. */
    public record Result(ItemStack output, int cost, int materialCost) {
    }

    @FunctionalInterface
    public interface Update {
        /** @return ozel sonuc, ya da vanilla davranisi surecekse null. */
        Result onUpdate(ItemStack left, ItemStack right);
    }

    public static void register(Update listener) {
        LISTENERS.add(listener);
    }

    /** Mixin tarafindan cagirilir. Ilk sonuc ureten dinleyici kazanir. */
    public static Result fire(ItemStack left, ItemStack right) {
        for (Update listener : LISTENERS) {
            Result result = listener.onUpdate(left, right);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
