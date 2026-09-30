package dev.weaponleveling.core.chronicle;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * One permanent moment in a weapon's history.
 *
 * @param kind    what happened
 * @param subject the entity type id the moment is about, or an empty string when it has none
 * @param detail  kind-specific number: the mastery tier for {@link Kind#MASTERY}, otherwise zero
 * @param day     the in-game day on which it happened
 */
public record ChronicleEntry(Kind kind, String subject, int detail, long day) {

    public enum Kind {
        FIRST_KILL,
        MASTERY,
        RELIC,
        LEGACY,
        TITLED;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        /** Empty for names written by a newer mod version, so old clients drop them instead of failing. */
        public static Optional<Kind> fromSerializedName(String value) {
            if (value == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(valueOf(value.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }
        }
    }

    public ChronicleEntry {
        Objects.requireNonNull(kind, "kind");
        subject = subject == null ? "" : subject;
        if (detail < 0 || day < 0) {
            throw new IllegalArgumentException("Chronicle entry contains a negative value");
        }
    }
}
