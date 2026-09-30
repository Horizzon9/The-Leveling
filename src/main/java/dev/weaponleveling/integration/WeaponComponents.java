package dev.weaponleveling.integration;

import com.mojang.serialization.Codec;
import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.core.chronicle.WeaponChronicle;
import dev.weaponleveling.core.model.WeaponProgress;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WeaponComponents {
    public static final DeferredRegister.DataComponents REGISTER = DeferredRegister.createDataComponents(
            Registries.DATA_COMPONENT_TYPE, WeaponLevelingMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponProgress>> PROGRESS =
            REGISTER.registerComponentType("progress", builder -> builder
                    .persistent(WeaponProgressCodecs.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(WeaponProgressCodecs.CODEC)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BROKEN =
            REGISTER.registerComponentType("broken", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponChronicle>> CHRONICLE =
            REGISTER.registerComponentType("chronicle", builder -> builder
                    .persistent(WeaponChronicleCodecs.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(WeaponChronicleCodecs.CODEC)));

    private WeaponComponents() {
    }
}
