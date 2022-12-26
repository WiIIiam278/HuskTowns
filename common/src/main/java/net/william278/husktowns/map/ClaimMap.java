package net.william278.husktowns.map;

import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClaimMap {

    private final HuskTowns plugin;
    private final int width;
    private final int height;
    private final Chunk center;
    private final World world;

    private ClaimMap(int width, int height, @NotNull Chunk center, @NotNull World world, @NotNull HuskTowns plugin) {
        this.width = width;
        this.height = height;
        this.center = center;
        this.world = world;
        this.plugin = plugin;
    }

    @NotNull
    public Component toComponent(@Nullable CommandUser user) throws IllegalArgumentException {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Map width and height must be greater than 0");
        }
        Component map = Component.empty();
        for (int y = center.getZ() - (height / 2); y < center.getZ() + (height / 2); y++) {
            for (int x = center.getX() - (width / 2); x < center.getX() + (width / 2); x++) {
                final Chunk chunk = Chunk.at(x, y);
                final MapSquare square = plugin.getClaimAt(chunk, world)
                        .map(townClaim -> MapSquare.claim(chunk, world, townClaim, plugin))
                        .orElseGet(() -> MapSquare.wilderness(chunk, world, plugin));
                if (user instanceof OnlineUser onlineUser && onlineUser.getChunk().equals(chunk)) {
                    square.markAsCurrentPosition(true);
                }
                map = map.append(square.toComponent());
            }
            map = map.append(Component.newline());
        }
        return map;
    }

    @NotNull
    public Component toComponent() throws IllegalArgumentException {
        return toComponent(null);
    }

    @NotNull
    public static Builder builder(@NotNull HuskTowns plugin) {
        return new Builder(plugin);
    }

    public static class Builder {
        private final HuskTowns plugin;

        private int width;

        private int height;

        private Chunk center;

        private World world;


        private Builder(@NotNull HuskTowns plugin) {
            this.plugin = plugin;
        }

        @NotNull
        public Builder width(int width) {
            this.width = width;
            return this;
        }

        @NotNull
        public Builder height(int height) {
            this.height = height;
            return this;
        }

        @NotNull
        public Builder center(@NotNull Chunk center) {
            this.center = center;
            return this;
        }

        @NotNull
        public Builder world(@NotNull World world) {
            this.world = world;
            return this;
        }

        @NotNull
        public ClaimMap build() {
            return new ClaimMap(width, height, center, world, plugin);
        }

    }

}
