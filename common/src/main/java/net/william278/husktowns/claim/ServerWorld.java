package net.william278.husktowns.claim;

import org.jetbrains.annotations.NotNull;

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
