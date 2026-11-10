package dev.weaponleveling.core.chronicle;

import dev.weaponleveling.core.model.LegacyTier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeaponChronicleTest {
    private static final FoeMastery MASTERY = new FoeMastery(List.of(2, 4), 0.05);
    private static final String FORMAT = String.valueOf((char) 0xA7);
    private static final String NEWLINE = String.valueOf((char) 10);

    @Test
    void firstKillIsRecordedOnceAndCountsPerFoe() {
        WeaponChronicle chronicle = WeaponChronicle.EMPTY
                .recordKill("minecraft:zombie", 3, MASTERY).chronicle()
                .recordKill("minecraft:skeleton", 5, MASTERY).chronicle();

        assertEquals(1, chronicle.killsOf("minecraft:zombie"));
        assertEquals(1, chronicle.killsOf("minecraft:skeleton"));
        ChronicleEntry firstKill = chronicle.first(ChronicleEntry.Kind.FIRST_KILL).orElseThrow();
        assertEquals("minecraft:zombie", firstKill.subject());
        assertEquals(3, firstKill.day());
        assertEquals(1, chronicle.entries().stream()
                .filter(entry -> entry.kind() == ChronicleEntry.Kind.FIRST_KILL).count());
    }

    @Test
    void crossingAThresholdReportsTheTierAndKeepsOnlyTheHighestEntry() {
        WeaponChronicle chronicle = WeaponChronicle.EMPTY;
        int[] reached = new int[4];
        for (int kill = 0; kill < 4; kill++) {
            WeaponChronicle.KillOutcome outcome = chronicle.recordKill("minecraft:zombie", kill, MASTERY);
            reached[kill] = outcome.reachedTier();
            chronicle = outcome.chronicle();
        }

        assertEquals(0, reached[0]);
        assertEquals(1, reached[1]);
        assertEquals(0, reached[2]);
        assertEquals(2, reached[3]);
        assertEquals(2, chronicle.recordedMasteryTier("minecraft:zombie"));
        assertEquals(1, chronicle.entries().stream()
                .filter(entry -> entry.kind() == ChronicleEntry.Kind.MASTERY).count());
    }

    @Test
    void disabledMasteryStillCountsButNeverAwardsTiers() {
        WeaponChronicle chronicle = WeaponChronicle.EMPTY;
        for (int kill = 0; kill < 10; kill++) {
            WeaponChronicle.KillOutcome outcome = chronicle.recordKill("minecraft:player", 0,
                    FoeMastery.DISABLED);
            assertEquals(0, outcome.reachedTier());
            chronicle = outcome.chronicle();
        }
        assertEquals(10, chronicle.killsOf("minecraft:player"));
        assertEquals(0, chronicle.recordedMasteryTier("minecraft:player"));
    }

    @Test
    void fullTallyEvictsTheLeastHuntedUnmasteredFoe() {
        WeaponChronicle chronicle = WeaponChronicle.EMPTY;
        chronicle = killTimes(chronicle, "mod:mastered", 2);
        for (int foe = 1; foe < WeaponChronicle.MAX_TRACKED_FOES; foe++) {
            chronicle = killTimes(chronicle, "mod:foe_" + foe, foe == 1 ? 1 : 3 + foe);
        }
        assertEquals(WeaponChronicle.MAX_TRACKED_FOES, chronicle.foeTally().size());

        chronicle = killTimes(chronicle, "mod:newcomer", 1);

        assertEquals(WeaponChronicle.MAX_TRACKED_FOES, chronicle.foeTally().size());
        assertEquals(1, chronicle.killsOf("mod:newcomer"));
        assertEquals(0, chronicle.killsOf("mod:foe_1"));
        assertEquals(2, chronicle.killsOf("mod:mastered"));
    }

    @Test
    void masteredFoesAreNeverEvicted() {
        WeaponChronicle chronicle = WeaponChronicle.EMPTY;
        for (int foe = 0; foe < WeaponChronicle.MAX_TRACKED_FOES; foe++) {
            chronicle = killTimes(chronicle, "mod:foe_" + foe, 2);
        }

        WeaponChronicle.KillOutcome outcome = chronicle.recordKill("mod:newcomer", 0, MASTERY);

        assertEquals(0, outcome.chronicle().killsOf("mod:newcomer"));
        assertEquals(WeaponChronicle.MAX_TRACKED_FOES, outcome.chronicle().foeTally().size());
    }

    @Test
    void favoredFoesAreSortedByKillsThenId() {
        WeaponChronicle chronicle = killTimes(killTimes(killTimes(WeaponChronicle.EMPTY,
                "minecraft:skeleton", 1), "minecraft:zombie", 3), "minecraft:creeper", 1);

        List<String> order = chronicle.favoredFoes(3).stream().map(java.util.Map.Entry::getKey).toList();

        assertEquals(List.of("minecraft:zombie", "minecraft:creeper", "minecraft:skeleton"), order);
    }

    @Test
    void bearerIsSetOnlyOnce() {
        WeaponChronicle chronicle = WeaponChronicle.EMPTY.withBearer("Alex").withBearer("Steve");

        assertEquals("Alex", chronicle.bearer());
        assertSame(chronicle, chronicle.withBearer(" "));
    }

    @Test
    void identityDaysAreKeptAfterTheFirstPromotion() {
        WeaponChronicle chronicle = WeaponChronicle.EMPTY
                .recordIdentity(LegacyTier.RELIC, 10)
                .recordIdentity(LegacyTier.RELIC, 20)
                .recordIdentity(LegacyTier.LEGACY, 30)
                .recordIdentity(LegacyTier.NONE, 40);

        assertEquals(10, chronicle.first(ChronicleEntry.Kind.RELIC).orElseThrow().day());
        assertEquals(30, chronicle.first(ChronicleEntry.Kind.LEGACY).orElseThrow().day());
        assertEquals(2, chronicle.entries().size());
    }

    @Test
    void emptyChronicleReportsEmpty() {
        assertTrue(WeaponChronicle.EMPTY.isEmpty());
        assertFalse(WeaponChronicle.EMPTY.withBearer("Alex").isEmpty());
    }

    @Test
    void rejectsValuesOutsideStorageBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> new WeaponChronicle("", "", java.util.Map.of("minecraft:zombie", 0), List.of(),
                        ChronicleRecords.EMPTY));
    }

    @Test
    void titlesAreSanitizedAndRecordedOnce() {
        String messy = "  " + FORMAT + "cShadow" + NEWLINE + "   Reaper  ";
        WeaponChronicle named = WeaponChronicle.EMPTY.withTitle(messy, 4).withTitle("Dawnbreaker", 9);

        assertEquals("Dawnbreaker", named.title());
        assertEquals(9, named.first(ChronicleEntry.Kind.TITLED).orElseThrow().day());
        assertEquals(1, named.entries().size());
        assertEquals("Shadow Reaper", WeaponChronicle.sanitizeTitle(messy));
        assertEquals(WeaponChronicle.MAX_TITLE_LENGTH, WeaponChronicle.sanitizeTitle("x".repeat(80)).length());
        assertThrows(IllegalArgumentException.class,
                () -> WeaponChronicle.EMPTY.withTitle(FORMAT + "a  ", 0));
    }

    @Test
    void armorHitsCountTowardMasteryWithoutAFirstKill() {
        WeaponChronicle.KillOutcome first = WeaponChronicle.EMPTY.recordHit("minecraft:zombie", 0, MASTERY);
        WeaponChronicle.KillOutcome second = first.chronicle().recordHit("minecraft:zombie", 0, MASTERY);

        assertEquals(2, second.chronicle().killsOf("minecraft:zombie"));
        assertEquals(1, second.reachedTier());
        assertTrue(second.chronicle().first(ChronicleEntry.Kind.FIRST_KILL).isEmpty());
    }

    @Test
    void recordsOnlyEverImprove() {
        ChronicleRecords records = ChronicleRecords.EMPTY.withFeat(12.5, 0, 1).withFeat(8, 40, 3);

        assertEquals(12.5, records.strongestHit());
        assertEquals(40, records.longestShot());
        assertEquals(3, records.longestStreak());
        assertSame(records, records.withFeat(1, 1, 1));
        assertEquals(0, ChronicleRecords.EMPTY.withFeat(Double.NaN, -5, -1).strongestHit());
        assertSame(WeaponChronicle.EMPTY, WeaponChronicle.EMPTY.withRecords(ChronicleRecords.EMPTY));
        assertFalse(WeaponChronicle.EMPTY.withRecords(records).isEmpty());
    }

    private static WeaponChronicle killTimes(WeaponChronicle chronicle, String foe, int times) {
        WeaponChronicle result = chronicle;
        for (int kill = 0; kill < times; kill++) {
            result = result.recordKill(foe, 0, MASTERY).chronicle();
        }
        return result;
    }
}
