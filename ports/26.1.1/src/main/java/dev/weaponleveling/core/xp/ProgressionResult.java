package dev.weaponleveling.core.xp;

import dev.weaponleveling.core.model.WeaponProgress;

public record ProgressionResult(WeaponProgress progress, int levelsGained, int pointsGained) {
}
