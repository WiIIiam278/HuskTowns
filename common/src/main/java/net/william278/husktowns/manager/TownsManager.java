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
import net.kyori.adventure.text.format.TextColor;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Action;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.Flag;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.events.IMemberJoinEvent;
import net.william278.husktowns.events.IMemberLeaveEvent;
import net.william278.husktowns.hook.EconomyHook;
import net.william278.husktowns.menu.ColorPicker;
import net.william278.husktowns.menu.RulesConfig;
import net.william278.husktowns.network.Message;
import net.william278.husktowns.network.Payload;
import net.william278.husktowns.town.*;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
import net.william278.husktowns.util.Validator;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TownsManager {

    private static final String SPAWN_PRIVACY_BYPASS_PERMISSION = "husktowns.spawn_privacy_bypass";
    private final HuskTowns plugin;

    protected TownsManager(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
    }

    public void createTown(@NotNull OnlineUser user, @NotNull String townName) {
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

        // Check against town creation collateral requirements
        final BigDecimal collateral = plugin.getSettings().getTowns().isRequireFirstLevelCollateral()
            ? plugin.getLevels().getLevelUpCost(0) : BigDecimal.ZERO;
        final Optional<EconomyHook> hook = plugin.getEconomyHook();
        if (!collateral.equals(BigDecimal.ZERO) && hook.isPresent() && !hook.get().hasMoney(user, collateral)) {
            plugin.getLocales().getLocale("error_economy_insufficient_funds", hook.get().formatMoney(collateral))
                .ifPresent(user::sendMessage);
            return;
        }

        // Fire the event and create the town
        plugin.fireEvent(plugin.getTownCreateEvent(user, townName), (event -> {
            final Town town = createTownData(user, event.getTownName());
            if (!collateral.equals(BigDecimal.ZERO) && hook.isPresent()) {
                hook.ifPresent(economyHook -> economyHook.takeMoney(user, collateral, "Founded " + town.getName()));
            }

            plugin.getLocales().getLocale("town_created", town.getName())
                .ifPresent(user::sendMessage);
            plugin.checkAdvancements(town, user);
            plugin.fireEvent(plugin.getPostTownCreateEvent(user, town));
        }));
    }

    @NotNull
    public Town createTownData(@NotNull OnlineUser user, @NotNull String townName) {
        final Town town = plugin.getDatabase().createTown(townName, user);
        plugin.getTowns().add(town);
        plugin.getMessageBroker().ifPresent(broker -> Message.builder()
            .type(Message.Type.TOWN_UPDATE)
            .payload(Payload.integer(town.getId()))
            .target(Message.TARGET_ALL, Message.TargetType.SERVER)
            .build()
            .send(broker, user));
        return town;
    }

    public void deleteTownConfirm(@NotNull OnlineUser user, boolean confirmed) {
        plugin.getManager().ifMayor(user, mayor -> {
            if (!confirmed) {
                plugin.getLocales().getLocale("town_delete_confirm",
                    mayor.town().getName()).ifPresent(user::sendMessage);
                return;
            }

            this.deleteTown(user, mayor.town());
        });
    }

    protected void deleteTown(@NotNull OnlineUser user, @NotNull Town town) {
        plugin.fireEvent(plugin.getTownDisbandEvent(user, town), (event -> {
            plugin.getManager().sendTownMessage(town, plugin.getLocales()
                .getLocale("town_deleted_notification", town.getName())
                .map(MineDown::toComponent).orElse(Component.empty()));

            deleteTownData(user, town);
            plugin.getLocales().getLocale("town_deleted", town.getName())
                .ifPresent(user::sendMessage);
        }));
    }

    public void deleteTownData(@Nullable OnlineUser user, @NotNull Town town) {
        plugin.getMapHook().ifPresent(mapHook -> mapHook.removeClaimMarkers(town));
        plugin.getDatabase().deleteTown(town.getId());
        plugin.removeTown(town);
        plugin.getClaimWorlds().values().forEach(world -> {
            if (world.removeTownClaims(town.getId()) > 0) {
                plugin.getDatabase().updateClaimWorld(world);
            }
        });

        // Propagate the town deletion to all servers
        if (user != null) {
            plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                .type(Message.Type.TOWN_DELETE)
                .payload(Payload.integer(town.getId()))
                .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                .build()
                .send(broker, user));
        }
    }

    public void inviteMember(@NotNull OnlineUser user, @NotNull String target) {
        plugin.getManager().ifMember(user, Privilege.INVITE, (member -> {
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
            final Invite invite = Invite.create(town.getId(), user);
            if (localUser.isEmpty()) {
                if (!plugin.getSettings().getCrossServer().isEnabled()) {
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
        final Optional<Town> town = plugin.findTown(invite.getTownId());
        if (plugin.getUserTown(user).isPresent() || town.isEmpty()) {
            plugin.log(Level.WARNING, "Received an invalid invite from "
                + invite.getSender().getUsername() + " to " + invite.getTownId());
            return;
        }

        plugin.addInvite(user.getUuid(), invite);
        plugin.getLocales().getLocale("invite_received",
            invite.getSender().getUsername(), town.get().getName()).ifPresent(user::sendMessage);
        plugin.getLocales().getLocale("invite_buttons",
            invite.getSender().getUsername()).ifPresent(user::sendMessage);
    }

    public void handleInviteReply(@NotNull OnlineUser user, boolean accepted, @Nullable String selectedInviter) {
        final Optional<Invite> invite = plugin.getLastInvite(user, selectedInviter);
        if (invite.isEmpty()) {
            plugin.getLocales().getLocale("error_no_invites")
                .ifPresent(user::sendMessage);
            return;
        }

        if (accepted) {
            this.acceptInvite(user, invite.get());
        } else {
            this.declineInvite(user, invite.get());
        }
    }

    private void declineInvite(@NotNull OnlineUser user, @NotNull Invite invite) {
        plugin.getLocales().getLocale("invite_declined", invite.getSender().getUsername())
            .ifPresent(user::sendMessage);

        // Reply to the sender
        plugin.getOnlineUsers().stream()
            .filter(online -> online.equals(invite.getSender())).findFirst()
            .ifPresent(sender -> plugin.getLocales().getLocale("invite_declined_by", user.getUsername())
                .ifPresent(sender::sendMessage));

        plugin.getMessageBroker().ifPresent(broker -> Message.builder()
            .type(Message.Type.TOWN_INVITE_REPLY)
            .payload(Payload.bool(false))
            .target(invite.getSender().getUsername(), Message.TargetType.PLAYER)
            .build()
            .send(broker, user));
        plugin.removeInvite(user.getUuid(), invite);
    }

    private void acceptInvite(@NotNull OnlineUser user, @NotNull Invite invite) {
        final Optional<Town> optionalTown = plugin.findTown(invite.getTownId());
        if (optionalTown.isEmpty()) {
            plugin.getLocales().getLocale("error_town_no_longer_exists")
                .ifPresent(user::sendMessage);
            return;
        }

        final Town userTown = optionalTown.get();
        if (userTown.getMembers().size() >= userTown.getMaxMembers(plugin)) {
            plugin.getLocales().getLocale("error_town_member_limit_reached",
                    Integer.toString(optionalTown.get().getMembers().size()),
                    Integer.toString(userTown.getMaxMembers(plugin)))
                .ifPresent(user::sendMessage);
            return;
        }

        final Role role = plugin.getRoles().getDefaultRole();
        plugin.fireEvent(plugin.getMemberJoinEvent(user, userTown, role, IMemberJoinEvent.JoinReason.ACCEPT_INVITE),
            (onJoin -> plugin.getManager().editTown(user, onJoin.getTown(), (town -> {
                town.addMember(user.getUuid(), plugin.getRoles().getDefaultRole());
                town.getLog().log(Action.of(user, Action.Type.MEMBER_JOIN,
                    user.getUsername() + " (" + invite.getSender().getUsername() + ")"));

                plugin.getLocales().getLocale("invite_accepted", town.getName())
                    .ifPresent(user::sendMessage);
                plugin.clearInvites(user.getUuid());
            }), (town -> {
                plugin.getLocales().getLocale("user_joined_town",
                        user.getUsername(), town.getName()).map(MineDown::toComponent)
                    .ifPresent(message -> plugin.getManager().sendTownMessage(town, message));

                plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                    .type(Message.Type.TOWN_INVITE_REPLY)
                    .payload(Payload.bool(true))
                    .target(invite.getSender().getUsername(), Message.TargetType.PLAYER)
                    .build()
                    .send(broker, user));
            }))));
    }

    public void leaveTown(@NotNull OnlineUser user) {
        plugin.getManager().ifMember(user, (member -> {
            // Check if the user is the mayor
            if (member.role().equals(plugin.getRoles().getMayorRole())) {
                plugin.getLocales().getLocale("error_mayor_cannot_leave")
                    .ifPresent(user::sendMessage);
                return;
            }

            // Remove the user from the town
            plugin.fireEvent(plugin.getMemberLeaveEvent(user, member.town(), member.role(), IMemberLeaveEvent.LeaveReason.LEAVE),
                (onLeave -> plugin.getManager().editTown(user, onLeave.getTown(), (town -> {

                    town.removeMember(user.getUuid());
                    town.getLog().log(Action.of(user, Action.Type.MEMBER_LEAVE, user.getUsername()));
                    plugin.getLocales().getLocale("left_town", town.getName())
                        .ifPresent(user::sendMessage);
                }), (town -> {
                    // Broadcast the departure to local town members
                    plugin.getLocales().getLocale("user_left_town",
                            user.getUsername(), town.getName()).map(MineDown::toComponent)
                        .ifPresent(message -> plugin.getManager().sendTownMessage(town, message));
                    plugin.editUserPreferences(user, (preferences -> preferences.setTownChatTalking(false)));
                }))));
        }));
    }

    public void removeMember(@NotNull OnlineUser user, @NotNull String memberName) {
        plugin.getManager().ifMember(user, Privilege.EVICT, (member -> {
            final Optional<User> evicted = plugin.getDatabase().getUser(memberName).map(SavedUser::user);
            if (evicted.isEmpty()) {
                plugin.getLocales().getLocale("error_user_not_found", memberName)
                    .ifPresent(user::sendMessage);
                return;
            }

            final Optional<Member> evictedMember = plugin.getUserTown(evicted.get());
            if (evictedMember.isEmpty() || evictedMember.map(townMember -> !townMember.town().equals(member.town())).orElse(false)) {
                plugin.getLocales().getLocale("error_other_not_in_town",
                    evicted.get().getUsername()).ifPresent(user::sendMessage);
                return;
            }

            if (evictedMember.get().role().getWeight() >= member.role().getWeight()) {
                plugin.getLocales().getLocale("error_member_higher_role", evicted.get().getUsername())
                    .ifPresent(user::sendMessage);
                return;
            }

            plugin.fireEvent(plugin.getMemberLeaveEvent(evictedMember.get().user(), evictedMember.get().town(), evictedMember.get().role(), IMemberLeaveEvent.LeaveReason.EVICTED),
                (onEvicted -> plugin.getManager().editTown(user, onEvicted.getTown(), (town -> {
                    town.removeMember(evicted.get().getUuid());
                    town.getLog().log(Action.of(user, Action.Type.EVICT, evicted.get().getUsername() + " (" + user.getUsername() + ")"));
                    plugin.getLocales().getLocale("evicted_user", evicted.get().getUsername(),
                        town.getName()).ifPresent(user::sendMessage);

                    plugin.getOnlineUsers().stream()
                        .filter(online -> online.equals(evicted.get()))
                        .findFirst()
                        .ifPresentOrElse(onlineUser -> {
                                plugin.getLocales()
                                    .getLocale("evicted_you", town.getName(), user.getUsername())
                                    .ifPresent(onlineUser::sendMessage);
                                plugin.editUserPreferences(evicted.get(),
                                    (preferences -> preferences.setTownChatTalking(false)));
                            },
                            () -> plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                                .type(Message.Type.TOWN_EVICTED)
                                .target(memberName, Message.TargetType.PLAYER)
                                .build()
                                .send(broker, user)));
                }))));
        }));
    }

    public void promoteMember(@NotNull OnlineUser user, @NotNull String memberName) {
        plugin.getManager().ifMember(user, Privilege.PROMOTE, (member -> {
            final Optional<User> promoted = plugin.getDatabase().getUser(memberName).map(SavedUser::user);
            if (promoted.isEmpty()) {
                plugin.getLocales().getLocale("error_user_not_found", memberName)
                    .ifPresent(user::sendMessage);
                return;
            }

            final Optional<Member> promotedMember = plugin.getUserTown(promoted.get());
            if (promotedMember.isEmpty() || promotedMember.map(townMember1 -> !townMember1.town()
                .equals(member.town())).orElse(false)) {
                plugin.getLocales().getLocale("error_other_not_in_town",
                    promoted.get().getUsername()).ifPresent(user::sendMessage);
                return;
            }

            final Optional<Role> nextRole = plugin.getRoles().fromWeight(promotedMember.get().role().getWeight() + 1);
            if (promotedMember.get().role().getWeight() >= member.role().getWeight() - 1 || nextRole.isEmpty()) {
                plugin.getLocales().getLocale("error_member_higher_role")
                    .ifPresent(user::sendMessage);
                return;
            }

            final Role newRole = nextRole.get();
            plugin.fireEvent(plugin.getMemberRoleChangeEvent(promoted.get(), promotedMember.get().town(), promotedMember.get().role(), newRole),
                (onPromoted -> plugin.getManager().editTown(user, onPromoted.getTown(), (town -> {
                    town.getMembers().put(promoted.get().getUuid(), newRole.getWeight());
                    plugin.getLocales().getLocale("promoted_user",
                        promoted.get().getUsername(), newRole.getName()).ifPresent(user::sendMessage);

                    plugin.getOnlineUsers().stream()
                        .filter(online -> online.equals(promoted.get()))
                        .findFirst()
                        .ifPresentOrElse(onlineUser -> plugin.getLocales()
                                .getLocale("promoted_you", newRole.getName(), user.getUsername())
                                .ifPresent(onlineUser::sendMessage),
                            () -> plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                                .type(Message.Type.TOWN_PROMOTED)
                                .payload(Payload.integer(newRole.getWeight()))
                                .target(memberName, Message.TargetType.PLAYER)
                                .build()
                                .send(broker, user)));
                }))));
        }));
    }

    public void demoteMember(@NotNull OnlineUser user, @NotNull String memberName) {
        plugin.getManager().ifMember(user, Privilege.DEMOTE, (member -> {
            final Optional<User> demoted = plugin.getDatabase().getUser(memberName).map(SavedUser::user);
            if (demoted.isEmpty()) {
                plugin.getLocales().getLocale("error_user_not_found", memberName)
                    .ifPresent(user::sendMessage);
                return;
            }

            final Optional<Member> demotedMember = plugin.getUserTown(demoted.get());
            if (demotedMember.isEmpty() || demotedMember.map(townMember1 -> !townMember1.town()
                .equals(member.town())).orElse(false)) {
                plugin.getLocales().getLocale("error_other_not_in_town",
                    demoted.get().getUsername()).ifPresent(user::sendMessage);
                return;
            }

            if (demotedMember.get().role().getWeight() >= member.role().getWeight()) {
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
            plugin.fireEvent(plugin.getMemberRoleChangeEvent(demotedMember.get().user(), demotedMember.get().town(), demotedMember.get().role(), newRole),
                (onDemote -> plugin.getManager().editTown(user, onDemote.getTown(), (town -> {
                    town.getMembers().put(demoted.get().getUuid(), newRole.getWeight());
                    plugin.getLocales().getLocale("demoted_user",
                        demoted.get().getUsername(), newRole.getName()).ifPresent(user::sendMessage);

                    plugin.getOnlineUsers().stream()
                        .filter(online -> online.equals(demoted.get()))
                        .findFirst()
                        .ifPresentOrElse(onlineUser -> plugin.getLocales()
                                .getLocale("demoted_you", newRole.getName(), user.getUsername())
                                .ifPresent(onlineUser::sendMessage),
                            () -> plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                                .type(Message.Type.TOWN_DEMOTED)
                                .payload(Payload.integer(newRole.getWeight()))
                                .target(memberName, Message.TargetType.PLAYER)
                                .build()
                                .send(broker, user)));

                }))));
        }));
    }

    public void renameTown(@NotNull OnlineUser user, @NotNull String newName) {
        plugin.getManager().memberEditTown(user, Privilege.RENAME, (member -> {
            final Town town = member.town();
            if (!town.getName().equalsIgnoreCase(newName) && !plugin.getValidator().isValidTownName(newName)) {
                plugin.getLocales().getLocale("error_invalid_town_name")
                    .ifPresent(user::sendMessage);
                return false;
            }

            town.getLog().log(Action.of(user, Action.Type.RENAME_TOWN, town.getName() + " → " + newName));
            town.setName(newName);
            plugin.getMapHook().ifPresent(map -> map.reloadClaimMarkers(town));
            plugin.getLocales().getLocale("town_renamed", town.getName())
                .map(MineDown::toComponent)
                .ifPresent(message -> plugin.getManager().sendTownMessage(town, message));
            return true;
        }));
    }

    public void setTownBio(@NotNull OnlineUser user, @NotNull String newBio) {
        plugin.getManager().memberEditTown(user, Privilege.SET_BIO, (member -> {
            if (!plugin.getValidator().isValidTownMetadata(newBio)) {
                plugin.getLocales().getLocale("error_invalid_meta",
                    Integer.toString(Validator.MAX_TOWN_META_LENGTH)).ifPresent(user::sendMessage);
                return false;
            }

            final Town town = member.town();
            town.getLog().log(Action.of(user, Action.Type.UPDATE_BIO,
                town.getBio().map(bio -> bio + " → ").orElse("") + newBio));
            town.setBio(newBio);
            town.getBio().flatMap(bio -> plugin.getLocales().getLocale("town_bio_set", bio))
                .ifPresent(user::sendMessage);
            return true;
        }));
    }

    public void setTownGreeting(@NotNull OnlineUser user, @NotNull String newGreeting) {
        plugin.getManager().memberEditTown(user, Privilege.SET_GREETING, (member -> {
            if (!plugin.getValidator().isValidTownMetadata(newGreeting)) {
                plugin.getLocales().getLocale("error_invalid_meta",
                    Integer.toString(Validator.MAX_TOWN_META_LENGTH)).ifPresent(user::sendMessage);
                return false;
            }

            final Town town = member.town();
            town.getLog().log(Action.of(user, Action.Type.UPDATE_GREETING,
                town.getGreeting().map(greeting -> greeting + " → ").orElse("") + newGreeting));
            town.setGreeting(newGreeting);
            town.getGreeting().flatMap(greeting -> plugin.getLocales().getLocale("town_greeting_set",
                greeting, town.getColorRgb())).ifPresent(user::sendMessage);
            return true;
        }));
    }

    public void setTownFarewell(@NotNull OnlineUser user, @NotNull String newFarewell) {
        plugin.getManager().memberEditTown(user, Privilege.SET_FAREWELL, (member -> {
            if (!plugin.getValidator().isValidTownMetadata(newFarewell)) {
                plugin.getLocales().getLocale("error_invalid_meta",
                    Integer.toString(Validator.MAX_TOWN_META_LENGTH)).ifPresent(user::sendMessage);
                return false;
            }

            final Town town = member.town();
            town.getLog().log(Action.of(user, Action.Type.UPDATE_FAREWELL,
                town.getFarewell().map(farewell -> farewell + " → ").orElse("") + newFarewell));
            town.setFarewell(newFarewell);
            town.getFarewell().flatMap(farewell -> plugin.getLocales().getLocale("town_farewell_set",
                farewell, town.getColorRgb())).ifPresent(user::sendMessage);
            return true;
        }));
    }

    public void setTownColor(@NotNull OnlineUser user, @Nullable String newColor) {
        plugin.getManager().memberEditTown(user, Privilege.SET_COLOR, (member -> {
            if (newColor == null) {
                plugin.getLocales().getLocale("town_color_picker_title").ifPresent(user::sendMessage);
                user.sendMessage(ColorPicker.builder().command("/husktowns:town color").build().toComponent());
                return false;
            }

            // Parse color
            final TextColor color = TextColor.fromHexString(newColor);
            if (color == null) {
                plugin.getLocales().getLocale("error_invalid_color").ifPresent(user::sendMessage);
                return false;
            }

            final Town town = member.town();
            final String newColorRgb = String.format("#%02x%02x%02x", color.red(), color.green(), color.blue());
            town.getLog().log(Action.of(user, Action.Type.UPDATE_COLOR, town.getColorRgb() + " → " + newColorRgb));
            town.setTextColor(color);
            plugin.getMapHook().ifPresent(map -> map.reloadClaimMarkers(town));
            plugin.getMapHook().ifPresent(map -> map.reloadClaimMarkers(town));
            plugin.getLocales().getLocale("town_color_changed", member.town().getName(), member.town().getColorRgb())
                .ifPresent(user::sendMessage);
            return true;
        }));
    }

    public void setTownSpawn(@NotNull OnlineUser user, @NotNull Position position) {
        plugin.getManager().memberEditTown(user, Privilege.SET_SPAWN, (member -> {
            plugin.getManager().ifClaimOwner(member, user, position.getChunk(), position.getWorld(), (claim -> {
                final Town town = member.town();
                final Spawn spawn = Spawn.of(position, plugin.getServerName());
                town.getLog().log(Action.of(user, Action.Type.UPDATE_SPAWN, town.getSpawn().map(
                    oldSpawn -> oldSpawn + " → ").orElse("") + spawn));
                town.setSpawn(spawn);
                plugin.getLocales().getLocale("town_spawn_set", member.town().getName())
                    .ifPresent(user::sendMessage);
            }));
            return true;
        }));
    }

    public void setSpawnPrivacy(@NotNull OnlineUser user, boolean isPublic) {
        plugin.getManager().memberEditTown(user, Privilege.SPAWN_PRIVACY, (member -> {
            final Town town = member.town();
            if (town.getSpawn().isEmpty()) {
                plugin.getLocales().getLocale("error_town_spawn_not_set")
                    .ifPresent(user::sendMessage);
                return false;
            }
            final Spawn spawn = town.getSpawn().get();
            if (spawn.isPublic() != isPublic) {
                town.getLog().log(Action.of(user, Action.Type.UPDATE_SPAWN_IS_PUBLIC, spawn.isPublic() + " → " + isPublic));
                spawn.setPublic(isPublic);
                town.setSpawn(spawn);
            }
            plugin.getLocales().getLocale("town_spawn_privacy_set_" + (isPublic ? "public" : "private"), town.getName())
                .ifPresent(user::sendMessage);
            return true;
        }));
    }

    public void clearTownSpawn(@NotNull OnlineUser user) {
        plugin.getManager().memberEditTown(user, Privilege.SET_SPAWN, (member -> {
            final Town town = member.town();
            if (town.getSpawn().isEmpty()) {
                plugin.getLocales().getLocale("error_town_spawn_not_set")
                    .ifPresent(user::sendMessage);
                return false;
            }
            town.getLog().log(Action.of(user, Action.Type.CLEAR_SPAWN));
            town.clearSpawn();
            plugin.getLocales().getLocale("town_spawn_cleared", town.getName())
                .ifPresent(user::sendMessage);
            return true;
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
        final Spawn spawn = town.getSpawn().get();

        if (!user.hasPermission(SPAWN_PRIVACY_BYPASS_PERMISSION) && !spawn.isPublic()) {
            if (member.isEmpty() || !(member.get().town().equals(town))) {
                plugin.getLocales().getLocale("error_town_spawn_not_public")
                    .ifPresent(user::sendMessage);
                return;
            }
            if (!member.get().hasPrivilege(plugin, Privilege.SPAWN)) {
                plugin.getLocales().getLocale("error_insufficient_privileges", town.getName())
                    .ifPresent(user::sendMessage);
                return;
            }
        }
        plugin.getLocales().getLocale("teleporting_town_spawn", town.getName())
            .ifPresent(user::sendMessage);
        plugin.teleportUser(user, spawn.getPosition(), spawn.getServer(), false);
    }

    public void depositMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount) {
        final Optional<EconomyHook> optionalHook = plugin.getEconomyHook();
        if (optionalHook.isEmpty()) {
            plugin.getLocales().getLocale("error_economy_not_in_use")
                .ifPresent(user::sendMessage);
            return;
        }

        plugin.getManager().memberEditTown(user, Privilege.DEPOSIT, (member -> {
            final EconomyHook economy = optionalHook.get();
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                plugin.getLocales().getLocale("error_invalid_money_amount",
                    economy.formatMoney(new BigDecimal(0))).ifPresent(user::sendMessage);
                return false;
            }

            final Town town = member.town();
            if (!economy.takeMoney(user, amount, "Town " + town.getId() + ":" + town.getName() + " deposit")) {
                plugin.getLocales().getLocale("error_economy_insufficient_funds",
                    economy.formatMoney(amount)).ifPresent(user::sendMessage);
                return false;
            }

            town.getLog().log(Action.of(user, Action.Type.DEPOSIT_MONEY, economy.formatMoney(amount)));
            town.setMoney(town.getMoney().add(amount));
            plugin.getLocales().getLocale("town_economy_deposit", economy.formatMoney(amount),
                economy.formatMoney(town.getMoney())).ifPresent(user::sendMessage);
            return true;
        }));
    }

    public void withdrawMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount) {
        final Optional<EconomyHook> optionalHook = plugin.getEconomyHook();
        if (optionalHook.isEmpty()) {
            plugin.getLocales().getLocale("error_economy_not_in_use")
                .ifPresent(user::sendMessage);
            return;
        }

        plugin.getManager().memberEditTown(user, Privilege.WITHDRAW, (member -> {
            final EconomyHook economy = optionalHook.get();
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                plugin.getLocales().getLocale("error_invalid_money_amount",
                    economy.formatMoney(new BigDecimal(0))).ifPresent(user::sendMessage);
                return false;
            }

            final Town town = member.town();
            final BigDecimal withdrawal = amount.min(town.getMoney());
            economy.giveMoney(user, withdrawal, "Town " + town.getId() + ":" + town.getName() + " withdrawal");
            town.getLog().log(Action.of(user, Action.Type.WITHDRAW_MONEY, economy.formatMoney(withdrawal)));
            town.setMoney(town.getMoney().subtract(withdrawal));
            plugin.getLocales().getLocale("town_economy_withdraw", economy.formatMoney(withdrawal),
                economy.formatMoney(town.getMoney())).ifPresent(user::sendMessage);
            return true;
        }));
    }

    public void levelUpTownConfirm(@NotNull OnlineUser user, boolean confirm) {
        plugin.getManager().memberEditTown(user, Privilege.LEVEL_UP, (member -> {
            final Town town = member.town();
            if (town.getLevel() >= plugin.getLevels().getMaxLevel()) {
                plugin.getLocales().getLocale("error_town_max_level")
                    .ifPresent(user::sendMessage);
                return false;
            }
            final BigDecimal price = plugin.getLevels().getLevelUpCost(town.getLevel());

            // Check town balance
            final BigDecimal townBalance = town.getMoney();
            if (townBalance.compareTo(price) < 0) {
                plugin.getLocales().getLocale("error_economy_town_insufficient_funds",
                    plugin.formatMoney(price)).ifPresent(user::sendMessage);
                return false;
            }

            // Require confirmation
            if (!confirm) {
                plugin.getLocales().getLocale("town_level_up_confirm", plugin.formatMoney(price),
                        town.getName(), Integer.toString(town.getLevel() + 1))
                    .ifPresent(user::sendMessage);
                return false;
            }

            town.getLog().log(Action.of(user, Action.Type.LEVEL_UP,
                town.getLevel() + " → " + (town.getLevel() + 1)));
            town.setLevel(town.getLevel() + 1);
            town.setMoney(townBalance.subtract(price));
            return true;
        }), (member -> {
            final Town town = member.town();
            plugin.getLocales().getLocale("town_levelled_up", town.getName(),
                    Integer.toString(town.getLevel()))
                .map(MineDown::toComponent)
                .ifPresent(message -> plugin.getManager().sendTownMessage(town, message));
            plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                .type(Message.Type.TOWN_LEVEL_UP)
                .payload(Payload.integer(town.getId()))
                .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                .build()
                .send(broker, user));
        }));
    }

    public void showTownRelations(@NotNull OnlineUser user, @Nullable String townName) {
        final Optional<Town> optionalTown = townName == null ? plugin.getUserTown(user).map(Member::town) :
            plugin.findTown(townName);
        if (optionalTown.isEmpty()) {
            if (townName == null) {
                plugin.getLocales().getLocale("error_not_in_town")
                    .ifPresent(user::sendMessage);
            } else {
                plugin.getLocales().getLocale("error_town_not_found", townName)
                    .ifPresent(user::sendMessage);
            }
            return;
        }

        // Show relations if there are any
        final Town town = optionalTown.get();
        final Map<Town, Town.Relation> relations = town.getRelations(plugin);
        if (relations.isEmpty()) {
            plugin.getLocales().getLocale("error_no_town_relations", town.getName())
                .ifPresent(user::sendMessage);
            return;
        }
        plugin.getLocales().getLocale("town_relations_title", town.getName())
            .ifPresent(user::sendMessage);

        // Show allies
        final List<Town> allies = relations.entrySet().stream()
            .filter(entry -> entry.getValue().equals(Town.Relation.ALLY))
            .map(Map.Entry::getKey).collect(Collectors.toList());
        this.showRelationList(user, allies, "town_relations_allies");

        // Show enemies
        final List<Town> enemies = relations.entrySet().stream()
            .filter(entry -> entry.getValue().equals(Town.Relation.ENEMY))
            .map(Map.Entry::getKey).collect(Collectors.toList());
        this.showRelationList(user, enemies, "town_relations_enemies");
    }

    private void showRelationList(@NotNull OnlineUser user, @NotNull List<Town> relations, @NotNull String locale) {
        if (!relations.isEmpty()) {
            plugin.getLocales().getRawLocale(locale,
                relations.stream()
                    .map(relation -> plugin.getLocales()
                        .getRawLocale("town_relation_item",
                            Locales.escapeText(relation.getName()),
                            relation.getColorRgb(),
                            Locales.escapeText(relation.getBio().orElse("")))
                        .orElse(relation.getName()))
                    .collect(Collectors.joining(", "))
            ).map(l -> new MineDown(l).toComponent()).ifPresent(user::sendMessage);
        }
    }

    public void setTownRelation(@NotNull OnlineUser user, @NotNull Town.Relation relation, @NotNull String otherTown) {
        plugin.getManager().memberEditTown(user, Privilege.MANAGE_RELATIONS, (member -> {
            final Town town = member.town();
            final Optional<Town> optionalOtherTown = plugin.findTown(otherTown);
            if (optionalOtherTown.isEmpty() || optionalOtherTown.get().equals(town)) {
                plugin.getLocales().getLocale("error_town_not_found", otherTown)
                    .ifPresent(user::sendMessage);
                return false;
            }

            final Town other = optionalOtherTown.get();
            if (town.getRelations(plugin).containsKey(other) && town.getRelations(plugin).get(other).equals(relation)) {
                plugin.getLocales().getLocale("error_town_relation_already_set",
                    town.getName(), other.getName()).ifPresent(user::sendMessage);
                return false;
            }
            town.setRelationWith(other, relation);
            town.getLog().log(Action.of(user, Action.Type.SET_RELATION,
                other.getName() + ": " + relation.name().toLowerCase()));
            plugin.getLocales().getLocale(
                switch (relation) {
                    case ALLY -> "town_relation_set_ally";
                    case ENEMY -> "town_relation_set_enemy";
                    default -> "town_relation_set_neutral";
                },
                town.getName(),
                other.getName()
            ).ifPresent(locale -> plugin.getManager().sendTownMessage(town, locale.toComponent()));
            return true;
        }));
    }

    public void transferOwnership(@NotNull OnlineUser user, @NotNull String memberName) {
        plugin.getManager().mayorEditTown(user, (mayor -> {
            // Validate target is a member of the town
            final Town town = mayor.town();
            final Optional<User> targetUser = plugin.getDatabase().getUser(memberName).map(SavedUser::user);
            if (targetUser.isEmpty()) {
                plugin.getLocales().getLocale("error_user_not_found", memberName)
                    .ifPresent(user::sendMessage);
                return false;
            }

            final Optional<Member> member = plugin.getUserTown(targetUser.get());
            if (member.isEmpty() || !member.get().town().equals(town)) {
                plugin.getLocales().getLocale("error_other_not_in_town",
                    targetUser.get().getUsername()).ifPresent(user::sendMessage);
                return false;
            }
            if (member.get().role().getWeight() >= mayor.role().getWeight()) {
                plugin.getLocales().getLocale("error_user_not_found",
                    targetUser.get().getUsername()).ifPresent(user::sendMessage);
                return false;
            }

            town.getMembers().put(mayor.user().getUuid(), plugin.getRoles().getDefaultRole().getWeight());
            town.getMembers().put(targetUser.get().getUuid(), plugin.getRoles().getMayorRole().getWeight());
            town.getLog().log(Action.of(user, Action.Type.TRANSFER_OWNERSHIP,
                mayor.user().getUsername() + " → " + targetUser.get().getUsername()));
            return true;
        }), (member -> {
            final Town town = member.town();
            plugin.getLocales().getLocale("town_transferred", town.getName(), memberName)
                .map(MineDown::toComponent)
                .ifPresent(message -> plugin.getManager().sendTownMessage(town, message));
            plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                .type(Message.Type.TOWN_TRANSFERRED)
                .payload(Payload.integer(town.getId()))
                .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                .build()
                .send(broker, user));
        }));
    }

    public void showTownLogs(@NotNull OnlineUser user, int page) {
        plugin.getManager().ifMember(user, Privilege.VIEW_LOGS, (member -> {
            final TreeMap<OffsetDateTime, Action> actions = new TreeMap<>(Comparator.reverseOrder());
            actions.putAll(member.town().getLog().getActions());
            final Locales locales = plugin.getLocales();
            final String NOT_APPLICABLE = plugin.getLocales().getNotApplicable();
            user.sendMessage(PaginatedList.of(actions.entrySet().stream()
                        .map(entry -> locales.getRawLocale("town_audit_log_list_item",
                                entry.getKey().format(DateTimeFormatter.ofPattern("dd MMM")),
                                entry.getKey().format(DateTimeFormatter.ofPattern("dd MMM, yyyy, HH:mm:ss")),
                                Locales.escapeText(entry.getValue().getUser().map(User::getUsername)
                                    .orElse(NOT_APPLICABLE)),
                                Locales.escapeText(entry.getValue().getType().name().toLowerCase()),
                                Locales.escapeText(locales.truncateText(entry.getValue().getDetails()
                                    .orElse(NOT_APPLICABLE), 10)),
                                Locales.escapeText(entry.getValue().getDetails()
                                    .orElse(NOT_APPLICABLE)))
                            .orElse(entry.getValue().toString()))
                        .toList(),
                    locales.getBaseList(plugin.getSettings().getGeneral().getListItemsPerPage())
                        .setHeaderFormat(locales.getRawLocale("town_audit_log_list_title",
                            Locales.escapeText(member.town().getName())).orElse(""))
                        .setItemSeparator("\n").setCommand("/husktowns:town log")
                        .build())
                .getNearestValidPage(page));
        }));
    }

    public void setFlagRule(@NotNull OnlineUser user, @NotNull Flag flag, @NotNull Claim.Type type, boolean value, boolean showMenu) {
        plugin.getManager().memberEditTown(user, Privilege.SET_RULES, (member -> {
            final Town town = member.town();
            town.getRules().get(type).setFlag(flag, value);
            town.getLog().log(Action.of(user, Action.Type.SET_FLAG_RULE, flag.getName().toLowerCase() + ": " + value));

            plugin.getLocales().getLocale("town_flag_set", flag.getName().toLowerCase(), Boolean.toString(value),
                    type.name().toLowerCase()).ifPresent(user::sendMessage);
            if (showMenu) {
                RulesConfig.of(plugin, town, user).show();
            }
            return true;
        }));
    }

    public void showRulesConfig(@NotNull OnlineUser user) {
        plugin.getManager().ifMember(user, Privilege.SET_RULES,
            (member -> RulesConfig.of(plugin, member.town(), user).show()));
    }

    public void showPlayerInfo(@NotNull CommandUser executor, @NotNull String username) {
        final Optional<SavedUser> user = plugin.getDatabase().getUser(username);
        if (user.isEmpty()) {
            plugin.getLocales().getLocale("error_user_not_found", username)
                .ifPresent(executor::sendMessage);
            return;
        }

        final Optional<Member> optionalMember = plugin.getUserTown(user.get().user());
        username = user.get().user().getUsername();
        if (optionalMember.isEmpty()) {
            plugin.getLocales().getLocale("town_player_info_not_in_town", username)
                .ifPresent(executor::sendMessage);
            return;
        }

        final Member member = optionalMember.get();
        plugin.getLocales().getLocale(
                "town_player_info",
                username, member.town().getName(), member.role().getName(), member.town().getColorRgb(),
                Integer.toString(member.town().getLevel()),
                plugin.formatMoney(member.town().getMoney()),
                Integer.toString(member.town().getClaimCount()),
                Integer.toString(member.town().getMaxClaims(plugin)),
                Integer.toString(member.town().getMembers().size()),
                Integer.toString(member.town().getMaxMembers(plugin)),
                member.town().getBio()
                    .map(bio -> plugin.getLocales().truncateText(bio, 120))
                    .orElse(plugin.getLocales().getNotApplicable())
            )
            .ifPresent(executor::sendMessage);
    }

    public void sendChatMessage(@NotNull OnlineUser user, @Nullable String message) {
        plugin.getManager().ifMember(user, Privilege.CHAT, (member -> {
            if (message == null) {
                plugin.editUserPreferences(user, (preferences) -> {
                    preferences.setTownChatTalking(!preferences.isTownChatTalking());
                    plugin.getLocales().getLocale(preferences.isTownChatTalking()
                            ? "town_chat_talking" : "town_chat_not_talking")
                        .ifPresent(user::sendMessage);
                });
                return;
            }

            // Send locally
            sendLocalChatMessage(message, member, plugin);

            // Send globally via a message
            plugin.getMessageBroker().ifPresent(broker -> Message.builder()
                .type(Message.Type.TOWN_CHAT_MESSAGE)
                .payload(Payload.string(message))
                .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                .build()
                .send(broker, user));
        }));
    }

    public void sendLocalChatMessage(@NotNull String text, @NotNull Member member, @NotNull HuskTowns plugin) {
        final Town town = member.town();
        plugin.getLocales().getLocale("town_chat_message_format",
                town.getName(), town.getColorRgb(), member.user().getUsername(),
                member.role().getName(), text)
            .map(MineDown::toComponent)
            .ifPresent(locale -> plugin.getManager().sendTownMessage(town, locale));
        plugin.getManager().admin().sendLocalSpyMessage(town, member, text);
    }

}
