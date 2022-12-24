package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class TownCommand extends Command {
    public TownCommand(@NotNull HuskTowns plugin) {
        super("town", List.of("t"), plugin);
        setConsoleExecutable(true);
        final ChildCommand HELP_CHILD_COMMAND = getHelpCommand();

        setDefaultExecutor(HELP_CHILD_COMMAND);
        setChildren(List.of(HELP_CHILD_COMMAND,
                new CreateCommand(this, plugin),
                new InfoCommand(this, plugin)));
    }

    /**
     * Create a new town
     */
    public static class CreateCommand extends ChildCommand {
        public CreateCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("create", List.of("found"), parent, "<town_name>", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<String> name = parseStringArg(args, 0);
            plugin.log(Level.INFO, "Town name: " + name);
            if (name.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
                return;
            }
            plugin.log(Level.INFO, "Making town with " + name.get());

            plugin.getManager().towns().createTown((OnlineUser) executor, name.get());
        }
    }

    /**
     * Command for viewing information about a town
     */
    public static class InfoCommand extends ChildCommand {

        protected InfoCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("info", List.of("about"), parent, "[town_name]", plugin);
            setConsoleExecutable(true);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<String> townName = parseStringArg(args, 0);

            Optional<Town> town;
            if (townName.isEmpty()) {
                if (executor instanceof OnlineUser user) {
                    town = plugin.getUserTown(user).map(Member::town);
                    if (town.isEmpty()) {
                        plugin.getLocales().getLocale("error_not_in_town")
                                .ifPresent(executor::sendMessage);
                        return;
                    }
                } else {
                    plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                            .ifPresent(executor::sendMessage);
                    return;
                }
            } else {
                town = plugin.findTown(townName.get());
            }

            if (town.isEmpty()) {
                plugin.getLocales().getLocale("error_town_not_found", townName.orElse(""))
                        .ifPresent(executor::sendMessage);
                return;
            }

            CompletableFuture.runAsync(() -> executor.sendMessage(town.get().getOverview(executor, plugin)));
        }
    }

    /**
     * Command for listing towns
     */
    public static class DirectoryCommand {

    }

    /**
     * Command for viewing nearby towns
     */
    public static class MapCommand {

    }

    /**
     * Command for viewing a town's members
     */
    public static class LogCommand {

    }

}
