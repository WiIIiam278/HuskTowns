package net.william278.husktowns.town;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.hook.EconomyHook;
import net.william278.husktowns.map.ClaimMap;
import net.william278.husktowns.menu.ColorPicker;
import net.william278.husktowns.menu.RulesConfig;
import net.william278.husktowns.network.Message;
import net.william278.husktowns.network.Payload;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
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
import java.util.logging.Level;
import java.util.stream.Collectors;

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
        if (plugin.getTowns().contains(town)) {
            plugin.getTowns().replaceAll(t -> t.getId() == town.getId() ? town : t);
        } else {
            plugin.getTowns().add(town);
        }
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

    private Optional<TownClaim> validateClaimOwnership(@NotNull Member member, @NotNull OnlineUser user,
                                                       @NotNull Chunk chunk, @NotNull World world) {
        final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
        if (existingClaim.isEmpty()) {
            plugin.getLocales().getLocale("error_chunk_not_claimed")
                    .ifPresent(user::sendMessage);
            return Optional.empty();
        }

        final TownClaim claim = existingClaim.get();
        final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
        if (claimWorld.isEmpty()) {
            plugin.getLocales().getLocale("error_world_not_claimable")
                    .ifPresent(user::sendMessage);
            return Optional.empty();
        }

        final Town town = member.town();
        if (!claim.town().equals(town)) {
            plugin.getLocales().getLocale("error_chunk_claimed_by", claim.town().getName())
                    .ifPresent(user::sendMessage);
            return Optional.empty();
        }
        return existingClaim;
    }

    public void sendTownNotification(@NotNull Town town, @NotNull Component message) {
        plugin.getOnlineUsers().stream()
                .filter(user -> town.getMembers().containsKey(user.getUuid()))
                .filter(user -> plugin.getUserPreferences(user.getUuid())
                        .map(Preferences::isTownNotifications).orElse(true))
                .forEach(user -> user.sendMessage(message));
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

        public void deleteTown(@NotNull OnlineUser user, boolean confirmed) {
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

            if (!confirmed) {
                plugin.getLocales().getLocale("town_delete_confirm", town.getName())
                        .ifPresent(user::sendMessage);
                return;
            }

            deleteTownData(user, town);
        }

        private void deleteTownData(@NotNull OnlineUser user, @NotNull Town town) {
            plugin.runAsync(() -> {
                plugin.getManager().sendTownNotification(town, plugin.getLocales()
                        .getLocale("town_deleted_notification", town.getName())
                        .map(MineDown::toComponent).orElse(Component.empty()));

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
                final int maxMembers = member.town().getMaxMembers(plugin);
                if (currentMembers >= maxMembers) {
                    plugin.getLocales().getLocale("error_town_member_limit_reached",
                                    Integer.toString(currentMembers), Integer.toString(maxMembers))
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Optional<User> databaseTarget = plugin.getDatabase().getUser(target).map(SavedUser::user);
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
                            .type(Message.Type.TOWN_INVITE_REQUEST)
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
                    plugin.log(Level.WARNING, "Received an invalid invite from " + invite.getSender() + " to " + invite.getTownId());
                    return;
                }

                plugin.addInvite(user.getUuid(), invite);
                plugin.getLocales().getLocale("invite_received",
                        invite.getSender().getUsername(), town.get().getName()).ifPresent(user::sendMessage);
                plugin.getLocales().getLocale("invite_buttons",
                        invite.getSender().getUsername()).ifPresent(user::sendMessage);
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

                if (accepted) {
                    final Optional<Town> town = plugin.findTown(invite.get().getTownId());
                    if (town.isEmpty()) {
                        plugin.getLocales().getLocale("error_town_no_longer_exists")
                                .ifPresent(user::sendMessage);
                        return;
                    }

                    if (town.get().getMembers().size() >= town.get().getMaxMembers(plugin)) {
                        plugin.getLocales().getLocale("error_town_member_limit_reached",
                                        Integer.toString(town.get().getMembers().size()), Integer.toString(town.get().getMaxMembers(plugin)))
                                .ifPresent(user::sendMessage);
                        return;
                    }

                    town.get().addMember(user.getUuid(), plugin.getRoles().getDefaultRole());
                    town.get().getLog().log(Action.of(user, Action.Type.MEMBER_JOIN,
                            user.getUsername() + " (" + invite.get().getSender().getUsername() + ")"));
                    plugin.getDatabase().updateTown(town.get());
                    plugin.getLocales().getLocale("invite_accepted", town.get().getName())
                            .ifPresent(user::sendMessage);

                    // Broadcast the acceptance to local town members
                    plugin.getLocales().getLocale("user_joined_town",
                                    user.getUsername(), town.get().getName()).map(MineDown::toComponent)
                            .ifPresent(message -> plugin.getManager().sendTownNotification(town.get(), message));
                } else {
                    plugin.getLocales().getLocale("invite_declined", invite.get().getSender().getUsername())
                            .ifPresent(user::sendMessage);

                    // Reply to the sender
                    plugin.getOnlineUsers().stream()
                            .filter(online -> online.getUuid().equals(invite.get().getSender().getUuid())).findFirst()
                            .ifPresent(sender -> plugin.getLocales().getLocale("invite_declined_by", user.getUsername())
                                    .ifPresent(sender::sendMessage));
                }

                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_INVITE_REPLY)
                        .payload(Payload.bool(accepted))
                        .target(invite.get().getSender().getUsername(), Message.TargetType.PLAYER)
                        .build()
                        .send(broker, user));
                plugin.removeInvite(user.getUuid(), invite.get());
            });
        }

        public void removeMember(@NotNull OnlineUser user, @NotNull String target) {
            plugin.getManager().validateTownMembership(user, Privilege.EVICT).ifPresent(member -> plugin.runAsync(() -> {
                final Optional<User> evicted = plugin.getDatabase().getUser(target).map(SavedUser::user);
                if (evicted.isEmpty()) {
                    plugin.getLocales().getLocale("error_user_not_found", target)
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Optional<Member> evictedMember = plugin.getUserTown(evicted.get());
                if (evictedMember.isEmpty() || evictedMember.map(townMember -> !townMember.town().equals(member.town())).orElse(false)) {
                    plugin.getLocales().getLocale("error_other_not_in_town",
                            evicted.get().getUsername()).ifPresent(user::sendMessage);
                    return;
                }

                final Town town = member.town();
                if (evictedMember.get().role().getWeight() >= member.role().getWeight()) {
                    plugin.getLocales().getLocale("error_member_higher_role", evicted.get().getUsername())
                            .ifPresent(user::sendMessage);
                    return;
                }

                town.removeMember(evicted.get().getUuid());
                town.getLog().log(Action.of(user, Action.Type.EVICT, evicted.get().getUsername() + " (" + user.getUsername() + ")"));
                plugin.getManager().updateTown(user, town);
                plugin.getLocales().getLocale("evicted_user", evicted.get().getUsername(),
                        town.getName()).ifPresent(user::sendMessage);

                Optional<? extends OnlineUser> evictedPlayer = plugin.getOnlineUsers().stream()
                        .filter(online -> online.getUuid().equals(evicted.get().getUuid()))
                        .findFirst();
                if (evictedPlayer.isPresent()) {
                    plugin.getLocales().getLocale("evicted_you", town.getName(), user.getUsername())
                            .ifPresent(evictedPlayer.get()::sendMessage);
                    return;
                }
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_EVICTED)
                        .payload(Payload.string(user.getUsername()))
                        .target(evicted.get().getUsername(), Message.TargetType.PLAYER)
                        .build()
                        .send(broker, user));
            }));
        }

        public void promoteMember(@NotNull OnlineUser user, @NotNull String member) {
            plugin.getManager().validateTownMembership(user, Privilege.PROMOTE).ifPresent(townMember -> plugin.runAsync(() -> {
                final Optional<User> promoted = plugin.getDatabase().getUser(member).map(SavedUser::user);
                if (promoted.isEmpty()) {
                    plugin.getLocales().getLocale("error_user_not_found", member)
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Optional<Member> promotedMember = plugin.getUserTown(promoted.get());
                if (promotedMember.isEmpty() || promotedMember.map(townMember1 -> !townMember1.town()
                        .equals(townMember.town())).orElse(false)) {
                    plugin.getLocales().getLocale("error_other_not_in_town",
                            promoted.get().getUsername()).ifPresent(user::sendMessage);
                    return;
                }

                final Town town = townMember.town();
                final Optional<Role> nextRole = plugin.getRoles().fromWeight(promotedMember.get().role().getWeight() + 1);
                if (promotedMember.get().role().getWeight() >= townMember.role().getWeight() - 1 || nextRole.isEmpty()) {
                    plugin.getLocales().getLocale("error_member_higher_role")
                            .ifPresent(user::sendMessage);
                    return;
                }
                final Role newRole = nextRole.get();

                town.getMembers().put(promoted.get().getUuid(), newRole.getWeight());
                plugin.getManager().updateTown(user, town);
                plugin.getLocales().getLocale("promoted_user",
                        promoted.get().getUsername(), newRole.getName()).ifPresent(user::sendMessage);

                Optional<? extends OnlineUser> evictedPlayer = plugin.getOnlineUsers().stream()
                        .filter(online -> online.getUuid().equals(promoted.get().getUuid()))
                        .findFirst();
                if (evictedPlayer.isPresent()) {
                    plugin.getLocales().getLocale("promoted_you", newRole.getName(),
                            user.getUsername()).ifPresent(evictedPlayer.get()::sendMessage);
                    return;
                }
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_PROMOTED)
                        .payload(Payload.string(user.getUsername()))
                        .target(promoted.get().getUsername(), Message.TargetType.PLAYER)
                        .build()
                        .send(broker, user));
            }));
        }

        public void demoteMember(@NotNull OnlineUser user, @NotNull String member) {
            plugin.getManager().validateTownMembership(user, Privilege.DEMOTE).ifPresent(townMember -> plugin.runAsync(() -> {
                final Optional<User> demoted = plugin.getDatabase().getUser(member).map(SavedUser::user);
                if (demoted.isEmpty()) {
                    plugin.getLocales().getLocale("error_user_not_found", member)
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Optional<Member> demotedMember = plugin.getUserTown(demoted.get());
                if (demotedMember.isEmpty() || demotedMember.map(townMember1 -> !townMember1.town()
                        .equals(townMember.town())).orElse(false)) {
                    plugin.getLocales().getLocale("error_other_not_in_town",
                            demoted.get().getUsername()).ifPresent(user::sendMessage);
                    return;
                }

                final Town town = townMember.town();
                if (demotedMember.get().role().getWeight() >= townMember.role().getWeight()) {
                    plugin.getLocales().getLocale("error_member_higher_role")
                            .ifPresent(user::sendMessage);
                    return;
                }

                final Optional<Role> nextRole = plugin.getRoles().fromWeight(demotedMember.get().role().getWeight() - 1);
                if (nextRole.isEmpty()) {
                    plugin.getLocales().getLocale("error_member_lowest_role",
                            demoted.get().getUsername()).ifPresent(user::sendMessage);
                    return;
                }
                final Role newRole = nextRole.get();

                town.getMembers().put(demoted.get().getUuid(), newRole.getWeight());
                plugin.getManager().updateTown(user, town);
                plugin.getLocales().getLocale("demoted_user",
                        demoted.get().getUsername(), newRole.getName()).ifPresent(user::sendMessage);

                Optional<? extends OnlineUser> evictedPlayer = plugin.getOnlineUsers().stream()
                        .filter(online -> online.getUuid().equals(demoted.get().getUuid()))
                        .findFirst();
                if (evictedPlayer.isPresent()) {
                    plugin.getLocales().getLocale("demoted_you", newRole.getName(),
                            user.getUsername()).ifPresent(evictedPlayer.get()::sendMessage);
                    return;
                }
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_DEMOTED)
                        .payload(Payload.string(user.getUsername()))
                        .target(demoted.get().getUsername(), Message.TargetType.PLAYER)
                        .build()
                        .send(broker, user));
            }));
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
                            .map(MineDown::toComponent)
                            .ifPresent(message -> plugin.getManager().sendTownNotification(town, message));

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
                    town.getGreeting().flatMap(greeting -> plugin.getLocales().getLocale("town_greeting_set",
                            greeting, town.getColorRgb())).ifPresent(user::sendMessage);
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
                    town.getFarewell().flatMap(farewell -> plugin.getLocales().getLocale("town_farewell_set",
                            farewell, town.getColorRgb())).ifPresent(user::sendMessage);
                });
            });
        }

        public void setTownColor(@NotNull OnlineUser user, @Nullable String newColor) {
            plugin.getManager().validateTownMembership(user, Privilege.SET_COLOR).ifPresent(member -> {
                if (newColor == null) {
                    plugin.getLocales().getLocale("town_color_picker_title").ifPresent(user::sendMessage);
                    user.sendMessage(ColorPicker.builder().command("/husktowns:town color").build().toComponent());
                    return;
                }

                // Parse java.awt.Color object from input hex string
                final Color color;
                try {
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
            plugin.getManager().validateTownMembership(user, Privilege.SET_SPAWN).ifPresent(member -> plugin.getManager()
                    .validateClaimOwnership(member, user, position.getChunk(), position.getWorld()).ifPresent(claim -> plugin.runAsync(() -> {
                        final Town town = member.town();
                        final Spawn spawn = Spawn.of(position, plugin.getServerName());
                        town.getLog().log(Action.of(user, Action.Type.UPDATE_SPAWN, town.getSpawn().map(
                                oldSpawn -> oldSpawn + " → ").orElse("") + spawn));
                        town.setSpawn(spawn);
                        plugin.getManager().updateTown(user, town);
                        plugin.getLocales().getLocale("town_spawn_set", town.getName())
                                .ifPresent(user::sendMessage);
                    })));
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

        public void teleportToTownSpawn(@NotNull OnlineUser user, @Nullable String townName) {
            final Optional<Town> optionalTown = townName == null ? plugin.getUserTown(user).map(Member::town) :
                    plugin.getTowns().stream().filter(t -> t.getName().equalsIgnoreCase(townName)).findFirst();
            if (optionalTown.isEmpty()) {
                plugin.getLocales().getLocale("error_town_spawn_not_found")
                        .ifPresent(user::sendMessage);
                return;
            }

            final Optional<Member> member = plugin.getUserTown(user);
            final Town town = optionalTown.get();
            if (town.getSpawn().isEmpty()) {
                plugin.getLocales().getLocale("error_town_spawn_not_set")
                        .ifPresent(user::sendMessage);
                return;
            }

            if (!town.getSpawn().get().isPublic() && member.isEmpty() || member.isPresent() && !member.get().town().equals(town)) {
                plugin.getLocales().getLocale("error_town_spawn_not_public")
                        .ifPresent(user::sendMessage);
                return;
            }

            final Spawn spawn = town.getSpawn().get();
            plugin.getLocales().getLocale("teleporting_town_spawn", town.getName())
                    .ifPresent(user::sendMessage);
            if (spawn.getServer() != null && !spawn.getServer().equals(plugin.getServerName())) {
                final Optional<Preferences> optionalPreferences = plugin.getUserPreferences(user.getUuid());
                optionalPreferences.ifPresent(preferences -> plugin.runAsync(() -> {
                    preferences.setCurrentTeleportTarget(spawn.getPosition());
                    plugin.getDatabase().updateUser(user, preferences);
                    plugin.getMessageBroker().ifPresent(broker -> broker.changeServer(user, spawn.getServer()));
                }));
                return;
            }

            plugin.runSync(() -> {
                user.teleportTo(spawn.getPosition());
                plugin.getLocales().getLocale("teleportation_complete")
                        .ifPresent(user::sendActionBar);
            });
        }

        public void depositMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount) {
            final Optional<EconomyHook> optionalHook = plugin.getEconomyHook();
            if (optionalHook.isEmpty()) {
                plugin.getLocales().getLocale("error_economy_not_in_use")
                        .ifPresent(user::sendMessage);
                return;
            }
            final EconomyHook economy = optionalHook.get();

            plugin.getManager().validateTownMembership(user, Privilege.DEPOSIT).ifPresent(member -> {
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    plugin.getLocales().getLocale("error_invalid_money_amount",
                            economy.formatMoney(new BigDecimal(0))).ifPresent(user::sendMessage);
                    return;
                }

                plugin.runAsync(() -> {
                    final Town town = member.town();
                    if (!economy.hasMoney(user, amount)) {
                        plugin.getLocales().getLocale("error_economy_insufficient_funds",
                                economy.formatMoney(amount)).ifPresent(user::sendMessage);
                        return;
                    }
                    economy.takeMoney(user, amount);
                    town.getLog().log(Action.of(user, Action.Type.DEPOSIT_MONEY, economy.formatMoney(amount)));
                    town.setMoney(town.getMoney().add(amount));
                    plugin.getManager().updateTown(user, town);
                    plugin.getLocales().getLocale("town_economy_deposit", economy.formatMoney(amount),
                            economy.formatMoney(town.getMoney())).ifPresent(user::sendMessage);
                });
            });
        }

        public void withdrawMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount) {
            final Optional<EconomyHook> optionalHook = plugin.getEconomyHook();
            if (optionalHook.isEmpty()) {
                plugin.getLocales().getLocale("error_economy_not_in_use")
                        .ifPresent(user::sendMessage);
                return;
            }
            final EconomyHook economy = optionalHook.get();

            plugin.getManager().validateTownMembership(user, Privilege.WITHDRAW).ifPresent(member -> {
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    plugin.getLocales().getLocale("error_invalid_money_amount",
                            economy.formatMoney(new BigDecimal(0))).ifPresent(user::sendMessage);
                    return;
                }

                plugin.runAsync(() -> {
                    final Town town = member.town();
                    final BigDecimal withdrawal = amount.min(town.getMoney());
                    economy.giveMoney(user, withdrawal);
                    town.getLog().log(Action.of(user, Action.Type.WITHDRAW_MONEY, economy.formatMoney(withdrawal)));
                    town.setMoney(town.getMoney().subtract(withdrawal));
                    plugin.getManager().updateTown(user, town);
                    plugin.getLocales().getLocale("town_economy_withdraw", economy.formatMoney(withdrawal),
                            economy.formatMoney(town.getMoney())).ifPresent(user::sendMessage);
                });
            });
        }

        public void levelUpTown(@NotNull OnlineUser user) {
            plugin.getManager().validateTownMembership(user, Privilege.LEVEL_UP).ifPresent(member -> {
                final Town town = member.town();
                if (town.getLevel() >= plugin.getLevels().getMaxLevel()) {
                    plugin.getLocales().getLocale("error_town_max_level")
                            .ifPresent(user::sendMessage);
                    return;
                }

                final BigDecimal price = plugin.getLevels().getLevelUpCost(town.getLevel());
                final BigDecimal townBalance = town.getMoney();
                if (townBalance.compareTo(price) < 0) {
                    plugin.getLocales().getLocale("error_economy_town_insufficient_funds",
                                    plugin.getEconomyHook().map(hook -> hook.formatMoney(price)).orElse(price.toString()))
                            .ifPresent(user::sendMessage);
                    return;
                }

                town.getLog().log(Action.of(user, Action.Type.LEVEL_UP,
                        town.getLevel() + " → " + (town.getLevel() + 1)));
                town.setLevel(town.getLevel() + 1);
                town.setMoney(townBalance.subtract(price));
                plugin.getManager().updateTown(user, town);

                // Notify town members
                plugin.getLocales().getLocale("town_levelled_up", Integer.toString(town.getLevel()))
                        .map(MineDown::toComponent)
                        .ifPresent(message -> plugin.getManager().sendTownNotification(town, message));
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_LEVEL_UP)
                        .payload(Payload.integer(town.getId()))
                        .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                        .build()
                        .send(broker, user));
            });
        }

        public void transferOwnership(@NotNull OnlineUser user, @NotNull String target) {
            final Optional<Member> mayor = plugin.getUserTown(user);
            if (mayor.isEmpty()) {
                plugin.getLocales().getLocale("error_not_in_town")
                        .ifPresent(user::sendMessage);
                return;
            }

            final Town town = mayor.get().town();
            if (!town.getMayor().equals(user.getUuid())) {
                plugin.getLocales().getLocale("error_not_town_mayor", town.getName())
                        .ifPresent(user::sendMessage);
                return;
            }

            // Validate target is a member of the town
            final Optional<User> targetUser = plugin.getDatabase().getUser(target).map(SavedUser::user);
            if (targetUser.isEmpty()) {
                plugin.getLocales().getLocale("error_user_not_found", target)
                        .ifPresent(user::sendMessage);
                return;
            }
            final Optional<Member> member = plugin.getUserTown(targetUser.get());
            if (member.isEmpty() || !member.get().town().equals(town)) {
                plugin.getLocales().getLocale("error_other_not_in_town",
                        targetUser.get().getUsername()).ifPresent(user::sendMessage);
                return;
            }
            if (member.get().role().getWeight() >= mayor.get().role().getWeight()) {
                plugin.getLocales().getLocale("error_user_not_found",
                        targetUser.get().getUsername()).ifPresent(user::sendMessage);
                return;
            }

            plugin.runAsync(() -> {
                town.getMembers().put(mayor.get().user().getUuid(), plugin.getRoles().getDefaultRole().getWeight());
                town.getMembers().put(targetUser.get().getUuid(), plugin.getRoles().getMayorRole().getWeight());
                town.getLog().log(Action.of(user, Action.Type.TRANSFER_OWNERSHIP,
                        mayor.get().user().getUsername() + " → " + targetUser.get().getUsername()));
                plugin.getManager().updateTown(user, town);

                // Notify town members
                plugin.getLocales().getLocale("town_transferred", town.getName(),
                                targetUser.get().getUsername()).map(MineDown::toComponent)
                        .ifPresent(message -> plugin.getManager().sendTownNotification(town, message));
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_TRANSFERRED)
                        .payload(Payload.integer(town.getId()))
                        .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                        .build()
                        .send(broker, user));
            });
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

        public void setFlagRule(@NotNull OnlineUser user, @NotNull Flag flag, @NotNull Claim.Type type, boolean value, boolean showMenu) {
            plugin.getManager().validateTownMembership(user, Privilege.SET_RULES).ifPresent(member -> {
                final Town town = member.town();
                town.getRules().get(type).setFlag(flag, value);
                town.getLog().log(Action.of(user, Action.Type.SET_FLAG_RULE, flag.name().toLowerCase() + ": " + value));
                plugin.getManager().updateTown(user, town);
                plugin.getLocales().getLocale("town_flag_set", flag.name().toLowerCase(), Boolean.toString(value),
                        type.name().toLowerCase()).ifPresent(user::sendMessage);
                if (showMenu) {
                    showRulesConfig(user);
                }
            });
        }

        public void showRulesConfig(@NotNull OnlineUser user) {
            plugin.getManager().validateTownMembership(user, Privilege.SET_RULES)
                    .ifPresent(member -> RulesConfig.of(plugin, member.town(), user).show());
        }

        public void sendChatMessage(@NotNull OnlineUser user, @Nullable String message) {
            plugin.getManager().validateTownMembership(user, Privilege.CHAT).ifPresent(member -> {
                if (message == null) {
                    plugin.getUserPreferences(user.getUuid()).ifPresent(preferences -> {
                        preferences.setTownChatTalking(!preferences.isTownChatTalking());
                        plugin.runAsync(() -> plugin.getDatabase().updateUser(user, preferences));
                        plugin.getLocales().getLocale(preferences.isTownChatTalking() ? "town_chat_talking" : "town_chat_not_talking")
                                .ifPresent(user::sendMessage);
                    });
                    return;
                }

                final Town town = member.town();
                plugin.getLocales().getLocale("town_chat_message_format",
                                town.getName(), town.getColorRgb(), member.user().getUsername(),
                                member.role().getName(), message)
                        .map(MineDown::toComponent)
                        .ifPresent(locale -> plugin.getManager().sendTownNotification(town, locale));

                // Send globally via message
                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                        .type(Message.Type.TOWN_CHAT_MESSAGE)
                        .payload(Payload.string(message))
                        .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                        .build()
                        .send(broker, user));
            });
        }

        public void leaveTown(@NotNull OnlineUser executor) {
            // Check if the user is in a town
            final Optional<Member> member = plugin.getUserTown(executor);
            if (member.isEmpty()) {
                plugin.getLocales().getLocale("error_not_in_town")
                        .ifPresent(executor::sendMessage);
                return;
            }

            // Check if the user is the mayor
            if (member.get().role().equals(plugin.getRoles().getMayorRole())) {
                plugin.getLocales().getLocale("error_mayor_cannot_leave")
                        .ifPresent(executor::sendMessage);
                return;
            }

            // Remove the user from the town
            final Town town = member.get().town();
            plugin.runAsync(() -> {
                town.removeMember(member.get().user().getUuid());
                plugin.getManager().updateTown(executor, town);
                plugin.getLocales().getLocale("left_town", town.getName())
                        .ifPresent(executor::sendMessage);
                plugin.getLocales().getLocale("user_left_town", member.get().user().getUsername())
                        .map(MineDown::toComponent)
                        .ifPresent(message -> plugin.getManager().sendTownNotification(town, message));
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
                if (town.getClaimCount() + 1 > town.getMaxClaims(plugin)) {
                    plugin.getLocales().getLocale("error_claim_limit_reached", Integer.toString(town.getClaimCount()),
                                    Integer.toString(town.getMaxClaims(plugin)))
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

                plugin.runAsync(() -> {
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
                });
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

    public static class Admin {
        private final HuskTowns plugin;

        private Admin(@NotNull HuskTowns plugin) {
            this.plugin = plugin;
        }

        public void createAdminClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, boolean showMap) {
            final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
            if (existingClaim.isPresent()) {
                plugin.getLocales().getLocale("error_chunk_claimed_by")
                        .ifPresent(user::sendMessage);
                return;
            }

            final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
            if (claimWorld.isEmpty()) {
                plugin.getLocales().getLocale("error_world_not_claimable")
                        .ifPresent(user::sendMessage);
                return;
            }

            plugin.runAsync(() -> {
                final TownClaim claim = new TownClaim(plugin.getAdminTown(), Claim.at(chunk));
                claimWorld.get().addAdminClaim(claim.claim());
                plugin.getDatabase().updateClaimWorld(claimWorld.get());
                plugin.getLocales().getLocale("admin_claim_created",
                                Integer.toString(chunk.getX()), Integer.toString(chunk.getZ()))
                        .ifPresent(user::sendMessage);
                plugin.highlightClaim(user, claim);
                if (showMap) {
                    user.sendMessage(ClaimMap.builder(plugin)
                            .center(user.getChunk()).world(user.getWorld())
                            .build()
                            .toComponent(user));
                }
            });
        }

        public void deleteClaim(@NotNull OnlineUser user, @NotNull World world, @NotNull Chunk chunk, boolean showMap) {
            final Optional<TownClaim> existingClaim = plugin.getClaimAt(chunk, world);
            if (existingClaim.isEmpty()) {
                plugin.getLocales().getLocale("error_chunk_not_claimed")
                        .ifPresent(user::sendMessage);
                return;
            }

            final Optional<ClaimWorld> claimWorld = plugin.getClaimWorld(world);
            assert claimWorld.isPresent();

            plugin.runAsync(() -> {
                if (existingClaim.get().isAdminClaim(plugin)) {
                    claimWorld.get().removeAdminClaim(chunk);
                } else {
                    claimWorld.get().removeClaim(existingClaim.get().town(), chunk);
                    existingClaim.get().town().setClaimCount(existingClaim.get().town().getClaimCount() - 1);
                    plugin.getManager().updateTown(user, existingClaim.get().town());
                }
                plugin.getDatabase().updateClaimWorld(claimWorld.get());
                plugin.getLocales().getLocale("claim_deleted", Integer.toString(chunk.getX()),
                        Integer.toString(chunk.getZ())).ifPresent(user::sendMessage);
                if (showMap) {
                    user.sendMessage(ClaimMap.builder(plugin)
                            .center(user.getChunk()).world(user.getWorld())
                            .build()
                            .toComponent(user));
                }
            });
        }

        public void deleteTown(@NotNull OnlineUser user, @NotNull String townName) {
            plugin.getTowns().stream()
                    .filter(town -> town.getName().equalsIgnoreCase(townName))
                    .findFirst()
                    .ifPresentOrElse(town -> plugin.getManager().towns().deleteTownData(user, town),
                            () -> plugin.getLocales().getLocale("error_town_not_found", townName)
                                    .ifPresent(user::sendMessage));
        }

        public void assumeTownOwnership(@NotNull OnlineUser user, @NotNull String townName) {
            plugin.getTowns().stream()
                    .filter(town -> town.getName().equalsIgnoreCase(townName))
                    .findFirst()
                    .ifPresentOrElse(town -> {
                        final Optional<Member> existingMembership = plugin.getUserTown(user);
                        if (existingMembership.isPresent()) {
                            plugin.getLocales().getLocale("error_already_in_town")
                                    .ifPresent(user::sendMessage);
                            return;
                        }

                        plugin.runAsync(() -> {
                            town.getMembers().put(town.getMayor(), plugin.getRoles().getDefaultRole().getWeight());
                            town.getMembers().put(user.getUuid(), plugin.getRoles().getMayorRole().getWeight());
                            town.getLog().log(Action.of(user, Action.Type.ADMIN_ASSUME_OWNERSHIP, user.getUsername()));
                            plugin.getManager().updateTown(user, town);

                            plugin.getLocales().getLocale("town_assumed_ownership", town.getName())
                                    .ifPresent(user::sendMessage);
                        });
                    }, () -> plugin.getLocales().getLocale("error_town_not_found", townName)
                            .ifPresent(user::sendMessage));
        }
    }
}
