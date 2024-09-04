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

package net.william278.husktowns.war;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Spawn;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * A declaration of war between two towns
 *
 * @param attackingTown the attacking town (sending the declaration)
 * @param defendingTown the defending town
 * @param sender        the sender of the declaration
 * @param expiryTime    the time the declaration expires
 * @since 2.6
 */
public record Declaration(
    @Expose @SerializedName("attacking_town") int attackingTown,
    @Expose @SerializedName("defending_town") int defendingTown,
    @Expose BigDecimal wager,
    @Expose User sender,
    @Expose @SerializedName("expiry_time") OffsetDateTime expiryTime
) {
    @NotNull
    public static Declaration create(@NotNull Member sender, @NotNull Town defendingTown, @NotNull BigDecimal wager,
                                     @NotNull HuskTowns plugin) {
        return new Declaration(
            sender.town().getId(),
            defendingTown.getId(),
            wager,
            sender.user(),
            OffsetDateTime.now().plusMinutes(Math.max(
                0, plugin.getSettings().getTowns().getRelations().getWars().getDeclarationExpiry()
            ))
        );
    }

    public boolean hasExpired() {
        return OffsetDateTime.now().isAfter(expiryTime);
    }

    @NotNull
    public Optional<String> getWarServerName(@NotNull HuskTowns plugin) {
        return getDefendingTown(plugin).flatMap(Town::getSpawn).map(Spawn::getServer);
    }

    public Optional<Town> getAttackingTown(@NotNull HuskTowns plugin) {
        return plugin.findTown(attackingTown);
    }

    public Optional<Town> getDefendingTown(@NotNull HuskTowns plugin) {
        return plugin.findTown(defendingTown);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Declaration declaration) {
            return declaration.attackingTown == attackingTown && declaration.defendingTown == defendingTown;
        }
        return false;
    }

}
