package dev.weaponleveling.dev;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.weaponleveling.WeaponLevelingMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Opt-in, real-registry verification; excluded from production JARs. */
@EventBusSubscriber(modid = WeaponLevelingMod.MOD_ID)
public final class MendingVerification {
    private MendingVerification() {
    }

    @SubscribeEvent
    public static void verify(ServerStartedEvent event) throws CommandSyntaxException {
        if (!Boolean.getBoolean("weaponleveling.verifyMending")) {
            return;
        }
        var server = event.getServer();
        var level = server.overworld();
        var mending = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.MENDING);
        var player = FakePlayerFactory.getMinecraft(level);
        player.getAbilities().instabuild = false;
        var target = new Zombie(EntityType.ZOMBIE, level);
        target.setNoAi(true);
        target.setPos(0, 100, 0);
        level.addFreshEntity(target);
        int checked = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            // Wolf armor is repairable with scutes but deliberately not enchantable in vanilla.
            if (!stack.isDamageableItem() || stack.is(Items.WOLF_ARMOR)) {
                continue;
            }
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            require(stack.supportsEnchantment(mending), id + " does not support Mending");
            target.setItemSlot(EquipmentSlot.MAINHAND, stack);
            int result = server.getCommands().getDispatcher().execute(
                    "enchant " + target.getUUID() + " minecraft:mending 1",
                    server.createCommandSourceStack().withSuppressedOutput());
            require(result == 1 && EnchantmentHelper.getItemEnchantmentLevel(mending, stack) == 1,
                    id + " failed /enchant");
            int repeated = server.getCommands().getDispatcher().execute(
                    "enchant " + target.getUUID() + " minecraft:mending 1",
                    server.createCommandSourceStack().withSuppressedOutput());
            require(repeated == 1 && EnchantmentHelper.getItemEnchantmentLevel(mending, stack) == 1,
                    id + " failed harmless Mending reapplication");

            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            var bookEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            bookEnchantments.set(mending, 1);
            book.set(DataComponents.STORED_ENCHANTMENTS, bookEnchantments.toImmutable());
            var anvil = new AnvilMenu(1, player.getInventory());
            anvil.getSlot(0).set(new ItemStack(item));
            anvil.getSlot(1).set(book);
            anvil.createResult();
            ItemStack output = anvil.getSlot(2).getItem();
            require(!output.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(mending, output) == 1,
                    id + " failed survival anvil + Mending book");
            System.out.println("MENDING_ITEM_OK: " + id + " /enchant + reapplication + survival anvil");
            checked++;
        }
        // Reproduce a missing/incomplete supported-items definition without modifying any world datapacks.
        Enchantment incompleteMending = new Enchantment(mending.value().description(),
                Enchantment.definition(HolderSet.direct(), 2, 1, Enchantment.constantCost(25),
                        Enchantment.constantCost(75), 4, EquipmentSlotGroup.ANY),
                mending.value().exclusiveSet(), mending.value().effects());
        for (Item item : new Item[]{Items.DIAMOND_HELMET, Items.ELYTRA, Items.DIAMOND_SWORD, Items.SHIELD}) {
            ItemStack stack = new ItemStack(item);
            require(incompleteMending.isSupportedItem(stack) && incompleteMending.canEnchant(stack),
                    "Incomplete Mending support fallback failed for " + item);
        }
        require(!incompleteMending.isSupportedItem(new ItemStack(Items.STONE)),
                "Mending fallback must not enchant non-damageable items");
        require(!incompleteMending.isSupportedItem(new ItemStack(Items.WOLF_ARMOR)),
                "Vanilla wolf armor restriction must remain intact");
        var protection = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.PROTECTION);
        require(!new ItemStack(Items.DIAMOND_SWORD).supportsEnchantment(protection),
                "Other enchantment applicability must remain unchanged");
        var infinity = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.INFINITY);
        ItemStack infinityBow = new ItemStack(Items.BOW);
        infinityBow.enchant(infinity, 1);
        target.setItemSlot(EquipmentSlot.MAINHAND, infinityBow);
        try {
            server.getCommands().getDispatcher().execute(
                    "enchant " + target.getUUID() + " minecraft:mending 1",
                    server.createCommandSourceStack().withSuppressedOutput());
            throw new IllegalStateException("Mending must still conflict with Infinity");
        } catch (CommandSyntaxException expected) {
            require(EnchantmentHelper.getItemEnchantmentLevel(mending, infinityBow) == 0,
                    "Mending must not be added to an Infinity bow");
        }
        target.discard();
        System.out.println("MENDING_VERIFICATION_OK: " + checked
                + " damageable item types passed /enchant, reapplication and survival anvil;"
                + " incomplete support fallback and vanilla restrictions passed");
        server.halt(false);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("MENDING_VERIFICATION_FAILED: " + message);
        }
    }
}
