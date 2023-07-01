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
