package dev.weaponleveling.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Imlecin uzerinde bulundugu slota erisim; 26.x'te vanilla getSlotUnderMouse sunmaz. */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("hoveredSlot")
    Slot weaponleveling$getHoveredSlot();
}
