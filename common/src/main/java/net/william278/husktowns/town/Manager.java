package net.william278.husktowns.town;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.network.Message;
import net.william278.husktowns.network.Payload;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import net.william278.husktowns.util.ColorPicker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Optional;

public class Manager {

    private final Towns towns;
    private final Claims claims;
    private final Admin admin;

    public Manager(@NotNull HuskTowns plugin) {
        this.towns = new Towns(plugin);
        this.claims = new Claims(plugin);
        this.admin = new Admin(plugin);
    }

    @NotNull
    public Towns towns() {
        return towns;
    }

    @NotNull
    public Claims claims() {
        return claims;
    }

    @NotNull
    public Admin admin() {
        return admin;
    }

    public static class Towns {
        private final HuskTowns plugin;

        private Towns(@NotNull HuskTowns plugin) {
            this.plugin = plugin;
        }

        public void createTown(@NotNull OnlineUser user, @NotNull String townName) {
            plugin.runAsync(() -> {
                // Check the user isn't already in a town
                if (plugin.getUserTown(user).isPresent()) {
                    plugin.getLocales().getLocale("error_already_in_town")
                            .ifPresent(user::sendMessage);
                    return;
                }

                // Check against invalid names and duplicates
                if (!plugin.getValidator().isValidTownName(townName)) {
                    plugin.getLocales().getLocale("error_invalid_town_name")
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Town town = plugin.getDatabase().createTown(townName, user);
                plugin.getTowns().add(town);
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_UPDATE)
                        .payload(Payload.integer(town.getId()))
                        .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                        .build()
                        .send(broker, user));

                plugin.getLocales().getLocale("town_created", town.getName())
                        .ifPresent(user::sendMessage);
            });
        }

