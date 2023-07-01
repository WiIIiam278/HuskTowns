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

package net.william278.husktowns.listener;

import org.jetbrains.annotations.NotNull;

/**
 * Legacy action types for backwards API compatibility
 *
 * @deprecated See {@link net.william278.husktowns.listener.Operation.Type Operation.Type}
 */
@Deprecated(since = "2.0")
public enum ActionType {
    /**
     * When a player attacks another player
     */
    PVP(Operation.Type.PLAYER_DAMAGE_PLAYER),
    /**
     * When a player shoots another player with a projectile
     */
    PVP_PROJECTILE(Operation.Type.PLAYER_DAMAGE_PLAYER),
    /**
     * When a player takes damage from an explosion caused by a mob
     */
    MOB_EXPLOSION_HURT(Operation.Type.EXPLOSION_DAMAGE_ENTITY),
    /**
     * When a player attacks a mob
     */
    PVE(Operation.Type.PLAYER_DAMAGE_ENTITY),
    /**
     * When a player attacks a mob with a projectile
     */
    PVE_PROJECTILE(Operation.Type.PLAYER_DAMAGE_ENTITY),
    /**
     * When a monster spawns naturally in the world
     */
    MONSTER_SPAWN(Operation.Type.MONSTER_SPAWN),
    /**
     * When a mob griefs the world (excluding explosions; e.g. an Enderman)
     */
    MOB_GRIEF_WORLD(Operation.Type.MONSTER_DAMAGE_TERRAIN),
    /**
     * When a monster explosion griefs the world (e.g. Creeper)
     */
    MOB_EXPLOSION_DAMAGE(Operation.Type.MONSTER_DAMAGE_TERRAIN),
    /**
     * When a block exploding griefs the world (e.g. TNT)
     */
    BLOCK_EXPLOSION_DAMAGE(Operation.Type.EXPLOSION_DAMAGE_TERRAIN),
    /**
     * When fire destroys a block in the world
     */
    FIRE_DAMAGE(Operation.Type.FIRE_BURN),
    /**
     * When fire spreads
     */
    FIRE_SPREAD(Operation.Type.FIRE_SPREAD),
    /**
     * When a player interacts with an entity in the world
     */
    ENTITY_INTERACTION(Operation.Type.ENTITY_INTERACT),
    /**
     * When a player interacts with blocks in the world
     */
    INTERACT_BLOCKS(Operation.Type.BLOCK_INTERACT),
    /**
     * When a player interacts with redstone components in the world
     */
    INTERACT_REDSTONE(Operation.Type.REDSTONE_INTERACT),
    /**
     * When a player interacts with the world in some other way
     * Note: Not in use
     */
    INTERACT_WORLD(Operation.Type.BLOCK_INTERACT),
    /**
     * When a player opens a container (e.g. Chests, Hoppers, etc.)
     */
    OPEN_CONTAINER(Operation.Type.CONTAINER_OPEN),
    /**
     * When a player uses a spawn egg item
     */
    USE_SPAWN_EGG(Operation.Type.USE_SPAWN_EGG),
    /**
     * When a player places a hanging entity (i.e. Paintings, Item Frames)
     */
    PLACE_HANGING_ENTITY(Operation.Type.PLACE_HANGING_ENTITY),
    /**
     * When a player breaks a hanging entity
     */
    BREAK_HANGING_ENTITY(Operation.Type.BREAK_HANGING_ENTITY),
    /**
     * When a player shoots a hanging entity with a projectile
     */
    BREAK_HANGING_ENTITY_PROJECTILE(Operation.Type.BREAK_HANGING_ENTITY),
    /**
     * When a player manipulates (changing equipment of) an armour stand
     */
    ARMOR_STAND_MANIPULATE(Operation.Type.ENTITY_INTERACT),
    /**
     * When a player fills a bucket
     */
    FILL_BUCKET(Operation.Type.FILL_BUCKET),
    /**
     * When a player empties a bucket
     */
    EMPTY_BUCKET(Operation.Type.EMPTY_BUCKET),
    /**
     * When a player places a block
     */
    PLACE_BLOCK(Operation.Type.BLOCK_PLACE),
    /**
     * When the player breaks a block
     */
    BREAK_BLOCK(Operation.Type.BLOCK_BREAK),
    /**
     * When the player places crop blocks
     */
    PLACE_CROPS(Operation.Type.FARM_BLOCK_PLACE),
    /**
     * When the player breaks crop blocks
     */
    BREAK_CROPS(Operation.Type.FARM_BLOCK_BREAK);

    private final Operation.Type operationType;

    ActionType(@NotNull Operation.Type operationTypeEquivalent) {
        this.operationType = operationTypeEquivalent;
    }

    @NotNull
    public Operation.Type getOperationType() {
        return operationType;
    }
}