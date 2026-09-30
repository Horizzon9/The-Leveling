package dev.weaponleveling.mixin;

import dev.weaponleveling.platform.event.ItemUseEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NeoForge'un `ArrowLooseEvent` + `LivingEntityUseItemEvent.Stop` olaylarinin vanilla karsiligi.
 *
 * HEAD secimi kritik: `releaseUsingItem()` govdesi esyanin `releaseUsing(...)` metodunu cagirir ve
 * mermi ORADA olusturulur. Basa baglanmak, mermi dunyaya eklenmeden once "hangi silahla atildi"
 * kaydinin olusturulmasini garanti eder - mermi dogdugunda kaydi arayan taraf onu hazir bulur.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityUseItemMixin {
    @Shadow
    public abstract ItemStack getUseItem();

    @Inject(method = "releaseUsingItem", at = @At("HEAD"))
    private void weaponleveling$onReleaseUsingItem(CallbackInfo callback) {
        ItemStack useItem = getUseItem();
        if (!useItem.isEmpty()) {
            ItemUseEvents.fireReleaseUsing((LivingEntity) (Object) this, useItem);
        }
    }
}
