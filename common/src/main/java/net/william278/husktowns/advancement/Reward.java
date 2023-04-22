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

package net.william278.husktowns.advancement;

import com.google.gson.annotations.Expose;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class Reward {

    @Expose
    private Type type;
    @Expose
    private int quantity;
    @Expose
    @Nullable
    private String value;

    private Reward(@NotNull Type type, int quantity, @Nullable String value) {
        this.type = type;
        this.quantity = quantity;
        this.value = value;
    }

    public void give(@NotNull OnlineUser user) {
        // todo
    }

    @SuppressWarnings("unused")
    private Reward() {
    }

    @NotNull
    public static Builder of(@NotNull Type type) {
        return new Builder(type);
    }

    public enum Type {
        TOWN_LEVELS,
        TOWN_MONEY,
        PLAYER_MONEY,
        PLAYER_EXPERIENCE,
        PLAYER_LEVELS,
        PLAYER_ITEMS
    }

    private static class Builder {
        private final Type type;
        private int quantity;
        private String value;

        private Builder(@NotNull Type type) {
            this.type = type;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder value(@NotNull String value) {
            this.value = value;
            return this;
        }

        public Reward build() {
            return new Reward(type, quantity, value);
        }
    }

}
