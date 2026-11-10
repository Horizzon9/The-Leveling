package dev.weaponleveling.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Imlecin uzerinde bulundugu slota erisim.
 *
 * NeoForge, `AbstractContainerScreen`a kendi yamasiyla public bir `getSlotUnderMouse()` ekler;
 * vanilla 1.21.11'de boyle bir metot YOKTUR (bytecode ile dogrulandi), yalnizca korumali
 * `hoveredSlot` alani vardir. Bu erisimci o alani disa acar.
 */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("hoveredSlot")
    Slot weaponleveling$getHoveredSlot();
}
