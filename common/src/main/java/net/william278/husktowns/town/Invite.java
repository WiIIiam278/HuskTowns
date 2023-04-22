/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
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
