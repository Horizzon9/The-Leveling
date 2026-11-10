package dev.weaponleveling.mixin;

import dev.weaponleveling.server.SoulboundService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Takes soulbound items out of a dying player's inventory before {@code dropEquipment} scatters it.
 * Hooking the vanilla method keeps the behaviour identical on every loader.
 */
@Mixin(Player.class)
public abstract class PlayerSoulboundMixin {
    @Inject(method = "dropEquipment", at = @At("HEAD"))
    private void weaponleveling$keepSoulboundItems(CallbackInfo callback) {
        if ((Object) this instanceof ServerPlayer player) {
            SoulboundService.stashOnDeath(player);
        }
    }
}
