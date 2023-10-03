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

package net.william278.husktowns.events;

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

/**
 * An event fired when a player removes a claimed chunk from their town
 */
public interface IUnClaimEvent extends OnlineUserEvent, TownEvent, Cancellable {

    /**
     * Get the town-claim mapping that was removed
     *
     * @return the {@link TownClaim} that was removed
     */
    @NotNull
    TownClaim getTownClaim();

    /**
     * Get the {@link Town} who unclaimed the chunk
     *
     * @return the {@link Town} who unclaimed the chunk
     */
    @NotNull
    default Town getTown() {
        return getTownClaim().town();
    }

    /**
     * Get the {@link Claim} that was removed
     *
     * @return the {@link Claim} that was removed
     */
    @NotNull
    default Claim getClaim() {
        return getTownClaim().claim();
    }

}
