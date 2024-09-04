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

package net.william278.husktowns.api;

import net.kyori.adventure.text.Component;
import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.map.ClaimMap;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * The Bukkit implementation of the HuskTowns API. Get the instance with {@link #getInstance()}.
 *
 * @since 3.0
 */
@SuppressWarnings("unused")
public class BukkitHuskTownsAPI extends HuskTownsAPI {

    /**
     * <b>(Internal use only)</b> - Constructor, instantiating the API.
     *
     * @param plugin The HuskTowns plugin instance
     * @since 1.0
     */
    protected BukkitHuskTownsAPI(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    /**
     * Get an instance of the HuskTowns API.
     *
     * @return instance of the HuskTowns API
     * @throws NotRegisteredException if the API has not yet been registered.
     * @since 1.0
     */
    @NotNull
    public static BukkitHuskTownsAPI getInstance() throws NotRegisteredException {
        return (BukkitHuskTownsAPI) HuskTownsAPI.getInstance();
    }

    /**
     * <b>(Internal use only)</b> - Unregister the API instance.
     *
     * @since 1.0
     */
    @ApiStatus.Internal
    public static void register(@NotNull BukkitHuskTowns plugin) {
        instance = new BukkitHuskTownsAPI(plugin);
    }

    /**
     * Get a {@link ClaimWorld} by the {@link org.bukkit.World} it maps to.
     *
     * @param world The {@link org.bukkit.World} the {@link ClaimWorld} maps to
     * @return The {@link ClaimWorld}, if it exists
     * @since 2.0
     */
    public Optional<ClaimWorld> getClaimWorld(@NotNull org.bukkit.World world) {
        return plugin.getClaimWorld(getWorld(world));
    }

    /**
     * Get a {@link ClaimWorld} by the {@link org.bukkit.World} it maps to, and if it exists, edit it using the provided
     * {@link Consumer} and save the changes.
     *
     * @param world  The {@link org.bukkit.World} the {@link ClaimWorld} maps to
     * @param editor A {@link Consumer} that edits the {@link ClaimWorld}
     * @since 2.0
     */
    public void editClaimWorld(@NotNull org.bukkit.World world, @NotNull Consumer<ClaimWorld> editor) {
        editClaimWorld(getWorld(world), editor);
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
     * Edit the claim at the specified {@link org.bukkit.Chunk}
     *
     * @param chunk  The {@link org.bukkit.Chunk} to edit the claim at
     * @param editor A {@link Consumer} that edits the claim
     * @since 2.0
     */
    public void editClaimAt(@NotNull org.bukkit.Chunk chunk, @NotNull Consumer<TownClaim> editor) {
        editClaimAt(Chunk.at(chunk.getX(), chunk.getZ()), getWorld(chunk.getWorld()), editor);
    }

    /**
     * Edit the claim at the specified {@link Location}
     *
     * @param location A {@link Location} that lies within the claim to edit
     * @param editor   A {@link Consumer} that edits the claim
     * @since 2.0
     */
    public void editClaimAt(@NotNull Location location, @NotNull Consumer<TownClaim> editor) {
        editClaimAt(getPosition(location), editor);
    }

    /**
     * Highlights a claim at a {@link Position} for a player.
     * <p/>
     * This will display particle effects around the edge of the chunk the claim is in on the surface level.
     *
     * @param player   The {@link Player} to highlight the claim for
     * @param location The {@link Location} that lies within the claim to highlight
     * @since 2.5.4
     */
    public void highlightClaimAt(@NotNull Player player, @NotNull Location location) {
        highlightClaimAt(getOnlineUser(player), getPosition(location));
    }

    /**
     * Highlights a claim at a {@link Position} for a player.
     * <p/>
     * This will display particle effects around the edge of the chunk the claim is in on the surface level for 5s.
     *
     * @param player The {@link Player} to highlight the claim for
     * @param chunk  The {@link org.bukkit.Chunk} that the claim occupies to highlight
     * @since 2.5.4
     */
    public void highlightClaimAt(@NotNull Player player, @NotNull org.bukkit.Chunk chunk) {
        highlightClaimAt(getOnlineUser(player), Chunk.at(chunk.getX(), chunk.getZ()), getWorld(chunk.getWorld()));
    }

    /**
     * Highlights a {@link TownClaim claim} for a player
     * <p/>
     * This will display particle effects around the edge of the chunk the claim is in on the surface level for 5s.
     *
     * @param player The {@link Player} to highlight the claim for
     * @param claim  The {@link TownClaim} to highlight
     * @since 2.5.4
     */
    public void highlightClaim(@NotNull Player player, @NotNull TownClaim claim) {
        highlightClaim(getOnlineUser(player), claim);
    }

    /**
     * Highlights a {@link TownClaim claim} for a player for a specified duration (in seconds)
     *
     * @param player   The {@link Player} to highlight the claim for
     * @param claim    The {@link TownClaim} to highlight
     * @param duration The duration (in seconds) to highlight the claim for
     */
    public void highlightClaim(@NotNull Player player, @NotNull TownClaim claim, long duration) {
        highlightClaim(getOnlineUser(player), claim, duration);
    }

    /**
     * Highlight several {@link TownClaim}s for a player
     * <p/>
     * This will display particle effects around the edge of all the chunks the claims occupy on the surface level for 5s.
     *
     * @param player The {@link OnlineUser} to highlight the claims for
     * @param claims The list of {@link TownClaim}s to highlight
     * @since 2.5.4
     */
    public void highlightClaims(@NotNull Player player, @NotNull Collection<TownClaim> claims) {
        highlightClaims(getOnlineUser(player), claims);
    }

    /**
     * Highlight several {@link TownClaim}s for a player for a specified duration (in seconds)
     * <p/>
     * This will display particle effects around the edge of all the chunks the claims occupy on the surface level.
     *
     * @param player   The {@link OnlineUser} to highlight the claims for
     * @param claims   The list of {@link TownClaim}s to highlight
     * @param duration The duration (in seconds) to highlight the claims for
     * @since 2.5.4
     */
    public void highlightClaims(@NotNull Player player, @NotNull Collection<TownClaim> claims, long duration) {
        highlightClaims(getOnlineUser(player), claims, duration);
    }

    /**
     * Stop highlighting claims for a player
     * <p/>
     * This will remove any active particle highlighting effects from the player.
     *
     * @param player The {@link Player} to stop highlighting claims for
     * @since 2.5.4
     */
    public void stopHighlightingClaims(@NotNull Player player) {
        stopHighlightingClaims(getOnlineUser(player));
    }

    /**
     * Get a {@link ClaimMap} centered on a specified {@link org.bukkit.Chunk}.
     * <p/>
     * The map will be centered on the specified chunk, and will be the size specified in the config.
     *
     * @param chunk The {@link org.bukkit.Chunk} to center the map on
     * @return The {@link ClaimMap} centered on the chunk
     * @since 2.5.4
     */
    @NotNull
    public ClaimMap getClaimMap(@NotNull org.bukkit.Chunk chunk) {
        return getClaimMap(Chunk.at(chunk.getX(), chunk.getZ()), getWorld(chunk.getWorld()));
    }

    /**
     * Get a {@link ClaimMap} centered on a specified {@link Location}.
     *
     * @param location The {@link Location} to center the map on
     * @return The {@link ClaimMap} centered on the location
     * @since 2.5.4
     */
    @NotNull
    public ClaimMap getClaimMap(@NotNull Location location) {
        return getClaimMap(getPosition(location));
    }

    /**
     * Get the adventure {@link Component} representation of a {@link ClaimMap} centered on a specified {@link Location}
     * <p>
     * The map will use the default width/height specified in the plugin settings.
     * </p>
     * This is a shortcut for {@code #getClaimMap(Position)#getComponent()}
     *
     * @param location The {@link Location} to center the map on
     * @return The {@link Component} representation of the {@link ClaimMap}
     * @since 2.5.4
     */
    @NotNull
    public Component getClaimMapComponent(@NotNull Location location) {
        return getClaimMapComponent(getPosition(location));
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
     * Gets the {@link Town} by name, and, if it exists, edits it through the {@link Consumer} provided, then saves the
     * changes.
     *
     * @param actor    An actor to edit the town. Note that they do not necessarily need to be a member of or have privileges
     *                 in the town being edited
     * @param townName The name of the town to edit
     * @param editor   A {@link Consumer} that edits the town
     * @throws IllegalArgumentException if the town has an invalid name, bio, greeting or farewell message after the edits
     * @since 2.0
     */
    public void editTown(@NotNull Player actor, @NotNull String townName, @NotNull Consumer<Town> editor) throws IllegalArgumentException {
        editTown(getOnlineUser(actor), townName, editor);
    }

    /**
     * Gets the {@link Town} by ID, and, if it exists, edits it through the {@link Consumer} provided, then saves the
     * changes.
     *
     * @param actor  An actor to edit the town. Note that they do not necessarily need to be a member of or have privileges
     *               in the town being edited
     * @param townId The ID of the town to edit
     * @param editor A {@link Consumer} that edits the town
     * @throws IllegalArgumentException if the town has an invalid name, bio, greeting or farewell message after the edits
     * @since 2.0
     */
    public void editTown(@NotNull Player actor, int townId, @NotNull Consumer<Town> editor) throws IllegalArgumentException {
        editTown(getOnlineUser(actor), townId, editor);
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
        return BukkitUser.adapt(player, plugin);
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

}
