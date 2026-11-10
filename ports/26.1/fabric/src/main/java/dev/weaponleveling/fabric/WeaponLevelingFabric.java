package dev.weaponleveling.fabric;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.config.ClientConfig;
import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.content.ModContent;
import dev.weaponleveling.content.ModWorldgen;
import dev.weaponleveling.integration.WeaponComponents;
import dev.weaponleveling.loot.ModLootModifiers;
import dev.weaponleveling.network.NetworkHandler;
import dev.weaponleveling.server.ArmorAttributeEvents;
import dev.weaponleveling.server.ToolEvents;
import dev.weaponleveling.server.ArmorProgressEvents;
import dev.weaponleveling.server.BrokenWeaponEvents;
import dev.weaponleveling.server.CombatEvents;
import dev.weaponleveling.server.LegacyIdentityEvents;
import dev.weaponleveling.server.PlayerLifecycleEvents;
import dev.weaponleveling.server.RangedWeaponTracker;
import dev.weaponleveling.server.WeaponCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Ortak (her iki taraf) giris noktasi.
 *
 * SIRA ONEMLI: config once yuklenir, cunku kayit ve olay kurulumundaki bazi kararlar ayar
 * degerlerini okur. Ardindan icerik kaydi (NeoForge'da bu geciktirilmis registry olaylariyla
 * oluyordu, Fabric'te dogrudan), sonra olay aboneleri gelir.
 *
 * NeoForge'da olay aboneleri `@EventBusSubscriber` ile OTOMATIK bulunuyordu; Fabric'te boyle bir
 * tarama yoktur, bu yuzden her abone burada ACIKCA kaydedilir - hangi kancanin var oldugu tek
 * bakista gorunur.
 */
public final class WeaponLevelingFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        loadConfigs();

        WeaponComponents.register();
        ModContent.register();
        ModWorldgen.register();
        ModLootModifiers.register();
        NetworkHandler.register();

        CombatEvents.register();
        ArmorProgressEvents.register();
        ArmorAttributeEvents.register();
        ToolEvents.register();
        BrokenWeaponEvents.register();
        RangedWeaponTracker.register();
        LegacyIdentityEvents.register();
        PlayerLifecycleEvents.register();
        WeaponCommands.register();

        WeaponLevelingMod.LOGGER.info("The Leveling initialised for Fabric");
    }

    /**
     * NeoForge, config dosyalarini `container.registerConfig(...)` ile kendisi yonetiyordu.
     * Fabric'te boyle bir altyapi yok; dosyalar Fabric'in config dizinine ayni adlarla yazilir,
     * yani ayni dosya iki loader arasinda tasinabilir.
     */
    private void loadConfigs() {
        var configDir = FabricLoader.getInstance().getConfigDir();
        ServerConfig.SPEC.load(configDir.resolve("weaponleveling-server.toml"));
        ClientConfig.SPEC.load(configDir.resolve("weaponleveling-client.toml"));
    }
}
