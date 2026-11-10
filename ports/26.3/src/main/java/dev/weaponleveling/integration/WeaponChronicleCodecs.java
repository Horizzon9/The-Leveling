package dev.weaponleveling.integration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.weaponleveling.core.chronicle.ChronicleEntry;
import dev.weaponleveling.core.chronicle.ChronicleRecords;
import dev.weaponleveling.core.chronicle.WeaponChronicle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persistent and network form of {@link WeaponChronicle}.
 *
 * Decoding is deliberately forgiving. A component that fails to decode makes Minecraft discard the
 * whole item stack, so malformed, oversized or newer-version data is trimmed to what this version
 * understands instead of being rejected.
 */
public final class WeaponChronicleCodecs {
    private static final Codec<RawEntry> RAW_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("kind").forGetter(RawEntry::kind),
            Codec.STRING.optionalFieldOf("subject", "").forGetter(RawEntry::subject),
            Codec.INT.optionalFieldOf("detail", 0).forGetter(RawEntry::detail),
            Codec.LONG.optionalFieldOf("day", 0L).forGetter(RawEntry::day)
    ).apply(instance, RawEntry::new));

    private static final Codec<ChronicleRecords> RECORDS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("strongest_hit", 0.0).forGetter(ChronicleRecords::strongestHit),
            Codec.DOUBLE.optionalFieldOf("longest_shot", 0.0).forGetter(ChronicleRecords::longestShot),
            Codec.INT.optionalFieldOf("longest_streak", 0).forGetter(ChronicleRecords::longestStreak)
    ).apply(instance, ChronicleRecords::new));

    private static final Codec<WeaponChronicle> STORED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("bearer", "").forGetter(WeaponChronicle::bearer),
            Codec.STRING.optionalFieldOf("title", "").forGetter(WeaponChronicle::title),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("foes", Map.of())
                    .forGetter(WeaponChronicle::foeTally),
            RAW_ENTRY_CODEC.listOf().optionalFieldOf("entries", List.of())
                    .forGetter(chronicle -> chronicle.entries().stream().map(RawEntry::of).toList()),
            RECORDS_CODEC.optionalFieldOf("records", ChronicleRecords.EMPTY).forGetter(WeaponChronicle::records)
    ).apply(instance, WeaponChronicleCodecs::sanitize));

    public static final Codec<WeaponChronicle> CODEC = STORED_CODEC;

    private WeaponChronicleCodecs() {
    }

    private static WeaponChronicle sanitize(String bearer, String title, Map<String, Integer> foes,
                                            List<RawEntry> rawEntries, ChronicleRecords records) {
        String safeBearer = bearer.length() > WeaponChronicle.MAX_BEARER_LENGTH
                ? bearer.substring(0, WeaponChronicle.MAX_BEARER_LENGTH) : bearer;
        String safeTitle = WeaponChronicle.sanitizeTitle(title);
        Map<String, Integer> safeFoes = new LinkedHashMap<>();
        foes.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(WeaponChronicle.MAX_TRACKED_FOES)
                .forEach(entry -> safeFoes.put(entry.getKey(), entry.getValue()));
        List<ChronicleEntry> safeEntries = rawEntries.stream()
                .map(RawEntry::toEntry)
                .flatMap(Optional::stream)
                .limit(WeaponChronicle.MAX_ENTRIES)
                .toList();
        return new WeaponChronicle(safeBearer, safeTitle, safeFoes, safeEntries, records);
    }

    private record RawEntry(String kind, String subject, int detail, long day) {
        static RawEntry of(ChronicleEntry entry) {
            return new RawEntry(entry.kind().serializedName(), entry.subject(), entry.detail(), entry.day());
        }

        Optional<ChronicleEntry> toEntry() {
            return ChronicleEntry.Kind.fromSerializedName(kind)
                    .map(parsed -> new ChronicleEntry(parsed, subject, Math.max(0, detail), Math.max(0, day)));
        }
    }
}
