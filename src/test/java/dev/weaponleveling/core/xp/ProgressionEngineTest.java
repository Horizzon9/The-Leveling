package dev.weaponleveling.core.xp;

import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.core.model.LegacyTier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProgressionEngineTest {
    @Test
    void crossesMultipleLevelsWithoutUpgradePoints() {
        var curve = new XpCurve(100, 7, 1.0323, List.of());
        var result = ProgressionEngine.addXp(WeaponProgress.create(6), 30, curve);

        assertEquals(4, result.progress().level());
        assertEquals(4, result.levelsGained());
        assertEquals(0, result.progress().unspentPoints());
    }

    @Test
    void capsAtMaximumLevelAndStopsCurrentXp() {
        var curve = new XpCurve(3, 7, 1.0323, List.of());
        var result = ProgressionEngine.addXp(WeaponProgress.create(6), Long.MAX_VALUE, curve);

        assertEquals(3, result.progress().level());
        assertEquals(0, result.progress().currentXp());
    }

    @Test
    void explicitTableOverridesFormula() {
        var curve = new XpCurve(4, 7, 1.0323, List.of(10L, 20L, 30L));

        assertEquals(10, curve.requiredXp(0));
        assertEquals(20, curve.requiredXp(1));
    }

    @Test
    void totalXpSelectsTheCorrespondingLevelAndProgress() {
        var curve = new XpCurve(5, 10, 1.0, List.of());
        var result = ProgressionEngine.setTotalXp(WeaponProgress.create(6), 27, curve);

        assertEquals(2, result.level());
        assertEquals(7, result.currentXp());
        assertEquals(27, result.totalXp());
        assertEquals(0, result.unspentPoints());
    }

    @Test
    void transferredProgressUsesExactlyHalfOfTheSourceTotalXp() {
        var curve = new XpCurve(5, 10, 1.0, List.of());
        long transferredXp = 50 / 2;
        var result = ProgressionEngine.setTotalXp(WeaponProgress.create(6), transferredXp, curve);

        assertEquals(25, result.totalXp());
        assertEquals(2, result.level());
        assertEquals(5, result.currentXp());
    }

    @Test
    void loweringLevelCanReturnToZeroButNeverBelowIt() {
        var curve = new XpCurve(5, 10, 1.0, List.of());
        var levelFour = ProgressionEngine.setLevel(WeaponProgress.create(6), 4, curve);
        var lowered = ProgressionEngine.setLevel(levelFour.withProgress(4, 0, 30, 0), 0, curve);

        assertEquals(0, lowered.level());
        assertEquals(0, lowered.currentXp());
        assertEquals(0, lowered.totalXp());
        assertEquals(0, lowered.unspentPoints());
    }

    @Test
    void exactLevelZeroIsTheValidStartingLevel() {
        var curve = new XpCurve(5, 10, 1.0, List.of());

        var result = ProgressionEngine.setLevel(WeaponProgress.create(6), 0, curve);

        assertEquals(0, result.level());
        assertEquals(0, result.totalXp());
    }

    @Test
    void fiveHundredLevelDefaultCurveStartsAtZeroAndReachesTheReleaseTarget() {
        var curve = new XpCurve(500, 100, 1.02432611896515, List.of());

        assertEquals(100, curve.requiredXp(0));
        assertEquals(124, curve.requiredXp(9));
        assertEquals(1_080, curve.requiredXp(99));
        assertEquals(680_844_514L, curve.totalXpForLevel(500));
    }

    @Test
    void grantsRelicAndLegacyIdentityAtConfiguredPercentages() {
        var curve = new XpCurve(100, 10, 1.0, List.of());
        var policy = new LegacyPolicy(25, 100);

        var relic = ProgressionEngine.setLevel(WeaponProgress.create(6), 25, curve, policy);
        var legacy = ProgressionEngine.setLevel(relic, 100, curve, policy);

        assertEquals(LegacyTier.RELIC, relic.legacyTier());
        assertEquals(LegacyTier.LEGACY, legacy.legacyTier());
    }

    @Test
    void earnedIdentityDoesNotDisappearWhenLevelIsReduced() {
        var curve = new XpCurve(100, 10, 1.0, List.of());
        var relic = ProgressionEngine.setLevel(WeaponProgress.create(6), 25, curve);

        var lowered = ProgressionEngine.setLevel(relic, 0, curve);

        assertEquals(0, lowered.level());
        assertEquals(LegacyTier.RELIC, lowered.legacyTier());
    }

    @Test
    void resettingAnItemStartsACompletelyNewIdentity() {
        var curve = new XpCurve(100, 10, 1.0, List.of());
        var legacy = ProgressionEngine.setLevel(WeaponProgress.create(6), 100, curve);

        var reset = legacy.reset();

        assertEquals(0, reset.level());
        assertEquals(LegacyTier.NONE, reset.legacyTier());
    }
}
