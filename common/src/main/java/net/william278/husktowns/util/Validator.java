package net.william278.husktowns.util;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

/**
 * Validator utility class for validating user input
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class Validator {

    public static final int MAX_TOWN_NAME_LENGTH = 16;
    public static final int MIN_TOWN_NAME_LENGTH = 3;
    public static final int MAX_TOWN_META_LENGTH = 256;

    private final HuskTowns plugin;

    public Validator(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if a town name is valid, including that it is not already in use
     *
     * @param name The town name to check
     * @return True if the town name is valid as per the plugin settings, false otherwise
     */
    public boolean isValidTownName(@NotNull String name) {
        return plugin.getTowns().stream().noneMatch(town -> town.getName().equalsIgnoreCase(name))
               && isLegalTownName(name);
    }

    /**
     * Check if a town name is valid
     *
     * @param name The town name to check
     * @return True if the town name is valid as per the plugin settings, false otherwise
     */
    public boolean isLegalTownName(@NotNull String name) {
        return (isAsciiOnly(name) || plugin.getSettings().doAllowUnicodeNames())
               && !containsWhitespace(name)
               && name.length() <= MAX_TOWN_NAME_LENGTH && name.length() >= MIN_TOWN_NAME_LENGTH
               && !name.equalsIgnoreCase(plugin.getSettings().getAdminTownName());
    }

    /**
     * Validate town bios, farewell messages, and greeting messages
     *
     * @param meta The meta to validate
     * @return Whether the meta is valid against the plugin settings
     */
    public boolean isValidTownMetadata(@NotNull String meta) {
        return (isAsciiOnly(meta) || plugin.getSettings().doAllowUnicodeMeta()) && meta.length() <= MAX_TOWN_META_LENGTH;
    }

    // Check if a string contains only ASCII characters
    private static boolean isAsciiOnly(@NotNull String string) {
        return string.matches("\\A\\p{ASCII}*\\z");
    }

    // Check if a string contains whitespace
    private static boolean containsWhitespace(@NotNull String string) {
        return string.matches(".*\\s.*");
    }

}
