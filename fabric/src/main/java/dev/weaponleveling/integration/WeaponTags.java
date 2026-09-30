package dev.weaponleveling.integration;

import dev.weaponleveling.WeaponLevelingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class WeaponTags {
    public static final TagKey<Item> LEVELABLE_WEAPONS = create("levelable_weapons");
    public static final TagKey<Item> LEVELABLE_SWORDS = create("levelable_swords");
    public static final TagKey<Item> LEVELABLE_AXES = create("levelable_axes");
    public static final TagKey<Item> LEVELABLE_RANGED_WEAPONS = create("levelable_ranged_weapons");
    public static final TagKey<Item> EXCLUDED_WEAPONS = create("excluded_weapons");

    private WeaponTags() {
    }

    private static TagKey<Item> create(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, path));
    }
}
