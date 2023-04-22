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

package net.william278.husktowns.hook;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public abstract class EconomyHook extends Hook {

    protected EconomyHook(@NotNull HuskTowns plugin, @NotNull String name) {
        super(plugin, name);
    }

    /**
     * Take money from a player
     * @param user The player to take money from
     * @param amount The amount of money to take
     * @param reason The reason for taking money
     * @return Whether the transaction was successful, false if the player does not have enough money
     */
    public abstract boolean takeMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason);

    public abstract void giveMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason);

    public abstract String formatMoney(@NotNull BigDecimal amount);

}
