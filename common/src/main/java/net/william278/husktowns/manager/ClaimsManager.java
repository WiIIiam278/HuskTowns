package net.william278.husktowns.manager;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.map.ClaimMap;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Collectors;

public class ClaimsManager {
    private final HuskTowns plugin;

    protected ClaimsManager(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    public void createClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, boolean showMap) {
        plugin.getManager().validateTownMembership(user, Privilege.CLAIM).ifPresent(member -> {
            final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
            if (existingClaim.isPresent()) {
                plugin.getLocales().getLocale("error_chunk_claimed_by", existingClaim.get().town().getName())
                        .ifPresent(user::sendMessage);
                return;
            }

            final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
            if (claimWorld.isEmpty()) {
                plugin.getLocales().getLocale("error_world_not_claimable")
                        .ifPresent(user::sendMessage);
                return;
            }

            final Claim claim = Claim.at(chunk);
            final ClaimWorld worldClaims = claimWorld.get();

            final Town town = member.town();
            if (town.getClaimCount() + 1 > town.getMaxClaims(plugin)) {
                plugin.getLocales().getLocale("error_claim_limit_reached", Integer.toString(town.getClaimCount()),
                                Integer.toString(town.getMaxClaims(plugin)))
                        .ifPresent(user::sendMessage);
                return;
            }

            plugin.runSync(() -> {
                final TownClaim townClaim = new TownClaim(town, claim);
                plugin.fireClaimEvent(user, townClaim).ifPresent(event -> plugin.runAsync(() -> {
                    worldClaims.addClaim(townClaim);
                    plugin.getDatabase().updateClaimWorld(worldClaims);
                    town.setClaimCount(town.getClaimCount() + 1);
                    town.getLog().log(Action.of(user, Action.Type.CREATE_CLAIM, claim.toString()));
                    plugin.getManager().updateTown(user, town);

                    plugin.getLocales().getLocale("claim_created", Integer.toString(chunk.getX()),
                                    Integer.toString(chunk.getZ()), town.getName())
                            .ifPresent(user::sendMessage);

                    plugin.highlightClaim(user, townClaim);
                    if (showMap) {
                        user.sendMessage(ClaimMap.builder(plugin)
                                .center(user.getChunk()).world(user.getWorld())
                                .build()
                                .toComponent(user));
                    }
                }));
            });
        });
    }

