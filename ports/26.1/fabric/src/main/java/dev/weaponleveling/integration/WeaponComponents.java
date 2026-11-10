package dev.weaponleveling.integration;

import com.mojang.serialization.Codec;
import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.core.chronicle.WeaponChronicle;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.platform.registry.RegistryEntry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

/**
 * Esya basina kalici ve ag uzerinde eslenen veri bilesenleri.
 *
 * Bilesen kimlikleri ve codec'leri NeoForge surumuyle BIREBIR aynidir - ayni dunyada/ayni sunucuda
 * iki loader arasinda esya verisi tasinabilsin diye. Yalnizca kayit mekanizmasi degisti:
 * NeoForge'un geciktirilmis kaydi yerine Fabric'in dogrudan `Registry.register` cagrisi.
 */
public final class WeaponComponents {
    public static final RegistryEntry<DataComponentType<WeaponProgress>> PROGRESS = RegistryEntry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, "progress"),
            DataComponentType.<WeaponProgress>builder()
                    .persistent(WeaponProgressCodecs.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(WeaponProgressCodecs.CODEC))
                    .build());

    public static final RegistryEntry<DataComponentType<Boolean>> BROKEN = RegistryEntry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, "broken"),
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    public static final RegistryEntry<DataComponentType<WeaponChronicle>> CHRONICLE = RegistryEntry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, "chronicle"),
            DataComponentType.<WeaponChronicle>builder()
                    .persistent(WeaponChronicleCodecs.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(WeaponChronicleCodecs.CODEC))
                    .build());

    private WeaponComponents() {
    }

    /** Sinifin statik kaydinin mod baslatilirken calismasini garanti eder. */
    public static void register() {
    }
}
