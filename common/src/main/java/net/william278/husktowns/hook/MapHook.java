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
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class MapHook extends Hook {

    protected MapHook(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    public abstract void setClaimMarker(@NotNull TownClaim claim, @NotNull World world);

    public abstract void removeClaimMarker(@NotNull TownClaim claim, @NotNull World world);

    public final void removeClaimMarkers(@NotNull Town town) {
        plugin.getWorlds().forEach(world -> plugin.getClaimWorld(world).ifPresent(
            claimWorld -> removeClaimMarkers(claimWorld.getTownClaims(town.getId(), plugin), world)
        ));
    }

    public abstract void setClaimMarkers(@NotNull List<TownClaim> claims, @NotNull World world);

    public final void setClaimMarkers(@NotNull Town town) {
        plugin.getWorlds().forEach(world -> plugin.getClaimWorld(world).ifPresent(
            claimWorld -> setClaimMarkers(claimWorld.getTownClaims(town.getId(), plugin), world)
        ));
    }

    public abstract void removeClaimMarkers(@NotNull List<TownClaim> claims, @NotNull World world);

    public final void reloadClaimMarkers(@NotNull Town town) {
        this.removeClaimMarkers(town);
        this.setClaimMarkers(town);
    }

    public abstract void clearAllMarkers();

    @NotNull
    protected final String getMarkerSetKey() {
        return plugin.getKey(getHookInfo().id().toLowerCase(), "markers").toString();
    }

}
