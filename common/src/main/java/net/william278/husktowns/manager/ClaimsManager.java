package net.william278.husktowns.manager;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.map.ClaimMap;
import net.william278.husktowns.network.Message;
import net.william278.husktowns.network.Payload;
import net.william278.husktowns.town.Privilege;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
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
        plugin.getManager().ifMember(user, Privilege.CLAIM, (member -> {
            final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
            if (existingClaim.isPresent()) {
                plugin.getLocales().getLocale("error_chunk_claimed_by", existingClaim.get().town().getName())
                        .ifPresent(user::sendMessage);
                return;
            }

            final Optional<ClaimWorld> optionalClaimWorld = plugin.getClaimWorld(world);
            if (optionalClaimWorld.isEmpty()) {
                plugin.getLocales().getLocale("error_world_not_claimable")
                        .ifPresent(user::sendMessage);
                return;
            }

            // Check against nearby claims
            final ClaimWorld claimWorld = optionalClaimWorld.get();
            final Optional<TownClaim> nearbyClaim = claimWorld
                    .getClaimsNear(chunk, plugin.getSettings().getMinimumChunkSeparation(), plugin).stream()
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
                .orElseThrow(() -> new IllegalArgumentException("World is not claimable"));
        if (claim.isAdminClaim(plugin)) {
            claimWorld.addAdminClaim(claim.claim());
        } else {
            claimWorld.addClaim(claim);
            plugin.getManager().editTown(user, claim.town(), (town -> {
                town.setClaimCount(town.getClaimCount() + 1);
                town.getLog().log(Action.of(user, Action.Type.CREATE_CLAIM, claim.claim().toString()));

                if (town.getClaimCount() == 1 && plugin.getSettings().doFirstClaimAutoSetSpawn()) {
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

    public void deleteAllClaims(@NotNull OnlineUser user, @NotNull Town town) {
        plugin.getMapHook().ifPresent(mapHook -> mapHook.removeClaimMarkers(town));
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
        plugin.getManager().sendTownNotification(town, plugin.getLocales()
                .getLocale("deleted_all_claims_notification", town.getName())
                .map(MineDown::toComponent).orElse(Component.empty()));
    }

    public void deleteClaimData(@NotNull OnlineUser user, @NotNull TownClaim claim, @NotNull World world) throws IllegalArgumentException {
        final ClaimWorld claimWorld = plugin.getClaimWorld(world)
                .orElseThrow(() -> new IllegalArgumentException("World is not claimable"));
        if (claim.isAdminClaim(plugin)) {
            claimWorld.removeAdminClaim(claim.claim().getChunk());
        } else {
            plugin.getManager().editTown(user, claim.town(), (town -> {
                claimWorld.removeClaim(claim.town(), claim.claim().getChunk());
                town.setClaimCount(town.getClaimCount() - 1);
                town.getLog().log(Action.of(user, Action.Type.DELETE_CLAIM, claim.claim().toString()));
            }));
        }

        plugin.getDatabase().updateClaimWorld(claimWorld);
        plugin.getMapHook().ifPresent(map -> map.removeClaimMarker(claim, world));
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
                                    members.isEmpty() ? plugin.getLocales().getRawLocale("not_applicable")
                                            .orElse("N/A") : members)
                            .ifPresent(user::sendMessage);
                })), () -> plugin.getLocales().getLocale("error_not_in_town").ifPresent(user::sendMessage));
    }

    public void toggleAutoClaiming(@NotNull OnlineUser user) {
        plugin.getManager().ifMember(user, Privilege.CLAIM, member -> {
            final Preferences preferences = plugin.getUserPreferences(user.getUuid()).orElse(Preferences.getDefaults());
            final boolean autoClaim = !preferences.isAutoClaimingLand();
            preferences.setAutoClaimingLand(autoClaim);
            plugin.runAsync(() -> plugin.getDatabase().updateUser(user, preferences));

            plugin.getLocales().getLocale("auto_claim_" + (autoClaim ? "enabled" : "disabled"))
                    .ifPresent(user::sendMessage);
            if (autoClaim && plugin.getClaimAt(user.getPosition()).isEmpty()) {
                createClaim(user, user.getWorld(), user.getChunk(), false);
            }
        });
    }
}
