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

import com.google.gson.annotations.Expose;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.william278.cloplib.operation.OperationType;
import net.william278.husktowns.config.Flags;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Claim rules, defining what players can do in a claim
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Rules {

    @Expose
    private Map<String, Boolean> flags;

    @Expose(deserialize = false, serialize = false)
    private Map<Flag, Boolean> calculatedFlags = null;

    private Rules(@NotNull Map<String, Boolean> flags) {
        this.flags = flags;
    }

    @NotNull
    private static Map<Flag, Boolean> getMapped(@NotNull Map<String, Boolean> flags, @NotNull Flags flagConfig) {
        return flags.entrySet().stream()
            .filter(f -> flagConfig.getFlag(f.getKey()).isPresent())
            .collect(Collectors.toMap(
                f -> flagConfig.getFlag(f.getKey()).orElseThrow(),
                Map.Entry::getValue,
                (a, b) -> b,
                HashMap::new
            ));
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
                e -> e.getKey().getName(),
                Map.Entry::getValue,
                (a, b) -> b,
                LinkedHashMap::new
            )));
    }

    /**
     * Create a new Rules instance from a map of flag names to their respective values
     * *
     *
     * @param flags the map of flag IDs to create from
     * @return the new Rules instance
     */
    @NotNull
    public static Rules from(@NotNull Map<String, Boolean> flags) {
        return new Rules(flags);
    }

    @NotNull
    public Map<Flag, Boolean> getCalculatedFlags(@NotNull Flags flagConfig) {
        return calculatedFlags == null ? calculatedFlags = getMapped(flags, flagConfig) : calculatedFlags;
    }

    public boolean hasFlagSet(@NotNull Flag flag) {
        return flags.containsKey(flag.getName());
    }

    /**
     * Set the value of a flag
     *
     * @param flag  the flag to set
     * @param value the value to set the flag to
     */
    public void setFlag(@NotNull Flag flag, boolean value) {
        flags.put(flag.getName(), value);
        calculatedFlags = null;
    }

    /**
     * Whether, for the given operation, the flag rules set indicate it should be cancelled
     *
     * @param type the operation type that is being performed in a region governed by these rules
     * @return Whether the operation should be canceled:
     * <p>
     * {@code true} if no flags have been set to {@code true} that permit the operation; {@code false} otherwise
     */
    public boolean cancelOperation(@NotNull OperationType type, @NotNull Flags flagConfig) {
        return getCalculatedFlags(flagConfig).entrySet().stream()
            .filter(Map.Entry::getValue)
            .noneMatch(entry -> entry.getKey().isOperationAllowed(type));
    }

}
