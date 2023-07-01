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

package net.william278.husktowns.town;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the member of a town
 *
 * @param user The {@link User}
 * @param town The {@link Town} they are a member of
 * @param role The {@link Role} the user has in the town
 */
public record Member(@NotNull User user, @NotNull Town town, @NotNull Role role) {

    /**
     * Get whether the member has the provided privilege in the town
     *
     * @param plugin    The HuskTowns plugin instance
     * @param privilege The {@link Privilege} to check
     * @return {@code true} if the member has the privilege, {@code false} otherwise
     */
    public boolean hasPrivilege(@NotNull HuskTowns plugin, @NotNull Privilege privilege) {
        return role().hasPrivilege(plugin, privilege);
    }

}