        public void deleteTown(@NotNull OnlineUser user) {
            plugin.runAsync(() -> {
                final Optional<Member> member = plugin.getUserTown(user);
                if (member.isEmpty()) {
                    plugin.getLocales().getLocale("error_not_in_town")
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Town town = member.get().town();
                if (!town.getMayor().equals(user.getUuid())) {
                    plugin.getLocales().getLocale("error_not_town_mayor", town.getName())
                            .ifPresent(user::sendMessage);
                    return;
                }

                plugin.getDatabase().deleteTown(town.getId());
                plugin.getTowns().remove(town);
                plugin.getClaimWorlds().values().forEach(world -> {
                    if (world.removeTownClaims(town.getId()) > 0) {
                        plugin.getDatabase().updateClaimWorld(world);
                    }
                });

                // Propagate the town deletion to all servers
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_DELETE)
                        .payload(Payload.integer(town.getId()))
                        .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                        .build()
                        .send(broker, user));

                plugin.getLocales().getLocale("town_deleted", town.getName())
                        .ifPresent(user::sendMessage);
            });
        }

        public void inviteMember(@NotNull OnlineUser user, @NotNull String target) {
            plugin.runAsync(() -> {
                final Optional<Member> member = plugin.getUserTown(user);
                if (member.isEmpty()) {
                    plugin.getLocales().getLocale("error_not_in_town")
                            .ifPresent(user::sendMessage);
                    return;
                }

                if (!member.get().hasPrivilege(plugin, Privilege.INVITE)) {
                    plugin.getLocales().getLocale("error_insufficient_privileges", member.get().town().getName())
                            .ifPresent(user::sendMessage);
                    return;
                }

                //todo capacity check

                final Optional<User> databaseTarget = plugin.getDatabase().getUser(target);
                if (databaseTarget.isEmpty()) {
                    plugin.getLocales().getLocale("error_user_not_found", target)
                            .ifPresent(user::sendMessage);
                    return;
                }
                if (plugin.getUserTown(databaseTarget.get()).isPresent()) {
                    plugin.getLocales().getLocale("error_other_already_in_town")
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Optional<? extends OnlineUser> localUser = plugin.findOnlineUser(target);
                final Town town = member.get().town();
                final Invite invite = Invite.create(town.getId(), user, target);
                if (localUser.isEmpty()) {
                    if (!plugin.getSettings().crossServer) {
                        plugin.getLocales().getLocale("error_user_not_found", target)
                                .ifPresent(user::sendMessage);
                        return;
                    }

                    plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                            .type(Message.Type.INVITE_REQUEST)
                            .payload(Payload.invite(invite))
                            .target(target, Message.TargetType.PLAYER)
                            .build()
                            .send(broker, user));
                } else {
                    handleInboundInvite(localUser.get(), invite);
                }

                plugin.getLocales().getLocale("invite_sent", target, town.getName())
                        .ifPresent(user::sendMessage);
            });
        }

        public void handleInboundInvite(@NotNull OnlineUser user, @NotNull Invite invite) {
            plugin.runAsync(() -> {
                final Optional<Town> town = plugin.findTown(invite.getTownId());
                if (plugin.getUserTown(user).isPresent() || town.isEmpty()) {
                    return;
                }

                plugin.addInvite(user.getUuid(), invite);
                plugin.getLocales().getLocale("invite_received", invite.getSender().getUsername(), town.get().getName())
                        .ifPresent(user::sendMessage);
                plugin.getLocales().getLocale("invite_buttons", invite.getSender().getUsername())
                        .ifPresent(user::sendMessage);
            });
        }

        public void handleInviteReply(@NotNull OnlineUser user, boolean accepted, @Nullable String selectedInviter) {
            plugin.runAsync(() -> {
                final Optional<Invite> invite = plugin.getLastInvite(user, selectedInviter);
                if (invite.isEmpty()) {
                    plugin.getLocales().getLocale("error_no_invites")
                            .ifPresent(user::sendMessage);
                    return;
                }

                //todo send invite reply to other server if it was a cross server invite,
                //todo save town to DB then send town update notifs globally,
                //todo capacity check
                /*if (accepted) {
                    final Optional<Town> town = plugin.findTown(invite.get().getTownId());
                    if (town.isEmpty()) {
                        return;
                    }

                    final Member member = Member.create(town.get().getId(), user.getUuid(), Privilege.MEMBER);
                    plugin.getDatabase().createMember(member);
                    town.get().addMember(member);
                    plugin.getDatabase().updateTown(town.get());
                    plugin.getLocales().getLocale("invite_accepted", town.get().getName())
                            .ifPresent(user::sendMessage);
                } else {
                    plugin.getLocales().getLocale("invite_declined", invite.get().getSender().getUsername())
                            .ifPresent(user::sendMessage);
                }*/

                plugin.removeInvite(user.getUuid(), invite.get());
            });
        }

        public void removeMember(@NotNull OnlineUser user, @NotNull String target) {

        }

        public void renameTown(@NotNull OnlineUser user, @NotNull String newName) {

        }

        public void setTownBio(@NotNull OnlineUser user, @NotNull String newBio) {

        }

        public void setTownGreeting(@NotNull OnlineUser user, @NotNull String newGreeting) {

        }

        public void setTownFarewell(@NotNull OnlineUser user, @NotNull String newFarewell) {

        }

        public void setTownColor(@NotNull OnlineUser user, @Nullable String newColor) {
            if (newColor == null) {
                plugin.getLocales().getLocale("town_color_picker_title").ifPresent(user::sendMessage);
                user.sendMessage(ColorPicker.builder().command("/husktowns:town color").build().toComponent());
                return;
            }

            final Optional<Member> member = plugin.getUserTown(user);
            if (member.isEmpty()) {
                plugin.getLocales().getLocale("error_not_in_town")
                        .ifPresent(user::sendMessage);
                return;
            }

            final Town town = member.get().town();
            if (!member.get().hasPrivilege(plugin, Privilege.SET_COLOR)) {
                plugin.getLocales().getLocale("error_insufficient_privileges", town.getName())
                        .ifPresent(user::sendMessage);
                return;
            }

            // Get awt color from hex string
            final Color color;
            try {
                color = Color.decode(newColor);
            } catch (NumberFormatException e) {
                plugin.getLocales().getLocale("error_invalid_color")
                        .ifPresent(user::sendMessage);
                return;
            }

            plugin.runAsync(() -> {
                town.setColor(color);
                plugin.getDatabase().updateTown(town);
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_UPDATE)
                        .payload(Payload.integer(town.getId()))
                        .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                        .build()
                        .send(broker, user));
                plugin.getLocales().getLocale("town_color_changed", town.getName(),
                                String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()))
                        .ifPresent(user::sendMessage);
            });
        }

        public void setTownSpawn(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {

        }

        public void clearTownSpawn(@NotNull OnlineUser user) {

        }

        public void depositTownBank(@NotNull OnlineUser user, int amount) {

        }
    }

    public static class Claims {
        private final HuskTowns plugin;

        private Claims(@NotNull HuskTowns plugin) {
            this.plugin = plugin;
        }

        public void createClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {
            plugin.runAsync(() -> {
                final Optional<Member> member = plugin.getUserTown(user);
                if (member.isEmpty()) {
                    plugin.getLocales().getLocale("error_not_in_town")
                            .ifPresent(user::sendMessage);
                    return;
                }

                if (!member.get().hasPrivilege(plugin, Privilege.CLAIM)) {
                    plugin.getLocales().getLocale("error_insufficient_privileges", member.get().town().getName())
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
                if (existingClaim.isPresent()) {
                    plugin.getLocales().getLocale("error_claim_already_exists", existingClaim.get().town().getName())
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
                if (claimWorld.isEmpty()) {
                    plugin.getLocales().getLocale("error_world_not_claimable")
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Town town = member.get().town();
                final Claim claim = Claim.at(chunk);
                final ClaimWorld worldClaims = claimWorld.get();

                if (town.getClaimCount() + 1 > town.getMaxClaims()) {
                    plugin.getLocales().getLocale("error_claim_limit_reached", Long.toString(town.getClaimCount()),
                                    Long.toString(town.getMaxClaims()))
                            .ifPresent(user::sendMessage);
                    return;
                }

                final TownClaim townClaim = new TownClaim(town, claim);
                worldClaims.addClaim(townClaim);
                plugin.getDatabase().updateClaimWorld(worldClaims);
                town.setClaimCount(town.getClaimCount() + 1);
                town.getLog().log(Action.of(user, Action.Type.CREATE_CLAIM, claim.toString()));
                plugin.getDatabase().updateTown(town);
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_UPDATE)
                        .payload(Payload.integer(town.getId()))
                        .target(Message.TARGET_ALL, Message.TargetType.PLAYER)
                        .build()
                        .send(broker, user));
                plugin.getLocales().getLocale("claim_created", Integer.toString(chunk.getX()),
                                Integer.toString(chunk.getZ()), town.getName())
                        .ifPresent(user::sendMessage);
                plugin.highlightClaim(user, townClaim);
            });
        }

        public void deleteClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {
            plugin.runAsync(() -> {
                final Optional<Member> member = plugin.getUserTown(user);
                if (member.isEmpty()) {
                    plugin.getLocales().getLocale("error_not_in_town")
                            .ifPresent(user::sendMessage);
                    return;
                }

                if (!member.get().hasPrivilege(plugin, Privilege.UNCLAIM)) {
                    plugin.getLocales().getLocale("error_insufficient_privileges", member.get().town().getName())
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
                if (existingClaim.isEmpty()) {
                    plugin.getLocales().getLocale("error_chunk_not_claimed")
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Town town = member.get().town();
                final TownClaim claim = existingClaim.get();
                final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
                if (claimWorld.isEmpty()) {
                    plugin.getLocales().getLocale("error_world_not_claimable")
                            .ifPresent(user::sendMessage);
                    return;
                }

                if (!claim.town().equals(town)) {
                    plugin.getLocales().getLocale("error_claim_already_exists", claim.town().getName())
                            .ifPresent(user::sendMessage);
                    return;
                }

                claimWorld.get().removeClaim(claim.town(), claim.claim().getChunk());
                plugin.getDatabase().updateClaimWorld(claimWorld.get());
                town.setClaimCount(town.getClaimCount() - 1);
                town.getLog().log(Action.of(user, Action.Type.DELETE_CLAIM, claim.toString()));
                plugin.getDatabase().updateTown(town);
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_UPDATE)
                        .payload(Payload.integer(town.getId()))
                        .target(Message.TARGET_ALL, Message.TargetType.PLAYER)
                        .build()
                        .send(broker, user));
                plugin.getLocales().getLocale("claim_deleted", Integer.toString(chunk.getX()),
                                Integer.toString(chunk.getZ()), town.getName())
                        .ifPresent(user::sendMessage);
            });
        }

        public void deleteAllClaims(@NotNull OnlineUser user) {

        }

        public void makeClaimPlot(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {

        }

        public void makeClaimFarm(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {

        }

        public void makeClaimRegular(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk) {

        }

        public void addPlotMember(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, @NotNull String target) {

        }

        public void removePlotMember(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, @NotNull String target) {

        }

    }

    public static class Admin {
        private final HuskTowns plugin;

        private Admin(@NotNull HuskTowns plugin) {
            this.plugin = plugin;
        }

    }
}
