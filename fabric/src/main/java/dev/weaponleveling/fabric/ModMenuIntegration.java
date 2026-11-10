package dev.weaponleveling.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.weaponleveling.client.WeaponSettingsScreen;

/** Opens the mod's settings from Mod Menu's configure button, like NeoForge's mod list does. */
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return WeaponSettingsScreen::new;
    }
}
