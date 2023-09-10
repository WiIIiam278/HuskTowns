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

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.advancement.Advancement;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.listener.Operation;
import net.william278.husktowns.map.ClaimMap;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
import net.william278.husktowns.util.Validator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Platform-agnostic HuskTowns API implementation, providing methods for interfacing with towns, claims and users.
 *
 * @since 2.0
 */
@SuppressWarnings("unused")
public interface IHuskTownsAPI {

    /**
     * <b>Internal use only</b> - Get the HuskTowns plugin instance
     *
     * @return The {@link HuskTowns} instance
     */
    @ApiStatus.Internal
    @NotNull
    HuskTowns getPlugin();

    /**
     * Returns if the plugin has finished loading data
     *
     * @return {@code true} if the plugin has finished loading data, {@code false} otherwise
     * @since 2.0
     */
    default boolean isLoaded() {
        return getPlugin().isLoaded();
    }

    /**
     * Sets the Town {@link Advancement Advancements} to be used by the plugin.
     * <p>
     * This will replace the current set of advancements (which by default is {@link Advancement#DEFAULT_ADVANCEMENTS}).
     *
     * @param advancements The root {@link Advancement} to use.
     * @throws IllegalStateException If advancements are disabled in the plugin settings
     * @apiNote Advancements set through this method are not persisted to the {@code advancements.json} file on disk
     * @since 2.4
     */
    default void setAdvancements(@NotNull Advancement advancements) throws IllegalStateException {
        if (!getPlugin().getSettings().doAdvancements()) {
            throw new IllegalStateException("Advancements are disabled in the config");
        }
        getPlugin().setAdvancements(advancements);
    }

    /**
     * Get the Town {@link Advancement Advancements} used by the plugin.
     *
     * @return The root {@link Advancement} used by the plugin, if advancements are enabled.
     * @since 2.4
     */
    default Optional<Advancement> getAdvancements() {
        return getPlugin().getAdvancements();
    }

    /**
     * Get the set of {@link Flag}s in use by the plugin.
     *
     * @return The set of {@link Flag}s currently in use
     * @since 2.5
     */
    @Unmodifiable
    default Set<Flag> getFlagSet() {
        return getPlugin().getFlags().getFlagSet();
    }

    /**
     * Get the {@link Flag} with the given name.
     *
     * @param name The name of the {@link Flag} to get
     * @return The {@link Flag} with the given name, if it exists
     * @since 2.5
     */
    default Optional<Flag> getFlag(@NotNull String name) {
        return getPlugin().getFlags().getFlag(name);
    }

    /**
     * Register one or more {@link Flag}s to be used by the plugin.
     *
     * @param flags The {@link Flag}s to register
     * @since 2.5
     */
    default void registerFlags(@NotNull Flag... flags) {
        final Set<Flag> flagSet = new LinkedHashSet<>(getFlagSet());
        flagSet.addAll(Arrays.asList(flags));
        getPlugin().getFlags().setFlags(flagSet);
    }


    /**
     * Get a list of {@link ClaimWorld}s on this server. If you want to get the {@link ClaimWorld}s on every server,
     * see {@link #getAllClaimWorlds()}.
     *
     * @return A list of {@link ClaimWorld}s
     * @since 2.0
     */
    @NotNull
    default List<ClaimWorld> getClaimWorlds() {
        return getPlugin().getClaimWorlds().values().stream().toList();
    }

