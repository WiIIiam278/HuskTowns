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

package net.william278.husktowns.claim;

import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;
import net.william278.cloplib.operation.OperationType;
import net.william278.husktowns.config.Flags;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Claim rules, defining what players can do in a claim
 */
public class Rules {

    @Expose
    private Map<String, Boolean> flags;

    private Rules(@NotNull Map<String, Boolean> flags) {
        this.flags = flags;
    }

    /**
     * Create a new Rules instance from a map of flag names to their respective values
     * *
     *
     * @param rules the map of flag IDs to create from
     * @return the new Rules instance
     */
    @NotNull
    public static Rules from(@NotNull Map<String, Boolean> rules) {
        return new Rules(Maps.newLinkedHashMap(rules));
    }

    /**
     * Create a new Rules instance from a {@link Flag}-value map
     *
     * @param rules the rules map to create from
     * @return the new Rules instance
     */
    @NotNull
    public static Rules of(@NotNull Map<Flag, Boolean> rules) {
        return new Rules(rules.entrySet().stream()
                .collect(Collectors.toMap(
                        f -> f.getKey().getName().toLowerCase(Locale.ENGLISH),
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new
                )));
    }

    @SuppressWarnings("unused")
    private Rules() {
    }

    /**
     * Set the value of a flag
     *
     * @param flag  the flag to set
     * @param value the value to set the flag to
     */
    public void setFlag(@NotNull Flag flag, boolean value) {
        if (flags.containsKey(flag.getName())) {
            flags.replace(flag.getName(), value);
        } else {
            flags.put(flag.getName(), value);
        }
    }

    /**
     * Get the map of {@link Flag}s to their respective values
     * <p>
     * Any flags set in the town's rules that aren't defined on this server will be ignored.
     *
     * @return the map of flags to their respective values
     */
    @NotNull
    public Map<Flag, Boolean> getFlagMap(@NotNull Flags flagConfig) {
        return flags.entrySet().stream()
                .filter(f -> flagConfig.getFlag(f.getKey()).isPresent())
                .collect(Collectors.toMap(
                        f -> flagConfig.getFlag(f.getKey()).orElseThrow(),
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
    }

    /**
     * Whether, for the given operation, the flag rules set indicate it should be cancelled
     *
     * @param type       the operation type that is being performed in a region governed by these rules
     * @param flagConfig the flag configuration to use
     * @return Whether the operation should be canceled:
     * <p>
     * {@code true} if no flags have been set to {@code true} that permit the operation; {@code false} otherwise
     */
    public boolean cancelOperation(@NotNull OperationType type, @NotNull Flags flagConfig) {
        return getFlagMap(flagConfig).entrySet().stream()
                .filter(Map.Entry::getValue)
                .noneMatch(entry -> entry.getKey().isOperationAllowed(type));
    }

}
