package dev.weaponleveling.core.model;

import java.util.Locale;

/** Permanent identity earned by a levelable item through long-term use. */
public enum LegacyTier {
    NONE,
    RELIC,
    LEGACY;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static LegacyTier fromSerializedName(String value) {
        if (value == null) {
            return NONE;
        }
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return NONE;
        }
    }

    public static LegacyTier highest(LegacyTier first, LegacyTier second) {
        return first.ordinal() >= second.ordinal() ? first : second;
    }
}
