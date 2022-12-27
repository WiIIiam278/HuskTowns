package net.william278.husktowns.town;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.map.ClaimMap;
import net.william278.husktowns.network.Message;
import net.william278.husktowns.network.Payload;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import net.william278.husktowns.util.ColorPicker;
import net.william278.husktowns.util.Validator;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeMap;

public class Manager {

    private final HuskTowns plugin;
    private final Towns towns;
    private final Claims claims;
    private final Admin admin;

    public Manager(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
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

    private void updateTown(@NotNull OnlineUser user, @NotNull Town town) {
        plugin.getDatabase().updateTown(town);
        plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                .type(Message.Type.TOWN_UPDATE)
                .payload(Payload.integer(town.getId()))
                .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                .build()
                .send(broker, user));
    }

    private Optional<Member> validateTownMembership(@NotNull OnlineUser user, @NotNull Privilege privilege) {
        final Optional<Member> member = plugin.getUserTown(user);
        if (member.isEmpty()) {
            plugin.getLocales().getLocale("error_not_in_town")
                    .ifPresent(user::sendMessage);
            return Optional.empty();
        }

        if (!member.get().hasPrivilege(plugin, privilege)) {
            plugin.getLocales().getLocale("error_insufficient_privileges", member.get().town().getName())
                    .ifPresent(user::sendMessage);
            return Optional.empty();
        }
        return member;
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
            plugin.getManager().validateTownMembership(user, Privilege.INVITE).ifPresent(member -> plugin.runAsync(() -> {
                final int currentMembers = member.town().getMembers().size();
                final long maxMembers = member.town().getMaxMembers();
                if (currentMembers >= maxMembers) {
                    plugin.getLocales().getLocale("error_town_member_limit_reached",
                                    Integer.toString(currentMembers), Long.toString(maxMembers))
                            .ifPresent(user::sendMessage);
                    return;
                }

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
                final Town town = member.town();
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
            }));
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
            plugin.getManager().validateTownMembership(user, Privilege.RENAME).ifPresent(member -> {
                if (!plugin.getValidator().isValidTownName(newName)) {
                    plugin.getLocales().getLocale("error_invalid_town_name")
                            .ifPresent(user::sendMessage);
                    return;
                }

                plugin.runAsync(() -> {
                    final Town town = member.town();
                    town.getLog().log(Action.of(user, Action.Type.RENAME_TOWN, town.getName() + " → " + newName));
                    town.setName(newName);
                    plugin.getManager().updateTown(user, town);
                    plugin.getLocales().getLocale("town_renamed", town.getName())
                            .ifPresent(user::sendMessage);
                });
            });
        }

        public void setTownBio(@NotNull OnlineUser user, @NotNull String newBio) {
            plugin.getManager().validateTownMembership(user, Privilege.SET_BIO).ifPresent(member -> {
                if (!plugin.getValidator().isValidTownMetadata(newBio)) {
                    plugin.getLocales().getLocale("error_invalid_meta",
                            Integer.toString(Validator.MAX_TOWN_META_LENGTH)).ifPresent(user::sendMessage);
                    return;
                }

                plugin.runAsync(() -> {
                    final Town town = member.town();
                    town.getLog().log(Action.of(user, Action.Type.UPDATE_BIO,
                            town.getBio().map(bio -> bio + " → ").orElse("") + newBio));
                    town.setBio(newBio);
                    plugin.getManager().updateTown(user, town);
                    town.getBio().flatMap(bio -> plugin.getLocales().getLocale("town_bio_set", bio))
                            .ifPresent(user::sendMessage);
                });
            });
        }

        public void setTownGreeting(@NotNull OnlineUser user, @NotNull String newGreeting) {
            plugin.getManager().validateTownMembership(user, Privilege.SET_GREETING).ifPresent(member -> {
                if (!plugin.getValidator().isValidTownMetadata(newGreeting)) {
                    plugin.getLocales().getLocale("error_invalid_meta",
                            Integer.toString(Validator.MAX_TOWN_META_LENGTH)).ifPresent(user::sendMessage);
                    return;
                }

                plugin.runAsync(() -> {
                    final Town town = member.town();
                    town.getLog().log(Action.of(user, Action.Type.UPDATE_GREETING,
                            town.getGreeting().map(greeting -> greeting + " → ").orElse("") + newGreeting));
                    town.setGreeting(newGreeting);
                    plugin.getManager().updateTown(user, town);
                    town.getGreeting().flatMap(greeting -> plugin.getLocales().getLocale("town_greeting_set", greeting))
                            .ifPresent(user::sendMessage);
                });
            });
        }

