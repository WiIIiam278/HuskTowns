package net.william278.husktowns.claim;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link World} mapped to a specific server, by ID
 *
 * @param server The ID of the server the world is on
 * @param world  The world
 */
public record ServerWorld(@NotNull String server, @NotNull World world) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof final ServerWorld serverWorld)) {
            return false;
        }
        return serverWorld.server().equals(server) && serverWorld.world().equals(world);
    }

    @Override
    public String toString() {
        return server + "/" + world.getName();
    }
}
