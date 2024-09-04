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

package net.william278.husktowns.util;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.config.Settings;
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
     * Check if a town name is valid, including that it is not yet in use
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
        final Settings.TownSettings settings = plugin.getSettings().getTowns();
        return (name.matches(settings.getTownNameRegex()) || !settings.isRestrictTownNames())
            && !containsWhitespace(name)
            && name.length() <= MAX_TOWN_NAME_LENGTH && name.length() >= MIN_TOWN_NAME_LENGTH
            && plugin.getSettings().getGeneral().isTownNameAllowed(name)
            && !name.equalsIgnoreCase(settings.getAdminTown().getName());
    }

    /**
     * Validate town bios, farewell messages, and greeting messages
     *
     * @param meta The meta to validate
     * @return Whether the meta is valid against the plugin settings
     */
    public boolean isValidTownMetadata(@NotNull String meta) {
        final Settings.TownSettings settings = plugin.getSettings().getTowns();
        return (meta.matches(settings.getTownMetaRegex()) || !settings.isRestrictTownBios())
            && meta.length() <= MAX_TOWN_META_LENGTH;
    }

    // Check if a string contains whitespace
    private static boolean containsWhitespace(@NotNull String string) {
        return string.matches(".*\\s.*");
    }

}
