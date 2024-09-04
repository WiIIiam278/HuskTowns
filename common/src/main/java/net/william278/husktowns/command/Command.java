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

import de.themoep.minedown.adventure.MineDown;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Command extends Node implements TabProvider {

    public List<ChildCommand> children;
    private Executable defaultExecutor;

    protected Command(@NotNull String name, @NotNull List<String> aliases, @NotNull HuskTowns plugin) {
        super(name, aliases, plugin);
    }

    public void setDefaultExecutor(@NotNull Executable defaultExecutor) {
        this.defaultExecutor = defaultExecutor;
    }

    @NotNull
    protected Executable getDefaultExecutor() {
        if (defaultExecutor == null) {
            throw new IllegalStateException("Default executor not set");
        }
        return defaultExecutor;
    }

    public void setChildren(@NotNull List<ChildCommand> children) {
        this.children = children;
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (!executor.hasPermission(getPermission())) {
            plugin.getLocales().getLocale("error_no_permission")
                .ifPresent(executor::sendMessage);
            return;
        }
        if (args.length >= 1 && !children.isEmpty()) {
            for (Node child : children) {
                if (child.matchesInput(args[0])) {
                    if (!executor.hasPermission(child.getPermission())) {
                        plugin.getLocales().getLocale("error_no_permission")
                            .ifPresent(executor::sendMessage);
                        return;
                    }
                    if (!(executor instanceof OnlineUser) && !isConsoleExecutable()) {
                        plugin.getLocales().getLocale("error_command_in_game_only")
                            .ifPresent(executor::sendMessage);
                        return;
                    }
                    child.execute(executor, removeFirstArg(args));
                    return;
                }
            }
            plugin.getLocales().getLocale("error_unknown_command",
                    "/" + getName() + " " + getHelpCommand().getName())
                .ifPresent(executor::sendMessage);
            return;
        }
        if (!(executor instanceof OnlineUser) && !isConsoleExecutable()) {
            plugin.getLocales().getLocale("error_command_in_game_only")
                .ifPresent(executor::sendMessage);
            return;
        }
        this.defaultExecutor.execute(executor, args);
    }

    @Override
    @NotNull
    public final List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        if (args.length <= 1) {
            return TabProvider.getMatchingNames(args[0], user, children);
        } else {
            for (final Node child : children) {
                if (child.matchesInput(args[0]) && child instanceof TabProvider provider) {
                    return provider.getSuggestions(user, removeFirstArg(args));
                }
            }
        }
        return List.of();
    }

    @NotNull
    private String[] removeFirstArg(String[] args) {
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        return newArgs;
    }

    @NotNull
    public final String getUsage() {
        final String arguments = children.stream().map(Node::getName).collect(Collectors.joining("|"));
        return "/" + getName() + " [" + plugin.getLocales().truncateText(arguments, 40) + "]";
    }

    @NotNull
    public List<ChildCommand> getChildren() {
        return children;
    }

    @NotNull
    protected MineDown getChildCommandList(@NotNull CommandUser user, final int page) {
        final Locales locales = plugin.getLocales();
        return PaginatedList.of(getChildren().stream()
                    .filter(child -> child.canPerform(user))
                    .map(command -> locales.getRawLocale("command_list_item",
                            Locales.escapeText(command.getUsage()),
                            "/" + command.parent.getName() + " " + command.getName(),
                            command.getDescription().map(description ->
                                Locales.escapeText(description.length() > 50
                                    ? description.substring(0, 49).trim() + "…"
                                    : description)).orElse(""),
                            command.getDescription().map(Locales::escapeText).orElse(""))
                        .orElse(command.getUsage()))
                    .toList(),
                locales.getBaseList(plugin.getSettings().getGeneral().getListItemsPerPage())
                    .setHeaderFormat(locales.getRawLocale("child_command_list_title",
                        "/" + getName()).orElse(""))
                    .setItemSeparator("\n").setCommand("/husktowns:" + getName() + " help")
                    .build())
            .getNearestValidPage(page);
    }

    @NotNull
    public Command.HelpCommand getHelpCommand() {
        return new HelpCommand(this, plugin);
    }

    public static class HelpCommand extends ChildCommand implements TabProvider {

        protected HelpCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("help", List.of(), parent, "[page]", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            int page = parseIntArg(args, 0).orElse(1);
            executor.sendMessage(parent.getChildCommandList(executor, page));
        }

        @Override
        @Nullable
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return Stream.iterate(1, i -> i + 1)
                .limit(9)
                .map(String::valueOf)
                .collect(Collectors.toList());
        }
    }
}
