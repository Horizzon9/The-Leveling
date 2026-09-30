package dev.weaponleveling.content;

import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.core.xp.ProgressionEngine;
import dev.weaponleveling.core.xp.XpCurve;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExperienceTransferServiceTest {
    private static final XpCurve DEFAULT_CURVE = new XpCurve(
            500, 100, 1.02432611896515, List.of());

    @Test
    void retainsExactlyHalfOfEvenTotalXp() {
        assertEquals(5_000_000L, ExperienceTransferService.retainedXp(10_000_000L));
    }

    @Test
    void floorsOddXpInsteadOfCreatingXp() {
        assertEquals(50L, ExperienceTransferService.retainedXp(101L));
    }

    @Test
    void recomputesLevelFromRetainedTotalXp() {
        long sourceXp = DEFAULT_CURVE.totalXpForLevel(50) + 1_000_000L;
        long retainedXp = ExperienceTransferService.retainedXp(sourceXp);

        WeaponProgress transferred = ProgressionEngine.setTotalXp(
                WeaponProgress.create(7.0), retainedXp, DEFAULT_CURVE);

        assertEquals(retainedXp, transferred.totalXp());
        assertEquals(levelFor(retainedXp), transferred.level());
    }

    @Test
    void addsTransferredXpToExistingTargetXp() {
        assertEquals(750L, ExperienceTransferService.combinedXp(500L, 250L));
    }

    @Test
    void saturatesCombinedXpInsteadOfOverflowing() {
        assertEquals(Long.MAX_VALUE, ExperienceTransferService.combinedXp(Long.MAX_VALUE - 5, 10));
    }

    private static int levelFor(long totalXp) {
        int level = 0;
        long remaining = totalXp;
        while (level < DEFAULT_CURVE.maxLevel() && remaining >= DEFAULT_CURVE.requiredXp(level)) {
            remaining -= DEFAULT_CURVE.requiredXp(level);
            level++;
        }
        return level;
    }
}
