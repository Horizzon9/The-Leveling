package dev.weaponleveling.content;

import dev.weaponleveling.WeaponLevelingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** End-gated content used exclusively by the item-XP transfer system. */
public final class ModContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(WeaponLevelingMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WeaponLevelingMod.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
            Registries.MENU, WeaponLevelingMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB, WeaponLevelingMod.MOD_ID);

    public static final DeferredBlock<Block> END_AMBER_ORE = BLOCKS.registerSimpleBlock("end_amber_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(3.5F, 6.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE));
    public static final DeferredBlock<Block> NETHER_AMBER_ORE = BLOCKS.registerSimpleBlock("nether_amber_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.NETHER_ORE));
    public static final DeferredBlock<LegacyTransferTableBlock> LEGACY_TRANSFER_TABLE = BLOCKS.register(
            "legacy_transfer_table", () -> new LegacyTransferTableBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(4.0F, 8.0F)
                            .requiresCorrectToolForDrops().sound(SoundType.WOOD)));

    public static final DeferredItem<BlockItem> END_AMBER_ORE_ITEM = ITEMS.registerSimpleBlockItem(
            END_AMBER_ORE, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> NETHER_AMBER_ORE_ITEM = ITEMS.registerSimpleBlockItem(
            NETHER_AMBER_ORE, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> END_AMBER_INGOT = ITEMS.registerSimpleItem(
            "end_amber_ingot", new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> LEGACY_TRANSFER_TEMPLATE = ITEMS.registerSimpleItem(
            "legacy_transfer_template", new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<BlockItem> LEGACY_TRANSFER_TABLE_ITEM = ITEMS.registerSimpleBlockItem(
            LEGACY_TRANSFER_TABLE, new Item.Properties().rarity(Rarity.RARE));

    public static final DeferredHolder<MenuType<?>, MenuType<LegacyTransferMenu>> LEGACY_TRANSFER_MENU = MENUS.register(
            "legacy_transfer", () -> new MenuType<>(LegacyTransferMenu::new, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THE_LEVELLING_TAB = CREATIVE_MODE_TABS.register(
            "the_levelling", () -> CreativeModeTab.builder()
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
}
