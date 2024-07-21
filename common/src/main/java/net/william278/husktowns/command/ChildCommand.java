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

package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public abstract class ChildCommand extends Node {
    protected final Command parent;
    private final String usage;

    protected ChildCommand(@NotNull String name, @NotNull List<String> aliases, @NotNull Command parent,
                           @NotNull String usage, @NotNull HuskTowns plugin) {
        super(name, aliases, plugin);
        this.parent = parent;
        this.usage = usage;
    }

    @Override
    @NotNull
    public String getPermission() {
        return new StringJoiner(".")
            .add(parent.getPermission())
            .add(getName()).toString();
    }

    @NotNull
    public String getUsage() {
        return "/" + parent.getName() + " " + getName() + ((" " + usage).isBlank() ? "" : " " + usage);
    }

    @NotNull
    public final Optional<String> getDescription() {
        return plugin.getLocales().getRawLocale("command_" + parent.getName() + "_" + getName() + "_description");
    }

}
