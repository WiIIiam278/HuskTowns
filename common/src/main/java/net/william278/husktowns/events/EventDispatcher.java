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

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Interface for firing plugin API events
 */
public interface EventDispatcher {

    /**
     * Fire an event synchronously, then run a callback asynchronously
     *
     * @param event    The event to fire
     * @param callback The callback to run after the event has been fired
     * @param <T>      The type of event to fire
     */
    default <T extends Event> void fireEvent(@NotNull T event, @Nullable Consumer<T> callback) {
        getPlugin().runSync(() -> {
            if (!fireIsCancelled(event) && callback != null) {
                getPlugin().runAsync(() -> callback.accept(event));
            }
        });
    }

    /**
     * Fire an event synchronously
     *
     * @param event The event to fire
     * @param <T>   The type of event to fire
     */
    default <T extends Event> void fireEvent(@NotNull T event) {
        fireEvent(event, null);
    }

    /**
     * Fire an event on this thread, and return whether the event was canceled
     *
     * @param event The event to fire
     * @param <T>   The type of event to fire
     * @return Whether the event was canceled
     */
    <T extends Event> boolean fireIsCancelled(@NotNull T event);

    @NotNull
    IClaimEvent getClaimEvent(@NotNull OnlineUser user, @NotNull TownClaim claim);

    @NotNull
    IUnClaimEvent getUnClaimEvent(@NotNull OnlineUser user, @NotNull TownClaim claim);

    @NotNull
    IUnClaimAllEvent getUnClaimAllEvent(@NotNull OnlineUser user, @NotNull Town town);

    @NotNull
    ITownCreateEvent getTownCreateEvent(@NotNull OnlineUser user, @NotNull String townName);

    @NotNull
    IPostTownCreateEvent getPostTownCreateEvent(@NotNull OnlineUser user, @NotNull Town town);

    @NotNull
    ITownDisbandEvent getTownDisbandEvent(@NotNull OnlineUser user, @NotNull Town town);

    @NotNull
    IMemberJoinEvent getMemberJoinEvent(@NotNull OnlineUser user, @NotNull Town town, @NotNull Role role,
                                        @NotNull IMemberJoinEvent.JoinReason joinReason);

    @NotNull
    IMemberLeaveEvent getMemberLeaveEvent(@NotNull User user, @NotNull Town town, @NotNull Role role,
                                          @NotNull IMemberLeaveEvent.LeaveReason leaveReason);

    @NotNull
    IMemberRoleChangeEvent getMemberRoleChangeEvent(@NotNull User user, @NotNull Town town, @NotNull Role oldRole, @NotNull Role newRole);

    @NotNull
    IPlayerEnterTownEvent getPlayerEnterTownEvent(@NotNull OnlineUser user, @NotNull TownClaim claim,
                                                  @NotNull Position fromPosition, @NotNull Position toPosition);

    @NotNull
    IPlayerLeaveTownEvent getPlayerLeaveTownEvent(@NotNull OnlineUser user, @NotNull TownClaim claim,
                                                  @NotNull Position fromPosition, @NotNull Position toPosition);

    @NotNull
    HuskTowns getPlugin();

}
