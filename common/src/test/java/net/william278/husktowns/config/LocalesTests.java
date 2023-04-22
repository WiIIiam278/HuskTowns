/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns.config;

import net.william278.annotaml.Annotaml;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Objects;
import java.util.Set;

@DisplayName("Locales Tests")
public class LocalesTests {

    @Test
    @DisplayName("Test All Locale Keys Present")
    public void testAllLocalesPresent() {
        // Load locales/en-gb.yml as an InputStream
        try (InputStream localeStream = LocalesTests.class.getClassLoader().getResourceAsStream("locales/en-gb.yml")) {
            Assertions.assertNotNull(localeStream, "en-gb.yml is missing from the locales folder");
            final Locales englishLocales = Annotaml.create(Locales.class, localeStream).get();
            final Set<String> keys = englishLocales.rawLocales.keySet();

            // Iterate through every locale file in the locales folder
            URL url = LocalesTests.class.getClassLoader().getResource("locales");
            Assertions.assertNotNull(url, "locales folder is missing");

            for (File file : Objects.requireNonNull(new File(url.getPath()).listFiles(file -> file.getName().endsWith("yml")
                                                                                              && !file.getName().equals("en-gb.yml")))) {
                final Set<String> fileKeys = Annotaml.create(file, Locales.class).get().rawLocales.keySet();
                keys.forEach(key -> Assertions.assertTrue(fileKeys.contains(key),
                        "Locale key " + key + " is missing from " + file.getName()));
            }
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
