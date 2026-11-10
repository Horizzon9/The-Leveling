package dev.weaponleveling.loot;

import com.mojang.serialization.MapCodec;
import dev.weaponleveling.WeaponLevelingMod;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTER = DeferredRegister.create(
            NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, WeaponLevelingMod.MOD_ID);

    static {
        REGISTER.register("legacy_transfer_template_chests", () -> LegacyTransferTemplateLootModifier.CODEC);
    }

    private ModLootModifiers() {
    }
}
