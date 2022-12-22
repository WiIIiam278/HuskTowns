package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class TownCommand extends Command {
    public TownCommand(@NotNull HuskTowns plugin) {
        super("town", List.of("t"), plugin);
        final ChildCommand HELP_CHILD_COMMAND = getUsageCommand();

        setDefaultExecutor(HELP_CHILD_COMMAND);
        setChildren(List.of(HELP_CHILD_COMMAND));
    }

    /**
     * Create a new town
     */
    public static class Create extends ChildCommand {
        public Create(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("create", List.of("found"), parent, "<town_name>", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<String> name = parseStringArg(args, 0);
            if (name.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
                return;
            }

            plugin.getManager().towns().createTown((OnlineUser) executor, name.get());
        }
    }

    /**
     * Command for viewing information about a town
     */
    public static class View {

    }

    /**
     * Command for listing towns
     */
    public static class Directory {

    }

    /**
     * Command for viewing nearby towns
     */
    public static class Map {

    }

}
