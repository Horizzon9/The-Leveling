package dev.weaponleveling.content;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.platform.registry.RegistryEntry;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
    public static final RegistryEntry<Block> END_AMBER_ORE = block("end_amber_ore", Block::new,
            properties -> properties.mapColor(MapColor.SAND).strength(3.5F, 6.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE));
    public static final RegistryEntry<Block> NETHER_AMBER_ORE = block("nether_amber_ore", Block::new,
            properties -> properties.mapColor(MapColor.NETHER).strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.NETHER_ORE));
    public static final RegistryEntry<LegacyTransferTableBlock> LEGACY_TRANSFER_TABLE = block("legacy_transfer_table",
            LegacyTransferTableBlock::new,
            properties -> properties.mapColor(MapColor.COLOR_ORANGE)
                    .strength(4.0F, 8.0F).requiresCorrectToolForDrops().sound(SoundType.WOOD));

    public static final RegistryEntry<BlockItem> END_AMBER_ORE_ITEM = blockItem(END_AMBER_ORE,
            properties -> properties.rarity(Rarity.RARE));
    public static final RegistryEntry<BlockItem> NETHER_AMBER_ORE_ITEM = blockItem(NETHER_AMBER_ORE,
            properties -> properties.rarity(Rarity.RARE));
    public static final RegistryEntry<Item> END_AMBER_INGOT = item("end_amber_ingot", Item::new,
            properties -> properties.rarity(Rarity.RARE));
    public static final RegistryEntry<Item> LEGACY_TRANSFER_TEMPLATE = item("legacy_transfer_template", Item::new,
            properties -> properties.rarity(Rarity.EPIC));
    public static final RegistryEntry<BlockItem> LEGACY_TRANSFER_TABLE_ITEM = blockItem(LEGACY_TRANSFER_TABLE,
            properties -> properties.rarity(Rarity.RARE));

    public static final RegistryEntry<MenuType<LegacyTransferMenu>> LEGACY_TRANSFER_MENU = RegistryEntry.register(
            BuiltInRegistries.MENU, id("legacy_transfer"),
            new MenuType<>(LegacyTransferMenu::new, FeatureFlags.VANILLA_SET));

    // FabricCreativeModeTab.builder(): Fabric'te sekmenin "mod sekmesi" olarak isaretlenmesi ve dogru
    // sayfada gorunmesi icin gereken kurulum budur; vanilla builder'in aksine satir/sutun
    // yerlesimini Fabric yonetir. Baslik, ikon ve icerik NeoForge surumuyle aynidir.
    public static final RegistryEntry<CreativeModeTab> THE_LEVELLING_TAB = RegistryEntry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB, id("the_levelling"),
            FabricCreativeModeTab.builder()
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

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, path);
    }

    private static <T extends Block> RegistryEntry<T> block(String path,
            Function<BlockBehaviour.Properties, T> factory, UnaryOperator<BlockBehaviour.Properties> properties) {
        return RegistryEntry.registerWithKey(BuiltInRegistries.BLOCK, id(path),
                key -> factory.apply(properties.apply(BlockBehaviour.Properties.of()).setId(key)));
    }

    private static <T extends Item> RegistryEntry<T> item(String path,
            Function<Item.Properties, T> factory, UnaryOperator<Item.Properties> properties) {
        return RegistryEntry.registerWithKey(BuiltInRegistries.ITEM, id(path),
                key -> factory.apply(properties.apply(new Item.Properties()).setId(key)));
    }

    private static RegistryEntry<BlockItem> blockItem(RegistryEntry<? extends Block> block,
            UnaryOperator<Item.Properties> properties) {
        return item(block.getId().getPath(), itemProperties -> new BlockItem(block.get(), itemProperties),
                itemProperties -> properties.apply(itemProperties).useBlockDescriptionPrefix());
    }
}
