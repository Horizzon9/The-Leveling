package dev.weaponleveling.platform.registry;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

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
    private final Identifier id;
    private final T value;

    private RegistryEntry(Identifier id, T value) {
        this.id = id;
        this.value = value;
    }

    public static <R, T extends R> RegistryEntry<T> register(Registry<R> registry, Identifier id, T value) {
        Registry.register(registry, id, value);
        return new RegistryEntry<>(id, value);
    }

    /**
     * Nesneyi kayit anahtari bilindikten SONRA olusturur: Item ve Block ozellikleri
     * (`setId`) yapicidan once kimlik ister, bu yuzden hazir nesne yerine fabrika alinir.
     */
    public static <R, T extends R> RegistryEntry<T> registerWithKey(Registry<R> registry, Identifier id,
                                                                    Function<ResourceKey<R>, T> factory) {
        return register(registry, id, factory.apply(ResourceKey.create(registry.key(), id)));
    }

    @Override
    public T get() {
        return value;
    }

    public Identifier getId() {
        return id;
    }
}
