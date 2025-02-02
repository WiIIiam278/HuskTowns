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

package net.william278.husktowns.network;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.logging.Level;

public interface MessageHandler {

    // Handle inbound user list requests
    default void handleRequestUserList(@NotNull Message message, @Nullable OnlineUser receiver) {
        if (receiver == null) {
            return;
        }

        Message.builder()
                .type(Message.Type.UPDATE_USER_LIST)
                .payload(Payload.userList(getPlugin().getOnlineUsers().stream().map(online -> (User) online).toList()))
                .target(message.getSourceServer(), Message.TargetType.SERVER).build()
                .send(getBroker(), receiver);
    }

    // Handle inbound user list updates (returned from requests)
    default void handleUpdateUserList(@NotNull Message message) {
        message.getPayload().getUserList().ifPresent(
                (players) -> getPlugin().setUserList(message.getSourceServer(), players)
        );
    }

    default void handleTownDelete(@NotNull Message message) {
        message.getPayload().getInteger()
                .flatMap(townId -> getPlugin().getTowns().stream().filter(town -> town.getId() == townId).findFirst())
                .ifPresent(town -> getPlugin().runAsync(() -> {
                    getPlugin().getManager().sendTownMessage(town, getPlugin().getLocales()
                            .getLocale("town_deleted_notification", town.getName())
                            .map(MineDown::toComponent).orElse(Component.empty()));
                    getPlugin().getMapHook().ifPresent(mapHook -> mapHook.removeClaimMarkers(town));
                    getPlugin().removeTown(town);
                    getPlugin().getClaimWorlds().values().forEach(world -> {
                        if (world.removeTownClaims(town.getId()) > 0) {
                            getPlugin().getDatabase().updateClaimWorld(world);
                        }
                    });
                }));
    }

    default void handleTownDeleteAllClaims(@NotNull Message message) {
        message.getPayload().getInteger()
                .flatMap(townId -> getPlugin().getTowns().stream().filter(town -> town.getId() == townId).findFirst())
                .ifPresent(town -> getPlugin().runAsync(() -> {
                    getPlugin().getManager().sendTownMessage(town, getPlugin().getLocales()
                            .getLocale("deleted_all_claims_notification", town.getName())
                            .map(MineDown::toComponent).orElse(Component.empty()));
                    getPlugin().getMapHook().ifPresent(mapHook -> mapHook.removeClaimMarkers(town));
                    getPlugin().getClaimWorlds().values().forEach(world -> world.removeTownClaims(town.getId()));
                }));
    }

    default void handleTownUpdate(@NotNull Message message) {
        message.getPayload().getInteger()
                .flatMap(id -> getPlugin().getDatabase().getTown(id))
                .ifPresentOrElse(
                        getPlugin()::updateTown,
                        () -> getPlugin().log(Level.WARNING, "Failed to update town: Town not found")
                );
    }

    default void handleTownInviteRequest(@NotNull Message message, @Nullable OnlineUser receiver) {
        if (receiver == null) {
            return;
        }
        message.getPayload().getInvite().ifPresentOrElse(
                invite -> getPlugin().getManager().towns().handleInboundInvite(receiver, invite),
                () -> getPlugin().log(Level.WARNING, "Invalid town invite request payload!")
        );
    }

    default void handleTownInviteReply(@NotNull Message message, @Nullable OnlineUser receiver) {
        final Optional<Boolean> reply = message.getPayload().getBool();
        if (receiver == null || reply.isEmpty()) {
            return;
        }

        final boolean accepted = reply.get();
        final Optional<Member> member = getPlugin().getUserTown(receiver);
        if (member.isEmpty()) {
            return;
        }

        final Member townMember = member.get();
        if (!accepted) {
            getPlugin().getLocales().getLocale("invite_declined_by", message.getSender())
                    .ifPresent(receiver::sendMessage);
            return;
        }
        getPlugin().getLocales().getLocale("user_joined_town", message.getSender(),
                        townMember.town().getName()).map(MineDown::toComponent)
                .ifPresent(locale -> getPlugin().getManager().sendTownMessage(townMember.town(), locale));
    }

    default void handleTownChatMessage(@NotNull Message message) {
        message.getPayload().getString().ifPresent(text -> getPlugin().getDatabase().getUser(message.getSender())
                .flatMap(sender -> getPlugin().getUserTown(sender.user()))
                .ifPresent(member -> getPlugin().getManager().towns().sendLocalChatMessage(text, member, getPlugin())));
    }

    default void handleTownAction(@NotNull Message message) {
        message.getPayload().getInteger().flatMap(id -> getPlugin().getTowns().stream()
                .filter(town -> town.getId() == id).findFirst()).ifPresent(town -> {
            final Component locale = switch (message.getType()) {
                case TOWN_LEVEL_UP -> getPlugin().getLocales().getLocale("town_levelled_up",
                        Integer.toString(town.getLevel())).map(MineDown::toComponent).orElse(Component.empty());
                case TOWN_RENAMED -> getPlugin().getLocales().getLocale("town_renamed",
                        town.getName()).map(MineDown::toComponent).orElse(Component.empty());
                case TOWN_TRANSFERRED -> getPlugin().getLocales().getLocale("town_transferred",
                                town.getName(), getPlugin().getDatabase().getUser(town.getMayor())
                                        .map(SavedUser::user).map(User::getUsername).orElse("?"))
                        .map(MineDown::toComponent).orElse(Component.empty());
                default -> Component.empty();
            };
            getPlugin().getManager().sendTownMessage(town, locale);
        });
    }

