package dev.weaponleveling.core.combat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Server-side kill-XP scaling shared by vanilla and modded living entities. */
public final class MobXpBalance {
    private static final double MINIMUM_MULTIPLIER = 0.05;

    // NeoForge's ConfigValue caches and returns the same List instance until the config reloads,
    // so a reference-equality check is enough to know the parsed override map is still valid;
    // this turns kill-XP lookups from an O(entries) string-parse per kill into an O(1) map get.
    private static List<? extends String> cachedOverridesSource = null;
    private static Map<String, Double> cachedOverrides = Map.of();

    private MobXpBalance() {
    }

    public static double multiplier(String entityId, double maxHealth, double healthBaseline,
                                    List<? extends String> configuredOverrides) {
        Double configured = configuredOverrides(configuredOverrides).get(entityId);
        if (configured != null) {
            return configured;
        }
        return healthMultiplier(maxHealth, healthBaseline);
    }

    static double healthMultiplier(double maxHealth, double healthBaseline) {
        double safeBaseline = Double.isFinite(healthBaseline) && healthBaseline > 0 ? healthBaseline : 20.0;
        double safeMaxHealth = Double.isFinite(maxHealth) && maxHealth > 0 ? maxHealth : safeBaseline;
        return Math.max(MINIMUM_MULTIPLIER, safeMaxHealth / safeBaseline);
    }

    private static Map<String, Double> configuredOverrides(List<? extends String> entries) {
        if (entries == cachedOverridesSource) {
            return cachedOverrides;
        }
        Map<String, Double> parsed = parseOverrides(entries);
        cachedOverridesSource = entries;
        cachedOverrides = parsed;
        return parsed;
    }

    private static Map<String, Double> parseOverrides(List<? extends String> entries) {
        if (entries.isEmpty()) {
            return Map.of();
        }
        Map<String, Double> result = new HashMap<>();
        for (String entry : entries) {
            int separator = entry.lastIndexOf('=');
            if (separator <= 0) {
                continue;
            }
            String entityId = entry.substring(0, separator);
            if (result.containsKey(entityId)) {
                // The first valid value for a given entity id wins, matching the previous linear scan.
                continue;
            }
            try {
                double multiplier = Double.parseDouble(entry.substring(separator + 1));
                if (Double.isFinite(multiplier) && multiplier >= 0) {
                    result.put(entityId, multiplier);
                }
            } catch (NumberFormatException ignored) {
                // The config validator normally prevents this; fallback balance remains safe if a stale value appears.
            }
        }
        return result;
    }

}
