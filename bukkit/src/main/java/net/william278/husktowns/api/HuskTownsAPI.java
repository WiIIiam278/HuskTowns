package net.william278.husktowns.api;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * HuskTowns API v2, providing methods for interfacing with towns, claims and users.
 * <p>
 * Create an instance with {@link HuskTownsAPI#getInstance()}
 * {@inheritDoc}
 *
 * @since 2.0
 */
@SuppressWarnings("unused")
public class HuskTownsAPI implements BaseHuskTownsAPI {
    private static HuskTownsAPI instance;
    private final HuskTowns plugin;

    /**
     * <b>Internal use only</b> - Creates a new HuskTownsAPI instance
     *
     * @param plugin The HuskTowns plugin instance
     * @since 2.0
     */
    private HuskTownsAPI(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    /**
     * Entry point of the API. Get an instance of the {@link HuskTownsAPI}.
     *
     * @return The {@link HuskTownsAPI} instance
     * @since 2.0
     */
    @NotNull
    public static HuskTownsAPI getInstance() {
        return (instance == null) ? instance = new HuskTownsAPI(BukkitHuskTowns.getInstance()) : instance;
    }

    /**
     * Get a {@link TownClaim} at a {@link org.bukkit.Chunk}, if it exists.
     *
     * @param chunk The {@link org.bukkit.Chunk} to check
     * @return The {@link TownClaim}, if one has been made at the position
     * @since 2.0
     */

    public Optional<TownClaim> getClaimAt(@NotNull org.bukkit.Chunk chunk) {
        return getClaimAt(Chunk.at(chunk.getX(), chunk.getZ()), getWorld(chunk.getWorld()));
    }

    /**
     * Get a {@link TownClaim} at a {@link Location}, if it exists.
     *
     * @param location The {@link Location} to check
     * @return The {@link TownClaim}, if one has been made at the position
     * @since 2.0
     */

    public Optional<TownClaim> getClaimAt(@NotNull Location location) {
        return getClaimAt(getPosition(location));
    }

    /**
     * Get a list of {@link TownClaim}s in a particular {@link org.bukkit.World}
     *
     * @param world The {@link org.bukkit.World} to get claims in
     * @return A list of {@link TownClaim}s in the world
     * @since 2.0
     */
    public List<TownClaim> getClaims(@NotNull org.bukkit.World world) {
        return getClaims(getWorld(world));
    }

    /**
     * Update a {@link Town}
     *
     * @param player the {@link Player} to act as the executor of the update.
     *               In most cases this should be the user relevant to the update operation (i.e. the trigger).
     * @param town   the {@link Town} to update
     * @since 2.0
     */
    public void updateTown(@NotNull Player player, @NotNull Town town) {
        updateTown(getOnlineUser(player), town);
    }

    /**
     * Get the {@link Member} mapping for a user, identifying the map of their {@link net.william278.husktowns.town.Role}
     * to the {@link Town} they are in
     *
     * @param player the {@link Player} to get the member mapping for
     * @return the {@link Member} mapping for the {@link Player}, if they are in a town
     * @since 2.0
     */
    public Optional<Member> getUserTown(@NotNull Player player) {
        return getUserTown(getOnlineUser(player));
    }

    /**
     * Get an {@link OnlineUser} from a {@link Player}
     *
     * @param player the {@link Player} to get the {@link OnlineUser} for
     * @return the {@link OnlineUser} for the {@link Player}
     * @since 2.0
     */
    @NotNull
    public OnlineUser getOnlineUser(@NotNull Player player) {
        return BukkitUser.adapt(player);
    }

    /**
     * Returns a {@link Position} from a {@link Location}
     *
     * @param location The {@link Location} to convert
     * @return The {@link Position} at the given {@link Location}
     * @since 2.0
     */
    @NotNull
    public Position getPosition(@NotNull Location location) {
        assert location.getWorld() != null;
        return Position.at(location.getX(), location.getY(), location.getZ(),
                getWorld(location.getWorld()), location.getYaw(), location.getPitch());
    }

    /**
     * Returns a {@link World} from a {@link org.bukkit.World}
     *
     * @param world The {@link org.bukkit.World} to convert
     * @return The {@link World} at the given {@link org.bukkit.World}
     * @since 2.0
     */
    @NotNull
    public World getWorld(@NotNull org.bukkit.World world) {
        return World.of(world.getUID(), world.getName(), world.getEnvironment().name().toLowerCase());
    }

    /**
     * <b>Internal use only</b> - Returns the HuskTowns plugin instance.
     *
     * @return The HuskTowns plugin instance
     * @since 2.0
     */
    @Override
    @NotNull
    public HuskTowns getPlugin() {
        return plugin;
    }

}
