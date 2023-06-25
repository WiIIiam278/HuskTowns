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

import net.william278.husktowns.listener.Operation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Various flag types
 */
public class Flag implements Comparable<Flag> {

    @Deprecated(since = "2.5")
    public static final Flag EXPLOSION_DAMAGE = Defaults.EXPLOSION_DAMAGE.getFlag();
    @Deprecated(since = "2.5")
    public static final Flag FIRE_DAMAGE = Defaults.FIRE_DAMAGE.getFlag();
    @Deprecated(since = "2.5")
    public static final Flag MOB_GRIEFING = Defaults.MOB_GRIEFING.getFlag();
    @Deprecated(since = "2.5")
    public static final Flag MONSTER_SPAWNING = Defaults.MONSTER_SPAWNING.getFlag();
    @Deprecated(since = "2.5")
    public static final Flag PUBLIC_BUILD_ACCESS = Defaults.PUBLIC_BUILD_ACCESS.getFlag();
    @Deprecated(since = "2.5")
    public static final Flag PUBLIC_CONTAINER_ACCESS = Defaults.PUBLIC_CONTAINER_ACCESS.getFlag();
    @Deprecated(since = "2.5")
    public static final Flag PUBLIC_FARM_ACCESS = Defaults.PUBLIC_FARM_ACCESS.getFlag();
    @Deprecated(since = "2.5")
    public static final Flag PUBLIC_INTERACT_ACCESS = Defaults.PUBLIC_INTERACT_ACCESS.getFlag();
    @Deprecated(since = "2.5")
    public static final Flag PVP = Defaults.PVP.getFlag();

    private final String name;
    private final Set<Operation.Type> allowedOperations;

    private Flag(@NotNull String name, @NotNull Operation.Type... allowedOperations) {
        this.name = name;
        this.allowedOperations = Set.of(allowedOperations);
    }

    /**
     * Create a new flag with the given name and allowed operations
     *
     * @param name              The ID name of the flag
     * @param allowedOperations The operations allowed by this flag
     * @return The flag
     * @throws IllegalArgumentException If the name is empty or contains whitespace
     */
    @NotNull
    public static Flag of(@NotNull String name, @NotNull Set<Operation.Type> allowedOperations) throws IllegalArgumentException {
        if (name.isEmpty() || name.contains(" ")) {
            throw new IllegalArgumentException("Flag name cannot be empty or contain whitespace");
        }
        return new Flag(name.toLowerCase(Locale.ENGLISH), allowedOperations.toArray(new Operation.Type[0]));
    }

    /**
     * Get the operations allowed by this flag
     *
     * @return The operations allowed by this flag
     */
    @NotNull
    public Set<Operation.Type> getAllowedOperations() {
        return allowedOperations;
    }

    /**
     * Get whether this flag allows the given operation
     *
     * @param type The operation type to check
     * @return Whether the operation is allowed
     */
    public boolean isOperationAllowed(@NotNull Operation.Type type) {
        return getAllowedOperations().contains(type);
    }

    /**
     * Get the name of the flag
     *
     * @return The name of the flag
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Get the name of the flag
     *
     * @return The name of the flag
     * @deprecated Use {@link #getName()} instead
     */
    @Deprecated(since = "2.5")
    public String name() {
        return getName();
    }

    /**
     * Get a default flag from the given name
     *
     * @param id The name of the flag
     * @return The flag, or an empty optional if not found
     * @deprecated Use {@link Defaults#fromId(String)} instead
     */
    @Deprecated(since = "2.5")
    public static Optional<Flag> fromId(@NotNull String id) {
        return Defaults.fromId(id);
    }

    /**
     * Get the default set of flags
     *
     * @return The default set of flags
     */
    @NotNull
    public static Set<Flag> getDefaults() {
        return Arrays.stream(Defaults.values()).map(Defaults::getFlag).collect(Collectors.toSet());
    }

    /**
     * Get the default set of flags
     *
     * @return The default set of flags
     * @deprecated Use {@link #getDefaults()} instead
     */
    @Deprecated(since = "2.5")
    public static Flag[] values() {
        return getDefaults().toArray(new Flag[0]);
    }

    /**
     * Compare this flag to another
     *
     * @param other the object to be compared.
     * @return A string comparison integer-compare of the flag names
     */
    @Override
    public int compareTo(@NotNull Flag other) {
        return getName().compareTo(other.getName());
    }

    public enum Defaults {
        EXPLOSION_DAMAGE(
                Operation.Type.EXPLOSION_DAMAGE_TERRAIN,
                Operation.Type.EXPLOSION_DAMAGE_ENTITY
        ),
        FIRE_DAMAGE(
                Operation.Type.FIRE_SPREAD,
                Operation.Type.FIRE_BURN
        ),
        MOB_GRIEFING(
                Operation.Type.MONSTER_DAMAGE_TERRAIN
        ),
        MONSTER_SPAWNING(
                Operation.Type.MONSTER_SPAWN,
                Operation.Type.PLAYER_DAMAGE_MONSTER
        ),
        PUBLIC_BUILD_ACCESS(
                Operation.Type.BLOCK_BREAK,
                Operation.Type.BLOCK_PLACE,
                Operation.Type.CONTAINER_OPEN,
                Operation.Type.FARM_BLOCK_PLACE,
                Operation.Type.FARM_BLOCK_INTERACT,
                Operation.Type.FILL_BUCKET,
                Operation.Type.EMPTY_BUCKET,
                Operation.Type.BREAK_HANGING_ENTITY,
                Operation.Type.PLACE_HANGING_ENTITY,
                Operation.Type.BLOCK_INTERACT,
                Operation.Type.ENTITY_INTERACT,
                Operation.Type.REDSTONE_INTERACT,
                Operation.Type.USE_SPAWN_EGG,
                Operation.Type.PLAYER_DAMAGE_MONSTER,
                Operation.Type.PLAYER_DAMAGE_PERSISTENT_ENTITY,
                Operation.Type.PLAYER_DAMAGE_ENTITY
        ),
        PUBLIC_CONTAINER_ACCESS(
                Operation.Type.CONTAINER_OPEN
        ),
        PUBLIC_FARM_ACCESS(
                Operation.Type.BLOCK_INTERACT,
                Operation.Type.FARM_BLOCK_BREAK,
                Operation.Type.FARM_BLOCK_PLACE,
                Operation.Type.FARM_BLOCK_INTERACT,
                Operation.Type.PLAYER_DAMAGE_ENTITY
        ),
        PUBLIC_INTERACT_ACCESS(
                Operation.Type.BLOCK_INTERACT,
                Operation.Type.ENTITY_INTERACT,
                Operation.Type.REDSTONE_INTERACT
        ),
        PVP(
                Operation.Type.PLAYER_DAMAGE_PLAYER
        );

        private final Operation.Type[] allowedOperations;

        Defaults(@NotNull Operation.Type... allowedOperations) {
            this.allowedOperations = allowedOperations;
        }

        @NotNull
        public Flag getFlag() {
            return Flag.of(name().toLowerCase(), Set.of(allowedOperations));
        }

        /**
         * Get a default flag from the given name
         *
         * @param id The name of the flag
         * @return The flag, or an empty optional if not found
         */
        public static Optional<Flag> fromId(@NotNull String id) {
            return Arrays.stream(values())
                    .filter(flag -> flag.name().equalsIgnoreCase(id))
                    .map(Defaults::getFlag).findFirst();
        }

        /**
         * Get the name of this default flag
         *
         * @return The name of this default flag
         */
        @NotNull
        public String getName() {
            return name().toLowerCase(Locale.ENGLISH);
        }

    }
}
