package net.william278.husktowns.config;

import de.themoep.minedown.adventure.MineDown;
import net.william278.annotaml.YamlFile;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃      HuskTowns Locales       ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ See plugin about menu for international locale credits
        ┣╸ Formatted in MineDown: https://github.com/Phoenix616/MineDown
        ┗╸ Translate HuskTowns: https://william278.net/docs/husktowns/translations""",
        rootedMap = true)
public class Locales {

    /**
     * The raw set of locales loaded from yaml
     */
    public Map<String, String> rawLocales = new HashMap<>();

    /**
     * Returns a raw, un-formatted locale loaded from the locales file
     *
     * @param localeId String identifier of the locale, corresponding to a key in the file
     * @return An {@link Optional} containing the locale corresponding to the id, if it exists
     */
    public Optional<String> getRawLocale(@NotNull String localeId) {
        return Optional.ofNullable(rawLocales.get(localeId)).map(StringEscapeUtils::unescapeJava);
    }

    /**
     * Returns a raw, un-formatted locale loaded from the locales file, with replacements applied
     * <p>
     * Note that replacements will not be MineDown-escaped; use {@link #escapeMineDown(String)} to escape replacements
     *
     * @param localeId     String identifier of the locale, corresponding to a key in the file
     * @param replacements Ordered array of replacement strings to fill in placeholders with
     * @return An {@link Optional} containing the replacement-applied locale corresponding to the id, if it exists
     */
    public Optional<String> getRawLocale(@NotNull String localeId, @NotNull String... replacements) {
        return getRawLocale(localeId).map(locale -> applyReplacements(locale, replacements));
    }

    /**
     * Returns a MineDown-formatted locale from the locales file
     *
     * @param localeId String identifier of the locale, corresponding to a key in the file
     * @return An {@link Optional} containing the formatted locale corresponding to the id, if it exists
     */
    public Optional<MineDown> getLocale(@NotNull String localeId) {
        return getRawLocale(localeId).map(MineDown::new);
    }

    /**
     * Returns a MineDown-formatted locale from the locales file, with replacements applied
     * <p>
     * Note that replacements will be MineDown-escaped before application
     *
     * @param localeId     String identifier of the locale, corresponding to a key in the file
     * @param replacements Ordered array of replacement strings to fill in placeholders with
     * @return An {@link Optional} containing the replacement-applied, formatted locale corresponding to the id, if it exists
     */
    public Optional<MineDown> getLocale(@NotNull String localeId, @NotNull String... replacements) {
        return getRawLocale(localeId, Arrays.stream(replacements).map(Locales::escapeMineDown)
                .toArray(String[]::new)).map(MineDown::new);
    }

    /**
     * Apply placeholder replacements to a raw locale
     *
     * @param rawLocale    The raw, unparsed locale
     * @param replacements Ordered array of replacement strings to fill in placeholders with
     * @return the raw locale, with inserted placeholders
     */
    @NotNull
    private String applyReplacements(@NotNull String rawLocale, @NotNull String... replacements) {
        int replacementIndexer = 1;
        for (String replacement : replacements) {
            String replacementString = "%" + replacementIndexer + "%";
            rawLocale = rawLocale.replace(replacementString, replacement);
            replacementIndexer += 1;
        }
        return rawLocale;
    }

    /**
     * Escape a string from {@link MineDown} formatting for use in a MineDown-formatted locale
     *
     * @param string The string to escape
     * @return The escaped string
     */
    @NotNull
    public static String escapeMineDown(@NotNull String string) {
        return MineDown.escape(StringEscapeUtils.unescapeJava(string));
    }

    /**
     * Formats a description string, wrapping text on whitespace after 40 characters
     *
     * @param string The string to format
     * @return The line-break formatted string, or a String literal {@code "N/A"} if the input string is empty
     */
    @NotNull
    public String formatTextField(@NotNull String string, int wrapAfter) {
        if (string.isBlank()) {
            return this.getRawLocale("not_applicable").orElse("N/A");
        }
        return WordUtils.wrap(string, 40, "\n", true);
    }

    @SuppressWarnings("unused")
    public Locales() {
    }

}
