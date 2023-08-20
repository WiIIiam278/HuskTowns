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

package net.william278.husktowns.claim;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link World} mapped to a specific server, by ID
 *
 * @param server The ID of the server the world is on
 * @param world  The world
 */
public record ServerWorld(@NotNull String server, @NotNull World world) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof final ServerWorld serverWorld)) {
            return false;
        }
        return serverWorld.server().equals(server) && serverWorld.world().equals(world);
    }

    @Override
    public String toString() {
        return server + "/" + world.getName();
    }
}
