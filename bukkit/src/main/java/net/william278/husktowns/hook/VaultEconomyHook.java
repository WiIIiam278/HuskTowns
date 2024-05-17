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

import net.milkbowl.vault.economy.Economy;
import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.logging.Level;

public class VaultEconomyHook extends EconomyHook {
    protected Economy economy;

    @PluginHook(id = "Vault", register = PluginHook.Register.ON_ENABLE, platform = "bukkit")
    public VaultEconomyHook(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() throws IllegalStateException {
        final RegisteredServiceProvider<Economy> economyProvider = ((BukkitHuskTowns) plugin).getServer()
                .getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) {
            throw new IllegalStateException("Could not resolve Vault economy provider");
        }
        this.economy = economyProvider.getProvider();
        plugin.log(Level.INFO, "Enabled Vault economy hook");
    }

    @Override
    public boolean hasMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount) {
        return economy.has(((BukkitUser) user).getPlayer(), amount.doubleValue());
    }

    @Override
    public boolean takeMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason) {
        return economy.withdrawPlayer(((BukkitUser) user).getPlayer(), amount.doubleValue()).transactionSuccess();
    }

    @Override
    public void giveMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason) {
        economy.depositPlayer(((BukkitUser) user).getPlayer(), amount.doubleValue());
    }

    @Override
    @NotNull
    public String formatMoney(@NotNull BigDecimal amount) {
        return economy.format(amount.doubleValue());
    }

}
