/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
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
