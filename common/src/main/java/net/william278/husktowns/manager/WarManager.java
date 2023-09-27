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

package net.william278.husktowns.manager;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.war.Declaration;
import net.william278.husktowns.war.War;
import net.william278.husktowns.war.WarSystem;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WarManager implements WarSystem {

    private final HuskTowns plugin;
    private final List<War> activeWars;
    private final List<Declaration> pendingDeclarations;

    public WarManager(@NotNull HuskTowns plugin) {
        this.plugin = plugin;
        this.activeWars = new ArrayList<>();
        this.pendingDeclarations = new ArrayList<>();
    }

    @NotNull
    @Override
    public List<War> getActiveWars() {
        return activeWars;
    }

    @NotNull
    @Override
    public List<Declaration> getPendingDeclarations() {
        return pendingDeclarations;
    }

    @NotNull
    @Override
    @ApiStatus.Internal
    public HuskTowns getPlugin() {
        return plugin;
    }
}
