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

package net.william278.husktowns.listener;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.advancement.Advancement;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.network.Broker;
import net.william278.husktowns.network.Message;
import net.william278.husktowns.network.Payload;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import net.william278.husktowns.user.SavedUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface UserListener {

    default void handlePlayerJoin(@NotNull OnlineUser user) {
        getPlugin().runAsync(() -> {
            final Optional<SavedUser> userData = getPlugin().getDatabase().getUser(user.getUuid());
            if (userData.isEmpty()) {
                getPlugin().getDatabase().createUser(user, Preferences.getDefaults());
                getPlugin().setUserPreferences(user.getUuid(), Preferences.getDefaults());
                return;
            }

            // Update the user preferences if necessary
            final SavedUser savedUser = userData.get();
            final Preferences preferences = savedUser.preferences();
            boolean updateNeeded = !savedUser.user().getUsername().equals(user.getUsername());
            final Optional<Town> userTown = getPlugin().getUserTown(user).map(Member::town);

            // Notify if the user is in town chat, remove them if they are not in a town
            if (preferences.isTownChatTalking()) {
                if (userTown.isEmpty()) {
                    preferences.setTownChatTalking(false);
                    updateNeeded = true;
                } else {
                    getPlugin().getLocales().getLocale("town_chat_reminder")
                        .ifPresent(user::sendMessage);
                }
            }

            // Handle cross-server teleports
            if (getPlugin().getSettings().getCrossServer().isEnabled()) {
                if (getPlugin().getSettings().getCrossServer().getBrokerType() == Broker.Type.PLUGIN_MESSAGE
                    && getPlugin().getOnlineUsers().size() == 1) {
                    getPlugin().setLoaded(false);
                    getPlugin().loadData();
                }

                // Synchronize the global player list
                getPlugin().runSyncDelayed(() -> this.syncGlobalUserList(
                    user, getPlugin().getOnlineUsers().stream().map(online -> (User) online).toList()), user, 40L
                );

                // Handle teleportation completion
                if (preferences.getTeleportTarget().isPresent()) {
                    final Position position = preferences.getTeleportTarget().get();
                    getPlugin().runSync(() -> {
                        user.teleportTo(position);
                        getPlugin().getLocales().getLocale("teleportation_complete").ifPresent(locale -> user
                            .sendMessage(getPlugin().getSettings().getGeneral().getNotificationSlot(), locale));
                    }, user);

                    preferences.clearTeleportTarget();
                    updateNeeded = true;
                }
            }

            // Setup advancements
            final Optional<Advancement> advancements = getPlugin().getAdvancements();
            if (advancements.isPresent()) {
                final Advancement rootAdvancement = advancements.get();
                if (preferences.isCompletedAdvancement(rootAdvancement.getKey())) {
                    preferences.addCompletedAdvancement(rootAdvancement.getKey());
                    updateNeeded = true;
                }
                getPlugin().awardAdvancement(rootAdvancement, user);
            }

            // Save the user preferences
            getPlugin().setUserPreferences(user.getUuid(), preferences);
            if (updateNeeded) {
                getPlugin().getDatabase().updateUser(user, preferences);
            }

            // Check advancements
            userTown.ifPresent(town -> getPlugin().checkAdvancements(town, user));
        });
    }

    // When a player quits
    default void handlePlayerQuit(@NotNull OnlineUser user) {
        // Update global user list if needed
        if (getPlugin().getSettings().getCrossServer().isEnabled()) {
            final List<User> localPlayerList = getPlugin().getOnlineUsers().stream()
                .filter(u -> !u.equals(user)).map(u -> (User) u).toList();

            if (getPlugin().getSettings().getCrossServer().getBrokerType() == Broker.Type.REDIS) {
                this.syncGlobalUserList(user, localPlayerList);
                return;
            }
            getPlugin().getOnlineUsers().stream()
                .filter(u -> !u.equals(user))
                .findAny()
                .ifPresent(player -> this.syncGlobalUserList(player, localPlayerList));
        }

        // Handle war victory checks
        getPlugin().getManager().wars().ifPresent(wars -> wars.handlePlayerQuit(user));
    }

    default boolean handlePlayerChat(@NotNull OnlineUser user, @NotNull String message) {
        final Optional<Preferences> preferences = getPlugin().getUserPreferences(user.getUuid());
        if (preferences.isPresent() && preferences.get().isTownChatTalking()) {
            getPlugin().getManager().towns().sendChatMessage(user, message);
            return true;
        }
        return false;
    }

    // Synchronize the global player list
    default void syncGlobalUserList(@NotNull OnlineUser user, @NotNull List<User> onlineUsers) {
        final Optional<Broker> optionalBroker = getPlugin().getMessageBroker();
        if (optionalBroker.isEmpty()) {
            return;
        }
        final Broker broker = optionalBroker.get();

        // Send this server's player list to all servers
        Message.builder()
            .type(Message.Type.USER_LIST)
            .target(Message.TARGET_ALL, Message.TargetType.SERVER)
            .payload(Payload.userList(onlineUsers))
            .build().send(broker, user);

        // Clear cached global player lists and request updated lists from all servers
        if (getPlugin().getOnlineUsers().size() == 1) {
            getPlugin().getGlobalUserList().clear();
            Message.builder()
                .type(Message.Type.REQUEST_USER_LIST)
                .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                .build().send(broker, user);
        }
    }

    @NotNull
    HuskTowns getPlugin();

}
