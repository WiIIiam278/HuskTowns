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

package net.william278.husktowns.town;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an invitation sent to a player asking them to join a town
 */
public class Invite {

    @Expose
    @SerializedName("town_id")
    private int townId;

    @Expose
    private User sender;

    private Invite(int townId, @NotNull User sender) {
        this.townId = townId;
        this.sender = sender;
    }

    @SuppressWarnings("unused")
    private Invite() {
    }

    @NotNull
    public static Invite create(int townId, @NotNull User sender) {
        return new Invite(townId, sender);
    }

    public int getTownId() {
        return townId;
    }

    @NotNull
    public User getSender() {
        return sender;
    }

}
