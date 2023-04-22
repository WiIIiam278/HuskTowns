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

package net.william278.husktowns.listener;

import net.william278.husktowns.claim.Position;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Representation of a {@link Type type} of server event that is taking place at a {@link Position}
 */
public class Operation {

    private final Type type;
    private final Position position;
    private boolean silent;
    @Nullable
    private final OnlineUser user;

    private Operation(@Nullable OnlineUser user, @NotNull Type type, @NotNull Position position) {
        this.user = user;
        this.type = type;
        this.position = position;
        this.silent = getType().isSilent();
    }

    private Operation(@NotNull Type type, @NotNull Position position) {
        this(null, type, position);
    }

    /**
     * Create a new {@code Operation} from a {@link Type} and {@link Position}
     *
     * @param user     the user who performed the operation
     * @param type     the type of operation
     * @param position the position of the operation; where it took place
     * @return the new {@code Operation}
     */
    @NotNull
    public static Operation of(@Nullable OnlineUser user, @NotNull Type type, @NotNull Position position) {
        return new Operation(user, type, position);
    }

    /**
     * Create a new {@code Operation} from a {@link Type} and {@link Position}
     *
     * @param type     the type of operation
     * @param position the position of the operation; where it took place
     * @return the new {@code Operation}
     */
    @NotNull
    public static Operation of(@NotNull Type type, @NotNull Position position) {
        return new Operation(type, position);
    }

    /**
     * Create a new {@code Operation} from a {@link Type}, {@link Position}, and {@link OnlineUser}
     *
     * @param user     the user who performed the operation
     * @param type     the type of operation
     * @param position the position of the operation; where it took place
     * @param silent   whether the operation should be silent; not displayed to the user if it is cancelled
     * @return the new {@code Operation}
     */
    @NotNull
    public static Operation of(@Nullable OnlineUser user, @NotNull Type type, @NotNull Position position, boolean silent) {
        final Operation operation = of(user, type, position);
        operation.setSilent(silent);
        return operation;
    }

    /**
     * Create a new {@code Operation} from a {@link Type}, {@link Position}, and {@link OnlineUser}
     *
     * @param type     the type of operation
     * @param position the position of the operation; where it took place
     * @param silent   whether the operation should be silent; not displayed to the user if it is cancelled
     * @return the new {@code Operation}
     */
    @NotNull
    public static Operation of(@NotNull Type type, @NotNull Position position, boolean silent) {
        final Operation operation = of(type, position);
        operation.setSilent(silent);
        return operation;
    }

    /**
     * Set whether the operation should be silent; not displayed to the user if it is cancelled
     *
     * @param silent whether the operation should be silent
     */
    private void setSilent(boolean silent) {
        this.silent = silent;
    }

    /**
     * Get the {@link Type} of operation
     *
     * @return the type of operation
     */
    @NotNull
    public Type getType() {
        return type;
    }

    /**
     * Get whether the operation should be displayed to the user if it is cancelled
     *
     * @return {@code true} if the operation should be displayed to the user if it is cancelled; {@code false} otherwise
     */
    public boolean isVerbose() {
        return !silent;
    }

    /**
     * Get the {@link Position} of the operation; where it took place
     *
     * @return the position of the operation
     */
    @NotNull
    public Position getPosition() {
        return position;
    }

    /**
     * Get the {@link OnlineUser} who performed the operation, if any
     *
     * @return the user who performed the operation, wrapped in an {@link Optional}
     */
    public Optional<OnlineUser> getUser() {
        return Optional.ofNullable(user);
    }

    /**
     * Types of operations triggered by certain events
     */
    public enum Type {
        /**
         * When a player places a block
         */
        BLOCK_PLACE,
        /**
         * When a player breaks a block
         */
        BLOCK_BREAK,
        /**
         * When a player interacts with a block
         */
        BLOCK_INTERACT,
        /**
         * When a player interacts with redstone
         */
        REDSTONE_INTERACT(true),
        /**
         * When a player breaks a {@link net.william278.husktowns.config.SpecialTypes#isFarmBlock(String) farm block}
         */
        FARM_BLOCK_BREAK,
        /**
         * When a player places a {@link net.william278.husktowns.config.SpecialTypes#isFarmBlock(String) farm block}
         */
        FARM_BLOCK_PLACE,
        /**
         * When a player damages another player
         */
        PLAYER_DAMAGE_PLAYER,
        /**
         * When a player damages a hostile monster
         */
        PLAYER_DAMAGE_MONSTER,
        /**
         * When a player damages a (non-hostile) mob
         */
        PLAYER_DAMAGE_ENTITY,
        /**
         * When a player damages a mob that has been name-tagged or marked as persistent
         */
        PLAYER_DAMAGE_PERSISTENT_ENTITY,
        /**
         * When a hostile mob spawns
         */
        MONSTER_SPAWN(true),
        /**
         * When a mob damages terrain (e.g. an Enderman picking up a block)
         */
        MONSTER_DAMAGE_TERRAIN(true),
        /**
         * When an explosion damages terrain (breaks blocks)
         */
        EXPLOSION_DAMAGE_TERRAIN(true),
        /**
         * When an explosion causes an entity to take damage
         */
        EXPLOSION_DAMAGE_ENTITY(true),
        /**
         * When an ignited block is destroyed by fire
         */
        FIRE_BURN(true),
        /**
         * When fire spreads from an ignited block to another block
         */
        FIRE_SPREAD(true),
        /**
         * When a player fills a bucket
         */
        FILL_BUCKET,
        /**
         * When a player empties a bucket
         */
        EMPTY_BUCKET,
        /**
         * When a player places a hanging entity (e.g. a Painting)
         */
        PLACE_HANGING_ENTITY,
        /**
         * When a player breaks a hanging entity (e.g. a Painting)
         */
        BREAK_HANGING_ENTITY,
        /**
         * When a player interacts with an entity in some way
         */
        ENTITY_INTERACT,
        /**
         * When a player interacts with a {@link net.william278.husktowns.config.SpecialTypes#isFarmBlock(String) farm block}
         * in some way (e.g. Right-clicking crops with Bonemeal)
         */
        FARM_BLOCK_INTERACT,
        /**
         * When a player uses a Spawn Egg
         */
        USE_SPAWN_EGG,
        /**
         * When a player opens a container (e.g. chests, hoppers, furnaces, etc.)
         */
        CONTAINER_OPEN;

        private final boolean silent;

        Type(final boolean silent) {
            this.silent = silent;
        }

        Type() {
            this.silent = false;
        }

        /**
         * Indicates whether by default this operation should not notify a player when it is cancelled
         *
         * @return true if the operation should be silenced
         */
        private boolean isSilent() {
            return this.silent;
        }

    }

}
