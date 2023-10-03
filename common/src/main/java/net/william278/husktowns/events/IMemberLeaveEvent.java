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

import org.jetbrains.annotations.NotNull;

/**
 * Event that is fired when a member leaves a town.
 * <p>
 * Note that {@link ITownDisbandEvent} is fired when a town is disbanded instead.
 */
public interface IMemberLeaveEvent extends MemberEvent, Cancellable {

    /**
     * Get the {@link LeaveReason reason} the member left the town
     *
     * @return the reason why the member left the town
     */
    @NotNull
    @SuppressWarnings("unused")
    LeaveReason getLeaveReason();

    /**
     * Identifies the reason a member left a town
     */
    enum LeaveReason {
        /**
         * The member left the town of their own accord through the leave command
         */
        LEAVE,
        /**
         * The member was evicted (kicked) from the town
         */
        EVICTED
    }
}
