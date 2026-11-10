package dev.weaponleveling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mod kimligi ve ortak gunlukcu.
 *
 * NeoForge surumunde bu sinif ayni zamanda `@Mod` giris noktasiydi ve yapicisinda tum kayitlari
 * yapiyordu. Fabric'te giris noktalari ayri siniflardir (bkz
 * {@code dev.weaponleveling.fabric.WeaponLevelingFabric}), bu yuzden burada yalnizca her iki
 * tarafin da ihtiyac duydugu sabitler kalir.
 *
 * MOD_ID BILEREK AYNI: kaydedilmis esya verisi, veri bilesenleri ve ag paket kimlikleri bu degere
 * baglidir - NeoForge surumuyle ayni dunyayi/sunucuyu paylasabilmesi icin degistirilmemelidir.
 */
public final class WeaponLevelingMod {
    public static final String MOD_ID = "weaponleveling";
    public static final Logger LOGGER = LoggerFactory.getLogger("The Leveling");

    private WeaponLevelingMod() {
    }
}