        public void setTownFarewell(@NotNull OnlineUser user, @NotNull String newFarewell) {
            plugin.getManager().validateTownMembership(user, Privilege.SET_FAREWELL).ifPresent(member -> {
                if (!plugin.getValidator().isValidTownMetadata(newFarewell)) {
                    plugin.getLocales().getLocale("error_invalid_meta",
                            Integer.toString(Validator.MAX_TOWN_META_LENGTH)).ifPresent(user::sendMessage);
                    return;
                }

                plugin.runAsync(() -> {
                    final Town town = member.town();
                    town.getLog().log(Action.of(user, Action.Type.UPDATE_FAREWELL,
                            town.getFarewell().map(farewell -> farewell + " → ").orElse("") + newFarewell));
                    town.setFarewell(newFarewell);
                    plugin.getManager().updateTown(user, town);
                    town.getFarewell().flatMap(farewell -> plugin.getLocales().getLocale("town_farewell_set", farewell))
                            .ifPresent(user::sendMessage);
                });
            });
        }

        public void setTownColor(@NotNull OnlineUser user, @Nullable String newColor) {
            if (newColor == null) {
                plugin.getLocales().getLocale("town_color_picker_title").ifPresent(user::sendMessage);
                user.sendMessage(ColorPicker.builder().command("/husktowns:town color").build().toComponent());
                return;
            }

            plugin.getManager().validateTownMembership(user, Privilege.SET_COLOR).ifPresent(member -> {
                final Color color;
                try {
                    // Get awt color from hex string
                    color = Color.decode(newColor);
                } catch (NumberFormatException e) {
                    plugin.getLocales().getLocale("error_invalid_color")
                            .ifPresent(user::sendMessage);
                    return;
                }

                plugin.runAsync(() -> {
                    final Town town = member.town();
                    final String newColorRgb = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
                    town.getLog().log(Action.of(user, Action.Type.UPDATE_COLOR, town.getColorRgb() + " → " + newColorRgb));
                    town.setColor(color);
                    plugin.getManager().updateTown(user, town);
                    plugin.getLocales().getLocale("town_color_changed", town.getName(), newColorRgb)
                            .ifPresent(user::sendMessage);
                });
            });
        }

        public void setTownSpawn(@NotNull OnlineUser user, @NotNull Position position) {
            plugin.getManager().validateTownMembership(user, Privilege.SET_SPAWN).ifPresent(member -> plugin.runAsync(() -> {
                final Town town = member.town();
                final Spawn spawn = Spawn.of(position, plugin.getServerName());
                town.getLog().log(Action.of(user, Action.Type.UPDATE_SPAWN, town.getSpawn().map(
                        oldSpawn -> oldSpawn + " → ").orElse("") + spawn));
                town.setSpawn(spawn);
                plugin.getManager().updateTown(user, town);
                plugin.getLocales().getLocale("town_spawn_set", town.getName())
                        .ifPresent(user::sendMessage);
            }));
        }

        public void setSpawnPrivacy(@NotNull OnlineUser user, boolean isPublic) {
            plugin.getManager().validateTownMembership(user, Privilege.SET_SPAWN).ifPresent(member -> plugin.runAsync(() -> {
                final Town town = member.town();
                if (town.getSpawn().isEmpty()) {
                    plugin.getLocales().getLocale("error_town_spawn_not_set")
                            .ifPresent(user::sendMessage);
                    return;
                }
                final Spawn spawn = town.getSpawn().get();
                if (spawn.isPublic() != isPublic) {
                    town.getLog().log(Action.of(user, Action.Type.UPDATE_SPAWN_IS_PUBLIC, spawn.isPublic() + " → " + isPublic));
                    spawn.setPublic(isPublic);
                    town.setSpawn(spawn);
                    plugin.getManager().updateTown(user, town);
                }
                plugin.getLocales().getLocale("town_spawn_privacy_set_" + (isPublic ? "public" : "private"),
                        town.getName()).ifPresent(user::sendMessage);
            }));
        }

