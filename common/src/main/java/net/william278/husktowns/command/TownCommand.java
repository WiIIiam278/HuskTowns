package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.map.ClaimMap;
import net.william278.husktowns.town.Manager;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Overview;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TownCommand extends Command {
    public TownCommand(@NotNull HuskTowns plugin) {
        super("town", List.of("t"), plugin);
        setConsoleExecutable(true);
        setDefaultExecutor(new OverviewCommand(this, plugin));
        setChildren(List.of(
                getHelpCommand(),
                new CreateCommand(this, plugin),
                new ListCommand(this, plugin),
                new ClaimCommand(this, plugin, true),
                new ClaimCommand(this, plugin, false),
                new MapCommand(this, plugin),
                new ColorCommand(this, plugin),
                new MetaCommand(this, plugin, MetaCommand.Type.BIO),
                new MetaCommand(this, plugin, MetaCommand.Type.GREETING),
                new MetaCommand(this, plugin, MetaCommand.Type.FAREWELL),
                new RenameCommand(this, plugin),
                new LogCommand(this, plugin),
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
    public static class OverviewCommand extends ChildCommand {

        protected OverviewCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
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

            plugin.runAsync(() -> Overview.of(town.get(), executor, plugin).show());
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
                                                    Long.toString(town.getClaimCount()),
                                                    Long.toString(town.getMaxClaims()),
                                                    Long.toString(town.getMembers().size()),
                                                    Long.toString(town.getMaxMembers()),
                                                    town.getFoundedTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                                            .orElse(town.getName()))
                                    .toList(),
                            locales.getBaseList(plugin.getSettings().listItemsPerPage)
                                    .setHeaderFormat(locales.getRawLocale("town_list_title",
                                            Integer.toString(towns.size())).orElse(""))
                                    .setItemSeparator("\n").setCommand("/husktowns:town " + getName())
                                    .build())
                    .getNearestValidPage(page));
        }

    }

    /**
     * Command for claiming and unclaiming land
     */
    public static class ClaimCommand extends ChildCommand {

        private static final int MAX_CLAIM_RANGE_CHUNKS = 8;
        private final boolean creatingClaim;

        protected ClaimCommand(@NotNull Command parent, @NotNull HuskTowns plugin, boolean creatingClaim) {
            super(creatingClaim ? "claim" : "unclaim", List.of(), parent, "(<x> <z>) (-m)", plugin);
            this.creatingClaim = creatingClaim;
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final Chunk chunk = Chunk.at(parseIntArg(args, 0).orElse(user.getChunk().getX()),
                    parseIntArg(args, 1).orElse(user.getChunk().getZ()));
            final boolean showMap = parseStringArg(args, 2).map(arg -> arg.equals("-m")).orElse(false);
            if (user.getChunk().distanceBetween(chunk) > MAX_CLAIM_RANGE_CHUNKS) {
                plugin.getLocales().getLocale("error_claim_out_of_range")
                        .ifPresent(executor::sendMessage);
                return;
            }

            if (creatingClaim) {
                plugin.getManager().claims().createClaim(user, user.getWorld(), chunk, showMap);
            } else {
                plugin.getManager().claims().deleteClaim(user, user.getWorld(), chunk, showMap);
            }
        }
    }

    /**
     * Command for viewing nearby towns
     */
    public static class MapCommand extends ChildCommand {

        protected MapCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("map", List.of(), parent, "(<x> <z>) (world)", plugin);
            setConsoleExecutable(true);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Chunk chunk;
            final World world;
            if (executor instanceof OnlineUser user) {
                chunk = Chunk.at(parseIntArg(args, 0).orElse(user.getChunk().getX()),
                        parseIntArg(args, 1).orElse(user.getChunk().getZ()));
                world = parseStringArg(args, 2).flatMap(worldName -> plugin.getWorlds().stream()
                                .filter(w -> w.getName().equals(worldName))
                                .findFirst())
                        .orElse(user.getWorld());
            } else {
                final Optional<Integer> x = parseIntArg(args, 0);
                final Optional<Integer> z = parseIntArg(args, 1);
                world = parseStringArg(args, 2).flatMap(worldName -> plugin.getWorlds().stream()
                                .filter(w -> w.getName().equals(worldName))
                                .findFirst())
                        .orElse(plugin.getWorlds().get(0));
                if (x.isEmpty() || z.isEmpty()) {
                    plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                            .ifPresent(executor::sendMessage);
                    return;
                }
                chunk = Chunk.at(x.get(), z.get());
            }
            plugin.getLocales().getLocale("claim_map_title", Integer.toString(chunk.getX()),
                    Integer.toString(chunk.getZ())).ifPresent(executor::sendMessage);
            executor.sendMessage(ClaimMap.builder(plugin)
                    .center(chunk).world(world)
                    .width(11).height(11)
                    .build()
                    .toComponent(executor));
        }
    }

    public static class RenameCommand extends ChildCommand {

        protected RenameCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("rename", List.of(), parent, "<name>", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<String> name = parseStringArg(args, 0);
            final OnlineUser user = (OnlineUser) executor;
            if (name.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
                return;
            }
            plugin.getManager().towns().renameTown(user, name.get());
        }
    }

    /**
     * Command for viewing a town's members
     */
    public static class LogCommand extends ChildCommand {

        protected LogCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("logs", List.of("log", "audit"), parent, "(page)", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final int page = parseIntArg(args, 0).orElse(1);
            plugin.getManager().towns().showTownLogs(user, page);
        }
    }

    /**
     * Command for changing a town color
     */
    public static class ColorCommand extends ChildCommand {

        protected ColorCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("color", List.of("colour"), parent, "(rgb)", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final String rgbColor = parseStringArg(args, 0).orElse(null);
            plugin.getManager().towns().setTownColor((OnlineUser) executor, rgbColor);
        }
    }

    /**
     * Command for changing a town's bio
     */
    public static class MetaCommand extends ChildCommand {

        private final Type type;

        protected MetaCommand(@NotNull Command parent, @NotNull HuskTowns plugin, @NotNull Type type) {
            super(type.name().toLowerCase(), List.of(), parent, "<" + type.name().toLowerCase() + ">", plugin);
            this.type = type;
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<String> meta = parseGreedyString(args, 0);
            if (meta.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
                return;
            }
            final Manager.Towns manager = plugin.getManager().towns();
            switch (type) {
                case BIO -> manager.setTownBio((OnlineUser) executor, meta.get());
                case GREETING -> manager.setTownGreeting((OnlineUser) executor, meta.get());
                case FAREWELL -> manager.setTownFarewell((OnlineUser) executor, meta.get());
            }
        }

        public enum Type {
            BIO,
            GREETING,
            FAREWELL
        }
    }

}
