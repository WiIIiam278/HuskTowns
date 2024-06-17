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

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;

import static net.william278.husktowns.config.Settings.TownSettings;

public final class AdminTownCommand extends Command {
    public AdminTownCommand(@NotNull HuskTowns plugin) {
        super("admintown", List.of("at"), plugin);
        setConsoleExecutable(true);
        setOperatorCommand(true);
        setDefaultExecutor(getHelpCommand());

        final ArrayList<ChildCommand> childCommands = new ArrayList<>(Arrays.asList(
            new AdminClaimCommand(this, plugin, true),
            new AdminClaimCommand(this, plugin, false),
            new AdminToggleCommand(this, plugin, AdminToggleCommand.Type.IGNORE_CLAIMS),
            new AdminToggleCommand(this, plugin, AdminToggleCommand.Type.CHAT_SPY),
            new ManageTownCommand(this, plugin, ManageTownCommand.Type.DELETE),
            new ManageTownCommand(this, plugin, ManageTownCommand.Type.TAKE_OVER),
            new ManageBalanceCommand(this, plugin),
            new SetLevelCommand(this, plugin),
            new PruneCommand(this, plugin),
            new TownBonusCommand(this, plugin)
        ));
        if (plugin.getSettings().getGeneral().isDoAdvancements()) {
            childCommands.add(new AdvancementCommand(this, plugin));
        }
        childCommands.add((ChildCommand) getDefaultExecutor());
        setChildren(childCommands);
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

    private static class AdminClaimCommand extends ChildCommand implements ChunkTabProvider {
        private final boolean creatingClaim;

        protected AdminClaimCommand(@NotNull Command parent, @NotNull HuskTowns plugin, boolean creatingClaim) {
            super(creatingClaim ? "claim" : "unclaim", List.of(), parent,
                "[<x> <z>" + (!creatingClaim ? "|all <town> [confirm]" : "") + "] [-m]", plugin);
            setOperatorCommand(true);
            this.creatingClaim = creatingClaim;
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final Chunk chunk = Chunk.at(parseIntArg(args, 0).orElse(user.getChunk().getX()),
                parseIntArg(args, 1).orElse(user.getChunk().getZ()));
            final boolean deleteAllClaims = !creatingClaim && parseStringArg(args, 0).map(arg -> arg.equals("all")).orElse(false);
            final boolean showMap = !deleteAllClaims && parseStringArg(args, 2).map(arg -> arg.equals("-m")).orElse(false);

            if (creatingClaim) {
                plugin.getManager().admin().createAdminClaim(user, user.getWorld(), chunk, showMap);
            } else {
                // Delete a single claim
                if (!deleteAllClaims) {
                    plugin.getManager().admin().deleteClaim(user, user.getWorld(), chunk, showMap);
                    return;
                }

                // Delete a town's claims (defaults to the executor's town)
                final Optional<String> townName = parseStringArg(args, 1);
                final boolean confirmed = parseStringArg(args, 2).map(arg -> arg.equals("confirm")).orElse(false);
                if (townName.isEmpty()) {
                    plugin.getManager().claims().deleteAllClaimsConfirm(user, confirmed);
                    return;
                }

                // Delete all another town's claims
                if (!confirmed) {
                    plugin.getLocales().getLocale("delete_all_claims_confirm_other", townName.get())
                        .ifPresent(user::sendMessage);
                    return;
                }
                plugin.getManager().admin().deleteAllClaims(user, townName.get());
            }
        }

        @Override
        @NotNull
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return switch (args.length) {
                case 1 -> {
                    final ArrayList<String> suggestions = new ArrayList<>(ChunkTabProvider.super.suggest(user, args));
                    if (!creatingClaim) {
                        suggestions.add("all");
                    }
                    yield suggestions;
                }
                case 2 -> {
                    if (!creatingClaim && args[0].equalsIgnoreCase("all")) {
                        yield filter(plugin.getTowns().stream().map(Town::getName).toList(), args);
                    }
                    yield ChunkTabProvider.super.suggest(user, args);
                }
                default -> ChunkTabProvider.super.suggest(user, args);
            };
        }

        @Override
        @NotNull
        public List<World> getWorlds() {
            return List.of();
        }
    }

    private static class AdminToggleCommand extends ChildCommand {
        private final Type type;

        protected AdminToggleCommand(@NotNull Command parent, @NotNull HuskTowns plugin, @NotNull Type type) {
            super(type.name, type.aliases, parent, "", plugin);
            setOperatorCommand(true);
            this.type = type;
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            plugin.editUserPreferences(user, (preferences -> {
                final boolean newValue = !(type == Type.IGNORE_CLAIMS ? preferences.isIgnoringClaims()
                    : preferences.isTownChatSpying());
                if (type == Type.IGNORE_CLAIMS) {
                    preferences.setIgnoringClaims(newValue);
                } else {
                    preferences.setTownChatSpying(newValue);
                }
                plugin.getLocales().getLocale((type == Type.IGNORE_CLAIMS ?
                        "ignoring_claims_" : "town_chat_spy_") + (newValue ? "enabled" : "disabled"))
                    .ifPresent(user::sendMessage);
            }));
        }

