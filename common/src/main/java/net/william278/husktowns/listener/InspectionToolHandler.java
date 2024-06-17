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

package net.william278.husktowns.listener;

import net.william278.cloplib.operation.OperationPosition;
import net.william278.cloplib.operation.OperationUser;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface InspectionToolHandler {


    // When a player right-clicks with the inspection tool
    default void onPlayerInspect(@NotNull OperationUser operationUser, @NotNull OperationPosition clicked) {
        final OnlineUser user = (OnlineUser) operationUser;
        final Position position = (Position) clicked;

        // If sneaking, inspect all nearby claims
        if (user.isSneaking()) {
            handlePlayerInspectNearby(user, position.getChunk(), position.getWorld());
            return;
        }
        handlePlayerInspectChunk(user, position);
    }


    // When a player right-clicks to inspect a claim
    private void handlePlayerInspectChunk(@NotNull OnlineUser user, @NotNull Position position) {
        final Optional<TownClaim> claim = getPlugin().getClaimAt(position);
        if (claim.isPresent()) {
            final TownClaim townClaim = claim.get();
            final Claim claimData = townClaim.claim();
            getPlugin().highlightClaim(user, townClaim);
            if (townClaim.isAdminClaim(getPlugin())) {
                getPlugin().getLocales().getLocale("inspect_chunk_admin_claim",
                        Integer.toString(claimData.getChunk().getX()), Integer.toString(claimData.getChunk().getZ()))
                    .ifPresent(user::sendMessage);
                return;
            }
            getPlugin().getLocales().getLocale("inspect_chunk_claimed_" + claimData.getType().name().toLowerCase(),
                    Integer.toString(claimData.getChunk().getX()), Integer.toString(claimData.getChunk().getZ()),
                    townClaim.town().getName())
                .ifPresent(user::sendMessage);
            return;
        }
        if (getPlugin().getClaimWorld(user.getWorld()).isEmpty()) {
            getPlugin().getLocales().getLocale("inspect_chunk_not_claimable")
                .ifPresent(user::sendMessage);
            return;
        }
        getPlugin().getLocales().getLocale("inspect_chunk_not_claimed")
            .ifPresent(user::sendMessage);
    }

    // When a player uses shift+right-click to inspect nearby claims
    private void handlePlayerInspectNearby(@NotNull OnlineUser user, @NotNull Chunk center, @NotNull World world) {
        final Optional<ClaimWorld> optionalClaimWorld = getPlugin().getClaimWorld(world);
        if (optionalClaimWorld.isEmpty()) {
            getPlugin().getLocales().getLocale("inspect_chunk_not_claimable")
                .ifPresent(user::sendMessage);
            return;
        }
        final ClaimWorld claimWorld = optionalClaimWorld.get();

        final Settings.GeneralSettings settings = getPlugin().getSettings().getGeneral();
        final int radius = 1 + ((settings.getClaimMapWidth() + settings.getClaimMapHeight()) / 4);
        final List<TownClaim> nearbyClaims = claimWorld.getClaimsNear(center, radius, getPlugin().getPlugin());
        if (nearbyClaims.isEmpty()) {
            getPlugin().getLocales().getLocale("inspect_nearby_no_claims", Integer.toString(radius),
                    Integer.toString(center.getX()), Integer.toString(center.getZ()))
                .ifPresent(user::sendMessage);
            return;
        }
        getPlugin().highlightClaims(user, nearbyClaims);
        getPlugin().getLocales().getLocale("inspect_nearby_claims", Integer.toString(nearbyClaims.size()),
                Long.toString(nearbyClaims.stream().map(TownClaim::town).distinct().count()),
                Integer.toString(radius), Integer.toString(center.getX()), Integer.toString(center.getZ()))
            .ifPresent(user::sendMessage);
    }

    @NotNull
    HuskTowns getPlugin();

}
