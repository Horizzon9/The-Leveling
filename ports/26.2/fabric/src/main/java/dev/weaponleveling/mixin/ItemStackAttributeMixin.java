package dev.weaponleveling.mixin;

import dev.weaponleveling.platform.event.ItemAttributeEvents;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NeoForge'un `ItemAttributeModifierEvent`'inin vanilla karsiligi.
 *
 * TAIL secimi bilincli: vanilla once esyanin kendi degistiricilerini, sonra buyulerden gelenleri
 * yayinlar; bizimkiler en sona eklenir, yani mevcut hicbir degistiriciyi gizlemez ya da sirasini
 * bozmaz.
 *
 * `EquipmentSlotGroup` asiri yuklemesi 1.21.11'de uc parametreli bir tuketici alir (ucuncusu
 * degistiricinin ipucundaki gorunumu, `ItemAttributeModifiers.Display`). Buyulerden gelen
 * degistiricileri vanilla'nin kendisi `Display.attributeModifiers()` (varsayilan gorunum) ile
 * yayinlar; bizimkiler de ayni gorunumle eklenir.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackAttributeMixin {

    @Inject(
        method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
        at = @At("TAIL")
    )
    private void weaponleveling$extraModifiersForSlot(
        EquipmentSlot slot,
        BiConsumer<Holder<Attribute>, AttributeModifier> action,
        CallbackInfo callback
    ) {
        for (ItemAttributeEvents.Extra extra : ItemAttributeEvents.collect((ItemStack) (Object) this)) {
            if (extra.slots().test(slot)) {
                action.accept(extra.attribute(), extra.modifier());
            }
        }
    }

    @Inject(
        method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;"
            + "Lorg/apache/commons/lang3/function/TriConsumer;)V",
        at = @At("TAIL")
    )
    private void weaponleveling$extraModifiersForGroup(
        EquipmentSlotGroup group,
        TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> action,
        CallbackInfo callback
    ) {
        for (ItemAttributeEvents.Extra extra : ItemAttributeEvents.collect((ItemStack) (Object) this)) {
            if (extra.slots() == group) {
                action.accept(extra.attribute(), extra.modifier(), ItemAttributeModifiers.Display.attributeModifiers());
            }
        }
    }
}
