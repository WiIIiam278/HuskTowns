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

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class User implements Comparable<User> {

    @Expose
    private UUID uuid;
    @Expose
    private String username;

    protected User(@NotNull UUID uuid, @NotNull String username) {
        this.uuid = uuid;
        this.username = username;
    }

    @NotNull
    public static User of(@NotNull UUID uuid, @NotNull String username) {
        return new User(uuid, username);
    }

    @SuppressWarnings("unused")
    private User() {
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @NotNull
    public String getName() {
        return username;
    }

    @NotNull
    public String getUsername() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User user)) {
            return false;
        }
        return user.getUuid().equals(uuid);
    }

    @Override
    public int compareTo(@NotNull User o) {
        return username.compareTo(o.getUsername());
    }

}
