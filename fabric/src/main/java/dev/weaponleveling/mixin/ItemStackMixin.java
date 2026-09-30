package dev.weaponleveling.mixin;

import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.LevelableItemEligibility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

/** Keeps levelable weapons at zero durability rather than allowing vanilla to remove their stack. */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    // IMZA FARKI (NeoForge -> vanilla): NeoForge, `hurtAndBreak`in ucuncu parametresini kendi
    // yamasiyla `LivingEntity`ye genisletir; VANILLA 1.21.1'de bu parametre `ServerPlayer`dir
    // (mapping ve calisma zamani dogrulamasiyla teyit edildi). Oyuncu olmayan varliklar icin
    // vanilla buraya `null` gecer - `preserveOnBreak` zaten `instanceof ServerPlayer` ile
    // calistigi icin null guvenlidir ve davranis NeoForge surumuyle ayni kalir.
    @Redirect(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void weaponleveling$preserveBrokenWeapon(ItemStack stack, int amount, int damage, ServerLevel level,
                                                      ServerPlayer player, Consumer<Item> onBroken) {
        if (!BrokenWeaponHandler.preserveOnBreak(stack, player)) {
            stack.shrink(amount);
        }
    }

    @Redirect(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private void weaponleveling$skipVanillaBreakCallback(Consumer<Object> callback, Object rawItem) {
        // Vanilla's callback grants break statistics/advancements. The weapon was retained, so do not fire it.
        if (!(rawItem instanceof Item item) || !LevelableItemEligibility.isEligible(item.getDefaultInstance())) {
            callback.accept(rawItem);
        }
    }
}
