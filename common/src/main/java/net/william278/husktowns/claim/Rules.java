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

package net.william278.husktowns.claim;

import com.google.gson.annotations.Expose;
import net.william278.husktowns.listener.Operation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Claim rules, defining what players can do in a claim
 */
public class Rules {

    @Expose
    private Map<Flag, Boolean> flags;

    private Rules(@NotNull Map<Flag, Boolean> flags) {
        this.flags = flags;
    }

    /**
     * Create a new Rules instance from a {@link Flag}-value map
     *
     * @param rules the rules map to create from
     * @return the new Rules instance
     */
    @NotNull
    public static Rules of(@NotNull Map<Flag, Boolean> rules) {
        return new Rules(rules);
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
        if (flags.containsKey(flag)) {
            flags.replace(flag, value);
        } else {
            flags.put(flag, value);
        }
    }

    /**
     * Get the map of {@link Flag}s to their respective values
     *
     * @return the map of flags to their respective values
     */
    @NotNull
    public Map<Flag, Boolean> getFlagMap() {
        return flags;
    }

    /**
     * Whether, for the given operation, the flag rules set indicate it should be cancelled
     *
     * @param type the operation type that is being performed in a region governed by these rules
     * @return Whether the operation should be cancelled:
     * <p>
     * {@code true} if no flags have been set to {@code true} that permit the operation; {@code false} otherwise
     */
    public boolean cancelOperation(@NotNull Operation.Type type) {
        return flags.entrySet().stream()
                .filter(Map.Entry::getValue)
                .noneMatch(entry -> entry.getKey().isOperationAllowed(type));
    }
}
