package dev.weaponleveling.core.chronicle;

/** Tier labels in the same style as vanilla enchantment levels. */
public final class RomanNumerals {
    private static final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    private RomanNumerals() {
    }

    /** Roman form of 1–3999; other values fall back to plain digits. */
    public static String of(int value) {
        if (value < 1 || value > 3999) {
            return Integer.toString(value);
        }
        StringBuilder result = new StringBuilder();
        int remaining = value;
        for (int index = 0; index < VALUES.length; index++) {
            while (remaining >= VALUES[index]) {
                result.append(SYMBOLS[index]);
                remaining -= VALUES[index];
            }
        }
        return result.toString();
    }
}