        public enum Type {
            IGNORE_CLAIMS("ignoreclaims", "ignore"),
            CHAT_SPY("chatspy", "spy");

            private final String name;
            private final List<String> aliases;

            Type(@NotNull String name, @NotNull String... aliases) {
                this.name = name;
                this.aliases = Arrays.asList(aliases);
            }
        }
    }

    private static class ManageTownCommand extends ChildCommand implements TownTabProvider {
        private final Type manageCommandType;

        protected ManageTownCommand(@NotNull Command parent, @NotNull HuskTowns plugin, @NotNull Type type) {
            super(type.name, type.aliases, parent, "<town>", plugin);
            setOperatorCommand(true);
            this.manageCommandType = type;
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final String townName = parseStringArg(args, 0).orElse("");
            if (townName.isBlank()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(user::sendMessage);
                return;
            }
            switch (manageCommandType) {
                case DELETE -> plugin.getManager().admin().deleteTown(user, townName);
                case TAKE_OVER -> plugin.getManager().admin().takeOverTown(user, townName);
            }
        }

        @NotNull
        @Override
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return TownTabProvider.super.suggest(user, args);
        }

        @Override
        @NotNull
        public Set<Town> getTowns() {
            return plugin.getTowns();
        }

        private enum Type {
            DELETE("delete"),
            TAKE_OVER("takeover");

            private final String name;
            private final List<String> aliases;

