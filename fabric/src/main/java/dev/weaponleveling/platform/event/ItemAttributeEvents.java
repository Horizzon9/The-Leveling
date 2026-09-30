package dev.weaponleveling.platform.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/**
 * Bir esyanin tasidigi oznitelik degistiricilerine ekleme yapar - NeoForge'un
 * `ItemAttributeModifierEvent`'inin bu kod tabanindaki karsiligi. Fabric API'de karsiligi yoktur.
 *
 * DAGITIM NOKTASI: 1.21.1'de `ItemStack.getAttributeModifiers()` DIYE BIR METOT YOKTUR (mapping
 * ile dogrulandi); oznitelikler `ItemStack.forEachModifier(...)` uzerinden toplanir. Bu yuzden
 * {@code ItemStackAttributeMixin} o metodun HER IKI asiri yuklemesine de baglanir:
 * `EquipmentSlot` alani canli varliga oznitelik uygulanirken, `EquipmentSlotGroup` alani ise
 * vanilla ipucu (tooltip) olusturulurken kullanilir - ikisini birden baglamak, NeoForge olayinin
 * her iki yerde de gorunur olmasiyla ayni sonucu verir.
 */
public final class ItemAttributeEvents {
    private static final List<Provider> PROVIDERS = new ArrayList<>();

    private ItemAttributeEvents() {
    }

    /** Eklenecek tek bir degistirici ve gecerli oldugu slot grubu. */
    public record Extra(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slots) {
    }

    @FunctionalInterface
    public interface Provider {
        void provide(ItemStack stack, Consumer<Extra> out);
    }

    public static void register(Provider provider) {
        PROVIDERS.add(provider);
    }

    /** Mixin tarafindan cagirilir. Eklenecek bir sey yoksa bos liste doner (tahsis yok). */
    public static List<Extra> collect(ItemStack stack) {
        if (PROVIDERS.isEmpty() || stack.isEmpty()) {
            return List.of();
        }
        List<Extra> collected = new ArrayList<>(1);
        for (Provider provider : PROVIDERS) {
            provider.provide(stack, collected::add);
        }
        return collected;
    }
}
