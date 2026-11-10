package dev.weaponleveling;

import dev.weaponleveling.config.ClientConfig;
import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.client.ClientConfigScreenRegistration;
import dev.weaponleveling.client.ClientTooltipComponents;
import dev.weaponleveling.client.ClientItemDecorations;
import dev.weaponleveling.client.ClientContentRegistration;
import dev.weaponleveling.content.ModContent;
import dev.weaponleveling.integration.WeaponComponents;
import dev.weaponleveling.loot.ModLootModifiers;
import dev.weaponleveling.network.NetworkHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(WeaponLevelingMod.MOD_ID)
public final class WeaponLevelingMod {
    public static final String MOD_ID = "weaponleveling";

    public WeaponLevelingMod(IEventBus modBus, ModContainer container) {
        WeaponComponents.REGISTER.register(modBus);
        ModContent.BLOCKS.register(modBus);
        ModContent.ITEMS.register(modBus);
        ModContent.MENUS.register(modBus);
        ModContent.CREATIVE_MODE_TABS.register(modBus);
        ModLootModifiers.REGISTER.register(modBus);
        modBus.addListener(NetworkHandler::register);
        container.registerConfig(ModConfig.Type.SYNCED, ServerConfig.SPEC, "weaponleveling-server.toml");
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "weaponleveling-client.toml");
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ClientConfigScreenRegistration.register(container);
            modBus.addListener(ClientTooltipComponents::register);
            modBus.addListener(ClientItemDecorations::register);
            modBus.addListener(ClientContentRegistration::registerMenuScreens);
        }
    }
}