        public void clearTownSpawn(@NotNull OnlineUser user) {
            plugin.getManager().validateTownMembership(user, Privilege.SET_SPAWN).ifPresent(member -> plugin.runAsync(() -> {
                final Town town = member.town();
                if (town.getSpawn().isEmpty()) {
                    plugin.getLocales().getLocale("error_town_spawn_not_set")
                            .ifPresent(user::sendMessage);
                    return;
                }
                town.getLog().log(Action.of(user, Action.Type.CLEAR_SPAWN));
                town.clearSpawn();
                plugin.getManager().updateTown(user, town);
                plugin.getLocales().getLocale("town_spawn_cleared", town.getName())
                        .ifPresent(user::sendMessage);
            }));
        }

        public void depositTownBank(@NotNull OnlineUser user, @NotNull BigDecimal amount) {

        }

        public void showTownLogs(@NotNull OnlineUser user, int page) {
            plugin.getManager().validateTownMembership(user, Privilege.VIEW_LOGS).ifPresent(member -> {
                final TreeMap<OffsetDateTime, Action> actions = new TreeMap<>(Comparator.reverseOrder());
                actions.putAll(member.town().getLog().getActions());
                final Locales locales = plugin.getLocales();
                final String NOT_APPLICABLE = plugin.getLocales().getRawLocale("not_applicable").orElse("N/A");
                user.sendMessage(PaginatedList.of(actions.entrySet().stream()
                                        .map(entry -> locales.getRawLocale("town_audit_log_list_item",
                                                        entry.getKey().format(DateTimeFormatter.ofPattern("MMM dd")),
                                                        entry.getKey().format(DateTimeFormatter.ofPattern("MMM dd, yyyy, HH:mm:ss")),
                                                        Locales.escapeText(entry.getValue().getUser().map(User::getUsername)
                                                                .orElse(NOT_APPLICABLE)),
                                                        Locales.escapeText(entry.getValue().getAction().name().toLowerCase()),
                                                        Locales.escapeText(locales.truncateText(entry.getValue().getDetails()
                                                                .orElse(NOT_APPLICABLE), 10)),
                                                        Locales.escapeText(locales.wrapText(entry.getValue().getDetails()
                                                                .orElse(NOT_APPLICABLE), 40)))
                                                .orElse(entry.getValue().toString()))
                                        .toList(),
                                locales.getBaseList(plugin.getSettings().listItemsPerPage)
                                        .setHeaderFormat(locales.getRawLocale("town_audit_log_list_title",
                                                Locales.escapeText(member.town().getName())).orElse(""))
                                        .setItemSeparator("\n").setCommand("/husktowns:town log")
                                        .build())
                        .getNearestValidPage(page));
            });
        }
    }

    public static class Claims {
        private final HuskTowns plugin;

        private Claims(@NotNull HuskTowns plugin) {
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
                if (town.getClaimCount() + 1 > town.getMaxClaims()) {
                    plugin.getLocales().getLocale("error_claim_limit_reached", Long.toString(town.getClaimCount()),
                                    Long.toString(town.getMaxClaims()))
                            .ifPresent(user::sendMessage);
                    return;
                }

                plugin.runAsync(() -> {
                    final TownClaim townClaim = new TownClaim(town, claim);
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
                                .width(11).height(11)
                                .build()
                                .toComponent(user));
                    }
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

                plugin.runAsync(() -> {
                    claimWorld.get().removeClaim(claim.town(), claim.claim().getChunk());
                    plugin.getDatabase().updateClaimWorld(claimWorld.get());
                    town.setClaimCount(town.getClaimCount() - 1);
                    town.getLog().log(Action.of(user, Action.Type.DELETE_CLAIM, claim.toString()));
                    plugin.getManager().updateTown(user, town);

                    plugin.getLocales().getLocale("claim_deleted", Integer.toString(chunk.getX()),
                                    Integer.toString(chunk.getZ()), town.getName())
                            .ifPresent(user::sendMessage);

                    if (showMap) {
                        user.sendMessage(ClaimMap.builder(plugin)
                                .center(user.getChunk()).world(user.getWorld())
                                .width(11).height(11)
                                .build()
                                .toComponent(user));
                    }
                });
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
