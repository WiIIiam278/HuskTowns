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

package net.william278.husktowns.user;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Stream;

/**
 * A provider for the plugin user list, tracking online users across the network
 *
 * @since 3.1
 */
public interface UserProvider {

    @NotNull
    Map<UUID, OnlineUser> getOnlineUserMap();

    @NotNull
    OnlineUser getOnlineUser(@NotNull UUID uuid);

    @NotNull
    Map<String, List<User>> getGlobalUserList();

    @NotNull
    @Unmodifiable
    default Collection<OnlineUser> getOnlineUsers() {
        return getOnlineUserMap().values();
    }

    default Optional<OnlineUser> findOnlineUser(@NotNull String username) {
        return getOnlineUsers().stream()
                .filter(online -> online.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @NotNull
    default List<User> getUserList() {
        return Stream.concat(
                getGlobalUserList().values().stream().flatMap(Collection::stream),
                getPlugin().getOnlineUsers().stream()
        ).distinct().sorted().toList();
    }

    default void setUserList(@NotNull String server, @NotNull List<User> players) {
        getGlobalUserList().values().forEach(list -> {
            list.removeAll(players);
            list.removeAll(getPlugin().getOnlineUsers());
        });
        getGlobalUserList().put(server, players);
    }

    default boolean isUserOnline(@NotNull User user) {
        return getUserList().contains(user);
    }

    @NotNull
    HuskTowns getPlugin();

}