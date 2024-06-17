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

import net.william278.cloplib.operation.OperationType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents flags, which can be set on claims to allow or deny certain groups of operations
 */
public class Flag implements Comparable<Flag> {

    private final String name;
    private final Set<OperationType> allowedOperations;

    private Flag(@NotNull String name, @NotNull OperationType... allowedOperations) {
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
    public static Flag of(@NotNull String name, @NotNull Set<OperationType> allowedOperations) throws IllegalArgumentException {
        if (name.isEmpty() || name.contains(" ")) {
            throw new IllegalArgumentException("Flag name cannot be empty or contain whitespace");
        }
        return new Flag(name.toLowerCase(Locale.ENGLISH), allowedOperations.toArray(new OperationType[0]));
    }

    /**
     * Get the operations allowed by this flag
     *
     * @return The operations allowed by this flag
     */
    @NotNull
    public Set<OperationType> getAllowedOperations() {
        return allowedOperations;
    }

    /**
     * Get whether this flag allows the given operation
     *
     * @param type The operation type to check
     * @return Whether the operation is allowed
     */
    public boolean isOperationAllowed(@NotNull OperationType type) {
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

    /**
     * The default set of flag IDs to allowed operations
     */
    public enum Defaults {
        EXPLOSION_DAMAGE(
            OperationType.EXPLOSION_DAMAGE_TERRAIN,
            OperationType.EXPLOSION_DAMAGE_ENTITY
        ),
        FIRE_DAMAGE(
            OperationType.FIRE_SPREAD,
            OperationType.FIRE_BURN
        ),
        MOB_GRIEFING(
            OperationType.MONSTER_DAMAGE_TERRAIN
        ),
        MONSTER_SPAWNING(
            OperationType.MONSTER_SPAWN,
            OperationType.PASSIVE_MOB_SPAWN,
            OperationType.PLAYER_DAMAGE_MONSTER
        ),
        PUBLIC_BUILD_ACCESS(
            OperationType.BLOCK_BREAK,
            OperationType.BLOCK_PLACE,
            OperationType.CONTAINER_OPEN,
            OperationType.FARM_BLOCK_PLACE,
            OperationType.FARM_BLOCK_INTERACT,
            OperationType.FILL_BUCKET,
            OperationType.EMPTY_BUCKET,
            OperationType.BREAK_HANGING_ENTITY,
            OperationType.PLACE_HANGING_ENTITY,
            OperationType.BLOCK_INTERACT,
            OperationType.ENTITY_INTERACT,
            OperationType.REDSTONE_INTERACT,
            OperationType.USE_SPAWN_EGG,
            OperationType.PLAYER_DAMAGE_MONSTER,
            OperationType.PLAYER_DAMAGE_PERSISTENT_ENTITY,
            OperationType.PLAYER_DAMAGE_ENTITY,
            OperationType.ENDER_PEARL_TELEPORT
        ),
        PUBLIC_CONTAINER_ACCESS(
            OperationType.CONTAINER_OPEN
        ),
        PUBLIC_FARM_ACCESS(
            OperationType.BLOCK_INTERACT,
            OperationType.FARM_BLOCK_BREAK,
            OperationType.FARM_BLOCK_PLACE,
            OperationType.FARM_BLOCK_INTERACT,
            OperationType.PLAYER_DAMAGE_ENTITY
        ),
        PUBLIC_INTERACT_ACCESS(
            OperationType.BLOCK_INTERACT,
            OperationType.ENTITY_INTERACT,
            OperationType.REDSTONE_INTERACT,
            OperationType.ENDER_PEARL_TELEPORT
        ),
        PVP(
            OperationType.PLAYER_DAMAGE_PLAYER
        );

        private final OperationType[] allowedOperations;

        Defaults(@NotNull OperationType... allowedOperations) {
            this.allowedOperations = allowedOperations;
        }

        /**
         * Get the flag for this default
         *
         * @return The flag
         */
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
         * Get the name of this default flag (lowercase)
         *
         * @return The name of this default flag
         */
        @NotNull
        public String getName() {
            return name().toLowerCase(Locale.ENGLISH);
        }

    }
}
