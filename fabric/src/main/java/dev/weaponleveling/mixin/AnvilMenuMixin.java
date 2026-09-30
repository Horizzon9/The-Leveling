package dev.weaponleveling.mixin;

import dev.weaponleveling.platform.event.AnvilEvents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NeoForge'un `AnvilUpdateEvent`'inin vanilla karsiligi: kirik bir esyanin orste tek malzemeyle
 * onarilmasini saglar.
 *
 * Slot indeksleri vanilla `ItemCombinerMenu` duzenidir: 0 = sol girdi, 1 = sag girdi (malzeme),
 * 2 = sonuc. Ozel bir sonuc uretildiginde vanilla hesabi tamamen atlanir (`callback.cancel()`),
 * boylece vanilla'nin onarim/isimlendirme maliyeti sonucun uzerine yazamaz.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
    @Shadow
    private int repairItemCountCost;

    // 1.21.1'de `cost` private'dir; mixin disindan erisilemez.
    @Shadow
    @Final
    private DataSlot cost;

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void weaponleveling$repairBrokenItem(CallbackInfo callback) {
        AnvilMenu menu = (AnvilMenu) (Object) this;
        ItemStack left = menu.getSlot(0).getItem();
        ItemStack right = menu.getSlot(1).getItem();
        AnvilEvents.Result result = AnvilEvents.fire(left, right);
        if (result == null) {
            return;
        }
        menu.getSlot(2).set(result.output());
        repairItemCountCost = result.materialCost();
        cost.set(result.cost());
        menu.broadcastChanges();
        callback.cancel();
    }
}
