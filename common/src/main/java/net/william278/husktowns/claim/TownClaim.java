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

package net.william278.husktowns.claim;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public record TownClaim(@NotNull Town town, @NotNull Claim claim) {
    public static Optional<TownClaim> from(@NotNull Map.Entry<Integer, Claim> entry, @NotNull HuskTowns plugin) {
        return plugin.findTown(entry.getKey()).map(town -> new TownClaim(town, entry.getValue()));
    }

    public static TownClaim admin(@NotNull Chunk chunk, @NotNull HuskTowns plugin) {
        return new TownClaim(plugin.getAdminTown(), Claim.at(chunk));
    }

    public boolean isAdminClaim(@NotNull HuskTowns plugin) {
        return town.getName().equalsIgnoreCase(plugin.getSettings().getAdminTownName());
    }

    public boolean contains(@NotNull Position position) {
        return claim.contains(position);
    }

    @Override
    public String toString() {
        return "Town: " + town.getName() + ", Claim: " + claim;
    }
}
