package dev.weaponleveling.platform.registry;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

/**
 * Kayitli bir nesneye kimligiyle birlikte tutunan referans - NeoForge'un `DeferredHolder`,
 * `DeferredBlock` ve `DeferredItem` turlerinin bu kod tabanindaki karsiligi.
 *
 * NEDEN SARMALAYICI: mod genelinde yuzlerce `ModContent.X.get()` / `WeaponComponents.PROGRESS.get()`
 * cagrisi var. `.get()` sozlesmesini koruyarak o cagri yerlerinin HICBIRI degismedi.
 *
 * NEOFORGE'DAN FARK: NeoForge kaydi GECIKTIRIR (registry olayinda uygular), Fabric ise ANINDA
 * kaydeder. Bu yuzden `register(...)` cagrildigi anda nesne gercekten registry'dedir ve `get()`
 * hicbir zaman "henuz kayitli degil" durumuna dusmez.
 */
public final class RegistryEntry<T> implements Supplier<T> {
    private final ResourceLocation id;
    private final T value;

    private RegistryEntry(ResourceLocation id, T value) {
        this.id = id;
        this.value = value;
    }

    public static <R, T extends R> RegistryEntry<T> register(Registry<R> registry, ResourceLocation id, T value) {
        Registry.register(registry, id, value);
        return new RegistryEntry<>(id, value);
    }

    @Override
    public T get() {
        return value;
    }

    public ResourceLocation getId() {
        return id;
    }
}
