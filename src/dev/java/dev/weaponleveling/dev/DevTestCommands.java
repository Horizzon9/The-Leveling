package dev.weaponleveling.dev;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.core.xp.ProgressionEngine;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.LevelableItemEligibility;
import dev.weaponleveling.integration.WeaponComponents;
import dev.weaponleveling.integration.WeaponDamage;
import dev.weaponleveling.integration.WeaponEligibility;
import dev.weaponleveling.network.LevelUpSealPayload;
import dev.weaponleveling.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Developer-only test helpers. This class is deliberately compiled from
 * src/dev/java and is absent from the normal production JAR.
 */
@EventBusSubscriber(modid = WeaponLevelingMod.MOD_ID)
public final class DevTestCommands {
    private DevTestCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("weaponleveldev")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("help").executes(DevTestCommands::help))
                .then(Commands.literal("status").executes(context -> status(context, player(context))))
                .then(Commands.literal("kit").executes(context -> kit(context, player(context))))
                .then(Commands.literal("damage")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> damage(context, player(context),
                                        IntegerArgumentType.getInteger(context, "amount")))))
                .then(Commands.literal("break").executes(context -> forceBreak(context, player(context))))
                .then(Commands.literal("breakarmor").executes(context -> breakArmor(context, player(context))))
                .then(Commands.literal("setlevel")
                        .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                .executes(context -> setLevel(context, player(context),
                                        IntegerArgumentType.getInteger(context, "level")))))
                .then(Commands.literal("setxp")
                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                .executes(context -> setXp(context, player(context),
                                        LongArgumentType.getLong(context, "amount")))))
                .then(Commands.literal("addxp")
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                .executes(context -> addXp(context, player(context),
                                        LongArgumentType.getLong(context, "amount")))))
                .then(Commands.literal("seal").executes(context -> seal(context, player(context)))));
    }

    private static int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("[The Leveling DEV] /weaponleveldev <status|kit|damage|break|breakarmor|setlevel|setxp|addxp|seal>"), false);
        context.getSource().sendSuccess(() -> Component.literal("damage/break uses vanilla hurtAndBreak, so it tests the real broken-item preservation path."), false);
        return 1;
    }

    private static int kit(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        List<Item> testItems = List.of(
                Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.MACE, Items.BOW, Items.CROSSBOW, Items.TRIDENT,
                Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
        for (Item item : testItems) {
            give(player, new ItemStack(item));
        }
        give(player, new ItemStack(Items.ARROW, 64));
        context.getSource().sendSuccess(() -> Component.literal("[The Leveling DEV] Test kit added: melee, ranged, trident, mace, armor, and arrows."), true);
        return 1;
    }

    private static int damage(CommandContext<CommandSourceStack> context, ServerPlayer player, int amount) {
        ItemStack stack = requireDamageableLevelable(context, player);
        if (stack == null) {
            return 0;
        }
        stack.hurtAndBreak(amount, player.serverLevel(), player, ignored -> { });
        player.inventoryMenu.broadcastChanges();
        context.getSource().sendSuccess(() -> Component.literal("[The Leveling DEV] Applied " + amount + " durability damage through vanilla hurtAndBreak."), true);
        return status(context, player);
    }

    private static int forceBreak(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        ItemStack stack = requireDamageableLevelable(context, player);
        if (stack == null) {
            return 0;
        }
        int remaining = Math.max(1, stack.getMaxDamage() - stack.getDamageValue());
        int enoughToDefeatUnbreaking = (int) Math.min(Integer.MAX_VALUE, (long) remaining * 4L + 64L);
        stack.hurtAndBreak(enoughToDefeatUnbreaking, player.serverLevel(), player, ignored -> { });
        player.inventoryMenu.broadcastChanges();
        context.getSource().sendSuccess(() -> Component.literal("[The Leveling DEV] Forced the held item through its real break path."), true);
        return status(context, player);
    }

    private static int breakArmor(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        int brokenPieces = 0;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!LevelableItemEligibility.isEligibleOnServer(armor) || !armor.isDamageableItem()) {
                continue;
            }
            int remaining = Math.max(1, armor.getMaxDamage() - armor.getDamageValue());
            int enoughToDefeatUnbreaking = (int) Math.min(Integer.MAX_VALUE, (long) remaining * 4L + 64L);
            armor.hurtAndBreak(enoughToDefeatUnbreaking, player.serverLevel(), player, ignored -> { });
            brokenPieces++;
        }
        player.inventoryMenu.broadcastChanges();
        int finalBrokenPieces = brokenPieces;
        context.getSource().sendSuccess(() -> Component.literal("[The Leveling DEV] Sent " + finalBrokenPieces + " equipped armor piece(s) through the real break path."), true);
        return brokenPieces > 0 ? 1 : 0;
    }

    private static int setLevel(CommandContext<CommandSourceStack> context, ServerPlayer player, int level) {
        if (level > ServerConfig.MAX_LEVEL.get()) {
            context.getSource().sendFailure(Component.literal("[The Leveling DEV] Level must be between 0 and " + ServerConfig.MAX_LEVEL.get() + "."));
            return 0;
        }
        ItemStack stack = requireLevelable(context, player);
        if (stack == null) {
            return 0;
        }
        stack.set(WeaponComponents.PROGRESS.get(), ProgressionEngine.setLevel(progress(stack), level,
                ServerConfig.xpCurve(), ServerConfig.legacyPolicy()));
        player.inventoryMenu.broadcastChanges();
        return status(context, player);
    }

    private static int setXp(CommandContext<CommandSourceStack> context, ServerPlayer player, long totalXp) {
        long maximum = ServerConfig.xpCurve().totalXpForLevel(ServerConfig.MAX_LEVEL.get());
        if (totalXp > maximum) {
            context.getSource().sendFailure(Component.literal("[The Leveling DEV] Total XP must be at most " + maximum + "."));
            return 0;
        }
        ItemStack stack = requireLevelable(context, player);
        if (stack == null) {
            return 0;
        }
        stack.set(WeaponComponents.PROGRESS.get(), ProgressionEngine.setTotalXp(progress(stack), totalXp,
                ServerConfig.xpCurve(), ServerConfig.legacyPolicy()));
        player.inventoryMenu.broadcastChanges();
        return status(context, player);
    }

    private static int addXp(CommandContext<CommandSourceStack> context, ServerPlayer player, long amount) {
        ItemStack stack = requireLevelable(context, player);
        if (stack == null) {
            return 0;
        }
        WeaponProgress before = progress(stack);
        var result = ProgressionEngine.addXp(before, amount, ServerConfig.xpCurve(), ServerConfig.legacyPolicy());
        stack.set(WeaponComponents.PROGRESS.get(), result.progress());
        player.inventoryMenu.broadcastChanges();
        context.getSource().sendSuccess(() -> Component.literal("[The Leveling DEV] Added " + amount + " XP; gained " + result.levelsGained() + " level(s)."), true);
        return status(context, player);
    }

    /** Sends the custom HUD notification directly, independent of the configured notification mode. */
    private static int seal(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        ItemStack stack = requireLevelable(context, player);
        if (stack == null) {
            return 0;
        }
        PacketDistributor.sendToPlayer(player, new LevelUpSealPayload(
                BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getHoverName().getString(),
                progress(stack).level(), progress(stack).legacyTier(), false, true));
        context.getSource().sendSuccess(() -> Component.literal("[The Leveling DEV] Sent the HUD seal directly (config mode bypassed for testing)."), false);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            context.getSource().sendFailure(Component.literal("[The Leveling DEV] Hold an item in your main hand."));
            return 0;
        }
        boolean levelable = LevelableItemEligibility.isEligibleOnServer(stack);
        boolean broken = BrokenWeaponHandler.isBroken(stack);
        String durability = stack.isDamageableItem()
                ? Math.max(0, stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage()
                : "not damageable";
        String progressText = "not levelable";
        if (levelable) {
            WeaponProgress progress = progress(stack);
            progressText = "L" + progress.level() + ", XP " + progress.currentXp() + ", total " + progress.totalXp();
        }
        String finalProgressText = progressText;
        context.getSource().sendSuccess(() -> Component.literal("[The Leveling DEV] "
                + BuiltInRegistries.ITEM.getKey(stack.getItem())
                + " | count=" + stack.getCount()
                + " | durability=" + durability
                + " | levelable=" + levelable
                + " | broken=" + broken
                + " | " + finalProgressText), false);
        return levelable ? 1 : 0;
    }

    private static ItemStack requireDamageableLevelable(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        ItemStack stack = requireLevelable(context, player);
        if (stack == null) {
            return null;
        }
        if (!stack.isDamageableItem()) {
            context.getSource().sendFailure(Component.literal("[The Leveling DEV] The held levelable item has no durability."));
            return null;
        }
        return stack;
    }

    private static ItemStack requireLevelable(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!LevelableItemEligibility.isEligibleOnServer(stack)) {
            context.getSource().sendFailure(Component.literal("[The Leveling DEV] Hold a levelable weapon or armor item in your main hand."));
            return null;
        }
        return stack;
    }

    private static WeaponProgress progress(ItemStack stack) {
        WeaponProgress progress = stack.get(WeaponComponents.PROGRESS.get());
        if (progress != null) {
            return progress;
        }
        double baseDamage = WeaponEligibility.isEligibleOnServer(stack) ? WeaponDamage.baseAttackDamage(stack) : 0;
        WeaponProgress created = WeaponProgress.create(baseDamage);
        stack.set(WeaponComponents.PROGRESS.get(), created);
        return created;
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static ServerPlayer player(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return context.getSource().getPlayerOrException();
    }
}
