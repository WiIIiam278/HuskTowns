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

import net.william278.annotaml.YamlComment;
import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import net.william278.husktowns.claim.Flag;
import net.william278.husktowns.listener.Operation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃    HuskTowns Flags Config    ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ This file is for configuring flags. Flag IDs map to a list of permitted operations.
        ┗╸ Config Help: https://william278.net/docs/husktowns/config-files""")
public class Flags {

    @YamlComment("A map of flag IDs to allowed operations")
    @YamlKey("flags")
    public Map<String, List<String>> flags = new LinkedHashMap<>(
            Flag.getDefaults().stream().collect(Collectors.toMap(
                    Flag::getName,
                    flag -> flag.getAllowedOperations().stream().map(Enum::name).collect(Collectors.toList())
            ))
    );

    private Flags() {
    }

    /**
     * Get the set of {@link Flag flags} being used by the plugin
     *
     * @return the set of flags
     */
    @NotNull
    public Set<Flag> getFlagSet() {
        final Set<Flag> flagSet = new LinkedHashSet<>();
        for (Map.Entry<String, List<String>> entry : flags.entrySet()) {
            flagSet.add(Flag.of(
                    entry.getKey(),
                    entry.getValue().stream()
                            .map(a -> Operation.Type.fromId(a).orElseThrow(
                                    () -> new IllegalArgumentException("Invalid operation type in flags config: " + a)))
                            .collect(Collectors.toUnmodifiableSet())
            ));
        }
        return flagSet;
    }

    /**
     * Lookup a {@link Flag} by its ID
     *
     * @param flagId the ID of the flag to lookup
     * @return the flag, if found
     */
    public Optional<Flag> getFlag(@NotNull String flagId) {
        return getFlagSet().stream().filter(flag -> flag.getName().equalsIgnoreCase(flagId)).findFirst();
    }

}