    /**
     * Get the map of all {@link ClaimWorld}s on the network to each {@link ServerWorld}.
     * Only use this if you need every world on the network - otherwise, use {@link #getClaimWorlds()} to get the
     * worlds on this server.
     *
     * @return A map of {@link ServerWorld}s to {@link ClaimWorld}s in a future, which will complete when the data
     * is loaded from the database
     * @apiNote The future can complete exceptionally with an {@link IllegalStateException} if the plugin fails to
     * fetch the data from the database
     * @since 2.0
     */
    @NotNull
    default CompletableFuture<Map<ServerWorld, ClaimWorld>> getAllClaimWorlds() {
        final CompletableFuture<Map<ServerWorld, ClaimWorld>> future = new CompletableFuture<>();
        getPlugin().runAsync(() -> {
            try {
                final Map<ServerWorld, ClaimWorld> claimWorldMap = getPlugin().getDatabase().getAllClaimWorlds();
                future.complete(claimWorldMap);
            } catch (IllegalStateException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
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
     * Get a {@link ClaimWorld} by the {@link World} it maps to.
     *
     * @param world The {@link World} the {@link ClaimWorld} maps to
     * @return The {@link ClaimWorld}, if it exists
     * @since 2.0
     */
    default Optional<ClaimWorld> getClaimWorld(@NotNull World world) {
        return getPlugin().getClaimWorld(world);
    }

    /**
     * Update a {@link ClaimWorld} in the database
     *
     * @param claimWorld The {@link ClaimWorld} to update
     * @since 2.0
     */
    default void updateClaimWorld(@NotNull ClaimWorld claimWorld) {
        getPlugin().runAsync(() -> {
            getPlugin().getClaimWorlds().replaceAll((k, v) -> v.getId() == claimWorld.getId() ? claimWorld : v);
            getPlugin().getDatabase().updateClaimWorld(claimWorld);
        });
    }

    /**
     * Get a {@link ClaimWorld} by the {@link World} it maps to, and if it exists, edit it using the provided
     * {@link Consumer} and save the changes.
     *
     * @param world  The {@link World} the {@link ClaimWorld} maps to
     * @param editor A {@link Consumer} that edits the {@link ClaimWorld}
     * @since 2.0
     */
    default void editClaimWorld(@NotNull World world, @NotNull Consumer<ClaimWorld> editor) {
        getClaimWorld(world).ifPresent(claimWorld -> {
            editor.accept(claimWorld);
            updateClaimWorld(claimWorld);
        });
    }

    /**
     * Get a {@link ClaimWorld} by ID, and if it exists, edit it using the provided {@link Consumer}  and save the
     * changes.
     *
     * @param id     The ID of the {@link ClaimWorld}
     * @param editor A {@link Consumer} that edits the {@link ClaimWorld}
     * @since 2.0
     */
    default void editClaimWorld(int id, @NotNull Consumer<ClaimWorld> editor) {
        getClaimWorld(id).ifPresent(claimWorld -> {
            editor.accept(claimWorld);
            updateClaimWorld(claimWorld);
        });
    }

    /**
     * Get a list of {@link TownClaim}s in a particular {@link World}
     *
     * @param world The {@link World} to get claims in
     * @return A list of {@link TownClaim}s in the world
     * @since 2.0
     */
    default List<TownClaim> getClaims(@NotNull World world) {
        return getClaimWorld(world)
                .map(claimWorld -> claimWorld.getClaims(getPlugin()))
                .orElse(List.of());
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
     * Create a claim for a town
     *
     * @param actor The actor to use for creating the claim. Note that this user does not necessarily have to be a
     *              member of the town where the claim is being created.
     * @param town  The town to create the claim for
     * @param claim The claim to create
     * @param world The world the claim is in
     * @throws IllegalArgumentException if the claim overlaps with an existing claim
     * @since 2.0
     */
    default void createClaimAt(@NotNull OnlineUser actor, @NotNull Town town, @NotNull Claim claim, @NotNull World world) throws IllegalArgumentException {
        if (getClaimAt(claim.getChunk(), world).isPresent()) {
            throw new IllegalArgumentException("A claim already exists at " + claim.getChunk());
        }

        getPlugin().runAsync(() -> getPlugin().getManager().claims().createClaimData(actor, new TownClaim(town, claim), world));
    }

    /**
     * Create a claim for a town
     *
     * @param actor The actor to use for creating the claim. Note that this user does not necessarily have to be a
     *              member of the town where the claim is being created.
     * @param town  The town to create the claim for
     * @param chunk The chunk to make the claim in
     * @param world The world the claim is in
     * @throws IllegalArgumentException if the claim overlaps with an existing claim
     * @since 2.0
     */
    default void createClaimAt(@NotNull OnlineUser actor, @NotNull Town town, @NotNull Chunk chunk, @NotNull World world) {
        createClaimAt(actor, town, Claim.at(chunk), world);
    }

    /**
     * Create a claim for a town at a {@link Position}
     *
     * @param actor    The actor to use for creating the claim. Note that this user does not necessarily have to be a
     *                 member of the town where the claim is being created.
     * @param town     The town to create the claim for
     * @param position A {@link Position} that lies within the chunk to create the claim at
     * @throws IllegalArgumentException if the claim overlaps with an existing claim
     * @since 2.0
     */
    default void createClaimAt(@NotNull OnlineUser actor, @NotNull Town town, @NotNull Position position) {
        createClaimAt(actor, town, position.getChunk(), position.getWorld());
    }

    /**
     * Create an administrator-owned claim
     *
     * @param actor The actor for use for creating the claim. Note that this user does not necessarily have to have
     *              permission to create admin claims.
     * @param chunk The chunk to make the claim in
     * @param world The world the claim is in
     * @throws IllegalArgumentException if the claim overlaps with an existing claim
     * @since 2.0
     */
    default void createAdminClaimAt(@NotNull OnlineUser actor, @NotNull Chunk chunk, @NotNull World world) throws IllegalArgumentException {
        createClaimAt(actor, getPlugin().getAdminTown(), Claim.at(chunk), world);
    }

    /**
     * Create an administrator-owned claim
     *
     * @param actor    The actor for use for creating the claim. Note that this user does not necessarily have to have
     *                 permission to create admin claims.
     * @param position A {@link Position} that lies within the chunk to create the claim at
     * @throws IllegalArgumentException if the claim overlaps with an existing claim
     * @since 2.0
     */
    default void createAdminClaimAt(@NotNull OnlineUser actor, @NotNull Position position) {
        createAdminClaimAt(actor, position.getChunk(), position.getWorld());
    }

    /**
     * Delete the claim in the world at the specified chunk
     *
     * @param actor The actor for use for deleting the claim. Note that this user does not necessarily have to have
     *              the permission or privileges to delete the claim.
     * @param chunk The chunk to delete the claim at
     * @param world The world the claim is in
     * @throws IllegalArgumentException if there is no claim at the chunk in the world
     * @since 2.0
     */
    default void deleteClaimAt(@NotNull OnlineUser actor, @NotNull Chunk chunk, @NotNull World world) throws IllegalArgumentException {
        final TownClaim townClaim = getClaimAt(chunk, world)
                .orElseThrow(() -> new IllegalArgumentException("No claim exists at: " + chunk));
        getPlugin().runAsync(() -> getPlugin().getManager().claims().deleteClaimData(actor, townClaim, world));
    }

    /**
     * Delete the claim at the specified {@link Position}
     *
     * @param actor    The actor for use for deleting the claim. Note that this user does not necessarily have to have
     *                 the permission or privileges to delete the claim.
     * @param position A {@link Position} that lies within the claim to delete
     * @since 2.0
     */
    default void deleteClaimAt(@NotNull OnlineUser actor, @NotNull Position position) {
        deleteClaimAt(actor, position.getChunk(), position.getWorld());
    }

    /**
     * Update a town claim
     *
     * @param claim The claim to update
     * @param world The world the claim is in
     * @throws IllegalArgumentException if the claim does not exist
     * @since 2.0
     */
    default void updateClaim(@NotNull TownClaim claim, @NotNull World world) throws IllegalArgumentException {
        final ClaimWorld claimWorld = getClaimWorld(world)
                .orElseThrow(() -> new IllegalArgumentException("World \"" + world.getName() + "\" is not claimable"));
        getPlugin().runAsync(() -> {
            if (claim.isAdminClaim(getPlugin())) {
                return;
            }

            final ConcurrentLinkedQueue<Claim> claims = claimWorld.getClaims()
                    .computeIfAbsent(claim.town().getId(), k -> new ConcurrentLinkedQueue<>());
            claims.removeIf(c -> c.getChunk().equals(claim.claim().getChunk()));
            claims.add(claim.claim());
            getPlugin().getDatabase().updateClaimWorld(claimWorld);
        });
    }

    /**
     * Gets the {@link TownClaim} at a {@link Chunk} in a {@link World} and, if it exists, edits it through the
     * {@link Consumer} provided, then saves the changes.
     *
     * @param chunk  The chunk the claim to edit is in
     * @param world  The world the claim to edit is in
     * @param editor A {@link Consumer} that edits the claim
     * @apiNote If the claim does not exist, the consumer will not be called and no changes will be made.
     * @since 2.0
     */
    default void editClaimAt(@NotNull Chunk chunk, @NotNull World world, @NotNull Consumer<TownClaim> editor) {
        getClaimAt(chunk, world).ifPresent(claim -> {
            editor.accept(claim);
            updateClaim(claim, world);
        });
    }

    /**
     * Gets the {@link TownClaim} at a {@link Position} and, if it exists, edits it through the
     * {@link Consumer} provided, then saves the changes.
     *
     * @param position The {@link Position} that lies within the claim to edit
     * @param editor   A {@link Consumer} that edits the claim
     * @apiNote If the claim does not exist, the consumer will not be called and no changes will be made.
     * @since 2.0
     */
    default void editClaimAt(@NotNull Position position, @NotNull Consumer<TownClaim> editor) {
        editClaimAt(position.getChunk(), position.getWorld(), editor);
    }

    /**
     * Highlights a claim at a {@link Position} for a player.
     * <p/>
     * This will display particle effects around the edge of the chunk the claim is in on the surface level for 5s.
     *
     * @param user     The {@link OnlineUser} to highlight the claim for
     * @param position The {@link Position} that lies within the claim to highlight
     * @since 2.5.4
     */
    default void highlightClaimAt(@NotNull OnlineUser user, @NotNull Position position) {
        this.getClaimAt(position).ifPresent(claim -> this.highlightClaim(user, claim));
    }

    /**
     * Highlights a claim at a {@link Chunk} for a player
     * <p/>
     * This will display particle effects around the edge of the chunk the claim is in on the surface level for 5s.
     *
     * @param user  The {@link OnlineUser} to highlight the claim for
     * @param chunk The {@link Chunk} that lies within the claim to highlight
     * @param world The {@link World} the chunk is in
     * @since 2.5.4
     */
    default void highlightClaimAt(@NotNull OnlineUser user, @NotNull Chunk chunk, @NotNull World world) {
        this.getClaimAt(chunk, world).ifPresent(claim -> this.highlightClaim(user, claim));
    }

    /**
     * Highlights a {@link TownClaim claim} for a player
     * <p/>
     * This will display particle effects around the edge of the chunk the claim is in on the surface level for 5s.
     *
     * @param user  The {@link OnlineUser} to highlight the claim for
     * @param claim The {@link TownClaim} to highlight
     * @since 2.5.4
     */
    default void highlightClaim(@NotNull OnlineUser user, @NotNull TownClaim claim) {
        getPlugin().highlightClaim(user, claim);
    }

    /**
     * Highlights a {@link TownClaim claim} for a player for a specified duration (in seconds)
     * <p/>
     * This will display particle effects around the edge of all the chunks the claims occupy on the surface level.
     *
     * @param user     The {@link OnlineUser} to highlight the claim for
     * @param claim    The {@link TownClaim} to highlight
     * @param duration The duration (in seconds) to highlight the claim for
     */
    default void highlightClaim(@NotNull OnlineUser user, @NotNull TownClaim claim, long duration) {
        getPlugin().highlightClaims(user, List.of(claim), duration);
    }

    /**
     * Highlight several {@link TownClaim}s for a player
     * <p/>
     * This will display particle effects around the edge of all the chunks the claims occupy on the surface level.
     *
     * @param user   The {@link OnlineUser} to highlight the claims for
     * @param claims The list of {@link TownClaim}s to highlight
     * @since 2.5.4
     */
    default void highlightClaims(@NotNull OnlineUser user, @NotNull Collection<TownClaim> claims) {
        getPlugin().highlightClaims(user, claims.stream().toList());
    }

    /**
     * Highlight several {@link TownClaim}s for a player for a specified duration (in seconds)
     * <p/>
     * This will display particle effects around the edge of all the chunks the claims occupy on the surface level.
     *
     * @param user     The {@link OnlineUser} to highlight the claims for
     * @param claims   The list of {@link TownClaim}s to highlight
     * @param duration The duration (in seconds) to highlight the claims for
     * @since 2.5.4
     */
    default void highlightClaims(@NotNull OnlineUser user, @NotNull Collection<TownClaim> claims, long duration) {
        getPlugin().highlightClaims(user, claims.stream().toList(), duration);
    }

    /**
     * Stop highlighting claims for a player
     * <p/>
     * This will remove any active particle highlighting effects from the player.
     *
     * @param user The {@link OnlineUser} to stop highlighting claims for
     * @since 2.5.4
     */
    default void stopHighlightingClaims(@NotNull OnlineUser user) {
        getPlugin().stopHighlightingClaims(user);
    }

    /**
     * Get a {@link ClaimMap} with the specified width and height, centered on the specified {@link Chunk} in the
     * specified {@link World}.
     * </p>
     * ClaimMaps are used to visualise claims on a chat map and can be displayed as an adventure component.
     *
     * @param width  The width of the map
     * @param height The height of the map
     * @param center The {@link Chunk} to center the map on
     * @param world  The {@link World} the chunk is in
     * @return The {@link ClaimMap}
     * @throws IllegalArgumentException if the width or height is less than 1
     * @since 2.5.4
     */
    @NotNull
    default ClaimMap getClaimMap(int width, int height, @NotNull Chunk center, @NotNull World world) throws IllegalArgumentException {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Width and height must be greater than 0");
        }
        return ClaimMap.builder(getPlugin())
                .width(width)
                .height(height)
                .center(center)
                .world(world)
                .build();
    }

    /**
     * Get a {@link ClaimMap}, centered on the specified {@link Chunk} in the specified {@link World}.
     * </p>
     * The map will use the default width/height specified in the plugin settings.
     *
     * @param center The {@link Chunk} to center the map on
     * @param world  The {@link World} the chunk is in
     * @return The {@link ClaimMap}
     * @since 2.5.4
     */
    @NotNull
    default ClaimMap getClaimMap(@NotNull Chunk center, @NotNull World world) {
        return ClaimMap.builder(getPlugin())
                .center(center)
                .world(world)
                .build();
    }

    /**
     * Get a {@link ClaimMap}, centered on the specified {@link Position}.
     * </p>
     * The map will use the default width/height specified in the plugin settings.
     *
     * @param position The {@link Position} to center the map on
     * @return The {@link ClaimMap}
     * @since 2.5.4
     */
    @NotNull
    default ClaimMap getClaimMap(@NotNull Position position) {
        return ClaimMap.builder(getPlugin())
                .center(position.getChunk())
                .world(position.getWorld())
                .build();
    }

    /**
     * Get the adventure {@link Component} representation of a {@link ClaimMap} centered on a specified {@link Position}
     * <p>
     * The map will use the default width/height specified in the plugin settings.
     * </p>
     * This is a shortcut for {@code #getClaimMap(Position)#getComponent()}
     *
     * @param position The {@link Position} to center the map on
     * @return The {@link Component} representation of the {@link ClaimMap}
     * @since 2.5.4
     */
    @NotNull
    default Component getClaimMapComponent(@NotNull Position position) {
        return getClaimMap(position).toComponent();
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
     * Create a new {@link Town}
     *
     * @param creator The creator of the town
     * @param name    The name of the town
     * @return The created {@link Town} in a future, that will complete when it has been made
     * @throws IllegalArgumentException if the town name is invalid
     * @since 2.0
     */
    @NotNull
    default CompletableFuture<Town> createTown(@NotNull OnlineUser creator, @NotNull String name) throws IllegalArgumentException {
        final CompletableFuture<Town> townFuture = new CompletableFuture<>();
        if (!getPlugin().getValidator().isValidTownName(name)) {
            throw new IllegalArgumentException("Invalid town name: " + name);
        }
        getPlugin().runAsync(() -> townFuture.complete(getPlugin().getManager().towns().createTownData(creator, name)));
        return townFuture;
    }

    /**
     * Delete a {@link Town}
     *
     * @param actor An actor to delete the town. Note that they do not necessarily need to be the mayor, or even a
     *              member of the town being deleted
     * @param town  The town to delete
     * @since 2.0
     */
    default void deleteTown(@NotNull OnlineUser actor, @NotNull Town town) {
        getPlugin().runAsync(() -> getPlugin().getManager().towns().deleteTownData(actor, town));
    }

    /**
     * Update a {@link Town}
     *
     * @param user the {@link OnlineUser} to act as the executor of the update.
     *             In most cases this should be the user relevant to the update operation (i.e. the trigger).
     * @param town the {@link Town} to update
     * @throws IllegalArgumentException if the town has an invalid name, bio, greeting or farewell message
     * @since 2.0
     */
    default void updateTown(@NotNull OnlineUser user, @NotNull Town town) throws IllegalArgumentException {
        final Validator validator = getPlugin().getValidator();
        if (!validator.isLegalTownName(town.getName())) {
            throw new IllegalArgumentException("Invalid town name: " + town.getName());
        }
        if (!town.getBio().map(validator::isValidTownMetadata).orElse(true)) {
            throw new IllegalArgumentException("Invalid bio: " + town.getGreeting().orElse(""));
        }
        if (!town.getGreeting().map(validator::isValidTownMetadata).orElse(true)) {
            throw new IllegalArgumentException("Invalid greeting message: " + town.getGreeting().orElse(""));
        }
        if (!town.getFarewell().map(validator::isValidTownMetadata).orElse(true)) {
            throw new IllegalArgumentException("Invalid farewell message: " + town.getGreeting().orElse(""));
        }

        getPlugin().runAsync(() -> getPlugin().getManager().updateTownData(user, town));
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
    default void editTown(@NotNull OnlineUser actor, @NotNull String townName, @NotNull Consumer<Town> editor) throws IllegalArgumentException {
        getTown(townName).ifPresent(town -> {
            editor.accept(town);
            updateTown(actor, town);
        });
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
    default void editTown(@NotNull OnlineUser actor, int townId, @NotNull Consumer<Town> editor) throws IllegalArgumentException {
        getTown(townId).ifPresent(town -> {
            editor.accept(town);
            updateTown(actor, town);
        });
    }

    /**
     * Get a list of all {@link Town}s.
     *
     * @return A list of all {@link Town}s
     * @since 2.0
     */
    @NotNull
    default List<Town> getTowns() {
        return getPlugin().getTowns().stream().toList();
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

    /**
     * Get a {@link User} by their {@link UUID}
     *
     * @param user the {@link UUID} of the user
     * @return An optional {@link User}, if they exist, in a future completing when the database lookup has completed
     * @since 2.0
     */
    default CompletableFuture<Optional<User>> getUser(@NotNull UUID user) {
        final CompletableFuture<Optional<User>> userFuture = new CompletableFuture<>();
        getPlugin().runAsync(() -> userFuture.complete(getPlugin().getDatabase().getUser(user).map(SavedUser::user)));
        return userFuture;
    }

    /**
     * Get a {@link User} by their Minecraft account username
     *
     * @param username the username of the user
     * @return An optional {@link User}, if they exist, in a future completing when the database lookup has completed
     * @since 2.0
     */
    default CompletableFuture<Optional<User>> getUser(@NotNull String username) {
        final CompletableFuture<Optional<User>> userFuture = new CompletableFuture<>();
        getPlugin().runAsync(() -> userFuture.complete(getPlugin().getDatabase().getUser(username).map(SavedUser::user)));
        return userFuture;
    }

    /**
     * Get a player's Minecraft username by their {@link UUID}
     *
     * @param uuid the {@link UUID} of the player
     * @return the player's Minecraft username, if they exist, in a future completing when the database lookup has completed
     * @since 2.0
     */
    default CompletableFuture<Optional<String>> getUsername(@NotNull UUID uuid) {
        return getUser(uuid).thenApply(user -> user.map(User::getUsername));
    }

    /**
     * Get a player's {@link UUID} by their Minecraft username
     *
     * @param username the Minecraft username of the player
     * @return the player's {@link UUID}, if they exist, in a future completing when the database lookup has completed
     * @since 2.0
     */
    default CompletableFuture<Optional<UUID>> getUserUuid(@NotNull String username) {
        return getUser(username).thenApply(user -> user.map(User::getUuid));
    }

    /**
     * Get an {@link OnlineUser}'s {@link Preferences}
     *
     * @param user the {@link OnlineUser} to get the {@link Preferences} for
     * @return the {@link Preferences} for the {@link OnlineUser}
     * @since 2.0
     */
    @NotNull
    default Preferences getUserPreferences(@NotNull OnlineUser user) {
        return getPlugin().getUserPreferences(user.getUuid()).orElse(Preferences.getDefaults());
    }

    /**
     * Get a raw locale from the plugin locale file
     *
     * @param localeId     the locale ID to get
     * @param replacements the replacements to make in the locale
     * @return the locale, with replacements made
     * @since 2.0
     */
    default Optional<String> getRawLocale(@NotNull String localeId, @NotNull String... replacements) {
        return getPlugin().getLocales().getRawLocale(localeId, replacements);
    }

    /**
     * Get a locale from the plugin locale file
     *
     * @param localeId     the locale ID to get
     * @param replacements the replacements to make in the locale
     * @return the locale as a formatted adventure {@link Component}, with replacements made
     * @since 2.0
     */
    default Optional<Component> getLocale(@NotNull String localeId, @NotNull String... replacements) {
        return getPlugin().getLocales().getLocale(localeId, replacements).map(MineDown::toComponent);
    }

}