    public void deleteClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, boolean showMap) {
        plugin.getManager().validateTownMembership(user, Privilege.UNCLAIM).ifPresent(member -> {
            final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
            if (existingClaim.isEmpty()) {
                plugin.getLocales().getLocale("error_chunk_not_claimed")
                        .ifPresent(user::sendMessage);
                return;
            }

            final TownClaim claim = existingClaim.get();
            final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
            if (claimWorld.isEmpty()) {
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

            plugin.runSync(() -> plugin.fireUnClaimEvent(user, claim).ifPresent(event -> plugin.runAsync(() -> {
                claimWorld.get().removeClaim(claim.town(), claim.claim().getChunk());
                plugin.getDatabase().updateClaimWorld(claimWorld.get());
                town.setClaimCount(town.getClaimCount() - 1);
                town.getLog().log(Action.of(user, Action.Type.DELETE_CLAIM, claim.claim().toString()));
                plugin.getManager().updateTown(user, town);

                plugin.getLocales().getLocale("claim_deleted", Integer.toString(chunk.getX()),
                                Integer.toString(chunk.getZ()), town.getName())
                        .ifPresent(user::sendMessage);

                if (showMap) {
                    user.sendMessage(ClaimMap.builder(plugin)
                            .center(user.getChunk()).world(user.getWorld())
                            .build()
                            .toComponent(user));
                }
            })));
        });
    }

    public void makeClaimPlot(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {
        plugin.getManager().validateTownMembership(user, Privilege.SET_PLOT)
                .flatMap(member -> plugin.getManager().validateClaimOwnership(member, user, chunk, world))
                .ifPresent(claim -> {
                    if (claim.claim().getType() == Claim.Type.PLOT) {
                        makeClaimRegular(user, world, chunk);
                        return;
                    }
                    final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
                    assert claimWorld.isPresent();

                    plugin.runAsync(() -> {
                        claim.claim().setType(Claim.Type.PLOT);
                        plugin.getDatabase().updateClaimWorld(claimWorld.get());
                        claim.town().getLog().log(Action.of(user, Action.Type.MAKE_CLAIM_PLOT, claim.claim().toString()));
                        plugin.getManager().updateTown(user, claim.town());

                        plugin.getLocales().getLocale("claim_made_plot", Integer.toString(chunk.getX()),
                                Integer.toString(chunk.getZ())).ifPresent(user::sendMessage);
                    });
                });
    }

    public void makeClaimFarm(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {
        plugin.getManager().validateTownMembership(user, Privilege.SET_FARM)
                .flatMap(member -> plugin.getManager().validateClaimOwnership(member, user, chunk, world))
                .ifPresent(claim -> {
                    if (claim.claim().getType() == Claim.Type.FARM) {
                        makeClaimRegular(user, world, chunk);
                        return;
                    }
                    final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
                    assert claimWorld.isPresent();

                    plugin.runAsync(() -> {
                        claim.claim().setType(Claim.Type.FARM);
                        plugin.getDatabase().updateClaimWorld(claimWorld.get());
                        claim.town().getLog().log(Action.of(user, Action.Type.MAKE_CLAIM_FARM, claim.claim().toString()));
                        plugin.getManager().updateTown(user, claim.town());

                        plugin.getLocales().getLocale("claim_made_farm", Integer.toString(chunk.getX()),
                                Integer.toString(chunk.getZ())).ifPresent(user::sendMessage);
                    });
                });
    }

    public void makeClaimRegular(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {
        plugin.getManager().validateTownMembership(user, Privilege.SET_FARM)
                .flatMap(member -> plugin.getManager().validateClaimOwnership(member, user, chunk, world))
                .ifPresent(claim -> {
                    if (claim.claim().getType() == Claim.Type.CLAIM) {
                        plugin.getLocales().getLocale("error_claim_already_regular")
                                .ifPresent(user::sendMessage);
                        return;
                    }
                    final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
                    assert claimWorld.isPresent();

                    plugin.runAsync(() -> {
                        claim.claim().setType(Claim.Type.FARM);
                        plugin.getDatabase().updateClaimWorld(claimWorld.get());
                        claim.town().getLog().log(Action.of(user, Action.Type.MAKE_CLAIM_REGULAR, claim.claim().toString()));
                        plugin.getManager().updateTown(user, claim.town());

                        plugin.getLocales().getLocale("claim_made_regular", Integer.toString(chunk.getX()),
                                Integer.toString(chunk.getZ())).ifPresent(user::sendMessage);
                    });
                });
    }

    public void addPlotMember(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, @NotNull String target, boolean manager) {
        plugin.getUserTown(user).ifPresentOrElse(member -> plugin.getManager().validateClaimOwnership(member, user, chunk, world)
                .ifPresent(claim -> {
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
                        claim.town().getLog().log(Action.of(user, Action.Type.ADD_PLOT_MEMBER, claim.claim() + ": +" + targetUser.get().getUsername()));
                        plugin.getManager().updateTown(user, claim.town());

                        plugin.getLocales().getLocale("plot_member_added", targetUser.get().getUsername(),
                                        Integer.toString(chunk.getX()), Integer.toString(chunk.getZ()))
                                .ifPresent(user::sendMessage);
                    });
                }), () -> plugin.getLocales().getLocale("error_not_in_town").ifPresent(user::sendMessage));
    }

    public void removePlotMember(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, @NotNull String target) {
        plugin.getUserTown(user).ifPresentOrElse(member -> plugin.getManager().validateClaimOwnership(member, user, chunk, world)
                .ifPresent(claim -> {
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
                        claim.town().getLog().log(Action.of(user, Action.Type.REMOVE_PLOT_MEMBER, claim.claim() + ": -" + targetUser.get().getUsername()));
                        plugin.getManager().updateTown(user, claim.town());

                        plugin.getLocales().getLocale("plot_member_removed", targetUser.get().getUsername(),
                                        Integer.toString(chunk.getX()), Integer.toString(chunk.getZ()))
                                .ifPresent(user::sendMessage);
                    });
                }), () -> plugin.getLocales().getLocale("error_not_in_town").ifPresent(user::sendMessage));
    }

    public void listPlotMembers(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {
        plugin.getUserTown(user).ifPresentOrElse(member -> plugin.getManager().validateClaimOwnership(member, user, chunk, world)
                .ifPresent(claim -> {
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
                                    members.isEmpty() ? plugin.getLocales().getRawLocale("not_applicable")
                                            .orElse("N/A") : members)
                            .ifPresent(user::sendMessage);
                }), () -> plugin.getLocales().getLocale("error_not_in_town").ifPresent(user::sendMessage));
    }

    public void toggleAutoClaiming(@NotNull OnlineUser user) {
        plugin.getManager().validateTownMembership(user, Privilege.CLAIM)
                .flatMap(member -> plugin.getUserPreferences(user.getUuid())).ifPresent(preferences -> {
                    final boolean autoClaim = !preferences.isAutoClaimingLand();
                    preferences.setAutoClaimingLand(autoClaim);
                    plugin.runAsync(() -> plugin.getDatabase().updateUser(user, preferences));
                    plugin.getLocales().getLocale("auto_claim_" + (autoClaim ? "enabled" : "disabled"))
                            .ifPresent(user::sendMessage);

                    if (autoClaim) {
                        createClaim(user, user.getWorld(), user.getChunk(), false);
                    }
                });
    }
}
