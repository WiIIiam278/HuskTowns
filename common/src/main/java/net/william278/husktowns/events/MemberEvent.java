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

import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Role;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link TownEvent} that involves a {@link Member}
 */
public interface MemberEvent extends UserEvent, TownEvent {

    /**
     * Get the member involved in the event
     *
     * @return the member involved in the event
     */
    @NotNull
    default Member getMember() {
        return new Member(getUser(), getTown(), getMemberRole());
    }

    /**
     * Get the {@link Role} of the member involved in the event
     *
     * @return the role of the member involved in the event
     */
    @NotNull
    Role getMemberRole();

}
