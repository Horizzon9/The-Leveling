package dev.weaponleveling.integration;

import dev.weaponleveling.WeaponLevelingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Optional tags for narrowing or extending the built-in player-armor selection. */
public final class ArmorTags {
    public static final TagKey<Item> LEVELABLE_ARMOR = create("levelable_armor");
    public static final TagKey<Item> EXCLUDED_ARMOR = create("excluded_armor");

    private ArmorTags() {
    }

    private static TagKey<Item> create(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, path));
    }
}
