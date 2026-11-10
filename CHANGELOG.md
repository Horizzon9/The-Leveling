# Changelog

Bu proje [Semantic Versioning](https://semver.org/) yaklaşımını izler. Bu dosya bütün Minecraft sürümleri için ortaktır; her sürüm klasöründe aynı kopya bulunur.

## 1.2.1 — 2026-11-04

> Önce Minecraft 1.21.1 (NeoForge + Fabric) için yayımlanır.

### Fixed

- **Maksimum seviye artık düşürülebilir:** Ayarlar menüsü maksimum seviyeyi yalnızca mevcut değerden yukarı kabul ediyordu. Daha düşük bir değer girilince aynı sayfadaki bütün değişiklikler "izin verilen aralığın dışında" diye reddediliyordu. Artık `1–10000` arasındaki her değer kabul ediliyor.
- Sınırın üstünde kalan eşyaların kayıtlı seviyesi silinmez; sınır yeniden yükseltilince eski seviyeleri geri gelir. Hasar, zırh, alet ve kalkan bonusları sınırdaki değerde kalır. Zırhın "her 100 seviyede +1" tabanı da artık sınırı aşmaz.

### Notes

- Relic ve Legacy eşikleri maksimum seviyenin yüzdesidir ve bu kimlikler kalıcıdır. Sınırı düşürmek, eşiği geçen eşyaları kalıcı olarak Relic/Legacy yapar.

## 1.2.0 çoklu sürüm — 2026-11-01

1.2.0 artık desteklenen bütün Minecraft sürümlerinde aynı özellik setiyle yayımlanıyor. Önceki 1.21.11 ve 26.x portları 1.1.0 tabanında kalmıştı. Alet ve kalkan seviyelendirme, Mending uyumu, düşük dayanıklılık uyarısı, kırılan eşya ayarları, yarı maliyetli seviyeler, Mod Menu desteği ve eklenti API'si bu portlarda yoktu. Hepsi taşındı ve portlardaki uyumluluk hataları düzeltildi.

### Desteklenen sürümler

| Minecraft | NeoForge | Fabric Loader / API | Java | Mod Menu | Jade |
| --- | --- | --- | --- | --- | --- |
| 1.21.1 | 21.1.248 | 0.16.14 / 0.116.14+1.21.1 | 21 | 11.0.5 | 15.10.6 |
| 1.21.11 | 21.11.45 | 0.19.5 / 0.141.6+1.21.11 | 21 | 17.0.1 | 21.1.7 (NeoForge) / 21.1.6 (Fabric) |
| 26.1 | 26.1.0.19-beta | 0.20.1 / 0.145.1+26.1 | 25 | 18.0.2 | 26.1.12 / 26.1.11 |
| 26.1.1 | 26.1.1.15-beta | 0.20.1 / 0.145.4+26.1.1 | 25 | 18.0.2 | 26.1.12 / 26.1.11 |
| 26.1.2 | 26.1.2.124 | 0.20.1 / 0.155.3+26.1.2 | 25 | 18.0.2 | 26.1.12 / 26.1.11 |
| 26.2 | 26.2.0.93 | 0.20.1 / 0.161.0+26.2 | 25 | 20.0.3 | 26.2.10 |
| 26.3 | 26.3.0.99 | 0.20.1 / 0.165.0+26.3 | 25 | 21.0.0 | 26.3.5 |

JAR adları bütün sürümlerde `the-leveling-<neoforge|fabric>-<minecraft>-1.2.0.jar` biçimindedir. Mod kimliği (`weaponleveling`), eşya verisi ve ağ protokolü değişmedi. 1.21.1'de seviyelenmiş eşyalar yeni sürümlerde aynı şekilde okunur.

### Added (1.21.11 ve 26.x)

- 1.2.0'ın bütün özellikleri: alet seviyelendirme, kalkan seviyelendirme, Mending uyumu, düşük dayanıklılık uyarısı, `broken.preserveItems` / `broken.autoReplace`, zırh seviye bonusu, yarı maliyetli ve ölçeklenen XP eğrisi, `WeaponEligibility.registerWeaponSource(...)` eklenti API'si. Ayrıntılar aşağıdaki 1.2.0 bölümünde.
- 1.21.11'de Fabric için **Mod Menu** desteği: Mod Menu kuruluysa Configure düğmesi ayarlar ekranını açar.

### Fixed

- **Jade (Fabric, 1.21.1 ve 1.21.11):** Jade eklentisi Fabric'te hiç yüklenmiyordu. `@WailaPlugin` etiketi yalnızca NeoForge'da okunur; Fabric'te `fabric.mod.json` içinde `jade` entrypoint'i gerekir ve bu eklendi. 26.x portlarında bu giriş zaten vardı.
- **Kırılma istatistiği (26.1–26.2):** Kırılan eşya geri çağrısı bu sürümlerde `Item` alır. Port `ItemStack` beklediği için korunan silahlarda vanilla "eşya kırıldı" istatistiği ve başarım tetikleyicisi yine çalışıyordu. Artık sürüme göre doğru tür kullanılıyor; `ItemStack` yalnızca 26.3'te geçerli.
- **Kalkan seviyelendirme (1.21.11 ve 26.x):** Engelleme `Player.hurtCurrentlyUsedShield` yerine `BlocksAttacks.hurtBlockingItem` üzerinden işlenir. NeoForge'da sabit kalkan hasarı alan aşırı yükleme hedeflenir. Modlu kalkanlar `ShieldItem` sınıfından türemese de `blocks_attacks` bileşeniyle tanınır.
- **Dövüş ödülleri (1.21.11 ve 26.x):** Ölüm koruması sonrası XP hesabı, kaldırılan `hurt` yerine sunucu tarafındaki `hurtServer` üzerinden kapanır.
- **Dayanıklılık kancaları (1.21.11 ve 26.x):** Eşyayı silmeyen kırılma koruması `applyDamage` içindeki asıl kırılma noktasına bağlandı. Kalkan dayanıklılık tasarrufu ve düşük dayanıklılık uyarısı `hurtAndBreak` üzerinde kaldı. NeoForge `LivingEntity`, Fabric vanilla `ServerPlayer` imzasını kullanır.
- **Zırh türü tespiti (1.21.11 ve 26.x):** Bu sürümlerde `ArmorItem` ve `ElytraItem` sınıfları yok. Otomatik değiştirme ve aktarım için zırh yuvası `equippable` bileşeninden, elytra `glider` bileşeninden okunur.
- **26.x Fabric derlemesi:** Beş portun `fabric/build.gradle` dosyasında `mavenCentral()` önceki bloğun kapanışıyla aynı satıra yazılmıştı ve proje yapılandırılamıyordu. Düzeltildi.
- 1.21.11 ve 26.x'te `Inventory.items` erişimi `getNonEquipmentItems()` ile değiştirildi. `ResourceLocation` → `Identifier` ve `FMLEnvironment.getDist()` güncellemeleri yeni dosyalara da uygulandı.
- 26.1.2 ve sonrası NeoForge'da hasar olayı `getNewDamage` yerine `getHealthDamage` kullanır. 1.2.0'daki ölüm sonrası ödül akışı bu API ile çalışır.
- 26.1.2 ve sonrası NeoForge'da blok kırma olayı `BlockEvent.BreakEvent` yerine `BreakBlockEvent` olarak geçer. Alet XP'si ve kırık aletle kazıyı engelleme bu olaya bağlandı.
- 26.2 ve 26.3'te aksiyon çubuğu mesajı `Gui` yerine `Gui.hud` üzerinden gösterilir. Düşük dayanıklılık uyarısı buna göre güncellendi.

### Known issues

- NeoForge 26.1 ve 26.1.1 için yalnızca beta yapılar var. Bu betalarda Jade geliştirme ortamında yüklenmez; dağıtılan JAR'lar Jade olmadan sorunsuz çalışır.
- 1.21.11 ve 26.x NeoForge projelerinde JUnit test görevi FML başlatıcısında (`VersionSupportMatrix`) duruyor; birim testleri bu sürümlerin Gradle görevinden çalıştırılamıyor.
- Bu sürüm oyun içinde test edilmedi. Bütün sürümler ve iki loader derlendi; mixin hedefleri her sürümün Minecraft jar'ından `javap` ile doğrulandı.

### Technical

- Taşıma, 1.21.1 → 1.21.11 → 26.x sırasıyla üç yollu birleştirme ile yapıldı. Her portun kendi API değişiklikleri korundu ve 1.2.0 değişiklikleri üzerine eklendi.
- 26.x Fabric derlemesi Gradle'ın Java 25 ile çalışmasını ister (Loom 1.19.3).

## 1.2.0 — 2026-10-16

Alet ve kalkan seviyelendirme, yarı maliyetli seviyeler, Mending uyumu, düşük dayanıklılık uyarısı ve ayrı uyumluluk modları için eklenti API'si.

### Fixed

- Büyü ve benzeri dolaylı hasarla yapılan öldürmeler artık elde tutulan silaha XP yazmaz. Önceki yakın dövüş vuruşunun bekleyen kaydı aynı hedefe gelen sonraki hasara yanlışlıkla uygulanıyordu.
- Öldürme XP'si artık yalnızca hedef gerçekten ölürse verilir; hasar hedefin canını aşsa bile hayatta kalan hedeflere (antrenman mankenleri, totemle kurtulanlar) XP verilmez.
- Eksik öğe etiketi yüzünden bazı (modlu) eşyalara Mending konamaması düzeltildi; Mending artık dayanıklılığı olan her eşyada desteklenir ve `/enchant` ile tekrar uygulanması hata vermez.

### Added

- **Alet seviyelendirme:** Kazma, kürek ve çapa doğru aletle kırılan blokların sertliğinin `0,1` katı kadar XP kazanır (taş ortalama 0,15 XP; kazmak savaşmaktan hızlı olduğu için bilerek düşük) ve seviyeyle daha hızlı kazar (maksimum seviyede varsayılan +10 mining efficiency, Efficiency III kadar). Ayarlar: `tools.*`; modlu aletler için `#weaponleveling:levelable_tools`.
- **Yarı maliyetli seviyeler:** Her seviyenin gerektirdiği XP varsayılan olarak yarıya indi (`progression.xpRequirementMultiplier = 0.5`, formül ve tablo için geçerli; `1.0` eski maliyettir).
- **Düşük dayanıklılık uyarısı:** Silah, alet veya zırh kırılmaya yaklaştığında aksiyon çubuğunda uyarı çıkar; uyarı Unbreaking gibi etkilerden sonraki gerçek kayba göre verilir. İstemci ayarından kapatılabilir.
- **Kırılan eşya ayarları:** `broken.preserveItems` (varsayılan açık) kapatılırsa eşyalar vanilla'daki gibi kırılınca yok olur. `broken.autoReplace` (varsayılan kapalı) açılırsa kırılan eşyanın yerine envanterdeki uygun bir eşya konur ve sohbette bildirilir. İkisi de oyun içi ayar ekranından değiştirilir.
- **Zırh seviye bonusu:** Zırh parçaları her 50 seviyede +1 Armor kazanır (`armor.flatBonusPerLevel = 0.02`; 500. seviyede parça başına +10). Her parça ayrıca her 100 seviyede en az +1 Armor kazanır; zayıf veya eski `flatBonusPerLevel`/`maxLevelBonus` değerleri bu tabanı asla aşağı çekemez (100. seviyede daima en az +1 Armor). Tooltipte seviye bonusu vanilla değerle toplanıp tek satıra yazılır: +3 kask ve +2 bonus → tek satırda +5 Zırh; silahlarda komşu düz hasar satırları da tek toplam satırına katlanır.
- **Mod Menu (Fabric):** Mod Menu kuruluysa Configure düğmesi ayarlar ekranını açar.
- **Eklenti API'si:** `WeaponEligibility.registerWeaponSource(...)` ile ayrı uyumluluk modları kendi silahlarını seviyelenebilir yapabilir (The Leveling: Better Combat bunu kullanır).
- **Kalkan seviyelendirme:** Kalkanlar engelledikleri hasardan XP kazanır, seviye arttıkça engelleme başına daha az dayanıklılık kaybeder (maksimum seviyede varsayılan `%50` tasarruf) ve Relic/Legacy, aura, günlük, ruh bağı ve aktarım sistemlerine katılır. Ayarlar: `shield.levelingEnabled`, `shield.maxDurabilitySaving`.
- **Ölçeklenen XP eğrisi:** Eğri maksimum seviyeye göre otomatik uzar; seviye sınırını 1000 veya 1500 yapan sunucularda üst seviyeler artık imkânsız hale gelmez. 500 seviyede değişiklik yoktur. Kapatmak için `progression.scaleCurveWithMaxLevel = false`.

## 1.1.0 — 2026-10-03

Eşyalara okunabilir bir geçmiş kazandıran Silah Günlüğü ve onun üzerine kurulan sistemler: av ve zırh ustalığı, savaş ritmi, rekorlar, eşya adları, ruh bağı, kehribar aura, liderlik tablosu, başarımlar ve Nether Kehribarı Cevheri.

### Added

- **Silah Günlüğü:** Her silah ilk sahibini, en çok avladığı yaratık türlerini, ilk öldürmesini ve Relic/Legacy olduğu günü kalıcı olarak kaydeder. Genişletilmiş tooltip'te gösterilir; istemci `tooltip.showChronicle` ile kapatabilir.
- **Av Ustalığı:** Bir silah aynı yaratık türünden varsayılan olarak `100 / 500 / 2000` öldürmeye ulaştığında o türe karşı Ustalık I/II/III kazanır; her kademe yalnızca o türe karşı `+%5` hasar verir. Oyunculara karşı hiçbir zaman uygulanmaz. Kademeye ulaşıldığında sohbet bildirimi ve ses çalar.
- **Savaş Ritmi:** Aynı silahla `6` saniye içinde art arda yapılan öldürmeler seri oluşturur; ikinci öldürmeden itibaren her öldürme `+%10`, toplamda en fazla `+%50` XP verir. Seri, öldürme XP bildiriminde `Seri ×N` olarak görünür.
- **Zırh Ustalığı:** Zırh parçaları aynı türden `200 / 1000 / 4000` darbe aldıkça o türe karşı Ustalık I/II/III kazanır; her parçanın her kademesi o türden gelen hasarı `%1` azaltır (toplam en fazla `%50`, oyunculara karşı uygulanmaz).
- **Eşya adları:** Relic veya Legacy eşyalara `/leveling title <ad>` ile kalıcı ad verilir; yalnızca operatör yeniden adlandırabilir.
- **Dönüm noktası renkleri:** Eşya adı maksimum seviyenin `%10 / %25 / %50 / %100` değerlerinde yeşil, turkuaz, mor ve altın olur.
- **Kehribar aura:** Relic ve Legacy eşyalar envanterde nabız gibi atan kehribar köşe çerçevesi (Legacy'de dönen kıvılcım) ve elde tutulduğunda kehribar toz parçacıklarıyla ayırt edilir; büyü parlaması kullanılmaz.
- **Liderlik tablosu:** `/leveling top` dünyadaki en güçlü on silahı listeler.
- **Nether Kehribarı Cevheri:** End Kehribarı artık Nether'da netherrack içinde de çıkar; Nether altınına yakın ama biraz daha nadirdir (chunk başına 8 damar, en fazla 8 blok, Nether'ın tüm yüksekliklerinde, `Y=0–127`) ve aynı külçeyi verir.
- **Rekorlar:** Günlük, silahın en güçlü vuruşunu, en uzak menzilli isabetini ve en uzun serisini kaydeder.
- **Legacy öldürme efekti:** Legacy silahla yapılan öldürmeler kehribar parçacık patlaması ve ses çıkarır (`effects.legacyKillEffect`).
- **Ruh Bağı:** Legacy eşyalar ölümde düşmez ve yeniden doğunca aynı yuvaya döner; bekleyen eşyalar dünya verisinde saklanır (`soulbound.tier`: `legacy`, `relic` veya `off`).
- **Kayıpsız aktarım:** Sunucunun ayarlı maksimum seviyesine ulaşmış bir eşya, Miras Aktarım Masası'nda XP'sinin tamamını aktarır.
- **Toplam XP:** Genişletilmiş (Shift) tooltip eşyanın toplam kazandığı XP'yi gösterir.
- **Başarımlar:** İlk seviye, Relic, Legacy, eşya adlandırma, av ustalığı, en yüksek av ustalığı, 10'luk seri ve zırh ustalığı için sekiz başarım.
- Yeni sunucu ayarları: `chronicle.*`, `chronicle.foeMastery.*`, `chronicle.armorMastery.*`, `chronicle.titlesEnabled` ve `streak.*`; yeni istemci ayarları: `visual.milestoneNameColors`, `visual.identityAura`.
- Günlük, ustalık ve seri mantığı için birim testleri.

### Changed

- `/weaponlevel reset` ve Miras Aktarım Masası kaynak eşyayı sıfırlarken günlüğünü de temizler; sıfırlanan eşya yeni bir geçmişe başlar.
- Öldürme XP bildirimi uzun metinlere göre genişler.

### Removed

- Boss öldürme sayacı ve buna bağlı **Bossbane** kimlik yolu kaldırıldı. Boss tespiti yalnızca vanilla Ender Dragon ve Wither'ı tanıyabiliyordu; modlu boss'larda tutarsız sonuç vermemesi için tooltip artık yalnızca toplam yenilen sayısını gösterir. Eski eşyalardaki `boss_kills` verisi okunurken yok sayılır.

### Technical

- Günlük ayrı bir `weaponleveling:chronicle` veri bileşeninde tutulur; mevcut `weaponleveling:progress` verisi ve şeması değişmedi, eski eşyalar göç gerektirmeden çalışır.
- Günlük verisi sınırlıdır (en fazla 16 yaratık türü, 24 kayıt). Tablo dolduğunda ustalık kazanmamış en az avlanan tür yer açar; ustalık kazanılmış türler asla silinmez.
- Bozuk veya daha yeni sürümden gelen günlük verisi eşyayı kaybettirmek yerine anlaşılabilen kısmına indirgenir.

## 1.0.0 — 2026-09-18

The Leveling'in ilk kararlı sürümü. Bu sürüm, uzun süre kullanılan ekipmana kalıcı kimlik kazandıran Relic/Legacy sistemini ve eşya ilerlemesini kontrollü şekilde taşıyan Miras Aktarım sistemini ekler.

### Added

- Maksimum seviyenin yapılandırılabilir yüzdelerine ulaşıldığında kalıcı olarak kazanılan **Relic** ve **Legacy** eşya kimlikleri.
- End'de oluşan End Kehribarı Cevheri, fırınlanabilir End Kehribarı Külçesi, Miras Aktarım Şablonu ve Miras Aktarım Masası.
- Kaynak eşyanın toplam XP'sinin `%50` değerini uyumlu bir hedefe aktaran sunucu otoriteli ilerleme aktarımı.
- End City hazine sandıklarında `%5`; Bastion köprü, hoglin ahırı ve genel sandıklarında `%5`; Bastion hazine odası sandıklarında `%50` şablon bulunma ihtimali.
- Bir şablon, yedi End Kehribarı Külçesi ve bir End Taşı kullanarak iki şablona çoğaltılabilir.
- Miras Aktarım Masası ve yeni eşyalar için ayrı Creative sekmesi girdileri, İngilizce/Türkçe metinler, modeller, loot tabloları ve tarifler.

### Changed

- Aktarım hedefinin önceden XP kazanmış olmasına izin verilir; hedefin mevcut XP'si korunur ve aktarılan XP üzerine eklenir.
- Aktarımda kaynak eşya artık tüketilmez; işlemden sonra kaynak ilerlemesi sıfırlanır, hedef eşya sonuçla değiştirilir ve şablon tüketilir.
- Miras Aktarım Masası bütün `minecraft:planks` tahta türleriyle üretilebilir.
- End Kehribarı damarları yeni End chunk'larında `25` genel ve `25` yüzey odaklı denemeyle üretilir.
- Amber cevheri ve külçesi daha koyu, tutarlı bir renk paletine geçirildi.

### Fixed

- End Kehribarı biome modifier dosyasının NeoForge tarafından yüklenmemesine yol açan yanlış veri yolu düzeltildi.
- Aktarım sırasında kaynak eşyanın tamamen kaybolmasına neden olan tüketim davranışı düzeltildi.
- XP kazanmış hedeflerin gereksiz yere reddedilmesi kaldırıldı; yalnızca gerçek eşya türü/slot uyumsuzlukları engellenir.
- Miras Aktarım Masası'nın bir yan yüzünde alt taban dokusunun görünmesi düzeltildi.
- Miras Aktarım Şablonu ganimeti yalnızca End City ve Bastion sandık tablolarıyla sınırlandırıldı; Bastion oranları vanilla Netherite Yükseltme Şablonu oranlarının yarısına ayarlandı.

### Compatibility

- Mevcut 0.1.x eşya XP/seviye verileri korunur; mod kimliği ve ilerleme bileşeni kimliği değişmemiştir.
- Jade isteğe bağlıdır. Simply Swords, Spartan Weaponry Unofficial ve ArmorPlus için veri/öznitelik tabanlı uyumluluk devam eder.
- Epic Fight ve Better Combat gibi standart saldırı olaylarını tamamen değiştiren combat overhaul modları, gelecekte yayımlanması planlanan ayrı uyumluluk eklentileri gerektirebilir.

## 0.1.1-beta — 2026-09-03

Görsel okunabilirlik, öldürme XP dengesi ve modlu canlı uyumluluğuna odaklanan ikinci açık beta.

### Added

- Tanınmayan mod canlıları için maksimum cana dayalı otomatik XP ölçekleme. Düşmanlarda 20 sağlık `1×`, pasif ve neutral canlılarda aynı oranın yarısı kullanılır.
- Zombie, Warden, Wither, Ender Dragon, Ravager ve diğer vanilla canlılar arasında tehdit seviyesine göre ayrıntılı XP dengesi.
- `Compact`, `Standard` ve `Detailed` tooltip görünüm seçenekleri.
- Broken eşyaların envanter ikonunda çizilmiş çatlaklar ve kırmızı kenarlık.
- Mob öldürülünce kısa süreli, başarımlardan esinlenen `+XP` bildirimi.
- Seviye atlamalarında eşya ikonu, yeni seviye ve eşya adını gösteren sinematik toast bildirimi.
- `Shift + Orta Tık` ile eşyanın seviye, XP ve gücünü hover bilgisiyle sohbette paylaşma.
- Maksimum seviyedeki eşyalara özel tooltip vurgu rengi.

### Changed

- Eski düz beyaz actionbar seviye mesajı, sade ve yumuşak geçişli seviye toast’ına dönüştürüldü.
- Tooltip paneli daha okunaklı, daha az kalabalık ve vanilla arayüzüyle daha uyumlu hâle getirildi.
- Toplam hasar değerleri gereksiz uzun ondalıklar yerine en fazla iki basamakla, matematiksel yuvarlama kullanılarak gösteriliyor.
- Aynı anda kazanılan kill XP bildirimleri ekranda üst üste binmek yerine birleştiriliyor.
- Modlu düşmanlarda eski `6×` sağlık katsayısı sınırı kaldırıldı; ödül artık maksimum canla doğrudan orantılı ve son güvenlik sınırı `xp.maxXpPerAttack` tarafından uygulanıyor.
- İstemci tarafında her karede tekrar hesaplanan tooltip paneli metinleri/genişliği, envanter yuvası uygunluk kontrolü ve öldürme XP/seviye atlama bildirimlerinin metin ölçümü artık önbelleğe alınıyor; sunucu tarafında `xp.entityMultipliers` geçersiz kılmaları her öldürmede yeniden ayrıştırılmak yerine bir kez ayrıştırılıp önbelleğe alınıyor. Davranış değişmedi.

### Fixed

- Özel tooltip paneli ile vanilla/üçüncü taraf tooltip satırlarının görsel olarak çakışması giderildi.
- Uzun eşya adlarının seviye bildirim panelinin dışına taşması engellendi.
- Bazı mod canlılarının standart zombie ile aynı XP’yi vermesine neden olan genel fallback dengesi düzeltildi.

### Known beta limits

- Özel hasar veya projectile olaylarını tamamen değiştiren savaş modları ayrıca uyumluluk katmanı gerektirebilir.
- Modlu canlı XP’si maksimum canı temel alır; özel olarak zayıf/güçlü tasarlanmış canlılar `xp.entityMultipliers` ile sunucu tarafından ayarlanabilir.
- Dedicated server üzerinde iki gerçek oyuncuyla tam multiplayer testi ve bütün üçüncü taraf mod kombinasyonları henüz tamamlanmamıştır.

## 0.1.0-beta — 2026-08-29

İlk açık beta: Minecraft 1.21.1 ve NeoForge 21.1.248 için ekipman-seviye sistemi.

### Added

- Her silah ve zırh parçası için ayrı, kalıcı XP ve seviye verisi.
- Mob öldürmeleriyle silah XP'si; isteğe bağlı olarak oyuncu öldürmelerine aynı XP değeri.
- Kılıç, balta, mace, trident, yay, crossbow ve tag/öznitelik aracılığıyla uygun modlu silah desteği.
- Alınan hasarla zırh XP'si ve parça başına Armor bonusu.
- Seviye 0 taban kabul edilerek sınırlı, yüksek seviyelerde hızlanan hasar çarpanı eğrisi.
- Kırılan seviyelenebilir eşyanın `Broken` kalması ve doğru onarım malzemesiyle örste `+1` dayanıklılık geri kazanması.
- Sunucu otoriteli ayarlar, yönetici komutları, tooltip, tuş atamaları ve ESC menüsündeki `WL` düğmesi.
- Jade yüklüyken, yerde duran seviyelendirilmiş silah ve zırhlarda seviye/XP bilgisini gösteren isteğe bağlı overlay entegrasyonu.
- İngilizce ve Türkçe oyun içi metinler.

### Changed

- Varsayılan XP eğrisi: taban `100`, seviye başına çarpan `1.02432611896515`, maksimum seviye `500`; seviye 0'dan 500'e toplam yaklaşık 680,8 milyon XP gerekir.
- Varsayılan hasar eğrisi artık silahın kendi başlangıç hasarını korur: seviye 500'de `×11` hasar uygulanır; 7 hasarlı silah yaklaşık 77, 14 hasarlı silah yaklaşık 154 vurur.
- Zırh başına Armor artışı `+0.004` olarak ölçeklendi; seviye 0'da bonus uygulanmaz ve `+2 Armor` tavanına seviye 500'de ulaşır.
- Harici silah algılama varsayılan olarak açıktır; yanlış algılanan eşyalar config'ten hariç tutulabilir.
- Mace smash hasarı, trident'in yakın/atılmış saldırısı ve `#c:tools/spear` kullanan uyumlu mod spear'larının kendi özel hasarı, seviye çarpanından önce korunur; projectile ilerlemesi güvenli eşya kimliğiyle geri yazılır.
- Başlangıç seviyesi `0` yapıldı. Şema 1 ile kaydedilen eşyalar XP'sini koruyarak bir seviye aşağı taşınır; varsayılan XP büyüme katsayısı, seviye 500'deki yaklaşık 680,8 milyon XP hedefini koruyacak şekilde güncellendi.
- Zırhın varsayılan parça başı artışı `+0.004 Armor/seviye` oldu; seviye 0 tabanında `+2 Armor` tavanına tam seviye 500'de ulaşır.

### Known beta limits

- Denge değerleri, özellikle modlu silahların taban hasarları için geri bildirimle değişebilir.
- Özel projectile veya standart dışı saldırı olayı kullanan bazı mod silahları manuel tag/config tanımı isteyebilir.
- İki gerçek oyuncuyla dedicated server davranışı ve her üçüncü taraf mod kombinasyonu henüz doğrulanmış değildir.
