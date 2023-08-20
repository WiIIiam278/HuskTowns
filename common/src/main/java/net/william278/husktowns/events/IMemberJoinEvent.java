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
 * Event that is fired when a member joins a town
 */
public interface IMemberJoinEvent extends MemberEvent {

    /**
     * Get the {@link JoinReason reason} the member joined the town
     *
     * @return the reason the member joined the town
     */
    @NotNull
    @SuppressWarnings("unused")
    JoinReason getJoinReason();

    /**
     * Identifies the reason a member joined a town
     */
    enum JoinReason {
        /**
         * The member joined the town by invitation
         */
        ACCEPT_INVITE,
        /**
         * The member joined the town forcefully through an admin takeover
         */
        ADMIN_TAKE_OVER
    }

}
