package dev.weaponleveling.content;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.platform.registry.RegistryEntry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** End-gated content used exclusively by the item-XP transfer system. */
public final class ModContent {
    public static final RegistryEntry<Block> END_AMBER_ORE = block("end_amber_ore",
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(3.5F, 6.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryEntry<Block> NETHER_AMBER_ORE = block("nether_amber_ore",
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.NETHER_ORE)));
    public static final RegistryEntry<LegacyTransferTableBlock> LEGACY_TRANSFER_TABLE = block("legacy_transfer_table",
            new LegacyTransferTableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE)
                    .strength(4.0F, 8.0F).requiresCorrectToolForDrops().sound(SoundType.WOOD)));

    public static final RegistryEntry<BlockItem> END_AMBER_ORE_ITEM = item("end_amber_ore",
            new BlockItem(END_AMBER_ORE.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryEntry<BlockItem> NETHER_AMBER_ORE_ITEM = item("nether_amber_ore",
            new BlockItem(NETHER_AMBER_ORE.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryEntry<Item> END_AMBER_INGOT = item("end_amber_ingot",
            new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryEntry<Item> LEGACY_TRANSFER_TEMPLATE = item("legacy_transfer_template",
            new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryEntry<BlockItem> LEGACY_TRANSFER_TABLE_ITEM = item("legacy_transfer_table",
            new BlockItem(LEGACY_TRANSFER_TABLE.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryEntry<MenuType<LegacyTransferMenu>> LEGACY_TRANSFER_MENU = RegistryEntry.register(
            BuiltInRegistries.MENU, id("legacy_transfer"),
            new MenuType<>(LegacyTransferMenu::new, FeatureFlags.VANILLA_SET));

    // FabricItemGroup.builder(): Fabric'te sekmenin "mod sekmesi" olarak isaretlenmesi ve dogru
    // sayfada gorunmesi icin gereken kurulum budur; vanilla builder'in aksine satir/sutun
    // yerlesimini Fabric yonetir. Baslik, ikon ve icerik NeoForge surumuyle aynidir.
    public static final RegistryEntry<CreativeModeTab> THE_LEVELLING_TAB = RegistryEntry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB, id("the_levelling"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.weaponleveling"))
                    .icon(() -> new ItemStack(LEGACY_TRANSFER_TEMPLATE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(END_AMBER_ORE_ITEM.get());
                        output.accept(NETHER_AMBER_ORE_ITEM.get());
                        output.accept(END_AMBER_INGOT.get());
                        output.accept(LEGACY_TRANSFER_TEMPLATE.get());
                        output.accept(LEGACY_TRANSFER_TABLE_ITEM.get());
                    })
                    .build());

    private ModContent() {
    }

    /** Sinifin statik kaydinin mod baslatilirken calismasini garanti eder. */
    public static void register() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, path);
    }

    private static <T extends Block> RegistryEntry<T> block(String path, T block) {
        return RegistryEntry.register(BuiltInRegistries.BLOCK, id(path), block);
    }

    private static <T extends Item> RegistryEntry<T> item(String path, T item) {
        return RegistryEntry.register(BuiltInRegistries.ITEM, id(path), item);
    }
}
