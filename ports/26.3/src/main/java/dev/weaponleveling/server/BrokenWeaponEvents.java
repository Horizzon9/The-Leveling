package dev.weaponleveling.server;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.WeaponEligibility;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Prevents use of broken levelable weapons and supplies their one-material anvil repair. */
@EventBusSubscriber(modid = WeaponLevelingMod.MOD_ID)
public final class BrokenWeaponEvents {
    private BrokenWeaponEvents() {
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack brokenWeapon = event.getLeft();
        ItemStack material = event.getRight();
        if (!BrokenWeaponHandler.canRepairWith(brokenWeapon, material)) {
            return;
        }
        event.setOutput(BrokenWeaponHandler.repairOneDurability(brokenWeapon));
        event.setMaterialCost(1);
        event.setXpCost(1);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (BrokenWeaponHandler.isBroken(event.getEntity().getMainHandItem())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (BrokenWeaponHandler.isBroken(event.getItemStack())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player player
                && BrokenWeaponHandler.isBroken(player.getMainHandItem())
                && RangedWeaponTracker.findTrackedAttack(event.getSource().getDirectEntity(), player.getUUID()) == null) {
            event.setCanceled(true);
        }
    }
}