    default void handleTownUserAction(@NotNull Message message, @Nullable OnlineUser receiver) {
        if (receiver == null) {
            return;
        }
        switch (message.getType()) {
            case TOWN_DEMOTED -> getPlugin().getLocales().getLocale("demoted_you",
                    getPlugin().getRoles().fromWeight(message.getPayload().getInteger().orElse(-1))
                            .map(Role::getName).orElse("?"),
                    message.getSender()).ifPresent(receiver::sendMessage);
            case TOWN_PROMOTED -> getPlugin().getLocales().getLocale("promoted_you",
                    getPlugin().getRoles().fromWeight(message.getPayload().getInteger().orElse(-1))
                            .map(Role::getName).orElse("?"),
                    message.getSender()).ifPresent(receiver::sendMessage);
            case TOWN_EVICTED -> {
                getPlugin().getLocales().getLocale("evicted_you",
                        getPlugin().getUserTown(receiver).map(Member::town).map(Town::getName).orElse("?"),
                        message.getSender()).ifPresent(receiver::sendMessage);
                getPlugin().editUserPreferences(receiver, preferences -> preferences.setTownChatTalking(false));
            }
        }
    }

    default void handleTownWarDeclarationSent(@NotNull Message message) {
        getPlugin().getManager().wars().ifPresent(manager -> message.getPayload()
                .getDeclaration().ifPresent(declaration -> {
                    final Optional<Town> town = declaration.getAttackingTown(getPlugin());
                    final Optional<Town> defending = declaration.getDefendingTown(getPlugin());
                    if (town.isEmpty() || defending.isEmpty()) {
                        return;
                    }

                    // Add pending declaration
                    manager.getPendingDeclarations().add(declaration);

                    // Send notification
                    final Settings.TownSettings.RelationsSettings.WarSettings settings = getPlugin().getSettings().getTowns().getRelations().getWars();
                    getPlugin().getLocales().getLocale("war_declaration_notification",
                                    town.get().getName(), defending.get().getName(),
                                    getPlugin().formatMoney(declaration.wager()),
                                    Long.toString(settings.getDeclarationExpiry()))
                            .ifPresent(t -> {
                                getPlugin().getManager().sendTownMessage(town.get(), t.toComponent());
                                getPlugin().getManager().sendTownMessage(defending.get(), t.toComponent());
                            });

                    // Send options to the defending town.
                    getPlugin().getLocales().getLocale("war_declaration_options", town.get().getName())
                            .ifPresent(l -> getPlugin().getManager().sendTownMessage(defending.get(), l.toComponent()));
                }));
    }

    default void handleTownWarDeclarationAccept(@NotNull Message message, @Nullable OnlineUser receiver) {
        getPlugin().getManager().wars().ifPresent(manager -> message.getPayload()
                .getDeclaration().ifPresent(declaration -> {
                    manager.getPendingDeclarations().remove(declaration);
                    if (receiver == null) {
                        return;
                    }

                    final Optional<String> optionalServer = declaration.getWarServerName(getPlugin());
                    final Optional<Town> attacking = declaration.getAttackingTown(getPlugin());
                    final Optional<Town> defending = declaration.getDefendingTown(getPlugin());
                    if (optionalServer.isEmpty() || attacking.isEmpty() || defending.isEmpty()) {
                        return;
                    }

                    final String server = optionalServer.get();
                    if (getPlugin().getServerName().equalsIgnoreCase(server)) {
                        manager.startWar(
                                receiver, attacking.get(), defending.get(), declaration.wager(),
                                (startedWar) -> startedWar.teleportUsers(getPlugin())
                        );
                    }

                    final Settings.TownSettings.RelationsSettings.WarSettings settings = getPlugin().getSettings().getTowns().getRelations().getWars();
                    getPlugin().getLocales().getLocale("war_declaration_accepted",
                                    attacking.get().getName(), defending.get().getName(),
                                    Long.toString(settings.getWarZoneRadius()))
                            .ifPresent(l -> {
                                getPlugin().getManager().sendTownMessage(attacking.get(), l.toComponent());
                                getPlugin().getManager().sendTownMessage(defending.get(), l.toComponent());
                            });
                }));
    }

    default void handleTownWarEnd(@NotNull Message message) {
        message.getPayload().getInteger().ifPresent(loserId -> getPlugin().getManager().wars()
                .ifPresent(wars -> wars.getActiveWars().stream()
                        .filter(war -> war.getAttacking() == loserId || war.getDefending() == loserId)
                        .findFirst().ifPresent(wars::removeActiveWar)));
    }

    @NotNull
    Broker getBroker();

    @NotNull
    HuskTowns getPlugin();

}
