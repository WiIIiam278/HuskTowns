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

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public abstract class EconomyHook extends Hook {

    protected EconomyHook(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    /**
     * Check if a player has enough money
     *
     * @param user   The player to check
     * @param amount The amount of money to check
     * @return Whether the player has enough money
     */
    public abstract boolean hasMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount);

    /**
     * Take money from a player
     *
     * @param user   The player to take money from
     * @param amount The amount of money to take
     * @param reason The reason for taking money
     * @return Whether the transaction was successful, false if the player does not have enough money
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public abstract boolean takeMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason);

    /**
     * Give money to a player
     *
     * @param user   The player to give money to
     * @param amount The amount of money to give
     * @param reason The reason for giving money
     */
    public abstract void giveMoney(@NotNull OnlineUser user, @NotNull BigDecimal amount, @Nullable String reason);

    /**
     * Format a money amount
     *
     * @param amount The amount to format
     * @return The formatted amount
     */
    @NotNull
    public abstract String formatMoney(@NotNull BigDecimal amount);

}
