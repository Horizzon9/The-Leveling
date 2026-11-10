# The Leveling — 1.0.0

Minecraft 1.21.1 + NeoForge için, silah ve zırhların kendi XP/seviye verisini taşıdığı sunucu otoriteli bir mod. Kod, metinler ve veriler sıfırdan hazırlanmıştır; Lukas' Weapon Leveling kaynakları veya varlıkları kullanılmamıştır.

> `weaponleveling` mod kimliği, mevcut dünyalar ve eşya verileriyle uyumluluk için sabittir.

## Özellikler

- Her `ItemStack` için ayrı, kalıcı ve ağda eşitlenen veri bileşeni
- Varsayılan vanilla kılıçlar, baltalar, mace, trident, yaylar ve çapraz yaylar; datapack etiketleriyle dahil etme/hariç tutma
- `#c:tools/melee_weapon` ve `#c:tools/ranged_weapon` kullanan mod silahları için veri odaklı uyumluluk; ayrıca saldırı hasarı taşıyan harici silahlar için yapılandırılabilir otomatik algılama
- Simply Swords, Spartan Weaponry Unofficial ve ArmorPlus için tag/algılama temelli uyumluluk zemini
- Vanilla oyuncu XP'sinden ve Mending'den tamamen bağımsız silah XP'si
- Yalnızca uygun silahla öldürülen canlılar için sunucu taraflı silah XP'si; vanilla ve modlu canlıların ödülü maksimum canlarıyla doğru orantılıdır (`20 can = 1× kill bonus`)
- Varsayılan 0–500 seviye; `100 × 1.02432611896515^seviye` üstel XP eğrisi ve seviye 500'e kadar yaklaşık 680,8 milyon toplam XP
- Uzun süre kullanılan eşyalara kalıcı **Relic** ve **Legacy** kimliği; varsayılan eşikler maksimum seviyenin `%25` ve `%100` değeridir ve kazanılmış kimlik seviye düşürülse bile korunur
- Sunucu tarafında uygulanan, silahın kendi başlangıç hasarını koruyan sınırlı ve yüksek seviyelerde hızlanan hasar çarpanı eğrisi
- Varsayılan ayarlarda seviye 500'de `×11` hasar: 7 hasarlı elmas kılıç yaklaşık 77, 14 hasarlı mod kılıcı yaklaşık 154 vurur; eğri üssü ve çarpan tavanı sunucu tarafından değiştirilebilir
- Mace'in düşüşe bağlı smash hasarı, trident'in yakın/atılmış hasarı ve `#c:tools/spear` etiketli uyumlu mod spear'larının doğrudan veya projectile hasarı kendi özel hesaplarından sonra seviye çarpanıyla ölçeklenir
- Zırhın alınan hasarla seviye atlaması ve parça başına sınırlı Armor bonusu
- Dayanıklılık sıfıra indiğinde eşyanın kaybolmak yerine `Broken` olması; aynı malzemeden bir adetle örste `+1` dayanıklılıkla onarılması
- End adalarında yüzeyde ve duvar kesitlerinde rahatça bulunabilen End Kehribarı Cevheri, eritilerek elde edilen külçesi, End City ve Bastion sandıklarında bulunan Miras Aktarım Şablonu ve toplam XP'nin `%50` değerini hedef eşyaya taşıyan Miras Aktarım Masası
- Kısa/ayrıntılı tooltip, yay/crossbow için gerçek mermi hasarı çarpanı, yeniden atanabilir tuşlar ve sayfalı geliştirme ekranı
- Seviye atlayınca hotbar üstünde eşya ikonlu, kısa süreli amber seviye bildirimi; `notification.levelUp` ile actionbar/chat/off seçenekleri
- ESC menüsündeki envanter-yuvası boyutunda `WL` düğmesi ve NeoForge Mod Listesi config ekranı
- İsteğe bağlı Jade entegrasyonu: yerdeki seviyelendirilmiş silah veya zırha bakıldığında seviye ve XP bilgisini Jade panelinde gösterir
- İngilizce ve Türkçe metinler
- Yönetici komutları

## Kullanım

- Ayrıntılı tooltip: varsayılan `Sol Shift` (Controls ekranından değiştirilebilir)
- Geliştirme ekranı: envanterde imleci uygun silahın üstüne getirip varsayılan `U` tuşuna basın
- İlk vuruşta veya geliştirme ekranı açılırken silah verisi sunucuda oluşturulur
- Uygun bir silahın tooltip'i aktif seviye hasar bonusunu gösterir. Seviye 0 tabandır ve bonus vermez; yay ve diğer mermi silahları için aynı satır mermi hasarını belirtir.
- `ESC` → sağ üstteki `WL`: İlerleme, mob-öldürme XP'si, hasar eğrisi, harici silah algılama ve istemci gösterim ayarları. Sunucu ayarlarını yalnızca entegre sunucunun sahibi veya yetkili sunucu operatörü değiştirebilir; diğer oyuncular ekranı salt okunur görür.

## Miras Aktarım Masası

