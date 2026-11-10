package dev.weaponleveling.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.weaponleveling.client.WeaponSettingsScreen;

/** Mod Menu'nun Configure dugmesinden ayarlar ekranini acar. */
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return WeaponSettingsScreen::new;
    }
}