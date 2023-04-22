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

package net.william278.husktowns.events;

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

/**
 * An event fired when a player claims a chunk for their town
 */
public interface IClaimEvent extends OnlineUserEvent, TownEvent {

    /**
     * Get the town-claim mapping that was made
     *
     * @return the {@link TownClaim} that was made
     */
    @NotNull
    TownClaim getTownClaim();

    /**
     * Get the {@link Town} who claimed the chunk
     *
     * @return the {@link Town} who claimed the chunk
     */
    @NotNull
    default Town getTown() {
        return getTownClaim().town();
    }

    /**
     * Get the {@link Claim} that was made
     *
     * @return the {@link Claim} that was made
     */
    @NotNull
    default Claim getClaim() {
        return getTownClaim().claim();
    }

}
