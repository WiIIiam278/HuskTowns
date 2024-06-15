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

package net.william278.husktowns.manager;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.hook.WorldGuardHook;
import net.william278.husktowns.map.ClaimMap;
import net.william278.husktowns.network.Message;
import net.william278.husktowns.network.Payload;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ClaimsManager {
    private final HuskTowns plugin;

    protected ClaimsManager(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    public void createClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, boolean showMap) {
        plugin.getManager().ifMember(user, Privilege.CLAIM, (member -> {
            final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
            if (existingClaim.isPresent()) {
                plugin.getLocales().getLocale("error_chunk_claimed_by", existingClaim.get().town().getName())
                        .ifPresent(user::sendMessage);
                return;
            }
            final Optional<WorldGuardHook> worldGuardHook = plugin.getHookManager().getHook(WorldGuardHook.class);
            if (worldGuardHook.isPresent() && worldGuardHook.get().isChunkInRestrictedRegion(chunk, world.getName())) {
                plugin.getLocales().getLocale("error_chunk_not_claimable").ifPresent(user::sendMessage);
                return;
            }

            // Get the claim world
            final Optional<ClaimWorld> optionalClaimWorld = plugin.getClaimWorld(world);
            if (optionalClaimWorld.isEmpty()) {
                plugin.getLocales().getLocale("error_world_not_claimable")
                        .ifPresent(user::sendMessage);
                return;
            }
            final ClaimWorld claimWorld = optionalClaimWorld.get();

            // Carry out adjacency check
            final Settings.TownSettings settings = plugin.getSettings().getTowns();
            if (settings.isRequireClaimAdjacency() && member.town().getClaimCount() > 0) {
                final Optional<TownClaim> adjacentClaim = claimWorld
                        .getAdjacentClaims(chunk, plugin).stream()
                        .filter(claim -> claim.town().equals(member.town()))
                        .findFirst();
                if (adjacentClaim.isEmpty()) {
                    plugin.getLocales().getLocale("error_claim_not_adjacent")
                            .ifPresent(user::sendMessage);
                    return;
                }
            }

            // Carry out minimum chunk separation check
            final Optional<TownClaim> nearbyClaim = claimWorld
                    .getClaimsNear(chunk, settings.getMinimumChunkSeparation(), plugin).stream()
                    .filter(claim -> !claim.town().equals(member.town()))
                    .findFirst();
            if (nearbyClaim.isPresent()) {
                plugin.getLocales().getLocale("error_claim_too_close_to", nearbyClaim.get().town().getName())
                        .ifPresent(user::sendMessage);
                return;
            }

            final Claim claim = Claim.at(chunk);
            final Town town = member.town();
            if (town.getClaimCount() + 1 > town.getMaxClaims(plugin)) {
                plugin.getLocales().getLocale("error_claim_limit_reached", Integer.toString(town.getClaimCount()),
                                Integer.toString(town.getMaxClaims(plugin)))
                        .ifPresent(user::sendMessage);
                return;
            }

            // Create the claim, firing the event
            final TownClaim townClaim = new TownClaim(town, claim);
            plugin.fireEvent(plugin.getClaimEvent(user, townClaim), (event -> {
                createClaimData(user, townClaim, world);
                plugin.getLocales().getLocale("claim_created", Integer.toString(chunk.getX()),
                                Integer.toString(chunk.getZ()), town.getName())
                        .ifPresent(user::sendMessage);

                // Visualize the claim
                plugin.highlightClaim(user, townClaim);
                if (showMap) {
                    user.sendMessage(ClaimMap.builder(plugin)
                            .center(user.getChunk()).world(user.getWorld())
                            .build()
                            .toComponent(user));
                }
            }));
        }));
    }

    public void createClaimData(@NotNull OnlineUser user, @NotNull TownClaim claim, @NotNull World world) throws IllegalArgumentException {
        final ClaimWorld claimWorld = plugin.getClaimWorld(world)
                .orElseThrow(() -> new IllegalArgumentException("World \"" + world.getName() + "\" is not claimable"));
        if (claim.isAdminClaim(plugin)) {
            claimWorld.addAdminClaim(claim.claim());
        } else {
            claimWorld.addClaim(claim);
            plugin.getManager().editTown(user, claim.town(), (town -> {
                town.setClaimCount(town.getClaimCount() + 1);
                town.getLog().log(Action.of(user, Action.Type.CREATE_CLAIM, claim.claim().toString()));

                if (town.getClaimCount() == 1 && plugin.getSettings().getGeneral().isFirstClaimAutoSetspawn()) {
                    plugin.getManager().towns().setTownSpawn(user, user.getPosition());
                }
            }));
        }
        plugin.getDatabase().updateClaimWorld(claimWorld);
        plugin.getMapHook().ifPresent(map -> map.setClaimMarker(claim, world));
    }

    public void deleteClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, boolean showMap) {
        plugin.getManager().ifMember(user, Privilege.UNCLAIM, (member -> {
            final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
            if (existingClaim.isEmpty()) {
                plugin.getLocales().getLocale("error_chunk_not_claimed")
                        .ifPresent(user::sendMessage);
                return;
            }

            final TownClaim claim = existingClaim.get();
            if (plugin.getClaimWorld(world).isEmpty()) {
                plugin.getLocales().getLocale("error_world_not_claimable")
                        .ifPresent(user::sendMessage);
                return;
            }

            final Town town = member.town();
            if (!claim.town().equals(town)) {
                plugin.getLocales().getLocale("error_chunk_claimed_by", claim.town().getName())
                        .ifPresent(user::sendMessage);
                return;
            }

            // If the town spawn is within the chunk
            if (town.getSpawn().isPresent()) {
                if (claim.contains(town.getSpawn().get().getPosition())) {
                    if (!member.hasPrivilege(plugin, Privilege.SET_SPAWN)) {
                        plugin.getLocales().getLocale("error_cannot_delete_spawn_claim")
                                .ifPresent(user::sendMessage);
                        return;
                    }
                    plugin.getManager().towns().clearTownSpawn(user);
                }
            }

            plugin.fireEvent(plugin.getUnClaimEvent(user, claim), (event -> {
                // Delete the claim
                deleteClaimData(user, claim, world);
                plugin.getLocales().getLocale("claim_deleted", Integer.toString(chunk.getX()),
                                Integer.toString(chunk.getZ()), town.getName())
                        .ifPresent(user::sendMessage);

                if (showMap) {
                    user.sendMessage(ClaimMap.builder(plugin)
                            .center(user.getChunk()).world(user.getWorld())
                            .build()
                            .toComponent(user));
                }
            }));
        }));
    }

    public void deleteAllClaimsConfirm(@NotNull OnlineUser user, boolean confirmed) {
        plugin.getManager().ifMayor(user, (mayor) -> {
            if (!confirmed) {
                plugin.getLocales().getLocale("delete_all_claims_confirm")
                        .ifPresent(user::sendMessage);
                return;
            }

            if (mayor.town().getClaimCount() == 0) {
                plugin.getLocales().getLocale("error_town_no_claims")
                        .ifPresent(user::sendMessage);
                return;
            }

            this.deleteAllClaims(user, mayor.town());
        });
    }

    public void deleteAllClaims(@NotNull OnlineUser user, @NotNull Town town) throws IllegalStateException {
        plugin.fireEvent(plugin.getUnClaimAllEvent(user, town), (event -> {
            try {
                plugin.getMapHook().ifPresent(mapHook -> mapHook.removeClaimMarkers(town));
                plugin.getClaimWorlds().values().forEach(world -> world.removeTownClaims(town.getId()));
                plugin.getDatabase().getAllClaimWorlds().forEach((serverWorld, claimWorld) -> {
                    if (serverWorld.server().equals(plugin.getServerName())) {
                        if (claimWorld.removeTownClaims(town.getId()) > 0) {
                            plugin.getDatabase().updateClaimWorld(claimWorld);
                        }
                    }
                });
                plugin.getManager().editTown(user, town, (townToEdit -> {
                    townToEdit.setClaimCount(0);
                    townToEdit.clearSpawn();
                    townToEdit.getLog().log(Action.of(user, Action.Type.DELETE_ALL_CLAIMS));
                }));

                // Propagate the claim deletion to all servers
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_DELETE_ALL_CLAIMS)
                        .payload(Payload.integer(town.getId()))
                        .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                        .build()
                        .send(broker, user));

                // Send notification
                plugin.getManager().sendTownMessage(town, plugin.getLocales()
                        .getLocale("deleted_all_claims_notification", town.getName())
                        .map(MineDown::toComponent).orElse(Component.empty()));
            } catch (IllegalStateException e) {
                plugin.log(Level.SEVERE, "Failed to delete all claims for town " + town.getName(), e);
            }
        }));
    }

    public void deleteClaimData(@NotNull OnlineUser user, @NotNull TownClaim claim, @NotNull World world) throws IllegalArgumentException {
        final ClaimWorld claimWorld = plugin.getClaimWorld(world)
                .orElseThrow(() -> new IllegalArgumentException("World \"" + world.getName() + "\" is not claimable"));
        if (claim.isAdminClaim(plugin)) {
            claimWorld.removeAdminClaim(claim.claim().getChunk());
            plugin.getDatabase().updateClaimWorld(claimWorld);
            plugin.getMapHook().ifPresent(map -> map.removeClaimMarker(claim, world));
            return;
        }

        plugin.getManager().editTown(user, claim.town(), (town -> {
            claimWorld.removeClaim(claim.town(), claim.claim().getChunk());
            town.setClaimCount(town.getClaimCount() - 1);
            town.getLog().log(Action.of(user, Action.Type.DELETE_CLAIM, claim.claim().toString()));
        }), (town -> {
            plugin.getDatabase().updateClaimWorld(claimWorld);
            plugin.getMapHook().ifPresent(map -> map.removeClaimMarker(claim, world));
        }));
    }

    public void makeClaimPlot(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {
        plugin.getManager().ifMember(user, Privilege.SET_PLOT, member ->
                plugin.getManager().ifClaimOwner(member, user, chunk, world, townClaim -> {
                    if (townClaim.claim().getType() == Claim.Type.PLOT) {
                        makeClaimRegular(user, world, chunk);
                        return;
                    }
                    final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
                    assert claimWorld.isPresent();

                    plugin.runAsync(() -> {
                        townClaim.claim().setType(Claim.Type.PLOT);
                        plugin.getDatabase().updateClaimWorld(claimWorld.get());
                        plugin.getManager().editTown(user, townClaim.town(), (town -> town.getLog()
                                .log(Action.of(user, Action.Type.MAKE_CLAIM_PLOT, townClaim.claim().toString()))));
                        plugin.getLocales().getLocale("claim_made_plot", Integer.toString(chunk.getX()),
                                Integer.toString(chunk.getZ())).ifPresent(user::sendMessage);
                    });
                }));
    }

    public void makeClaimFarm(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {
        plugin.getManager().ifMember(user, Privilege.SET_FARM, member ->
                plugin.getManager().ifClaimOwner(member, user, chunk, world, townClaim -> {
                    if (townClaim.claim().getType() == Claim.Type.FARM) {
                        makeClaimRegular(user, world, chunk);
                        return;
                    }
                    final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
                    assert claimWorld.isPresent();

                    plugin.runAsync(() -> {
                        townClaim.claim().setType(Claim.Type.FARM);
                        plugin.getDatabase().updateClaimWorld(claimWorld.get());
                        plugin.getManager().editTown(user, townClaim.town(), (town -> town.getLog()
                                .log(Action.of(user, Action.Type.MAKE_CLAIM_FARM, townClaim.claim().toString()))));

                        plugin.getLocales().getLocale("claim_made_farm", Integer.toString(chunk.getX()),
                                Integer.toString(chunk.getZ())).ifPresent(user::sendMessage);
                    });
                }));
    }

    public void makeClaimRegular(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {
        plugin.getManager().ifMember(user, Privilege.SET_FARM, (member ->
                plugin.getManager().ifClaimOwner(member, user, chunk, world, (townClaim -> {
                    final Claim claim = townClaim.claim();
                    if (claim.getType() == Claim.Type.CLAIM) {
                        plugin.getLocales().getLocale("error_claim_already_regular")
                                .ifPresent(user::sendMessage);
                        return;
                    }
                    final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
                    assert claimWorld.isPresent();

                    plugin.runAsync(() -> {
                        claim.setType(Claim.Type.CLAIM);
                        plugin.getDatabase().updateClaimWorld(claimWorld.get());
                        plugin.getManager().editTown(user, townClaim.town(), (town -> town.getLog()
                                .log(Action.of(user, Action.Type.MAKE_CLAIM_REGULAR, townClaim.claim().toString()))));

                        plugin.getLocales().getLocale("claim_made_regular", Integer.toString(chunk.getX()),
                                Integer.toString(chunk.getZ())).ifPresent(user::sendMessage);
                    });
                }))));
    }

    public void addPlotMember(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, @NotNull String target, boolean manager) {
        plugin.getUserTown(user).ifPresentOrElse(member -> plugin.getManager()
                .ifClaimOwner(member, user, chunk, world, (claim -> {
                    if (claim.claim().getType() != Claim.Type.PLOT) {
                        plugin.getLocales().getLocale("error_claim_not_plot")
                                .ifPresent(user::sendMessage);
                        return;
                    }
                    if (!claim.claim().isPlotManager(user.getUuid()) && !member.hasPrivilege(plugin, Privilege.MANAGE_PLOT_MEMBERS)) {
                        plugin.getLocales().getLocale("error_insufficient_privileges", member.town().getName())
                                .ifPresent(user::sendMessage);
                        return;
                    }

                    if (manager && !member.hasPrivilege(plugin, Privilege.MANAGE_PLOT_MEMBERS)) {
                        plugin.getLocales().getLocale("error_plot_manager_privilege")
                                .ifPresent(user::sendMessage);
                        return;
                    }

                    final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
                    assert claimWorld.isPresent();

                    plugin.runAsync(() -> {
                        final Optional<User> targetUser = plugin.getDatabase().getUser(target).map(SavedUser::user);
                        if (targetUser.isEmpty()) {
                            plugin.getLocales().getLocale("error_user_not_found", target)
                                    .ifPresent(user::sendMessage);
                            return;
                        }

                        claim.claim().setPlotMember(targetUser.get().getUuid(), manager);
                        plugin.getDatabase().updateClaimWorld(claimWorld.get());
                        plugin.getManager().editTown(user, claim.town(), (town -> town.getLog().log(Action.of(user,
                                Action.Type.ADD_PLOT_MEMBER, claim.claim() + ": +" + targetUser.get().getUsername()))));

                        plugin.getLocales().getLocale("plot_member_added", targetUser.get().getUsername(),
                                        Integer.toString(chunk.getX()), Integer.toString(chunk.getZ()))
                                .ifPresent(user::sendMessage);
                    });
                })), () -> plugin.getLocales().getLocale("error_not_in_town").ifPresent(user::sendMessage));
    }

    public void removePlotMember(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, @NotNull String target) {
        plugin.getUserTown(user).ifPresentOrElse(member -> plugin.getManager()
                .ifClaimOwner(member, user, chunk, world, (claim -> {
                    if (claim.claim().getType() != Claim.Type.PLOT) {
                        plugin.getLocales().getLocale("error_claim_not_plot")
                                .ifPresent(user::sendMessage);
                        return;
                    }
                    if (!claim.claim().isPlotManager(user.getUuid()) && !member.hasPrivilege(plugin, Privilege.MANAGE_PLOT_MEMBERS)) {
                        plugin.getLocales().getLocale("error_insufficient_privileges", member.town().getName())
                                .ifPresent(user::sendMessage);
                        return;
                    }
                    final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
                    assert claimWorld.isPresent();

                    plugin.runAsync(() -> {
                        final Optional<User> targetUser = plugin.getDatabase().getUser(target).map(SavedUser::user);
                        if (targetUser.isEmpty()) {
                            plugin.getLocales().getLocale("error_user_not_found", target)
                                    .ifPresent(user::sendMessage);
                            return;
                        }

                        if (!claim.claim().isPlotMember(targetUser.get().getUuid())) {
                            plugin.getLocales().getLocale("error_insufficient_privileges", targetUser.get().getUsername())
                                    .ifPresent(user::sendMessage);
                            return;
                        }

                        claim.claim().removePlotMember(targetUser.get().getUuid());
                        plugin.getDatabase().updateClaimWorld(claimWorld.get());
                        plugin.getManager().editTown(user, claim.town(), (town -> town.getLog().log(Action.of(user,
                                Action.Type.REMOVE_PLOT_MEMBER, claim.claim() + ": -" + targetUser.get().getUsername()))));

                        plugin.getLocales().getLocale("plot_member_removed", targetUser.get().getUsername(),
                                        Integer.toString(chunk.getX()), Integer.toString(chunk.getZ()))
                                .ifPresent(user::sendMessage);
                    });
                })), () -> plugin.getLocales().getLocale("error_not_in_town").ifPresent(user::sendMessage));
    }

    public void claimPlot(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {
        plugin.getManager().ifMember(user, Privilege.CLAIM_PLOT, (member ->
                plugin.getManager().ifClaimOwner(member, user, chunk, world, (claim -> {
                    if (claim.claim().getType() != Claim.Type.PLOT) {
                        plugin.getLocales().getLocale("error_claim_not_plot")
                                .ifPresent(user::sendMessage);
                        return;
                    }
                    final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
                    assert claimWorld.isPresent();

                    plugin.runAsync(() -> {
                        if (!claim.claim().getPlotMembers().isEmpty()) {
                            plugin.getLocales().getLocale("error_plot_not_vacant")
                                    .ifPresent(user::sendMessage);
                            return;
                        }

                        claim.claim().setPlotMember(user.getUuid(), true);
                        plugin.getDatabase().updateClaimWorld(claimWorld.get());
                        plugin.getManager().editTown(user, claim.town(), (town -> town.getLog().log(Action.of(user,
                                Action.Type.CLAIM_VACANT_PLOT, claim.claim().toString()))));

                        plugin.getLocales().getLocale("plot_claimed", Integer.toString(chunk.getX()),
                                Integer.toString(chunk.getZ())).ifPresent(user::sendMessage);
                    });
                }))));
    }

    public void listPlotMembers(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {
        plugin.getUserTown(user).ifPresentOrElse(member -> plugin.getManager()
                .ifClaimOwner(member, user, chunk, world, (claim -> {
                    if (claim.claim().getType() != Claim.Type.PLOT) {
                        plugin.getLocales().getLocale("error_claim_not_plot")
                                .ifPresent(user::sendMessage);
                        return;
                    }
                    final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
                    assert claimWorld.isPresent();

                    final String members = claim.claim().getPlotMembers().stream()
                            .map(plugin.getDatabase()::getUser)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(SavedUser::user)
                            .map(plotMember -> plotMember.getUsername() +
                                    (claim.claim().isPlotManager(plotMember.getUuid())
                                            ? " " + plugin.getLocales().getRawLocale("plot_manager_mark")
                                            .orElse("[M]") : ""))
                            .collect(Collectors.joining(", "));
                    plugin.getLocales().getLocale("plot_members",
                                    Integer.toString(chunk.getX()), Integer.toString(chunk.getZ()),
                                    members.isEmpty() ? plugin.getLocales().getNotApplicable() : members)
                            .ifPresent(user::sendMessage);
                })), () -> plugin.getLocales().getLocale("error_not_in_town").ifPresent(user::sendMessage));
    }

    public void toggleAutoClaiming(@NotNull OnlineUser user) {
        plugin.getManager().ifMember(
                user, Privilege.CLAIM,
                member -> plugin.editUserPreferences(user, (preferences) -> {
                            final boolean autoClaim = !preferences.isAutoClaimingLand();

                            preferences.setAutoClaimingLand(autoClaim);
                            plugin.getLocales().getLocale("auto_claim_" + (autoClaim ? "enabled" : "disabled"))
                                    .ifPresent(user::sendMessage);
                            if (autoClaim && plugin.getClaimAt(user.getPosition()).isEmpty()) {
                                createClaim(user, user.getWorld(), user.getChunk(), false);
                            }
                        }
                )
        );
    }
}
