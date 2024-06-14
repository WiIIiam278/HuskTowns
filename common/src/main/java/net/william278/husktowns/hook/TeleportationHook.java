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

package net.william278.husktowns.hook;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

/**
 * A hook for handling player teleportation
 */
public abstract class TeleportationHook extends Hook {
    protected TeleportationHook(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    /**
     * Teleport a player to a given position
     *
     * @param user     the user to teleport
     * @param position the position to teleport to
     * @param server   the server to teleport to
     * @param instant  whether to teleport instantly
     */
    public abstract void teleport(@NotNull OnlineUser user, @NotNull Position position, @NotNull String server,
                                  boolean instant);

}