            Type(@NotNull String name, @NotNull String... aliases) {
                this.name = name;
                this.aliases = List.of(aliases);
            }
        }
    }

    private static class SetLevelCommand extends ChildCommand implements TownTabProvider {

        protected SetLevelCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("setlevel", List.of(), parent, String.format("<town> <1-%s>",
                plugin.getLevels().getMaxLevel()), plugin);
            setOperatorCommand(true);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final String townName = parseStringArg(args, 0).orElse("");
            if (townName.isBlank()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(user::sendMessage);
                return;
            }

            final Optional<Integer> level = parseIntArg(args, 1);
            if (level.isEmpty() || level.get() < 1 || level.get() > plugin.getLevels().getMaxLevel()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(user::sendMessage);
                return;
            }
            plugin.getManager().admin().setTownLevel(user, townName, level.get());
        }

        @NotNull
        @Override
        public Set<Town> getTowns() {
            return plugin.getTowns();
        }

        @NotNull
        @Override
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return (switch (args.length) {
                case 0, 1 -> TownTabProvider.super.suggest(user, args);
                case 2 -> IntStream.rangeClosed(1, plugin.getLevels().getMaxLevel())
                    .mapToObj(Integer::toString)
                    .toList();
                default -> List.of();
            });
        }
    }

    private static class ManageBalanceCommand extends ChildCommand implements TownTabProvider {

        protected ManageBalanceCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("balance", List.of("bal", "setbalance"), parent, String.format("<town> <%s> <amount>",
                String.join("|", MoneyOperation.getArguments())), plugin);
            setOperatorCommand(true);
            setConsoleExecutable(true);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<String> townName = parseStringArg(args, 0);
            final Optional<MoneyOperation> operation = parseStringArg(args, 1).flatMap(MoneyOperation::parse);
            final Optional<Double> amount = parseDoubleArg(args, 2);
            if (townName.isEmpty() || operation.isEmpty() || amount.isEmpty() || amount.get() < 0) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
                return;
            }

            // Edit the town balance
            final MoneyOperation action = operation.get();
            if (action == MoneyOperation.SET) {
                plugin.getManager().admin().setTownBalance(executor, townName.get(), BigDecimal.valueOf(amount.get()));
            } else {
                plugin.getManager().admin().changeTownBalance(executor, townName.get(), action == MoneyOperation.REMOVE
                    ? BigDecimal.valueOf(amount.get()).negate() : BigDecimal.valueOf(amount.get()));
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
            return (switch (args.length) {
                case 0, 1 -> TownTabProvider.super.suggest(user, args);
                case 2 -> MoneyOperation.getArguments();
                default -> List.of();
            });
        }

        public enum MoneyOperation {
            SET,
            ADD,
            REMOVE;

            private static Optional<MoneyOperation> parse(@NotNull String arg) {
                return Arrays.stream(values())
                    .filter(operation -> operation.name().equalsIgnoreCase(arg))
                    .findFirst();
            }

            @NotNull
            private static List<String> getArguments() {
                return Arrays.stream(values())
                    .map(MoneyOperation::name)
                    .map(String::toLowerCase).toList();
            }
        }
    }

    private static class TownBonusCommand extends ChildCommand implements TownTabProvider {

        protected TownBonusCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("bonus", List.of(), parent, "<town|user> <name> [<set|add|remove|clear> <bonus> <amount>]", plugin);
            setOperatorCommand(true);
            setConsoleExecutable(true);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<TargetType> targetType = parseStringArg(args, 0).flatMap(TargetType::parse);
            final Optional<String> targetName = parseStringArg(args, 1);
            final Optional<Operation> operation = parseStringArg(args, 2).flatMap(Operation::parse);
            final Optional<Town.Bonus> bonusType = parseStringArg(args, 3).flatMap(Town.Bonus::parse);
            final Optional<Integer> amount = parseIntArg(args, 4).map(num -> Math.max(0, num));
            if (targetType.isEmpty() || targetName.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
                return;
            }

            // Resolve the town name
            final String townName;
            if (targetType.get() == TargetType.USER) {
                final Optional<Town> target = plugin.getDatabase().getUser(targetName.get())
                    .flatMap(user -> plugin.getUserTown(user.user())).map(Member::town);
                if (target.isEmpty()) {
                    plugin.getLocales().getLocale("error_user_not_found", targetName.get())
                        .ifPresent(executor::sendMessage);
                    return;
                }

                townName = target.get().getName();
            } else {
                townName = targetName.get();
            }

            if (operation.isEmpty() || bonusType.isEmpty()) {
                plugin.getManager().admin().viewTownBonus(executor, townName);
                return;
            }

            final Town.Bonus bonus = bonusType.get();
            final int value = amount.orElse(0);
            switch (operation.get()) {
                case ADD -> plugin.getManager().admin().addTownBonus(executor, townName, bonus, value);
                case REMOVE -> plugin.getManager().admin().removeTownBonus(executor, townName, bonus, value);
                case SET -> plugin.getManager().admin().setTownBonus(executor, townName, bonus, value);
                case CLEAR -> plugin.getManager().admin().clearTownBonus(executor, townName, bonus);
            }
        }

        @Override
        @NotNull
        public Set<Town> getTowns() {
            return plugin.getTowns();
        }

        @Override
        @NotNull
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return switch (args.length) {
                case 0, 1 -> List.of("town", "user");
                case 2 -> args[0].equalsIgnoreCase("town") ? getTownNames() : List.of();
                case 3 -> List.of("set", "add", "remove", "clear");
                case 4 -> Arrays.stream(Town.Bonus.values())
                    .map(Town.Bonus::name)
                    .map(String::toLowerCase).toList();
                default -> List.of();
            };
        }

        public enum TargetType {
            TOWN,
            USER;

            private static Optional<TargetType> parse(@NotNull String string) {
                return Arrays.stream(values())
                    .filter(targetType -> targetType.name().equalsIgnoreCase(string))
                    .findFirst();
            }
        }

        public enum Operation {
            SET,
            ADD,
            REMOVE,
            CLEAR;

            private static Optional<Operation> parse(@NotNull String string) {
                return Arrays.stream(values())
                    .filter(operation -> operation.name().equalsIgnoreCase(string))
                    .findFirst();
            }
        }
    }

    public static class AdvancementCommand extends ChildCommand implements TabProvider {

        protected AdvancementCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("advancements", List.of(), parent, "[<list|reset> <player>]", plugin);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<String> operation = parseStringArg(args, 0);
            final Optional<String> target = parseStringArg(args, 1);
            if (operation.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
                return;
            }
            switch (operation.get().toLowerCase()) {
                case "list" -> plugin.getManager().admin().listAdvancements(executor, target.orElse(null));
                case "reset" -> {
                    if (target.isEmpty()) {
                        plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                            .ifPresent(executor::sendMessage);
                        return;
                    }
                    plugin.getManager().admin().resetAdvancements(executor, target.get());
                }
                default -> plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            }
        }

        @Override
        @NotNull
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return switch (args.length) {
                case 0 -> List.of("list", "reset");
                case 1 -> plugin.getOnlineUsers().stream()
                    .map(OnlineUser::getUsername)
                    .toList();
                default -> List.of();
            };
        }
    }

    private static class PruneCommand extends ChildCommand {

        protected PruneCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("prune", List.of(), parent, "[<days>|<d|w|m|y>] [confirm]", plugin);
            setOperatorCommand(true);
            setConsoleExecutable(true);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final TownSettings.TownPruningSettings settings = plugin.getSettings().getTowns().getPruneInactiveTowns();
            final int days = parseTimeArgAsDays(args, 0).orElse(settings.getPruneAfterDays());
            if (days <= 0) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
                return;
            }

            final boolean confirm = parseStringArg(args, 1).map("confirm"::equalsIgnoreCase).orElse(false);
            if (!confirm) {
                plugin.getLocales().getLocale("prune_inactive_towns_confirm", Integer.toString(days))
                    .ifPresent(executor::sendMessage);
                return;
            }

            // An actor is needed to prune cross-server, so report an error if nobody is online & cross-server is enabled
            final Optional<? extends OnlineUser> actor = executor instanceof OnlineUser online ? Optional.of(online)
                : plugin.getOnlineUsers().stream().findAny();
            if (actor.isEmpty() && plugin.getSettings().getCrossServer().isEnabled()) {
                plugin.getLocales().getLocale("error_command_in_game_only")
                    .ifPresent(executor::sendMessage);
                return;
            }

            final long pruned = plugin.pruneInactiveTowns(days, actor.orElse(null));
            plugin.getLocales().getLocale("prune_inactive_towns_success",
                    Long.toString(pruned), Integer.toString(days))
                .ifPresent(executor::sendMessage);
        }

    }

}
