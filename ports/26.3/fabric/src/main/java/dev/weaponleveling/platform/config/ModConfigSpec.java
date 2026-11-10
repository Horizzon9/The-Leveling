package dev.weaponleveling.platform.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.weaponleveling.WeaponLevelingMod;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * NeoForge'un `net.neoforged.neoforge.common.ModConfigSpec`'inin bu kod tabanindaki karsiligi.
 *
 * NEDEN AYNI SINIF/UYE ADLARI: ServerConfig ve ClientConfig toplam 30'dan fazla ayarı bu API ile
 * tanimlar ve mod genelinde yuzlerce `X.get()` cagrisi vardir. Ayni sekli koruyarak o dosyalarda
 * YALNIZCA import satiri degisti - varsayilanlar, araliklar, dogrulayicilar ve yorumlar birebir
 * ayni kaldi, yani davranis farki riski en aza indi.
 *
 * DOSYA BICIMI: NeoForge'un ModConfigSpec'i de NightConfig uzerine kuruludur ve ayni kutuphane
 * burada da kullanilir - uretilen TOML, NeoForge surumunun urettigiyle ayni bicimdedir (ayni
 * yorum satirlari, ayni noktali yollar). Boylece bir sunucu yoneticisi ayni config dosyasini iki
 * loader arasinda tasiyabilir.
 *
 * DEGERLERE ERISIM: NeoForge, config yuklenmeden once `get()` cagrilirsa istisna atar. Burada
 * bunun yerine varsayilan deger doner - mod kaydi ile config yuklemesi arasindaki dar pencerede
 * cokme yerine guvenli varsayilanla devam etmek Fabric'in baslatma sirasinda daha saglamdir.
 */
public final class ModConfigSpec {
    private final List<Entry<?>> entries;
    private Path file;

    private ModConfigSpec(List<Entry<?>> entries) {
        this.entries = List.copyOf(entries);
    }

    /**
     * Config dosyasini okur, eksik/gecersiz girdileri varsayilanla tamamlar ve sonucu diske yazar.
     * Yalnizca bir kez, mod baslatilirken cagrilir.
     */
    public void load(Path configFile) {
        this.file = configFile;
        try {
            java.nio.file.Files.createDirectories(configFile.getParent());
        } catch (java.io.IOException exception) {
            WeaponLevelingMod.LOGGER.error("Could not create config directory for {}", configFile, exception);
            return;
        }
        try (CommentedFileConfig config = CommentedFileConfig.builder(configFile).preserveInsertionOrder().sync().build()) {
            config.load();
            for (Entry<?> entry : entries) {
                entry.loadFrom(config);
            }
            writeAll(config);
            config.save();
        } catch (Exception exception) {
            // Bozuk bir TOML modun acilmasini engellememeli: bellekteki varsayilanlar gecerli kalir.
            WeaponLevelingMod.LOGGER.error("Could not read config {}, continuing with defaults", configFile, exception);
        }
    }

    /** Bellekteki guncel degerleri diske yazar (oyun ici ayar degisikliklerinden sonra cagrilir). */
    public void save() {
        if (file == null) {
            return;
        }
        try (CommentedFileConfig config = CommentedFileConfig.builder(file).preserveInsertionOrder().sync().build()) {
            config.load();
            writeAll(config);
            config.save();
        } catch (Exception exception) {
            WeaponLevelingMod.LOGGER.error("Could not save config {}", file, exception);
        }
    }

    private void writeAll(CommentedFileConfig config) {
        for (Entry<?> entry : entries) {
            entry.writeTo(config);
        }
    }

    // ---------------------------------------------------------------------------------------
    // Builder
    // ---------------------------------------------------------------------------------------

    public static final class Builder {
        private final List<Entry<?>> entries = new ArrayList<>();
        private String pendingComment;

        /** Bir sonraki tanimin uzerine yazilacak yorum satiri(lari). */
        public Builder comment(String... comment) {
            pendingComment = String.join(System.lineSeparator(), comment);
            return this;
        }

        public IntValue defineInRange(String path, int defaultValue, int min, int max) {
            return add(new IntValue(path, takeComment(), defaultValue, min, max));
        }

        public LongValue defineInRange(String path, long defaultValue, long min, long max) {
            return add(new LongValue(path, takeComment(), defaultValue, min, max));
        }

        public DoubleValue defineInRange(String path, double defaultValue, double min, double max) {
            return add(new DoubleValue(path, takeComment(), defaultValue, min, max));
        }

        public BooleanValue define(String path, boolean defaultValue) {
            return add(new BooleanValue(path, takeComment(), defaultValue));
        }

        public <T> ConfigValue<T> define(String path, T defaultValue) {
            return define(path, defaultValue, value -> true);
        }

        public <T> ConfigValue<T> define(String path, T defaultValue, Predicate<Object> validator) {
            return add(new ConfigValue<>(path, takeComment(), defaultValue, validator));
        }

        /**
         * Bos olmasina izin verilen liste. `newElement` yalnizca NeoForge'un duzenleme ekranlari
         * icindi; burada imza uyumlulugu adina kabul edilir ve kullanilmaz.
         */
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(
            String path,
            List<? extends T> defaultValue,
            Supplier<T> newElement,
            Predicate<Object> elementValidator
        ) {
            return add(new ListValue<>(path, takeComment(), defaultValue, elementValidator));
        }

