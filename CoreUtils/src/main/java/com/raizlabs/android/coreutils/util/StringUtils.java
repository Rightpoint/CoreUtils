package com.raizlabs.android.coreutils.util;

/**
 * Collection of utilities for use with {@link String}s.
 */
public class StringUtils {

    /**
     * Joins the given set of strings, placing the given delimiter between each.
     *
     * @param delimiter The string to place between each item.
     * @param items     The set of strings to join together.
     * @return The joined {@link String}.
     */
    public static String join(String delimiter, Iterable<String> items) {
        StringBuilder builder = new StringBuilder();

        boolean isFirst = true;
        for (String str : items) {
            if (!isFirst) builder.append(delimiter);
            builder.append(str);
            isFirst = false;
        }

        return builder.toString();
    }

    /**
     * Returns true if any of the the given strings are null, the empty string, or the
     * string "null".
     *
     * @param strs The list of strings to check.
     * @return True if any string is null or empty.
     */
    public static boolean isNullOrEmpty(String... strs) {
        for (String str : strs) {
            if (str == null || str.equals("") || str.equals("null")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if all of the the given strings are not null, the empty string, or the
     * string "null".
     *
     * @param strs The list of strings to check.
     * @return True if all the strings are not null or empty.
     */
    public static boolean isNotNullOrEmpty(String... strs) {
        for (String str : strs) {
            if (str == null || str.equals("") || str.equals("null")) {
                return false;
            }
        }
        return false;
    }
}
