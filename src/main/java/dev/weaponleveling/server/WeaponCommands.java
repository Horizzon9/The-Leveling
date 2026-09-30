package dev.weaponleveling.server;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.core.xp.ProgressionEngine;
import dev.weaponleveling.integration.WeaponComponents;
import dev.weaponleveling.integration.LevelableItemEligibility;
import dev.weaponleveling.integration.WeaponDamage;
import dev.weaponleveling.integration.WeaponEligibility;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = WeaponLevelingMod.MOD_ID)
public final class WeaponCommands {
    private WeaponCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        PlayerCommands.register(event.getDispatcher());
        event.getDispatcher().register(Commands.literal("weaponlevel")
                .requires(source -> source.hasPermission(ServerConfig.COMMAND_PERMISSION_LEVEL.get()))
                .then(Commands.literal("get")
                        .executes(context -> get(context, sourcePlayer(context)))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> get(context, EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("set")
                        .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                .executes(context -> setLevel(context, sourcePlayer(context),
                                        IntegerArgumentType.getInteger(context, "level"))))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                        .executes(context -> setLevel(context,
                                                EntityArgument.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "level"))))))
                .then(Commands.literal("addlevel")
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(context -> addLevel(context, sourcePlayer(context),
                                        IntegerArgumentType.getInteger(context, "amount"))))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(context -> addLevel(context,
                                                EntityArgument.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "amount"))))))
                .then(Commands.literal("setxp")
                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                .executes(context -> setXp(context, sourcePlayer(context),
                                        LongArgumentType.getLong(context, "amount"))))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                        .executes(context -> setXp(context,
                                                EntityArgument.getPlayer(context, "player"),
                                                LongArgumentType.getLong(context, "amount"))))))
                .then(Commands.literal("addxp")
                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                .executes(context -> addXp(context, sourcePlayer(context),
                                        LongArgumentType.getLong(context, "amount"))))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                        .executes(context -> addXp(context,
                                                EntityArgument.getPlayer(context, "player"),
                                                LongArgumentType.getLong(context, "amount"))))))
                .then(Commands.literal("reset")
                        .then(Commands.literal("confirm")
                                .executes(context -> reset(context, sourcePlayer(context))))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("confirm")
                                        .executes(context -> reset(context,
                                                EntityArgument.getPlayer(context, "player"))))))
                .then(Commands.literal("reload").executes(WeaponCommands::reload)));
    }

    private static int get(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        WeaponProgress progress = requireProgress(context, player);
        if (progress == null) {
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.weaponleveling.get",
                player.getDisplayName(), progress.level(), progress.currentXp(), progress.totalXp()), false);
        return 1;
    }

    private static int setLevel(CommandContext<CommandSourceStack> context, ServerPlayer player, int requested) {
        if (!ServerConfig.ALLOW_LEVEL_COMMANDS.get()) {
            return disabled(context);
        }
        WeaponProgress progress = requireProgress(context, player);
        if (progress == null) {
            return 0;
        }
        if (!isValidLevel(context, requested) || !mayReduceLevel(context, progress.level(), requested)) {
            return 0;
        }
        update(player, ProgressionEngine.setLevel(progress, requested, ServerConfig.xpCurve(),
                ServerConfig.legacyPolicy()));
        return success(context);
    }

    private static int addLevel(CommandContext<CommandSourceStack> context, ServerPlayer player, int amount) {
        if (!ServerConfig.ALLOW_LEVEL_COMMANDS.get()) {
            return disabled(context);
        }
        WeaponProgress progress = requireProgress(context, player);
        if (progress == null) {
            return 0;
        }
        long requestedLong = (long) progress.level() + amount;
        if (requestedLong < 0 || requestedLong > Integer.MAX_VALUE) {
            context.getSource().sendFailure(Component.translatable("command.weaponleveling.level_out_of_range",
                    0, ServerConfig.MAX_LEVEL.get()));
            return 0;
        }
        int requested = (int) requestedLong;
        if (!isValidLevel(context, requested) || !mayReduceLevel(context, progress.level(), requested)) {
            return 0;
        }
        update(player, ProgressionEngine.setLevel(progress, requested, ServerConfig.xpCurve(),
                ServerConfig.legacyPolicy()));
        return success(context);
    }

    private static int setXp(CommandContext<CommandSourceStack> context, ServerPlayer player, long amount) {
        if (!ServerConfig.ALLOW_XP_COMMANDS.get()) {
            return disabled(context);
        }
        if (amount > ServerConfig.MAX_COMMAND_XP.get()) {
            context.getSource().sendFailure(Component.translatable("command.weaponleveling.xp_out_of_range",
                    ServerConfig.MAX_COMMAND_XP.get()));
            return 0;
        }
        WeaponProgress progress = requireProgress(context, player);
        if (progress == null) {
            return 0;
        }
        WeaponProgress updated = ProgressionEngine.setTotalXp(progress, amount, ServerConfig.xpCurve(),
                ServerConfig.legacyPolicy());
        if (!mayReduceLevel(context, progress.level(), updated.level())) {
            return 0;
        }
        if (!mayReduceXp(context, progress.totalXp(), amount)) {
            return 0;
        }
        update(player, updated);
        return success(context);
    }

    private static int addXp(CommandContext<CommandSourceStack> context, ServerPlayer player, long amount) {
        if (!ServerConfig.ALLOW_XP_COMMANDS.get()) {
            return disabled(context);
        }
        if (amount > ServerConfig.MAX_COMMAND_XP.get()) {
            context.getSource().sendFailure(Component.translatable("command.weaponleveling.xp_out_of_range",
                    ServerConfig.MAX_COMMAND_XP.get()));
            return 0;
        }
        WeaponProgress progress = requireProgress(context, player);
        if (progress == null) {
            return 0;
        }
        update(player, ProgressionEngine.addXp(progress, amount, ServerConfig.xpCurve(),
                ServerConfig.legacyPolicy()).progress());
        return success(context);
    }

    private static int reset(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        WeaponProgress progress = requireProgress(context, player);
        if (progress == null) {
            return 0;
        }
        ChronicleService.clear(player.getMainHandItem());
        WeaponLeaderboard.forget(player.server, progress.instanceId());
        update(player, progress.reset());
        return success(context);
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        var server = context.getSource().getServer();
        server.reloadResources(server.getPackRepository().getSelectedIds());
        context.getSource().sendSuccess(() -> Component.translatable("command.weaponleveling.reload_started"), true);
        return 1;
    }

    private static WeaponProgress requireProgress(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        ItemStack weapon = player.getMainHandItem();
        if (!LevelableItemEligibility.isEligibleOnServer(weapon)) {
            context.getSource().sendFailure(Component.translatable("command.weaponleveling.invalid_weapon",
                    player.getDisplayName()));
            return null;
        }
        WeaponProgress progress = weapon.get(WeaponComponents.PROGRESS.get());
        if (progress == null) {
            double baseAttackDamage = WeaponEligibility.isEligibleOnServer(weapon) ? WeaponDamage.baseAttackDamage(weapon) : 0;
            progress = WeaponProgress.create(baseAttackDamage);
            weapon.set(WeaponComponents.PROGRESS.get(), progress);
        }
        return progress;
    }

    private static void update(ServerPlayer player, WeaponProgress progress) {
        player.getMainHandItem().set(WeaponComponents.PROGRESS.get(), progress);
        player.inventoryMenu.broadcastChanges();
    }

    private static int success(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.translatable("command.weaponleveling.success"), true);
        return 1;
    }

    private static boolean isValidLevel(CommandContext<CommandSourceStack> context, int level) {
        if (level >= 0 && level <= ServerConfig.MAX_LEVEL.get()) {
            return true;
        }
        context.getSource().sendFailure(Component.translatable("command.weaponleveling.level_out_of_range",
                0, ServerConfig.MAX_LEVEL.get()));
        return false;
    }

    private static boolean mayReduceLevel(CommandContext<CommandSourceStack> context, int current, int requested) {
        if (requested >= current || ServerConfig.ALLOW_LEVEL_REDUCTION.get()) {
            return true;
        }
        context.getSource().sendFailure(Component.translatable("command.weaponleveling.level_reduction_disabled"));
        return false;
    }

    private static boolean mayReduceXp(CommandContext<CommandSourceStack> context, long current, long requested) {
        if (requested >= current || ServerConfig.ALLOW_XP_REDUCTION.get()) {
            return true;
        }
        context.getSource().sendFailure(Component.translatable("command.weaponleveling.xp_reduction_disabled"));
        return false;
    }

    private static ServerPlayer sourcePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return context.getSource().getPlayerOrException();
    }

    private static int disabled(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.translatable("command.weaponleveling.command_disabled"));
        return 0;
    }
}
