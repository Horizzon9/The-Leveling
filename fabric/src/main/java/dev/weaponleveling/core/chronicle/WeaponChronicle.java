package dev.weaponleveling.core.chronicle;

import dev.weaponleveling.core.model.LegacyTier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The history an item writes for itself: who first carried it into battle, the name it earned,
 * which creatures it has faced most, and the moments worth remembering. Immutable; every change
 * returns a new value.
 *
 * Weapons count creatures they defeated; armor counts hits it endured from each creature type.
 * Storage stays bounded no matter how long an item lives. At most {@link #MAX_TRACKED_FOES}
 * creature types are counted. When a new type appears and every slot is taken, the least-faced
 * type that has not yet earned a mastery tier makes room; mastered types are never forgotten.
 *
 * @param bearer   name of the first player who used the item in battle, or empty
 * @param title    permanent name given to a Relic or Legacy item, or empty
 * @param foeTally encounters per entity type id
 * @param entries  notable moments, oldest first
 * @param records  personal bests set by the item
 */
public record WeaponChronicle(String bearer, String title, Map<String, Integer> foeTally,
                              List<ChronicleEntry> entries, ChronicleRecords records) {
    public static final int MAX_TRACKED_FOES = 16;
    public static final int MAX_ENTRIES = 24;
    public static final int MAX_BEARER_LENGTH = 32;
    public static final int MAX_TITLE_LENGTH = 32;
    public static final WeaponChronicle EMPTY = new WeaponChronicle("", "", Map.of(), List.of(),
            ChronicleRecords.EMPTY);

    private static final Comparator<Map.Entry<String, Integer>> MOST_FACED_FIRST =
            Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey());

    public WeaponChronicle {
        bearer = bearer == null ? "" : bearer;
        title = title == null ? "" : title;
        foeTally = Map.copyOf(foeTally);
        entries = List.copyOf(entries);
        records = records == null ? ChronicleRecords.EMPTY : records;
        if (bearer.length() > MAX_BEARER_LENGTH || title.length() > MAX_TITLE_LENGTH
                || foeTally.size() > MAX_TRACKED_FOES || entries.size() > MAX_ENTRIES) {
            throw new IllegalArgumentException("Weapon chronicle exceeds its storage bounds");
        }
        if (foeTally.values().stream().anyMatch(count -> count == null || count <= 0)) {
            throw new IllegalArgumentException("Foe tallies must be positive");
        }
    }

    /**
     * Reduces player input to a displayable title: formatting codes and control characters are
     * removed, whitespace is collapsed, and the result is cut to {@link #MAX_TITLE_LENGTH}.
     */
    public static String sanitizeTitle(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.replaceAll("\u00A7.?", "")
                .replaceAll("\\p{Cntrl}", "")
                .replaceAll("\\s+", " ")
                .strip();
        return cleaned.length() > MAX_TITLE_LENGTH ? cleaned.substring(0, MAX_TITLE_LENGTH).strip() : cleaned;
    }

    public boolean isEmpty() {
        return bearer.isEmpty() && title.isEmpty() && foeTally.isEmpty() && entries.isEmpty() && records.isEmpty();
    }

    public boolean hasTitle() {
        return !title.isEmpty();
    }

    public int killsOf(String foeId) {
        return foeTally.getOrDefault(foeId, 0);
    }

    /** Highest mastery tier this item has recorded against the foe, independent of current config. */
    public int recordedMasteryTier(String foeId) {
        return entries.stream()
                .filter(entry -> entry.kind() == ChronicleEntry.Kind.MASTERY && entry.subject().equals(foeId))
                .mapToInt(ChronicleEntry::detail)
                .max()
                .orElse(0);
    }

    public List<Map.Entry<String, Integer>> favoredFoes(int limit) {
        return foeTally.entrySet().stream()
                .sorted(MOST_FACED_FIRST)
                .limit(Math.max(0, limit))
                .toList();
    }

    public Optional<ChronicleEntry> first(ChronicleEntry.Kind kind) {
        return entries.stream().filter(entry -> entry.kind() == kind).findFirst();
    }

    public WeaponChronicle withBearer(String playerName) {
        if (!bearer.isEmpty() || playerName == null || playerName.isBlank()) {
            return this;
        }
        String trimmed = playerName.strip();
        return new WeaponChronicle(trimmed.length() > MAX_BEARER_LENGTH
                ? trimmed.substring(0, MAX_BEARER_LENGTH) : trimmed, title, foeTally, entries, records);
    }

    public WeaponChronicle withRecords(ChronicleRecords updated) {
        return updated == null || updated.equals(records) ? this
                : new WeaponChronicle(bearer, title, foeTally, entries, updated);
    }

    /** Gives the item its name and remembers the day. Renaming replaces the previous name and day. */
    public WeaponChronicle withTitle(String newTitle, long day) {
        String sanitized = sanitizeTitle(newTitle);
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("A title needs at least one visible character");
        }
        List<ChronicleEntry> newEntries = new ArrayList<>(entries);
        newEntries.removeIf(entry -> entry.kind() == ChronicleEntry.Kind.TITLED);
        newEntries.add(new ChronicleEntry(ChronicleEntry.Kind.TITLED, "", 0, Math.max(0, day)));
        trimOldestMastery(newEntries);
        return new WeaponChronicle(bearer, sanitized, foeTally, newEntries, records);
    }

    /**
     * Records one kill made by a weapon, including its first-kill milestone.
     *
     * @param mastery rules deciding whether the kill crosses a mastery tier; {@link FoeMastery#DISABLED}
     *                keeps counting without ever awarding a tier
     */
    public KillOutcome recordKill(String foeId, long day, FoeMastery mastery) {
        Objects.requireNonNull(foeId, "foeId");
        List<ChronicleEntry> newEntries = new ArrayList<>(entries);
        if (newEntries.stream().noneMatch(entry -> entry.kind() == ChronicleEntry.Kind.FIRST_KILL)) {
            newEntries.add(new ChronicleEntry(ChronicleEntry.Kind.FIRST_KILL, foeId, 0, Math.max(0, day)));
        }
        return tally(foeId, day, mastery, newEntries);
    }

    /** Records one hit endured by armor. Armor has no first-kill milestone, only encounters and mastery. */
    public KillOutcome recordHit(String foeId, long day, FoeMastery mastery) {
        Objects.requireNonNull(foeId, "foeId");
        return tally(foeId, day, mastery, new ArrayList<>(entries));
    }

    /** Remembers the day a Relic or Legacy identity was earned. Later demotions never erase it. */
    public WeaponChronicle recordIdentity(LegacyTier tier, long day) {
        ChronicleEntry.Kind kind = switch (Objects.requireNonNull(tier, "tier")) {
            case RELIC -> ChronicleEntry.Kind.RELIC;
            case LEGACY -> ChronicleEntry.Kind.LEGACY;
            case NONE -> null;
        };
        if (kind == null || first(kind).isPresent()) {
            return this;
        }
        List<ChronicleEntry> newEntries = new ArrayList<>(entries);
        newEntries.add(new ChronicleEntry(kind, "", 0, Math.max(0, day)));
        trimOldestMastery(newEntries);
        return new WeaponChronicle(bearer, title, foeTally, newEntries, records);
    }

    private KillOutcome tally(String foeId, long day, FoeMastery mastery, List<ChronicleEntry> newEntries) {
        Objects.requireNonNull(mastery, "mastery");
        Map<String, Integer> newTally = new HashMap<>(foeTally);
        int previous = killsOf(foeId);
        boolean tracked = newTally.containsKey(foeId) || makeRoom(newTally, mastery);
        int reachedTier = 0;
        if (tracked) {
            int count = previous == Integer.MAX_VALUE ? previous : previous + 1;
            newTally.put(foeId, count);
            int tier = mastery.tierFor(count);
            if (tier > mastery.tierFor(previous)) {
                reachedTier = tier;
                // Only the highest tier per foe is kept, so mastery entries stay bounded by tracked foes.
                newEntries.removeIf(entry -> entry.kind() == ChronicleEntry.Kind.MASTERY
                        && entry.subject().equals(foeId));
                newEntries.add(new ChronicleEntry(ChronicleEntry.Kind.MASTERY, foeId, tier, Math.max(0, day)));
            }
        }
        trimOldestMastery(newEntries);
        return new KillOutcome(new WeaponChronicle(bearer, title, newTally, newEntries, records), reachedTier);
    }

    private boolean makeRoom(Map<String, Integer> tally, FoeMastery mastery) {
        if (tally.size() < MAX_TRACKED_FOES) {
            return true;
        }
        Optional<String> evictable = tally.entrySet().stream()
                .filter(entry -> mastery.tierFor(entry.getValue()) == 0 && recordedMasteryTier(entry.getKey()) == 0)
                .min(Map.Entry.<String, Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey);
        evictable.ifPresent(tally::remove);
        return evictable.isPresent();
    }

    private static void trimOldestMastery(List<ChronicleEntry> entries) {
        while (entries.size() > MAX_ENTRIES) {
            int oldestMastery = -1;
            for (int index = 0; index < entries.size(); index++) {
                if (entries.get(index).kind() == ChronicleEntry.Kind.MASTERY) {
                    oldestMastery = index;
                    break;
                }
            }
            entries.remove(oldestMastery >= 0 ? oldestMastery : 0);
        }
    }

    /**
     * @param chronicle   the updated history
     * @param reachedTier the mastery tier this encounter unlocked, or zero when it unlocked none
     */
    public record KillOutcome(WeaponChronicle chronicle, int reachedTier) {
    }
}
