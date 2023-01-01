package net.william278.husktowns;

import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.town.Town;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * The legacy HuskTowns API, for maintaining compatibility with v1.0 plugins.
 *
 * @deprecated Use the new HuskTowns API@v2 instead
 */
@Deprecated(since = "2.0") //todo
public class HuskTownsAPI {

    private static HuskTownsAPI instance;
    private final BukkitHuskTowns plugin;

    private HuskTownsAPI() {
        this.plugin = BukkitHuskTowns.getInstance();
    }

    /**
     * Get a new instance of the {@link HuskTownsAPI}.
     *
     * @return instance of the {@link HuskTownsAPI}.
     * @deprecated Use the new HuskTowns API@v2 instead
     */
    @NotNull
    @Deprecated(since = "2.0")
    public static HuskTownsAPI getInstance() {
        return instance == null ? instance = new HuskTownsAPI() : instance;
    }

    /**
     * Check if the specified {@link Location} is in the wilderness (outside of a claim).
     *
     * @param location {@link Location} to check.
     * @return {@code true} if the {@link Location} is in the wilderness; otherwise return {@code false}.
     */
    @Deprecated(since = "2.0")
    public boolean isWilderness(@NotNull Location location) {
        return getClaimAt(location).isEmpty();
    }

    /**
     * Check if the specified {@link Block} is in the wilderness (outside a claim).
     *
     * @param block {@link Block} to check.
     * @return {@code true} if the {@link Block} is in the wilderness; otherwise return {@code false}.
     */
    @Deprecated(since = "2.0")
    public boolean isWilderness(@NotNull Block block) {
        return this.isWilderness(block.getLocation());
    }

    /**
     * Returns {@code true} if the chunk at the specified {@link Location} is claimed; otherwise returns {@code false}.
     *
     * @param location {@link Location} to check.
     * @return {@code true} if the chunk at {@link Location} is claimed; {@code false} otherwise.
     */
    public boolean isClaimed(@NotNull Location location) {
        return !this.isWilderness(location);
    }

    /**
     * Returns the name of the town at the specified {@link Location}.
     *
     * @param location {@link Location} to check.
     * @return the name of the town who has a claim at the specified {@link Location}; {@code null} if there is no claim there.
     */
    @Deprecated(since = "2.0")
    public String getTownAt(@NotNull Location location) {
        return getClaimAt(location)
                .map(TownClaim::town)
                .map(Town::getName)
                .orElse(null);
    }

    private Optional<TownClaim> getClaimAt(@NotNull Location location) {
        assert location.getWorld() != null;
        final Chunk position = Chunk.at(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        final World world = World.of(location.getWorld().getUID(), location.getWorld().getName(),
                location.getWorld().getEnvironment().name().toLowerCase());
        return plugin.getClaimAt(position, world);
    }

}
