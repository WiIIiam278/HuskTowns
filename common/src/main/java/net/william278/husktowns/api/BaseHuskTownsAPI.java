package net.william278.husktowns.api;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.listener.Operation;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Platform-agnostic HuskTowns API implementation, providing methods for interfacing with towns, claims and users.
 * {@inheritDoc}
 *
 * @since 2.0
 */
public interface BaseHuskTownsAPI {

    /**
     * <b>Internal use only</b> - Get the HuskTowns plugin instance
     *
     * @return The {@link HuskTowns} instance
     */
    @NotNull
    HuskTowns getPlugin();

    /**
     * Returns if the plugin has finished loading data
     *
     * @return {@code true} if the plugin has finished loading data, {@code false} otherwise
     */
    default boolean isLoaded() {
        return getPlugin().isLoaded();
    }

    /**
     * Get a {@link TownClaim} at a {@link Chunk} in a {@link World}, if it exists.
     *
     * @param chunk The {@link Chunk} to check
     * @param world The {@link World} the {@link Chunk} is in
     * @return The {@link TownClaim}, if one has been made at the chunk in the world
     * @since 2.0
     */
    default Optional<TownClaim> getClaimAt(@NotNull Chunk chunk, @NotNull World world) {
        return getPlugin().getClaimAt(chunk, world);
    }

    /**
     * Get a {@link TownClaim} at a {@link Position}, if it exists.
     *
     * @param position The {@link Position} to check
     * @return The {@link TownClaim}, if one has been made at the position
     * @since 2.0
     */
    default Optional<TownClaim> getClaimAt(@NotNull Position position) {
        return getPlugin().getClaimAt(position);
    }

    /**
     * Get whether an {@link Operation} is allowed
     *
     * @param operation The {@link Operation} to check against
     * @return Whether the {@link Operation} would be allowed
     * @see Operation#of(OnlineUser, Operation.Type, Position)
     * @see Operation#of(Operation.Type, Position)
     * @since 2.0
     */
    default boolean isOperationAllowed(@NotNull Operation operation) {
        return !getPlugin().getOperationHandler().cancelOperation(operation);
    }

    /**
     * Get a {@link Town} by its ID.
     *
     * @param id The ID of the town
     * @return The {@link Town}, if it exists
     * @since 2.0
     */
    default Optional<Town> getTown(int id) {
        return getPlugin().getTowns().stream().filter(t -> t.getId() == id).findFirst();
    }

    /**
     * Get a {@link Town} by its name.
     *
     * @param name The name of the town
     * @return The {@link Town}, if it exists
     * @since 2.0
     */
    default Optional<Town> getTown(@NotNull String name) {
        return getPlugin().getTowns().stream().filter(t -> t.getName().equalsIgnoreCase(name)).findFirst();
    }

    /**
     * Get a list of all {@link Town}s.
     *
     * @return A list of all {@link Town}s
     * @since 2.0
     */
    @NotNull
    default List<Town> getTowns() {
        return getPlugin().getTowns();
    }

    /**
     * Get a list of {@link ClaimWorld}s
     *
     * @return A list of {@link ClaimWorld}s
     */
    @NotNull
    default List<ClaimWorld> getClaimWorlds() {
        return getPlugin().getClaimWorlds().values().stream().toList();
    }

    /**
     * Get a {@link ClaimWorld} by its id.
     *
     * @param id The id of the {@link ClaimWorld}
     * @return The {@link ClaimWorld}, if it exists
     * @since 2.0
     */
    default Optional<ClaimWorld> getClaimWorld(int id) {
        return getPlugin().getClaimWorlds().values().stream().filter(w -> w.getId() == id).findFirst();
    }

    /**
     * Get a list of {@link TownClaim}s in a particular {@link World}
     *
     * @param world The {@link World} to get claims in
     * @return A list of {@link TownClaim}s in the world
     * @since 2.0
     */
    default List<TownClaim> getClaims(@NotNull World world) {
        return getPlugin().getClaimWorld(world)
                .map(claimWorld -> claimWorld.getClaims(getPlugin()))
                .orElse(List.of());
    }

    /**
     * Update a {@link Town}
     *
     * @param user the {@link OnlineUser} to act as the executor of the update.
     *             In most cases this should be the user relevant to the update operation (i.e. the trigger).
     * @param town the {@link Town} to update
     * @since 2.0
     */
    default void updateTown(@NotNull OnlineUser user, @NotNull Town town) {
        getPlugin().getManager().updateTown(user, town);
    }

    /**
     * Get the {@link Member} mapping for a user, identifying the map of their {@link net.william278.husktowns.town.Role}
     * to the {@link Town} they are in
     *
     * @param user the user to get the member mapping for
     * @return the {@link Member} mapping for the user, if they are in a town
     * @since 2.0
     */
    default Optional<Member> getUserTown(@NotNull User user) {
        return getPlugin().getUserTown(user);
    }

}
