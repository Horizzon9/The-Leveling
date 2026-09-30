package dev.weaponleveling.server;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.WeaponComponents;
import dev.weaponleveling.integration.WeaponDamage;
import dev.weaponleveling.integration.WeaponEligibility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import dev.weaponleveling.platform.event.ItemUseEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResultHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Associates player-owned projectile entities with the exact levelable item used to create them.
 * This covers bows, crossbows, thrown tridents, and compatible spear items tagged as ranged weapons.
 * The association exists only for the launch tick, then follows the projectile until it leaves the level.
 */
public final class RangedWeaponTracker {
    private static final Map<UUID, PendingLaunch> PENDING_LAUNCHES = new HashMap<>();
    private static final Map<UUID, RangedAttack> TRACKED_PROJECTILES = new HashMap<>();

    private RangedWeaponTracker() {
    }

    /**
     * Platform eslemesi:
     * - `PlayerInteractEvent.RightClickItem` -> Fabric `UseItemCallback` (dolu tatar yayinin atesi)
     * - `ArrowLooseEvent` + `LivingEntityUseItemEvent.Stop` -> {@link ItemUseEvents} (tek kanca;
     *   yay ve trident ayni vanilla yolundan gecer - bkz o sinifin dokumani)
     * - `EntityJoinLevelEvent` / `EntityLeaveLevelEvent` -> Fabric `ServerEntityEvents`
     *   (zaten yalnizca sunucu tarafinda atesler, bu yuzden eski `isClientSide` kontrolu dustu)
     * - `ServerTickEvent.Post` -> Fabric `ServerTickEvents.END_SERVER_TICK`
     */
    public static void register() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                prepareLaunch(serverPlayer, player.getItemInHand(hand));
            }
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        });

        ItemUseEvents.registerReleaseUsing((entity, stack) -> {
            if (entity instanceof ServerPlayer player) {
                prepareLaunch(player, stack);
            }
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof Projectile projectile)
                    || !(projectile.getOwner() instanceof ServerPlayer player)) {
                return;
            }
            PendingLaunch pending = PENDING_LAUNCHES.get(player.getUUID());
            if (pending == null || pending.gameTime() != player.serverLevel().getGameTime()) {
                return;
            }
            TRACKED_PROJECTILES.put(projectile.getUUID(), new RangedAttack(player.getUUID(), pending.weapon(),
                    pending.weaponInstanceId()));
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof Projectile projectile) {
                TRACKED_PROJECTILES.remove(projectile.getUUID());
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server ->
                PENDING_LAUNCHES.entrySet().removeIf(entry -> {
                    ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                    return player == null || player.serverLevel().getGameTime() > entry.getValue().gameTime();
                }));
    }

    public static RangedAttack findTrackedAttack(Entity directEntity, UUID attackerId) {
        if (directEntity == null) {
            return null;
        }
        RangedAttack attack = TRACKED_PROJECTILES.get(directEntity.getUUID());
        return attack != null && attack.attackerId().equals(attackerId) ? attack : null;
    }

    public static WeaponProgress currentProgress(RangedAttack attack, Entity directEntity, ServerPlayer attacker) {
        WeaponProgress progress = matchingProgress(attack.weapon(), attack.weaponInstanceId());
        if (progress != null) {
            return progress;
        }
        if (directEntity instanceof AbstractArrow arrow) {
            progress = matchingProgress(arrow.getPickupItemStackOrigin(), attack.weaponInstanceId());
            if (progress != null) {
                return progress;
            }
        }
        // Some modded spears retain their source stack in the player's inventory rather than extending
        // AbstractArrow. Match by the per-stack instance ID so a same-type spare weapon is never updated.
        return matchingProgress(findInInventory(attacker, attack.weaponInstanceId()), attack.weaponInstanceId());
    }

    public static void saveProgress(RangedAttack attack, Entity directEntity, ServerPlayer attacker,
                                    WeaponProgress progress) {
        forEachMatchingStack(attack, directEntity, attacker,
                stack -> stack.set(WeaponComponents.PROGRESS.get(), progress));
    }

    /**
     * Visits every live copy of the launching weapon: the stack captured at launch, a thrown weapon's
     * own item (tridents), and the stack now in the player's inventory. Only stacks that still carry the
     * launch instance id are visited, so a same-type spare weapon is never touched.
     */
    public static void forEachMatchingStack(RangedAttack attack, Entity directEntity, ServerPlayer attacker,
                                            Consumer<ItemStack> action) {
        if (matches(attack.weapon(), attack.weaponInstanceId())) {
            action.accept(attack.weapon());
        }
        if (directEntity instanceof AbstractArrow arrow
                && matches(arrow.getPickupItemStackOrigin(), attack.weaponInstanceId())) {
            action.accept(arrow.getPickupItemStackOrigin());
        }
        ItemStack inventoryWeapon = findInInventory(attacker, attack.weaponInstanceId());
        if (matches(inventoryWeapon, attack.weaponInstanceId())) {
            action.accept(inventoryWeapon);
        }
    }

    private static void prepareLaunch(ServerPlayer player, ItemStack weapon) {
        if (!WeaponEligibility.isRanged(weapon) || !WeaponEligibility.isEligibleOnServer(weapon)
                || BrokenWeaponHandler.isBroken(weapon)) {
            return;
        }
        WeaponProgress progress = weapon.get(WeaponComponents.PROGRESS.get());
        if (progress == null) {
            progress = WeaponProgress.create(WeaponDamage.baseAttackDamage(weapon));
            weapon.set(WeaponComponents.PROGRESS.get(), progress);
            player.inventoryMenu.broadcastChanges();
        }
        PENDING_LAUNCHES.put(player.getUUID(), new PendingLaunch(weapon, progress.instanceId(),
                player.serverLevel().getGameTime()));
    }

    private static WeaponProgress matchingProgress(ItemStack stack, UUID instanceId) {
        return matches(stack, instanceId) ? stack.get(WeaponComponents.PROGRESS.get()) : null;
    }

    private static boolean matches(ItemStack stack, UUID instanceId) {
        WeaponProgress progress = stack.get(WeaponComponents.PROGRESS.get());
        return progress != null && progress.instanceId().equals(instanceId);
    }

    private static ItemStack findInInventory(ServerPlayer player, UUID instanceId) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (matches(stack, instanceId)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private record PendingLaunch(ItemStack weapon, UUID weaponInstanceId, long gameTime) {
    }

    public record RangedAttack(UUID attackerId, ItemStack weapon, UUID weaponInstanceId) {
    }
}
