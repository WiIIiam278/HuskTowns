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
import net.william278.husktowns.user.Preferences;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * HuskTowns API v2, providing methods for interfacing with towns, claims and users.
 * <p>
 * Create an instance with {@link HuskTownsAPI#getInstance()}
 *
 * @since 2.0
 */
@SuppressWarnings("unused")
public class HuskTownsAPI implements IHuskTownsAPI {
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
     * Create a claim for a town
     *
     * @param actor The actor to use for creating the claim. Note that this user does not necessarily have to be a
     *              member of the town where the claim is being created.
     * @param town  The town to create the claim for
     * @param chunk The {@link org.bukkit.Chunk} to create the claim at
     * @throws IllegalArgumentException if the claim overlaps with an existing claim
     * @since 2.0
     */
    public void createClaimAt(@NotNull Player actor, @NotNull Town town, @NotNull org.bukkit.Chunk chunk) {
        createClaimAt(getOnlineUser(actor), town, Chunk.at(chunk.getX(), chunk.getZ()), getWorld(chunk.getWorld()));
    }

    /**
     * Create a claim for a town
     *
     * @param actor    The actor to use for creating the claim. Note that this user does not necessarily have to be a
     *                 member of the town where the claim is being created.
     * @param town     The town to create the claim for
     * @param location A {@link Location} that lies within the chunk to create the claim in
     * @throws IllegalArgumentException if the claim overlaps with an existing claim
     * @since 2.0
     */
    public void createClaimAt(@NotNull Player actor, @NotNull Town town, @NotNull Location location) {
        createClaimAt(getOnlineUser(actor), town, getPosition(location));
    }

    /**
     * Create an administrator-owned claim
     *
     * @param actor The actor for use for creating the claim. Note that this user does not necessarily have to have
     *              permission to create admin claims.
     * @param chunk The {@link org.bukkit.Chunk} to make the claim in
     * @throws IllegalArgumentException if the claim overlaps with an existing claim
     * @since 2.0
     */
    public void createAdminClaimAt(@NotNull Player actor, @NotNull org.bukkit.Chunk chunk) {
        createAdminClaimAt(getOnlineUser(actor), Chunk.at(chunk.getX(), chunk.getZ()), getWorld(chunk.getWorld()));
    }

    /**
     * Create an administrator-owned claim
     *
     * @param actor    The actor for use for creating the claim. Note that this user does not necessarily have to have
     *                 permission to create admin claims.
     * @param location A {@link Location} that lies within the chunk to create the claim in
     * @throws IllegalArgumentException if the claim overlaps with an existing claim
     * @since 2.0
     */
    public void createAdminClaimAt(@NotNull Player actor, @NotNull Location location) {
        createAdminClaimAt(getOnlineUser(actor), getPosition(location));
    }

    /**
     * Delete the claim in the world at the specified chunk
     *
     * @param actor The actor for use for deleting the claim. Note that this user does not necessarily have to have
     *              the permission or privileges to delete the claim.
     * @param chunk The {@link org.bukkit.Chunk} to delete the claim at
     * @throws IllegalArgumentException if there is no claim at the chunk in the world
     * @since 2.0
     */
    public void deleteClaimAt(@NotNull Player actor, @NotNull org.bukkit.Chunk chunk) {
        deleteClaimAt(getOnlineUser(actor), Chunk.at(chunk.getX(), chunk.getZ()), getWorld(chunk.getWorld()));
    }

    /**
     * Delete the claim at the specified {@link Location}
     *
     * @param actor    The actor for use for deleting the claim. Note that this user does not necessarily have to have
     *                 the permission or privileges to delete the claim.
     * @param location A {@link Location} that lies within the claim to delete
     * @since 2.0
     */
    public void deleteClaimAt(@NotNull Player actor, @NotNull Location location) {
        deleteClaimAt(getOnlineUser(actor), getPosition(location));
    }

    /**
     * Create a new {@link Town}
     *
     * @param creator The creator of the town
     * @param name    The name of the town
     * @return The created {@link Town} in a future, that will complete when it has been made
     * @throws IllegalArgumentException if the town name is invalid
     * @since 2.0
     */
    public CompletableFuture<Town> createTown(@NotNull Player creator, @NotNull String name) throws IllegalArgumentException {
        return createTown(getOnlineUser(creator), name);
    }

    /**
     * Delete a {@link Town}
     *
     * @param actor An actor to delete the town. Note that they do not necessarily need to be the mayor, or even a
     *              member of the town being deleted
     * @param town  The town to delete
     * @since 2.0
     */
    public void deleteTown(@NotNull Player actor, @NotNull Town town) {
        deleteTown(getOnlineUser(actor), town);
    }

    /**
     * Update a {@link Town}
     *
     * @param player the {@link Player} to act as the executor of the update.
     *               In most cases this should be the user relevant to the update operation (i.e. the trigger).
     * @param town   the {@link Town} to update
     * @throws IllegalArgumentException if the town has an invalid name, bio, greeting or farewell message
     * @since 2.0
     */
    public void updateTown(@NotNull Player player, @NotNull Town town) throws IllegalArgumentException {
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
     * Get an {@link Player}'s {@link Preferences}
     *
     * @param player the {@link Player} to get the {@link Preferences} for
     * @return the {@link Preferences} for the {@link Player}
     * @since 2.0
     */
    @NotNull
    public Preferences getUserPreferences(@NotNull Player player) {
        return getUserPreferences(getOnlineUser(player));
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
