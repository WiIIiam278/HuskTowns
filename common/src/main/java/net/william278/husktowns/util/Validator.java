package net.william278.husktowns.util;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

/**
 * Validator utility class for validating user input
 */
public class Validator {

    private static final int MAX_TOWN_NAME_LENGTH = 16;
    private static final int MIN_TOWN_NAME_LENGTH = 3;
    private static final int MAX_TOWN_BIO_LENGTH = 256;

    private final HuskTowns plugin;

    public Validator(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    public boolean isValidTownName(@NotNull String name) {
        return (isAsciiOnly(name) || plugin.getSettings().allowUnicodeNames) && !containsWhitespace(name)
               && name.length() <= MAX_TOWN_NAME_LENGTH && name.length() >= MIN_TOWN_NAME_LENGTH;
    }

    public boolean isValidTownBio(@NotNull String bio) {
        return (isAsciiOnly(bio) || plugin.getSettings().allowUnicodeNames) && bio.length() <= MAX_TOWN_BIO_LENGTH;
    }

    private static boolean isAsciiOnly(@NotNull String string) {
        return string.matches("\\A\\p{ASCII}*\\z");
    }

    private static boolean containsWhitespace(@NotNull String string) {
        return string.matches(".*\\s.*");
    }

}
