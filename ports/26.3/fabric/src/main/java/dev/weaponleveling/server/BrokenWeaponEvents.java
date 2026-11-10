package dev.weaponleveling.server;

import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.platform.event.AnvilEvents;
import dev.weaponleveling.platform.event.DamageEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

/**
 * Kirik esyanin davranisi: saldiramaz, sag tikla kullanilamaz, hasar veremez; orste tek malzemeyle
 * onarilir.
 *
 * Kaynak sürüme göre platform eslemesi:
 * - `AnvilUpdateEvent`          -> {@link AnvilEvents} (mixin ile vanilla `AnvilMenu.createResult`)
 * - `AttackEntityEvent`         -> Fabric `AttackEntityCallback`
 * - `PlayerInteractEvent.RightClickItem` -> Fabric `UseItemCallback`
 * - `LivingIncomingDamageEvent` (iptal) -> {@link DamageEvents} iptal kancasi
 */
public final class BrokenWeaponEvents {
    private BrokenWeaponEvents() {
    }

    public static void register() {
        AnvilEvents.register(BrokenWeaponEvents::onAnvilUpdate);
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
                BrokenWeaponHandler.isBroken(player.getMainHandItem())
                        ? InteractionResult.FAIL
                        : InteractionResult.PASS);
        UseItemCallback.EVENT.register((player, level, hand) ->
                BrokenWeaponHandler.isBroken(player.getItemInHand(hand))
                        ? InteractionResult.FAIL
                        : InteractionResult.PASS);
        DamageEvents.registerCancelIncoming((target, source) ->
                source.getEntity() instanceof net.minecraft.world.entity.player.Player player
                        && BrokenWeaponHandler.isBroken(player.getMainHandItem())
                        && RangedWeaponTracker.findTrackedAttack(source.getDirectEntity(), player.getUUID()) == null);
    }

    private static AnvilEvents.Result onAnvilUpdate(ItemStack brokenWeapon, ItemStack material) {
        if (!BrokenWeaponHandler.canRepairWith(brokenWeapon, material)) {
            return null;
        }
        return new AnvilEvents.Result(BrokenWeaponHandler.repairOneDurability(brokenWeapon), 1, 1);
    }
}
