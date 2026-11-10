package dev.weaponleveling.content;

import dev.weaponleveling.WeaponLevelingMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * End Amber cevherinin End biyomlarina eklenmesi.
 *
 * NeoForge tarafinda bu is `data/weaponleveling/neoforge/biome_modifier/end_amber_ore.json`
 * dosyasiyla yapiliyordu (`neoforge:add_features`). Fabric'te biyom degisikligi veri dosyasiyla
 * degil kodla tanimlanir; bu sinif AYNI iki yerlestirilmis ozelligi, AYNI biyom secimiyle
 * (`#minecraft:is_end`) ve AYNI uretim adiminda (underground ores) ekler. Yerlestirme/ozellik
 * JSON'lari degismedi - yalnizca "hangi biyomlara" bilgisi kodda.
 */
public final class ModWorldgen {
    private static final ResourceKey<PlacedFeature> END_AMBER_ORE = placedFeature("end_amber_ore");
    private static final ResourceKey<PlacedFeature> END_AMBER_ORE_SURFACE = placedFeature("end_amber_ore_surface");
    private static final ResourceKey<PlacedFeature> NETHER_AMBER_ORE = placedFeature("nether_amber_ore");

    private ModWorldgen() {
    }

    public static void register() {
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_END),
                GenerationStep.Decoration.UNDERGROUND_ORES, END_AMBER_ORE);
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_END),
                GenerationStep.Decoration.UNDERGROUND_ORES, END_AMBER_ORE_SURFACE);
        BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(),
                GenerationStep.Decoration.UNDERGROUND_ORES, NETHER_AMBER_ORE);
    }

    private static ResourceKey<PlacedFeature> placedFeature(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE,
                Identifier.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, path));
    }
}
