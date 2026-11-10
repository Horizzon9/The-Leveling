package dev.weaponleveling.server;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.combat.ToolMiningBonus;
import dev.weaponleveling.core.combat.ToolXp;
import dev.weaponleveling.core.model.LegacyTier;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.core.xp.ProgressionEngine;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.ToolEligibility;
import dev.weaponleveling.integration.WeaponComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Levelling for pickaxes, shovels and hoes. A block broken with the right tool trains it by the
 * block's hardness, so instantly broken blocks (grass, flowers) give nothing; a levelled tool mines faster.
 */
public final class ToolService {
    private ToolService() {
    }

    /** Called after a player broke a block. */
    public static void onBlockBroken(Player player, BlockGetter level, BlockPos pos, BlockState state) {
        if (!(player instanceof ServerPlayer serverPlayer) || !ServerConfig.TOOL_LEVELING_ENABLED.get()
                || (player.isCreative() && !ServerConfig.ALLOW_CREATIVE_PROGRESS.get())) {
            return;
        }
        ItemStack tool = player.getMainHandItem();
        if (!ToolEligibility.isEligible(tool) || BrokenWeaponHandler.isBroken(tool)
                || !tool.isCorrectToolForDrops(state)) {
            return;
        }
        float hardness = state.getDestroySpeed(level, pos);
        long earnedXp = ToolXp.award(hardness, ServerConfig.TOOL_XP_PER_HARDNESS.get(),
                ServerConfig.TOOL_MAX_XP_PER_BLOCK.get(), player.getRandom().nextDouble());
        if (earnedXp <= 0) {
            return;
        }
        WeaponProgress progress = tool.get(WeaponComponents.PROGRESS.get());
        if (progress == null) {
            progress = WeaponProgress.create(0);
        }
        // The hardness mined is kept in the shared combat-stat field and shown as mining work.
        var result = ProgressionEngine.addXp(progress.withCombatStats(hardness, false), earnedXp,
                ServerConfig.xpCurve(), ServerConfig.legacyPolicy());
        tool.set(WeaponComponents.PROGRESS.get(), result.progress());
        boolean identityUnlocked = result.progress().legacyTier().ordinal() > progress.legacyTier().ordinal();
        ChronicleService.recordArmorHit(tool, serverPlayer, null,
                identityUnlocked ? result.progress().legacyTier() : LegacyTier.NONE);
        if (result.levelsGained() > 0) {
            LevelUpNotifier.notify(serverPlayer, tool, result.progress().level(), result.progress().legacyTier(),
                    identityUnlocked);
        }
        ModAdvancements.onProgress(serverPlayer, result);
        serverPlayer.inventoryMenu.broadcastChanges();
    }

    /** A broken tool cannot break blocks until it is repaired, as a broken weapon cannot attack. */
    public static boolean blocksBreaking(Player player) {
        ItemStack tool = player.getMainHandItem();
        return ToolEligibility.isEligible(tool) && BrokenWeaponHandler.isBroken(tool);
    }

    /** The mining-efficiency modifier a levelled tool carries in the main hand, or null when it adds none. */
    public static AttributeModifier miningBonus(ItemStack tool) {
        if (!ServerConfig.TOOL_LEVELING_ENABLED.get() || !ToolEligibility.isEligible(tool)
                || BrokenWeaponHandler.isBroken(tool)) {
            return null;
        }
        WeaponProgress progress = tool.get(WeaponComponents.PROGRESS.get());
        double bonus = ToolMiningBonus.efficiency(progress == null ? 0 : progress.level(),
                ServerConfig.MAX_LEVEL.get(), ServerConfig.TOOL_MAX_MINING_BONUS.get());
        if (bonus <= 0) {
            return null;
        }
        Identifier itemId = BuiltInRegistries.ITEM.getKey(tool.getItem());
        return new AttributeModifier(Identifier.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID,
                "tool_level_bonus/" + itemId.getNamespace() + "/" + itemId.getPath()), bonus,
                AttributeModifier.Operation.ADD_VALUE);
    }
}
