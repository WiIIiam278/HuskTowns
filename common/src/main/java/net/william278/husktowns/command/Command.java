package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.ConsoleUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class Command extends Node implements TabProvider {

    public final List<Node> children;
    private final Executable defaultExecutor;

    protected Command(@NotNull String name, @NotNull List<String> aliases, @NotNull HuskTowns plugin,
                      @NotNull Executable defaultExecutor, @NotNull List<Node> children) {
        super(name, aliases, plugin);
        this.defaultExecutor = defaultExecutor;
        this.children = children;
    }

    protected Command(@NotNull String name, @NotNull List<String> aliases, final boolean consoleExecutable,
                      @NotNull HuskTowns plugin, @NotNull Executable defaultExecutor, @NotNull List<Node> children) {
        super(name, aliases, consoleExecutable, plugin);
        this.defaultExecutor = defaultExecutor;
        this.children = children;
    }

    @Override
    public final void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (executor.hasPermission(getPermission())) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }
        if (args.length >= 1) {
            for (Node child : children) {
                if (child.matchesInput(args[0])) {
                    if (executor.hasPermission(child.getPermission())) {
                        plugin.getLocales().getLocale("error_no_permission")
                                .ifPresent(executor::sendMessage);
                        return;
                    }
                    if (executor instanceof ConsoleUser && !isConsoleExecutable()) {
                        plugin.getLocales().getLocale("error_command_in_game_only")
                                .ifPresent(executor::sendMessage);
                        return;
                    }
                    child.execute(executor, args);
                    return;
                }
            }
        }
        if (executor instanceof ConsoleUser && !isConsoleExecutable()) {
            plugin.getLocales().getLocale("error_command_in_game_only")
                    .ifPresent(executor::sendMessage);
            return;
        }
        this.defaultExecutor.execute(executor, args);
    }

    @Override
    @Nullable
    public final List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        if (args.length <= 1) {
            return TabProvider.getMatchingNames(args[0], user, children);
        } else {
            for (Node child : children) {
                if (child.matchesInput(args[0]) && child instanceof TabProvider provider) {
                    return provider.suggest(user, args);
                }
            }
        }
        return null;
    }
}
