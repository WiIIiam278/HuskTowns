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
import java.util.Optional;

/**
 * Various flag types
 */
public enum Flag {
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

    Flag(@NotNull Operation.Type... allowedOperations) {
        this.allowedOperations = allowedOperations;
    }

    public boolean isOperationAllowed(@NotNull Operation.Type type) {
        return Arrays.asList(allowedOperations).contains(type);
    }

    public static Optional<Flag> fromId(@NotNull String id) {
        return Arrays.stream(values()).filter(flag -> flag.name().equalsIgnoreCase(id)).findFirst();
    }
}
