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

import com.google.common.collect.Lists;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.*;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.config.Settings;
import net.william278.husktowns.manager.TownsManager;
import net.william278.husktowns.manager.WarManager;
import net.william278.husktowns.map.ClaimMap;
import net.william278.husktowns.map.MapSquare;
import net.william278.husktowns.menu.Overview;
import net.william278.husktowns.town.Invite;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Role;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.User;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class TownCommand extends Command {
    public TownCommand(@NotNull HuskTowns plugin) {
        super("town", plugin.getSettings().getAliases(), plugin);
        setConsoleExecutable(true);
        setDefaultExecutor(new OverviewCommand(this, plugin, OverviewCommand.Type.TOWN));
        final ArrayList<ChildCommand> children = new ArrayList<>(List.of(getHelpCommand(),
            new CreateCommand(this, plugin),
            new ListCommand(this, plugin),
            new InviteCommand(this, plugin),
            new ClaimCommand(this, plugin, true),
            new ClaimCommand(this, plugin, false),
            new AutoClaimCommand(this, plugin),
            new MapCommand(this, plugin),
            new MemberCommand(this, plugin, MemberCommand.Type.PROMOTE),
            new MemberCommand(this, plugin, MemberCommand.Type.DEMOTE),
            new MemberCommand(this, plugin, MemberCommand.Type.EVICT),
            new LeaveCommand(this, plugin),
            new FarmCommand(this, plugin),
            new PlotCommand(this, plugin),
            new RulesCommand(this, plugin),
            new LevelCommand(this, plugin),
            new MetaCommand(this, plugin, MetaCommand.Type.BIO),
            new MetaCommand(this, plugin, MetaCommand.Type.GREETING),
            new MetaCommand(this, plugin, MetaCommand.Type.FAREWELL),
            new ColorCommand(this, plugin),
            new RenameCommand(this, plugin),
            new SpawnCommand(this, plugin),
            new SetSpawnCommand(this, plugin),
            new ClearSpawnCommand(this, plugin),
            new PrivacyCommand(this, plugin),
            new ChatCommand(this, plugin),
            new PlayerCommand(this, plugin),
            new OverviewCommand(this, plugin, OverviewCommand.Type.DEEDS),
            new OverviewCommand(this, plugin, OverviewCommand.Type.CENSUS),
            new LogCommand(this, plugin),
            new MemberCommand(this, plugin, MemberCommand.Type.TRANSFER),
            new DisbandCommand(this, plugin),
            (ChildCommand) getDefaultExecutor()));
        final Settings.TownSettings.RelationsSettings relations = plugin.getSettings().getTowns().getRelations();
        if (relations.isEnabled()) {
            children.add(new RelationsCommand(this, plugin));
            if (relations.getWars().isEnabled()) {
                children.add(new WarCommand(this, plugin));
            }
        }
        if (plugin.getEconomyHook().isPresent()) {
            children.add(new MoneyCommand(this, plugin, true));
            children.add(new MoneyCommand(this, plugin, false));
        }
        setChildren(children);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (!plugin.isLoaded()) {
            plugin.getLocales().getLocale("error_not_loaded")
                .ifPresent(executor::sendMessage);
            return;
        }
        super.execute(executor, args);
    }

    /**
     * Create a new town
     */
    private static class CreateCommand extends ChildCommand {
        public CreateCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("create", List.of("found"), parent, "<name>", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<String> name = parseGreedyString(args, 0);
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
    private static class OverviewCommand extends ChildCommand implements TownTabProvider {

        private static final int SQUARES_PER_DEEDS_COLUMN = 20;
        private final Type type;

        protected OverviewCommand(@NotNull Command parent, @NotNull HuskTowns plugin, @NotNull Type type) {
            super(type.name, type.aliases, parent, "[town]", plugin);
            this.type = type;
            setConsoleExecutable(true);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<String> townName = parseStringArg(args, 0);

            Optional<Town> optionalTown;
            if (townName.isEmpty()) {
                if (executor instanceof OnlineUser user) {
                    optionalTown = plugin.getUserTown(user).map(Member::town);
                    if (optionalTown.isEmpty()) {
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
                optionalTown = plugin.findTown(townName.get());
            }

            if (optionalTown.isEmpty()) {
                plugin.getLocales().getLocale("error_town_not_found", townName.orElse(""))
                    .ifPresent(executor::sendMessage);
                return;
            }

            final Town town = optionalTown.get();
            switch (type) {
                case TOWN -> plugin.runAsync(() -> Overview.of(town, executor, plugin).show());
                case DEEDS -> {
                    final int claimCount = town.getClaimCount();
                    if (claimCount <= 0) {
                        plugin.getLocales().getLocale("error_town_no_claims", town.getName())
                            .ifPresent(executor::sendMessage);
                        return;
                    }

                    final AtomicInteger column = new AtomicInteger(0);
                    final AtomicInteger total = new AtomicInteger(0);
                    final Component mapGrid = plugin.getWorlds().stream()
                        .map(world -> Map.entry(world, plugin.getClaimWorld(world)
                            .map(claimWorld -> claimWorld.getClaims().get(town.getId()))
                            .orElse(new ArrayList<>()).stream()
                            .map(claim -> new TownClaim(town, claim))
                            .toList()))
                        .flatMap((worldMap) -> worldMap.getValue().stream()
                            .peek(claim -> total.getAndIncrement())
                            .map(claim -> MapSquare.claim(
                                claim.claim().getChunk(),
                                worldMap.getKey(),
                                claim,
                                plugin
                            ))
                            .map(MapSquare::toComponent)
                            .map(square -> {
                                if (column.getAndIncrement() > SQUARES_PER_DEEDS_COLUMN) {
                                    column.set(0);
                                    return square.appendNewline();
                                }
                                return square;
                            }))
                        .reduce(Component.empty(), Component::append);

                    plugin.getLocales().getLocale("town_deeds_title", town.getName(),
                            Integer.toString(claimCount), Integer.toString(town.getMaxClaims(plugin)))
                        .ifPresent(executor::sendMessage);
                    executor.sendMessage(mapGrid);

                    if (total.get() < claimCount) {
                        plugin.getLocales().getLocale("town_deeds_other_servers",
                                Integer.toString(claimCount - total.get()))
                            .ifPresent(executor::sendMessage);
                    }
                }
                case CENSUS -> plugin.runAsync(() -> {
                    final TreeMap<Role, List<User>> members = new TreeMap<>(Comparator.comparingInt(Role::getWeight).reversed());
                    town.getMembers().forEach((uuid, roleWeight) -> plugin.getDatabase().getUser(uuid)
                        .ifPresent(user -> plugin.getRoles().fromWeight(roleWeight)
                            .ifPresent(role -> members.computeIfAbsent(role, k -> new ArrayList<>()).add(user.user()))));

                    Component component = plugin.getLocales().getLocale("town_census_title", town.getName(),
                            Integer.toString(town.getMembers().size()), Integer.toString(town.getMaxMembers(plugin)))
                        .map(MineDown::toComponent).orElse(Component.empty());
                    for (Map.Entry<Role, List<User>> users : members.entrySet()) {
                        component = component.appendNewline()
                            .append(plugin.getLocales().getRawLocale("town_census_line",
                                    Locales.escapeText(users.getKey().getName()),
                                    Integer.toString(users.getValue().size()),
                                    users.getValue().stream()
                                        .map(user -> plugin.getLocales().getRawLocale(String.format(
                                                "town_census_user_%s", plugin.getUserList()
                                                    .contains(user) ? "online" : "offline"
                                            ), Locales.escapeText(user.getUsername())
                                        ).orElse(Locales.escapeText(user.getUsername())))
                                        .collect(Collectors.joining(", "))
                                )
                                .map(l -> new MineDown(l).toComponent()).orElse(Component.empty()));
                    }
                    executor.sendMessage(component);
                });
            }
        }

        @Override
        @NotNull
        public Set<Town> getTowns() {
            return plugin.getTowns();
        }

        public enum Type {
            TOWN("info", "about"),
            DEEDS("deeds", "claims", "claimlist"),
            CENSUS("census", "members", "memberlist");

            private final String name;
            private final List<String> aliases;

            Type(@NotNull String name, @NotNull String... aliases) {
                this.name = name;
                this.aliases = List.of(aliases);
            }
        }
    }

    /**
     * Command for listing towns
     */
    private static class ListCommand extends ChildCommand implements PageTabProvider {
        final SortOption[] DISPLAYED_SORT_OPTIONS = {SortOption.LEVEL, SortOption.CLAIMS, SortOption.MEMBERS, SortOption.FOUNDED};

        protected ListCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("list", List.of(), parent, "[<sort_by> <ascending|descending>] [page]", plugin);
            setConsoleExecutable(true);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final SortOption sortOption = parseStringArg(args, 0).flatMap(SortOption::parse).orElse(SortOption.MEMBERS);
            final boolean ascending = parseStringArg(args, 1).map(s -> s.equalsIgnoreCase("ascending")).orElse(false);
            final int page = parseIntArg(args, args.length == 3 ? 2 : 0).orElse(1);
            final List<Town> towns = sortOption.sort(plugin.getTowns(), ascending);
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
                                Locales.escapeText(town.getBio()
                                    .orElse(plugin.getLocales().getNotApplicable())),
                                Integer.toString(town.getLevel()),
                                Integer.toString(town.getClaimCount()),
                                Integer.toString(town.getMaxClaims(plugin)),
                                Integer.toString(town.getMembers().size()),
                                Integer.toString(town.getMaxMembers(plugin)),
                                town.getFoundedTime().format(DateTimeFormatter.ofPattern("dd MMM yy")))
                            .orElse(town.getName()))
                        .toList(),
                    locales.getBaseList(plugin.getSettings().getGeneral().getListItemsPerPage())
                        .setHeaderFormat(getListTitle(locales, towns.size(), sortOption, ascending))
                        .setItemSeparator("\n")
                        .setCommand("/husktowns:town list " + sortOption.name() + " " + (ascending ? "ascending" : "descending"))
                        .build())
                .getNearestValidPage(page));
        }

        @NotNull
        private String getListTitle(@NotNull Locales locales, int townCount, @NotNull SortOption sort, boolean ascending) {
            return locales.getRawLocale("town_list_title",
                    locales.getRawLocale("town_list_sort_" + (ascending ? "ascending" : "descending"),
                        sort.name(), "%current_page%").orElse(""),
                    Integer.toString(townCount),
                    locales.getRawLocale("town_list_sort_options", getSortButtons(locales, sort, ascending))
                        .orElse(""))
                .orElse("");
        }

        @NotNull
        private String getSortButtons(@NotNull Locales locales, @NotNull SortOption sort, boolean ascending) {
            final StringJoiner options = new StringJoiner(locales.getRawLocale("town_list_sort_option_separator")
                .orElse("|"));
            for (SortOption option : DISPLAYED_SORT_OPTIONS) {
                boolean selected = option == sort;
                if (selected) {
                    options.add(locales.getRawLocale("town_list_sort_option_selected",
                            option.getTranslatedName(plugin.getLocales()))
                        .orElse(option.name()));
                    continue;
                }
                options.add(locales.getRawLocale("town_list_sort_option",
                        option.getTranslatedName(plugin.getLocales()),
                        option.name().toLowerCase(Locale.ENGLISH),
                        ascending ? "ascending" : "descending", "%current_page%")
                    .orElse(option.name()));
            }
            return options.toString();
        }

        @Override
        public int getPageCount() {
            return plugin.getTowns().size() / plugin.getSettings().getGeneral().getListItemsPerPage() + 1;
        }

        @Override
        @NotNull
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return switch (args.length) {
                case 0, 1 -> filter(Arrays.stream(SortOption.values())
                    .map(SortOption::name)
                    .map(String::toLowerCase)
                    .toList(), args);
                case 2 -> filter(List.of("ascending", "descending"), args);
                case 3 -> PageTabProvider.super.suggest(user, args);
                default -> List.of();
            };
        }

        /**
         * Options for sorting the town list
         */
        public enum SortOption {
            FOUNDED(Comparator.comparing(Town::getFoundedTime).thenComparing(Town::getName)),
            NAME(Comparator.comparing(Town::getName)),
            LEVEL(Comparator.comparingInt(Town::getLevel).thenComparing(Town::getName)),
            CLAIMS(Comparator.comparingInt(Town::getClaimCount).thenComparing(Town::getName)),
            MEMBERS(Comparator.comparingInt((Town town) -> town.getMembers().size()).thenComparing(Town::getName)),
            MONEY(Comparator.comparing(Town::getMoney));
            private final Comparator<Town> comparator;

            SortOption(@NotNull Comparator<Town> filter) {
                this.comparator = filter;
            }

            @NotNull
            private List<Town> sort(@NotNull Collection<Town> towns, boolean ascending) {
                final List<Town> sortedTowns = Lists.newArrayList(towns);
                sortedTowns.sort(comparator);
                if (!ascending) {
                    Collections.reverse(sortedTowns);
                }
                return sortedTowns;
            }

            private static Optional<SortOption> parse(@NotNull String name) {
                return Arrays.stream(values())
                    .filter(option -> option.name().equalsIgnoreCase(name))
                    .findFirst();
            }

            @NotNull
            private String getTranslatedName(@NotNull Locales locales) {
                return locales.getRawLocale("town_list_sort_option_label_" + name().toLowerCase())
                    .orElse(name().toLowerCase(Locale.ENGLISH));
            }
        }
    }

    private static class InviteCommand extends ChildCommand implements TabProvider {

        protected InviteCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("invite", List.of(), parent, "<(player)|(accept|decline) [target]>", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final Optional<String> userArgument = parseStringArg(args, 0);
            final Optional<String> targetArgument = parseStringArg(args, 1);
            if (userArgument.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(user::sendMessage);
                return;
            }

            final String argument = userArgument.get();
            switch (argument) {
                case "accept", "yes" -> plugin.getManager().towns()
                    .handleInviteReply(user, true, targetArgument.orElse(null));
                case "decline", "deny", "no" -> plugin.getManager().towns()
                    .handleInviteReply(user, false, targetArgument.orElse(null));
                default -> plugin.getManager().towns()
                    .inviteMember(user, argument);
            }
        }

        @Override
        @Nullable
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return switch (args.length) {
                case 0, 1 -> filter(getInviteTargetList(), args);
                case 2 -> plugin.getInvites().getOrDefault(((OnlineUser) user).getUuid(), new ArrayDeque<>())
                    .stream()
                    .map(Invite::getSender).map(User::getUsername)
                    .collect(Collectors.toList());
                default -> null;
            };
        }

        @NotNull
        private List<String> getInviteTargetList() {
            final List<String> users = new ArrayList<>(List.of("accept", "decline"));
            users.addAll(plugin.getUserList().stream().map(User::getUsername).toList());
            return users;
        }
    }

    private static class LeaveCommand extends ChildCommand {

        protected LeaveCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("leave", List.of(), parent, "", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            plugin.getManager().towns().leaveTown((OnlineUser) executor);
        }
    }

    private static class MemberCommand extends ChildCommand {

        private final Type type;

        protected MemberCommand(@NotNull Command parent, @NotNull HuskTowns plugin, @NotNull Type type) {
            super(type.name, type.aliases, parent, "<member>", plugin);
            this.type = type;
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final Optional<String> memberArgument = parseStringArg(args, 0);
            if (memberArgument.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
                return;
            }

            final String member = memberArgument.get();
            switch (type) {
                case EVICT -> plugin.getManager().towns().removeMember(user, member);
                case PROMOTE -> plugin.getManager().towns().promoteMember(user, member);
                case DEMOTE -> plugin.getManager().towns().demoteMember(user, member);
                case TRANSFER -> plugin.getManager().towns().transferOwnership(user, member);
            }
        }

        public enum Type {
            EVICT("evict", "kick"),
            PROMOTE("promote"),
            DEMOTE("demote"),
            TRANSFER("transfer");

            private final String name;
            private final List<String> aliases;

            Type(@NotNull String name, @NotNull String... aliases) {
                this.name = name;
                this.aliases = List.of(aliases);
            }
        }
    }

    /**
     * Command for claiming and unclaiming land
     */
    private static class ClaimCommand extends ChildCommand implements ChunkTabProvider {

        private static final int MAX_CLAIM_RANGE_CHUNKS = 8;
        private final boolean creatingClaim;

        protected ClaimCommand(@NotNull Command parent, @NotNull HuskTowns plugin, boolean creatingClaim) {
            super(creatingClaim ? "claim" : "unclaim", List.of(), parent,
                "[<x> <z>" + (!creatingClaim ? "|all [confirm]" : "") + "] [-m]", plugin);
            this.creatingClaim = creatingClaim;
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final boolean deleteAllClaims = !creatingClaim && parseStringArg(args, 0).map(arg -> arg.equals("all")).orElse(false);
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
                if (deleteAllClaims) {
                    plugin.getManager().claims().deleteAllClaimsConfirm(user, parseStringArg(args, 1)
                        .map(arg -> arg.equals("confirm")).orElse(false));
                    return;
                }

                plugin.getManager().claims().deleteClaim(user, user.getWorld(), chunk, showMap);
            }
        }

        @Override
        @NotNull
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            if (args.length == 1) {
                final ArrayList<String> suggestions = new ArrayList<>(ChunkTabProvider.super.suggest(user, args));
                if (!creatingClaim) {
                    suggestions.add("all");
                }
                return suggestions;
            }
            return ChunkTabProvider.super.suggest(user, args);
        }

        @Override
        @NotNull
        public List<World> getWorlds() {
            return List.of();
        }
    }

    private static class AutoClaimCommand extends ChildCommand {

        protected AutoClaimCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("autoclaim", List.of(), parent, "", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            plugin.getManager().claims().toggleAutoClaiming(user);
        }
    }

    /**
     * Command for viewing nearby towns
     */
    private static class MapCommand extends ChildCommand implements ChunkTabProvider {

        protected MapCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("map", List.of(), parent, "[<x> <z>] [world]", plugin);
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
            final ClaimMap map = ClaimMap.builder(plugin)
                .center(chunk).world(world)
                .build();
            executor.sendMessage(map.toComponent(executor));
            if (executor instanceof OnlineUser user) {
                map.highlightCenter(user);
            }
        }

        @Override
        @NotNull
        public List<World> getWorlds() {
            return plugin.getWorlds().stream()
                .filter(world -> plugin.getClaimWorlds().containsKey(world.getName()))
                .collect(Collectors.toList());
        }
    }

    private static class RulesCommand extends ChildCommand implements TabProvider {

        protected RulesCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("rules", List.of("settings", "flags"), parent, "[<flag> <claim_type> <true|false>] [-m]", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final Optional<Flag> flag = parseStringArg(args, 0).flatMap(id -> plugin.getFlags().getFlag(id));
            final Optional<Claim.Type> claimType = parseStringArg(args, 1).flatMap(Claim.Type::fromId);
            final Optional<Boolean> value = parseStringArg(args, 2).map(Boolean::parseBoolean);
            final boolean showMenu = parseStringArg(args, 3).map(arg -> arg.equals("-m")).orElse(false);
            if (flag.isPresent() && claimType.isPresent() && value.isPresent()) {
                plugin.getManager().towns().setFlagRule(user, flag.get(), claimType.get(), value.get(), showMenu);
                return;
            }
            plugin.getManager().towns().showRulesConfig(user);
        }

        @Override
        @Nullable
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return switch (args.length) {
                case 0, 1 -> filter(plugin.getFlags().getFlagSet().stream()
                    .map(Flag::getName)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList()), args);
                case 2 -> filter(Arrays.stream(Claim.Type.values())
                    .map(Claim.Type::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList()), args);
                case 3 -> filter(List.of("true", "false"), args);
                case 4 -> filter(List.of("-m"), args);
                default -> List.of();
            };
        }
    }

    private static class RenameCommand extends ChildCommand {

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
    private static class LogCommand extends ChildCommand implements PageTabProvider {

        protected LogCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("logs", List.of("log", "audit"), parent, "[page]", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final int page = parseIntArg(args, 0).orElse(1);
            plugin.getManager().towns().showTownLogs(user, page);
        }

        @Override
        public int getPageCount() {
            return 10;
        }
    }

    /**
     * Command for changing a town color
     */
    private static class ColorCommand extends ChildCommand implements TabProvider {

        final List<String> COLORS_OF_THE_DAY = new ArrayList<>();

        protected ColorCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("color", List.of("colour"), parent, "[#<color>]", plugin);
            for (int i = 0; i < 255; i++) {
                final int r = (int) (Math.random() * 255);
                final int g = (int) (Math.random() * 255);
                final int b = (int) (Math.random() * 255);
                final String hex = String.format("#%02x%02x%02x", r, g, b);
                COLORS_OF_THE_DAY.add(hex);
            }
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final String rgbColor = parseStringArg(args, 0).orElse(null);
            plugin.getManager().towns().setTownColor((OnlineUser) executor, rgbColor);
        }

        @Override
        @Nullable
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return args.length <= 1 ? filter(COLORS_OF_THE_DAY, args) : null;
        }
    }

    /**
     * Command for changing a town's bio
     */
    private static class MetaCommand extends ChildCommand {
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
            final TownsManager manager = plugin.getManager().towns();
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

    private static class SpawnCommand extends ChildCommand implements TownTabProvider {


        protected SpawnCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("spawn", List.of(), parent, "[town]", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<String> townName = parseStringArg(args, 0);
            final OnlineUser user = (OnlineUser) executor;
            plugin.getManager().towns().teleportToTownSpawn(user, townName.orElse(null));
        }

        @Override
        @NotNull
        @Unmodifiable
        public Set<Town> getTowns() {
            return plugin.getTowns().stream()
                .filter(town -> town.getSpawn().isPresent())
                .filter(town -> town.getSpawn().get().isPublic())
                .collect(Collectors.toSet());
        }
    }

    private static class SetSpawnCommand extends ChildCommand {

        protected SetSpawnCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("setspawn", List.of(), parent, "", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            plugin.getManager().towns().setTownSpawn(user, user.getPosition());
        }
    }

    private static class ClearSpawnCommand extends ChildCommand {
        protected ClearSpawnCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("clearspawn", List.of(), parent, "", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            plugin.getManager().towns().clearTownSpawn(user);
        }
    }

    private static class PrivacyCommand extends ChildCommand implements TabProvider {

        protected PrivacyCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("privacy", List.of("spawnprivacy"), parent, "<public|private>", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<String> privacy = parseStringArg(args, 0);
            if (privacy.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
                return;
            }
            final OnlineUser user = (OnlineUser) executor;
            final TownsManager manager = plugin.getManager().towns();
            manager.setSpawnPrivacy(user, privacy.get().equals("public"));
        }


        @Override
        @Nullable
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return args.length <= 1 ? filter(List.of("public", "private"), args) : List.of();
        }
    }

    private static class FarmCommand extends ChildCommand {

        protected FarmCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("farm", List.of(), parent, "", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            plugin.getManager().claims().makeClaimFarm(user, user.getWorld(), user.getChunk());
        }
    }

    private static class PlotCommand extends ChildCommand implements TabProvider {

        protected PlotCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("plot", List.of(), parent, "<members|claim|(<add|remove> <player> [manager])>", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final Optional<String> subCommand = parseStringArg(args, 0);
            if (subCommand.isEmpty()) {
                plugin.getManager().claims().makeClaimPlot(user, user.getWorld(), user.getChunk());
                return;
            }
            switch (subCommand.get().toLowerCase()) {
                case "add", "trust" -> {
                    final Optional<String> target = parseStringArg(args, 1);
                    final boolean manager = parseStringArg(args, 2).map(role -> role
                        .equalsIgnoreCase("manager")).orElse(false);
                    if (target.isEmpty()) {
                        plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                            .ifPresent(executor::sendMessage);
                        return;
                    }
                    plugin.getManager().claims().addPlotMember(user, user.getWorld(), user.getChunk(), target.get(), manager);
                }
                case "remove", "untrust" -> {
                    final Optional<String> target = parseStringArg(args, 1);
                    if (target.isEmpty()) {
                        plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                            .ifPresent(executor::sendMessage);
                        return;
                    }
                    plugin.getManager().claims().removePlotMember(user, user.getWorld(), user.getChunk(), target.get());
                }
                case "claim" -> plugin.getManager().claims()
                    .claimPlot(user, user.getWorld(), user.getChunk());
                case "members", "memberlist", "list" -> plugin.getManager().claims()
                    .listPlotMembers(user, user.getWorld(), user.getChunk());
                default -> plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            }
        }

        @Override
        @Nullable
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return switch (args.length) {
                case 0, 1 -> List.of("add", "remove", "members");
                case 2 -> List.of("add", "trust", "remove", "untrust").contains(args[0].toLowerCase(Locale.ENGLISH))
                    ? plugin.getUserList().stream().map(User::getUsername).toList() : List.of();
                case 3 -> List.of("add", "trust").contains(args[0].toLowerCase(Locale.ENGLISH))
                    ? List.of("manager") : List.of();
                default -> List.of();
            };
        }
    }

    private static class MoneyCommand extends ChildCommand {
        private final boolean deposit;

        protected MoneyCommand(@NotNull Command parent, @NotNull HuskTowns plugin, boolean deposit) {
            super(deposit ? "deposit" : "withdraw", List.of(), parent, "<amount>", plugin);
            this.deposit = deposit;
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final Optional<BigDecimal> amount = parseDoubleArg(args, 0).map(BigDecimal::valueOf);
            if (amount.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
                return;
            }
            if (deposit) {
                plugin.getManager().towns().depositMoney(user, amount.get());
            } else {
                plugin.getManager().towns().withdrawMoney(user, amount.get());
            }
        }
    }

    private static class RelationsCommand extends ChildCommand implements TabProvider {

        protected RelationsCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("relations", List.of(), parent, "[list (town)|set <ally|neutral|enemy> <other_town>]", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final String operation = parseStringArg(args, 0).orElse("list").toLowerCase(Locale.ENGLISH);
            if (!operation.equals("set")) {
                plugin.getManager().towns().showTownRelations((OnlineUser) executor, parseStringArg(args, 1)
                    .orElse(null));
                return;
            }

            final Optional<Town.Relation> relation = parseStringArg(args, 1).flatMap(Town.Relation::parse);
            final Optional<String> town = parseStringArg(args, 2);
            if (relation.isEmpty() || town.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
                return;
            }
            plugin.getManager().towns().setTownRelation((OnlineUser) executor, relation.get(), town.get());
        }

        @NotNull
        @Override
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return switch (args.length) {
                case 0, 1 -> List.of("set", "list");
                case 2 -> args[0].equalsIgnoreCase("set")
                    ? List.of("ally", "neutral", "enemy")
                    : plugin.getTowns().stream().map(Town::getName).toList();
                case 3 -> args[0].equalsIgnoreCase("set")
                    ? plugin.getTowns().stream().map(Town::getName).toList()
                    : List.of();
                default -> List.of();
            };
        }

    }

    private static class WarCommand extends ChildCommand implements TownTabProvider {

        protected WarCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("war", List.of(), parent, "<view [town]|declare (town) (wager)|accept|surrender>", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final String operation = parseStringArg(args, 0).orElse("view").toLowerCase(Locale.ENGLISH);
            final Optional<String> optionalTown = parseStringArg(args, 1);
            final WarManager wars = plugin.getManager().wars()
                .orElseThrow(() -> new IllegalStateException("No war manager present to handle war command!"));

            switch (operation) {
                case "view" -> {
                    final String town = optionalTown.orElse(null);
                    wars.showWarStatus((OnlineUser) executor, town);
                }
                case "declare" -> {
                    final BigDecimal minimum = BigDecimal.valueOf(plugin.getSettings().getTowns().getRelations()
                        .getWars().getMinimumWager());
                    final BigDecimal wager = parseDoubleArg(args, 1).map(BigDecimal::valueOf)
                        .orElse(minimum).max(BigDecimal.ZERO);
                    if (optionalTown.isEmpty()) {
                        plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                            .ifPresent(executor::sendMessage);
                        return;
                    }
                    final String town = optionalTown.get();
                    wars.sendWarDeclaration((OnlineUser) executor, town, wager);
                }
                case "accept" -> wars.acceptWarDeclaration((OnlineUser) executor);
                case "surrender" -> wars.surrenderWar((OnlineUser) executor);
                default -> plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            }
        }

        @NotNull
        @Override
        public Set<Town> getTowns() {
            return plugin.getTowns();
        }

        @NotNull
        @Override
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return switch (args.length) {
                case 0, 1 -> List.of("view", "declare", "accept", "surrender");
                case 2 -> List.of("view", "declare").contains(args[0].toLowerCase(Locale.ENGLISH))
                    ? plugin.getTowns().stream().map(Town::getName).toList() : List.of();
                case 3 -> args[0].toLowerCase(Locale.ENGLISH).equals("declare")
                    ? List.of(Double.toString(plugin.getSettings().getTowns().getRelations()
                    .getWars().getMinimumWager()))
                    : List.of();
                default -> List.of();
            };
        }
    }

    private static class LevelCommand extends ChildCommand implements TabProvider {

        protected LevelCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("level", List.of("levelup"), parent, "[confirm]", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            plugin.getManager().towns().levelUpTownConfirm(
                (OnlineUser) executor,
                parseStringArg(args, 0)
                    .map(arg -> arg.equalsIgnoreCase("confirm"))
                    .orElse(false)
            );
        }

        @Override
        @Nullable
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return args.length == 1 ? filter(List.of("confirm"), args) : List.of();
        }
    }

    private static class ChatCommand extends ChildCommand {
        protected ChatCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("chat", List.of("c"), parent, "[message]", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final Optional<String> message = parseGreedyString(args, 0);
            plugin.getManager().towns().sendChatMessage(user, message.orElse(null));
        }
    }

    private static class PlayerCommand extends ChildCommand implements TabProvider {
        protected PlayerCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("player", List.of("who"), parent, "<player>", plugin);
            this.setConsoleExecutable(true);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<String> target = parseStringArg(args, 0);
            if (target.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
                return;
            }
            plugin.getManager().towns().showPlayerInfo(executor, target.get());
        }

        @Nullable
        @Override
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return args.length < 2 ? plugin.getUserList().stream().map(User::getUsername).toList() : List.of();
        }
    }

    private static class DisbandCommand extends ChildCommand implements TabProvider {

        protected DisbandCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("disband", List.of("delete", "abandon"), parent, "[confirm]", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            plugin.getManager().towns().deleteTownConfirm(user, parseStringArg(args, 0)
                .map(confirm -> confirm.equalsIgnoreCase("confirm")).orElse(false));
        }

        @Override
        @Nullable
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return args.length == 1 ? filter(List.of("confirm"), args) : List.of();
        }
    }

}