- End Kehribarı Cevheri End'in tüm yüksekliğinde (`Y=0–255`), en fazla sekiz blokluk ve havaya açıkken de korunabilen yaygın damarlar halinde oluşur. Her yeni End chunk'ında tüm yüksekliğe yayılan 25 denemeye ek olarak, vanilla End adalarının yüzey bandında (`Y=40–96`) 25 odaklı deneme daha yapılır. Normal veya yüksek fırında End Kehribarı Külçesine dönüştürülür.
- İlk Miras Aktarım Şablonu End City hazine sandıklarında `%5` ihtimalle bulunur. Ayrıca Bastion köprü, hoglin ahırı ve genel sandıklarında `%5`; Bastion hazine odası sandıklarında `%50` ihtimalle çıkar. Bu Bastion oranları, aynı sandıklardaki Netherite Yükseltme Şablonu oranlarının yarısıdır. Şablon sıfırdan üretilemez.
- Mevcut bir şablon, yedi End Kehribarı Külçesi ve bir End Taşı kullanılarak iki şablona çoğaltılır. Şablon üst ortada, End Taşı merkezde ve külçeler kalan yuvalarda yer alır.
- Miras Aktarım Masası üst sırada iki End Kehribarı Külçesi, altındaki iki sırada toplam dört tahta kullanılarak 2×3 düzende üretilir. Her vanilla tahta türü kullanılabilir.
- Masadaki işlem `şablon + seviyeli kaynak + hedef` biçimindedir. Şablon ile hedef tüketilir; kaynak eşya kaybolmaz ancak ilerlemesi sıfırlanır. Hedef daha önce XP kazanmışsa mevcut XP'si korunur ve kaynaktan aktarılan `%50` XP üzerine eklenir. Hedefin büyüleri, adı ile dayanıklılığı korunarak sonuç oluşturulur.
- Kaynağın seviyesi doğrudan yarıya bölünmez. Şimdiye kadar kazandığı toplam XP aşağı yuvarlanarak ikiye bölünür; hedefin seviyesi sunucunun etkin XP eğrisinden yeniden hesaplanır.
- Silah XP'si yalnızca başka bir silaha aktarılır. Zırh aktarımında kaynak ve hedef aynı ekipman yuvasına ait olmalıdır.
- End Kehribarı Cevheri veya Külçesi ile silah, araç ya da zırh üretilemez.

## Datapack etiketleri

- `#weaponleveling:levelable_weapons`
- `#weaponleveling:levelable_swords`
- `#weaponleveling:levelable_axes`
- `#weaponleveling:levelable_ranged_weapons`
- `#weaponleveling:excluded_weapons`

Hariç tutma etiketi her zaman önceliklidir. Modlu silahlar bu etiketlere eklenebilir; özel saldırı sistemleri için uyumluluk garantisi yoktur.

`#c:tools/melee_weapon`, `#c:tools/ranged_weapon`, `#c:tools/spear`, ArmorPlus veya başka bir mod yüklü değilse bu isteğe bağlı tag girdileri yok sayılır; modun açılışını engellemez. Mace doğrudan saldırısında düşüş bonusunu korur; trident hem yakın dövüşte hem atıldığında kapsanır. `BowItem`/`CrossbowItem` taban sınıflarından gelen silahlar otomatik kapsanır; `SPEAR` kullanım animasyonlu ve config'te eklenmiş spear'lar da projectile takibine alınır. Oyuncunun kullandığı uygun bir silahın oluşturduğu mermi, atış anındaki eşya örneğine bağlanır; yalnızca öldürme XP'si verir. AbstractArrow tabanlı spear projectile'ları doğrudan kaynak eşyanın ilerlemesini taşır; başka projectile tabanları için eşya envanterde tutuluyorsa benzersiz eşya kimliği üzerinden geri yazılır.

## Yönetici komutları

Komutlar izin seviyesi 2 ister ve hedef oyuncunun ana elindeki uygun silahı değiştirir:

```text
/weaponlevel get [player]
/weaponlevel set [player] <level>
/weaponlevel addlevel [player] <amount>
/weaponlevel setxp [player] <absolute-total-xp>
/weaponlevel addxp [player] <amount>
/weaponlevel reset [player] confirm
/weaponlevel reload
```

`[player]` yazılmazsa komut, komutu yazan oyuncunun ana elindeki silaha uygulanır; konsoldan hedef oyuncu zorunludur. `set`, `addlevel` ve `setxp`, seviye 0'a kadar indirilebilir ancak negatif seviye kabul etmez. `setxp` mutlak silah XP'sini kullanır; eğriye göre oluşan seviye ve o seviye içindeki XP'yi otomatik hesaplar. `addlevel` negatif miktarla seviyeyi düşürebilir. Seviye ve XP düşürme izinleri ayrı `[commands]` ayarlarıyla kapatılabilir; seçilmiş geliştirmeler korunur ve harcanmamış puan sıfırın altına düşmez.

## Derleme

Java 21 gerekir; Gradle toolchain gerekirse uygun JDK'yı otomatik çözer.

```powershell
.\gradlew.bat build
```

Çıktı `build/libs/weaponleveling-neoforge-1.21.1-1.0.0.jar` yolunda oluşur.

Bu çalışma alanının `Masaüstü` yolundaki Türkçe karakter, ModDevGradle 2.0.144 JUnit argüman dosyasında bozulabildiğinden `test` görevi ASCII adlı bir junction/çalışma yolu üzerinden çalıştırılmalıdır. Normal `compileJava` ve `build` bundan etkilenmez. Ayrıntılar [geliştirici belgesinde](docs/DEVELOPER.md) yer alır.

## Belgeler

- Ana gereksinim: [THE_LEVELING_TASARIM.md](THE_LEVELING_TASARIM.md)
- Mimari ve port rehberi: [docs/DEVELOPER.md](docs/DEVELOPER.md)
- Örnek ayarlar: [docs/config-examples](docs/config-examples)
- Güncel yayın metinleri ve yükleme bilgileri: [docs/release/1.0.0.md](docs/release/1.0.0.md)
- Sürüm değişiklik notları: [CHANGELOG.md](CHANGELOG.md)
