package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TownCommand extends Command {
    public TownCommand(@NotNull HuskTowns plugin) {
        super("town", List.of("t"), plugin);
        setConsoleExecutable(true);
        setDefaultExecutor(new InfoCommand(this, plugin));
        setChildren(List.of(
                getHelpCommand(),
                new CreateCommand(this, plugin),
                new ListCommand(this, plugin),
                new ClaimCommand(this, plugin),
                (ChildCommand) getDefaultExecutor())
        );
    }

    /**
     * Create a new town
     */
    public static class CreateCommand extends ChildCommand {
        public CreateCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("create", List.of("found"), parent, "<name>", plugin);
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
    public static class InfoCommand extends ChildCommand {

        protected InfoCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("info", List.of("about"), parent, "(name)", plugin);
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
    public static class ListCommand extends ChildCommand {

        protected ListCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("list", List.of(), parent, "(page)", plugin);
            setConsoleExecutable(true);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final int page = parseIntArg(args, 0).orElse(1);
            final List<Town> towns = plugin.getTowns();
            final Locales locales = plugin.getLocales();
            if (towns.isEmpty()) {
                locales.getLocale("error_no_towns")
                        .ifPresent(executor::sendMessage);
                return;
            }
            executor.sendMessage(PaginatedList.of(towns.stream()
                                    .map(town -> locales.getRawLocale("town_list_item",
                                                    Locales.escapeText(town.getName()),
                                                    town.getColorRgb(),
                                                    Locales.escapeText(locales.wrapText(town.getBio()
                                                            .orElse(plugin.getLocales().getRawLocale("not_applicable")
                                                                    .orElse("N/A")), 40)),
                                                    Long.toString(town.getLevel()),
                                                    Long.toString(town.getMembers().size()),
                                                    Long.toString(town.getClaimCount()),
                                                    town.getFoundedTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                                            .orElse(town.getName()))
                                    .toList(),
                            locales.getBaseList(plugin.getSettings().listItemsPerPage)
                                    .setHeaderFormat(locales.getRawLocale("town_list_title",
                                            "/" + getName()).orElse(""))
                                    .setItemSeparator("\n").setCommand("/husktowns:town " + getName())
                                    .build())
                    .getNearestValidPage(page));
        }

    }

    /**
     * Command for claiming land
     */
    public static class ClaimCommand extends ChildCommand {

        private static final int MAX_CLAIM_RANGE_CHUNKS = 8;

        protected ClaimCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("claim", List.of(), parent, "(<x> <z>)", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final Chunk chunk = Chunk.at(parseIntArg(args, 0).orElse(user.getChunk().getX()),
                    parseIntArg(args, 1).orElse(user.getChunk().getZ()));
            if (user.getChunk().distanceBetween(chunk) > MAX_CLAIM_RANGE_CHUNKS) {
                plugin.getLocales().getLocale("error_claim_out_of_range")
                        .ifPresent(executor::sendMessage);
                return;
            }
            plugin.getManager().claims().createClaim(user, user.getWorld(), chunk);
        }
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
