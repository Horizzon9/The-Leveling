package dev.weaponleveling.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.weaponleveling.WeaponLevelingMod;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = WeaponLevelingMod.MOD_ID, value = Dist.CLIENT)
public final class ClientKeyMappings {
    private static final String CATEGORY = "key.categories.weaponleveling";

    public static final KeyMapping EXPAND_TOOLTIP = new KeyMapping("key.weaponleveling.expand_tooltip",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_SHIFT, CATEGORY);

    private ClientKeyMappings() {
    }

    /**
     * Tooltip rendering happens while a screen owns keyboard focus. The raw Shift fallback keeps
     * the default Left Shift behavior reliable even when the key mapping state is not refreshed.
     */
    public static boolean isTooltipExpandHeld() {
        return EXPAND_TOOLTIP.isDown() || Screen.hasShiftDown();
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(EXPAND_TOOLTIP);
    }
}
