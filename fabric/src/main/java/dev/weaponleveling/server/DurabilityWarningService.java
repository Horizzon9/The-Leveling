package dev.weaponleveling.server;

import dev.weaponleveling.network.LowDurabilityWarningPayload;
import dev.weaponleveling.platform.net.ModNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class DurabilityWarningService {
    private DurabilityWarningService() {
    }

    /** @param remainingBefore durability points left before the hit; the warning uses what is left afterwards. */
    public static void checkAndWarn(ServerPlayer player, ItemStack stack, int remainingBefore) {
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return;
        }
        int maxDamage = stack.getMaxDamage();
        int remainingAfter = maxDamage - stack.getDamageValue();
        if (remainingAfter <= 0 || remainingAfter >= remainingBefore) {
            return;
        }
        int threshold = Math.min(30, Math.max(5, (int) Math.ceil(maxDamage * 0.10)));
        boolean enteredLow = (remainingBefore > threshold && remainingAfter <= threshold);
        boolean enteredCritical = (remainingBefore > 5 && remainingAfter <= 5);
        boolean finalWarning = (remainingBefore > 1 && remainingAfter == 1);

        if (enteredLow || enteredCritical || finalWarning) {
            ModNetwork.sendToPlayer(player, new LowDurabilityWarningPayload(
                    stack.getHoverName().getString(), remainingAfter, maxDamage));
        }
    }
}