        public ModConfigSpec build() {
            return new ModConfigSpec(entries);
        }

        private <V extends ConfigValue<?>> V add(V value) {
            entries.add((Entry<?>) value);
            return value;
        }

        private String takeComment() {
            String comment = pendingComment;
            pendingComment = null;
            return comment;
        }
    }

    // ---------------------------------------------------------------------------------------
    // Values
    // ---------------------------------------------------------------------------------------

    private interface Entry<T> {
        void loadFrom(CommentedFileConfig config);

        void writeTo(CommentedFileConfig config);
    }

    /** Tek bir ayar. NeoForge'daki gibi `get()`/`set()` sunar. */
    public static class ConfigValue<T> implements Entry<T> {
        private final String path;
        private final String comment;
        private final T defaultValue;
        private final Predicate<Object> validator;
        private volatile T value;

        ConfigValue(String path, String comment, T defaultValue, Predicate<Object> validator) {
            this.path = path;
            this.comment = comment;
            this.defaultValue = defaultValue;
            this.validator = validator;
            this.value = defaultValue;
        }

        public T get() {
            return value;
        }

        public void set(T newValue) {
            if (newValue != null && accepts(newValue)) {
                value = newValue;
            }
        }

        public T getDefault() {
            return defaultValue;
        }

        public String path() {
            return path;
        }

        /** Ham TOML degerini bu ayarin turune cevirir; cevrilemiyorsa null doner. */
        @SuppressWarnings("unchecked")
        T coerce(Object raw) {
            return (T) raw;
        }

        boolean accepts(Object candidate) {
            return validator.test(candidate);
        }

        @Override
        public void loadFrom(CommentedFileConfig config) {
            Object raw = config.get(path);
            if (raw == null) {
                return;
            }
            T coerced;
            try {
                coerced = coerce(raw);
            } catch (RuntimeException exception) {
                coerced = null;
            }
            if (coerced != null && accepts(coerced)) {
                value = coerced;
            } else {
                WeaponLevelingMod.LOGGER.warn("Config value {} is invalid ({}), using default {}",
                    path, raw, defaultValue);
            }
        }

        @Override
        public void writeTo(CommentedFileConfig config) {
            config.set(path, writeValue());
            if (comment != null) {
                config.setComment(path, " " + comment.replace(System.lineSeparator(),
                    System.lineSeparator() + " "));
            }
        }

        Object writeValue() {
            return value;
        }
    }

    public static final class BooleanValue extends ConfigValue<Boolean> {
        BooleanValue(String path, String comment, boolean defaultValue) {
            super(path, comment, defaultValue, candidate -> candidate instanceof Boolean);
        }

        @Override
        Boolean coerce(Object raw) {
            return raw instanceof Boolean bool ? bool : null;
        }
    }

    public static final class IntValue extends ConfigValue<Integer> {
        IntValue(String path, String comment, int defaultValue, int min, int max) {
            super(path, comment, defaultValue,
                candidate -> candidate instanceof Number number
                    && number.intValue() >= min && number.intValue() <= max);
        }

        @Override
        Integer coerce(Object raw) {
            return raw instanceof Number number ? number.intValue() : null;
        }
    }

    public static final class LongValue extends ConfigValue<Long> {
        LongValue(String path, String comment, long defaultValue, long min, long max) {
            super(path, comment, defaultValue,
                candidate -> candidate instanceof Number number
                    && number.longValue() >= min && number.longValue() <= max);
        }

        @Override
        Long coerce(Object raw) {
            return raw instanceof Number number ? number.longValue() : null;
        }
    }

    public static final class DoubleValue extends ConfigValue<Double> {
        DoubleValue(String path, String comment, double defaultValue, double min, double max) {
            super(path, comment, defaultValue,
                candidate -> candidate instanceof Number number
                    && number.doubleValue() >= min && number.doubleValue() <= max);
        }

        @Override
        Double coerce(Object raw) {
            return raw instanceof Number number ? number.doubleValue() : null;
        }
    }

    /**
     * Liste ayari. Dogrulayici HER ELEMANA uygulanir (NeoForge'un `defineListAllowEmpty`
     * davranisi); gecersiz eleman iceren liste tumuyle reddedilip varsayilana duser.
     */
    private static final class ListValue<T> extends ConfigValue<List<? extends T>> {
        private final Predicate<Object> elementValidator;

        ListValue(String path, String comment, List<? extends T> defaultValue,
                  Predicate<Object> elementValidator) {
            super(path, comment, defaultValue, candidate -> true);
            this.elementValidator = elementValidator;
        }

        @Override
        @SuppressWarnings("unchecked")
        List<? extends T> coerce(Object raw) {
            return raw instanceof List<?> list ? (List<? extends T>) List.copyOf(list) : null;
        }

        @Override
        boolean accepts(Object candidate) {
            if (!(candidate instanceof List<?> list)) {
                return false;
            }
            for (Object element : list) {
                if (!elementValidator.test(element)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        Object writeValue() {
            return Collections.unmodifiableList(new ArrayList<>(get()));
        }
    }
}
