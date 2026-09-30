package dev.weaponleveling.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.chronicle.WeaponChronicle;
import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.integration.ArmorEligibility;
import dev.weaponleveling.integration.WeaponComponents;
import dev.weaponleveling.integration.WeaponEligibility;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Commands every player may use, unlike the operator-only {@code /weaponlevel}:
 * {@code /leveling title <name>} and {@code /leveling top}.
 */
public final class PlayerCommands {
    private static final int OPERATOR_LEVEL = 2;
    private static final int LEADERBOARD_SIZE = 10;

    private PlayerCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("leveling")
                .then(Commands.literal("title")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(context -> title(context, StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("top").executes(PlayerCommands::top)));
    }

    private static int title(CommandContext<CommandSourceStack> context, String requestedName)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (!ServerConfig.CHRONICLE_ENABLED.get() || !ServerConfig.TITLES_ENABLED.get()) {
            source.sendFailure(Component.translatable("command.weaponleveling.title.disabled"));
            return 0;
        }
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        WeaponProgress progress = stack.get(WeaponComponents.PROGRESS.get());
        boolean levelable = WeaponEligibility.isEligibleOnServer(stack) || ArmorEligibility.isEligible(stack);
        if (!levelable || progress == null || progress.legacyTier() == LegacyTier.NONE) {
            source.sendFailure(Component.translatable("command.weaponleveling.title.not_eligible"));
            return 0;
        }
        String name = WeaponChronicle.sanitizeTitle(requestedName);
        if (name.isEmpty()) {
            source.sendFailure(Component.translatable("command.weaponleveling.title.invalid",
                    WeaponChronicle.MAX_TITLE_LENGTH));
            return 0;
        }
        WeaponChronicle chronicle = ChronicleService.read(stack);
        // A name is part of the item's history; only an operator may rewrite it.
        if (chronicle.hasTitle() && !source.hasPermission(OPERATOR_LEVEL)) {
            source.sendFailure(Component.translatable("command.weaponleveling.title.already_named",
                    chronicle.title()));
            return 0;
        }
        stack.set(WeaponComponents.CHRONICLE.get(), chronicle.withBearer(player.getGameProfile().getName())
                .withTitle(name, ChronicleService.currentDay(player)));
        player.inventoryMenu.broadcastChanges();
        ModAdvancements.grant(player, ModAdvancements.NAMED_ITEM);
        source.sendSuccess(() -> Component.translatable("command.weaponleveling.title.success",
                stack.getHoverName(), name), false);
        return 1;
    }

    private static int top(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<WeaponLeaderboard.Entry> entries = WeaponLeaderboard.get(source.getServer()).top(LEADERBOARD_SIZE);
        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.weaponleveling.top.empty"), false);
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.weaponleveling.top.header"), false);
        for (int index = 0; index < entries.size(); index++) {
            WeaponLeaderboard.Entry entry = entries.get(index);
            int rank = index + 1;
            source.sendSuccess(() -> Component.translatable("command.weaponleveling.top.entry", rank,
                    entry.weaponName(), entry.owner(), entry.level(), entry.kills()), false);
        }
        return entries.size();
    }
}
