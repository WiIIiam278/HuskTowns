package net.william278.husktowns.listener;

import net.william278.husktowns.claim.Position;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * High-level abstract representation of a server event that took place
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

    @NotNull
    public static Operation of(@Nullable OnlineUser user, @NotNull Type type, @NotNull Position position) {
        return new Operation(user, type, position);
    }

    @NotNull
    public static Operation of(@NotNull Type type, @NotNull Position position) {
        return new Operation(type, position);
    }

    @NotNull
    public static Operation of(@Nullable OnlineUser user, @NotNull Type type, @NotNull Position position, boolean silent) {
        final Operation operation = of(user, type, position);
        operation.setSilent(silent);
        return operation;
    }

    @NotNull
    public static Operation of(@NotNull Type type, @NotNull Position position, boolean silent) {
        final Operation operation = of(type, position);
        operation.setSilent(silent);
        return operation;
    }

    private void setSilent(boolean silent) {
        this.silent = silent;
    }

    @NotNull
    public Type getType() {
        return type;
    }

    public boolean isVerbose() {
        return !silent;
    }

    @NotNull
    public Position getPosition() {
        return position;
    }

    public Optional<OnlineUser> getUser() {
        return Optional.ofNullable(user);
    }

    /**
     * Types of operations triggered by certain events
     */
    public enum Type {
        BLOCK_PLACE,
        BLOCK_BREAK,
        BLOCK_INTERACT,
        REDSTONE_INTERACT(true),
        FARM_BLOCK_BREAK,
        FARM_BLOCK_PLACE,
        PLAYER_DAMAGE_PLAYER,
        PLAYER_DAMAGE_MONSTER,
        PLAYER_DAMAGE_ENTITY,
        PLAYER_DAMAGE_PERSISTENT_ENTITY,
        MONSTER_SPAWN(true),
        MONSTER_DAMAGE_TERRAIN(true),
        EXPLOSION_DAMAGE_TERRAIN(true),
        EXPLOSION_DAMAGE_ENTITY(true),
        FIRE_BURN(true),
        FIRE_SPREAD(true),
        FILL_BUCKET,
        EMPTY_BUCKET,
        PLACE_HANGING_ENTITY,
        BREAK_HANGING_ENTITY,
        ENTITY_INTERACT,
        FARM_BLOCK_INTERACT,
        USE_SPAWN_EGG,
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
