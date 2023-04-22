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
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

/**
 * An event fired when a player walks out of a chunk claimed by a town
 */
@SuppressWarnings("unused")
public interface IPlayerLeaveTownEvent extends OnlineUserEvent {

    /**
     * Get the town-claim mapping of the claimed chunk the player has left
     *
     * @return the {@link TownClaim} of the claimed chunk the player has left
     */
    @NotNull
    TownClaim getLeftTownClaim();

    /**
     * Get the {@link Town} who owns the claimed chunk the player has left
     *
     * @return the {@link Town} who owns the claimed chunk the player has left
     */
    @NotNull
    default Town getLeft() {
        return getLeftTownClaim().town();
    }

    /**
     * Get the actual {@link Claim} that the player has left
     *
     * @return the {@link Claim} that the player has left
     */
    @NotNull
    default Claim getLeftClaim() {
        return getLeftTownClaim().claim();
    }

    /**
     * Get the {@link Position} the player exited from
     *
     * @return the {@link Position} the player exited from
     */
    @NotNull
    Position getFromPosition();

    /**
     * Get the new {@link Position} of the player after leaving the chunk
     *
     * @return the new {@link Position} of the player after leaving the chunk
     */
    @NotNull
    Position getToPosition();

}
