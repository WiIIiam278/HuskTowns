package net.william278.husktowns.claim;

import net.william278.husktowns.listener.Operation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * Various flag types
 */
public enum Flag {
    EXPLOSION_DAMAGE(Operation.Type.EXPLOSION_DAMAGE_TERRAIN,
            Operation.Type.EXPLOSION_DAMAGE_ENTITY),
    FIRE_DAMAGE(Operation.Type.FIRE_SPREAD,
            Operation.Type.FIRE_BURN),
    MOB_GRIEFING(Operation.Type.MONSTER_DAMAGE_TERRAIN),
    MONSTER_SPAWNING(Operation.Type.MONSTER_SPAWN),
    PUBLIC_BUILD_ACCESS(Operation.Type.BLOCK_BREAK,
            Operation.Type.BLOCK_PLACE,
            Operation.Type.CONTAINER_OPEN,
            Operation.Type.FARM_BLOCK_PLACE,
            Operation.Type.FARM_BLOCK_PLACE,
            Operation.Type.FILL_BUCKET,
            Operation.Type.EMPTY_BUCKET,
            Operation.Type.BREAK_HANGING_ENTITY,
            Operation.Type.PLACE_HANGING_ENTITY,
            Operation.Type.BLOCK_INTERACT,
            Operation.Type.ENTITY_INTERACT,
            Operation.Type.REDSTONE_INTERACT,
            Operation.Type.USE_SPAWN_EGG),
    PUBLIC_CONTAINER_ACCESS(Operation.Type.CONTAINER_OPEN),
    PUBLIC_FARM_ACCESS(Operation.Type.FARM_BLOCK_BREAK,
            Operation.Type.FARM_BLOCK_PLACE),
    PUBLIC_INTERACT_ACCESS(Operation.Type.BLOCK_INTERACT,
            Operation.Type.ENTITY_INTERACT,
            Operation.Type.REDSTONE_INTERACT),
    PVP(Operation.Type.PLAYER_DAMAGE_PLAYER);


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
