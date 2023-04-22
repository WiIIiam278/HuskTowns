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

import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface TownTabProvider extends TabProvider {

    @Override
    @NotNull
    default List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        return args.length == 1 ? filter(getTownNames(), args) : List.of();
    }

    @NotNull
    default List<String> getTownNames() {
        return getTowns().stream().map(Town::getName).sorted().toList();
    }

    @NotNull
    ConcurrentLinkedQueue<Town> getTowns();

}
