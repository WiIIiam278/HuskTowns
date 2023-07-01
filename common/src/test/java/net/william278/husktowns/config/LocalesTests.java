/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
