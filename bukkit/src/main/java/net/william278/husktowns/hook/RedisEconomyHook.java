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

import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.logging.Level;

public class RedisEconomyHook extends EconomyHook {

    private RedisEconomyAPI redisEconomy;

    public RedisEconomyHook(@NotNull HuskTowns plugin) {
        super(plugin, "RedisEconomy");
    }

    @Override
    public void onEnable() {
        this.redisEconomy = RedisEconomyAPI.getAPI();
        plugin.log(Level.INFO, "Enabled RedisEconomy hook");
    }

    @Override
    public boolean hasMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount) {
        return redisEconomy.getDefaultCurrency().has(user.getUuid(), amount.doubleValue());
    }

    @Override
    public boolean takeMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason) {
        return redisEconomy.getDefaultCurrency().withdrawPlayer(user.getUuid(), user.getUsername(), amount.doubleValue(), reason).transactionSuccess();
    }

    @Override
    public void giveMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason) {
        redisEconomy.getDefaultCurrency().depositPlayer(user.getUuid(), user.getUsername(), amount.doubleValue(), reason);
    }

    @Override
    @NotNull
    public String formatMoney(@NotNull BigDecimal amount) {
        return redisEconomy.getDefaultCurrency().format(amount.doubleValue());
    }

}
