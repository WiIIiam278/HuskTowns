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

import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.user.BukkitUser;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * {@inheritDoc}
 */
public class ClaimEvent extends PlayerEvent implements IClaimEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean isCancelled = false;
    private final TownClaim claim;

    public ClaimEvent(@NotNull BukkitUser user, @NotNull TownClaim claim) {
        super(user.getPlayer());
        this.claim = claim;
    }

    @Override
    @NotNull
    public TownClaim getTownClaim() {
        return claim;
    }

    /**
     * Get the legacy claimed chunk object representing the claim that was made
     *
     * @return the legacy claimed chunk object
     * @deprecated use {@link #getTownClaim()} instead
     */
    @NotNull
    @Deprecated(since = "2.0")
    public ClaimedChunk getClaimedChunk() {
        final World world = World.of(getPlayer().getWorld().getUID(),
                getPlayer().getWorld().getName(),
                getPlayer().getWorld().getEnvironment().name().toLowerCase());
        return ClaimedChunk.fromClaim(claim, world);
    }

    @Override
    @NotNull
    public OnlineUser getUser() {
        return BukkitUser.adapt(getPlayer());
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
