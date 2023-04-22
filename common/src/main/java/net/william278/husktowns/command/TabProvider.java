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

import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.ConsoleUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TabProvider {

    @Nullable
    List<String> suggest(@NotNull CommandUser user, @NotNull String[] args);

    @NotNull
    default List<String> getSuggestions(@NotNull CommandUser user, @NotNull String[] args) {
        List<String> suggestions = suggest(user, args);
        if (suggestions == null) {
            suggestions = List.of();
        }
        return filter(suggestions, args);
    }

    @NotNull
    default List<String> filter(@NotNull List<String> suggestions, @NotNull String[] args) {
        return suggestions.stream()
                .filter(suggestion -> args.length == 0 || suggestion.toLowerCase()
                        .startsWith(args[args.length - 1].toLowerCase().trim()))
                .toList();
    }

    @NotNull
    static List<String> getMatchingNames(@Nullable String argument, @NotNull CommandUser user,
                                         @NotNull List<? extends Node> providers) {
        return providers.stream()
                .filter(command -> !(user instanceof ConsoleUser) || command.isConsoleExecutable())
                .map(Node::getName)
                .filter(commandName -> argument == null || argument.isBlank() || commandName.toLowerCase()
                        .startsWith(argument.toLowerCase().trim()))
                .toList();
    }


}
