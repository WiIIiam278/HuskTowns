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

import com.google.gson.annotations.Expose;
import net.william278.cloplib.operation.OperationWorld;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class World implements OperationWorld {

    @Expose
    private UUID uuid;
    @Expose
    private String name;
    @Expose
    private String environment;

    private World(@NotNull UUID uuid, @NotNull String name, @NotNull String environment) {
        this.uuid = uuid;
        this.name = name;
        this.environment = environment;
    }

    @SuppressWarnings("unused")
    private World() {
    }

    @NotNull
    public static World of(@NotNull UUID uuid, @NotNull String name, @NotNull String environment) {
        return new World(uuid, name, environment.trim().toLowerCase());
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getEnvironment() {
        return environment;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof final World world)) {
            return false;
        }
        return world.getUuid().equals(uuid) || (uuid.equals(new UUID(0, 0)) && name.equals(world.name));
    }
}
