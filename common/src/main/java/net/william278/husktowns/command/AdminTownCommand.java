package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class AdminTownCommand extends Command {
    public AdminTownCommand(@NotNull HuskTowns plugin) {
        super("admintown", List.of("at"), plugin);
        setConsoleExecutable(true);
        setOperatorCommand(true);
        setDefaultExecutor(getHelpCommand());
        setChildren(List.of(
                new AdminClaimCommand(this, plugin, true),
                new AdminClaimCommand(this, plugin, false),
                new AdminToggleCommand(this, plugin, AdminToggleCommand.Type.IGNORE_CLAIMS),
                new AdminToggleCommand(this, plugin, AdminToggleCommand.Type.CHAT_SPY),
                new ManageTownCommand(this, plugin, ManageTownCommand.Type.DELETE),
                new ManageTownCommand(this, plugin, ManageTownCommand.Type.TAKE_OVER),
                new TownBonusCommand(this, plugin),
                (ChildCommand) getDefaultExecutor()));
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
            final boolean deleteAllClaims = !creatingClaim && parseStringArg(args, 0).map(arg -> arg.equals("all")).orElse(false);
            final Chunk chunk = Chunk.at(parseIntArg(args, 0).orElse(user.getChunk().getX()),
                    parseIntArg(args, 1).orElse(user.getChunk().getZ()));
            final boolean showMap = parseStringArg(args, 2).map(arg -> arg.equals("-m")).orElse(false);
            if (creatingClaim) {
                plugin.getManager().admin().createAdminClaim(user, user.getWorld(), chunk, showMap);
            } else {
                if (deleteAllClaims) {
                    parseStringArg(args, 1).ifPresentOrElse(
                            townName -> plugin.getManager().admin().deleteAllClaims(user, townName),
                            () -> plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                                    .ifPresent(user::sendMessage));
                    plugin.getManager().claims().deleteAllClaimsConfirm(user, parseStringArg(args, 1)
                            .map(arg -> arg.equals("confirm")).orElse(false));
                    return;
                }

                plugin.getManager().admin().deleteClaim(user, user.getWorld(), chunk, showMap);
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
            final Optional<Preferences> optionalPreferences = plugin.getUserPreferences(user.getUuid());
            if (optionalPreferences.isPresent()) {
                final Preferences preferences = optionalPreferences.get();
                final boolean newValue = !(type == Type.IGNORE_CLAIMS ? preferences.isIgnoringClaims()
                        : preferences.isTownChatSpying());
                if (type == Type.IGNORE_CLAIMS) {
                    preferences.setIgnoringClaims(newValue);
                } else {
                    preferences.setTownChatSpying(newValue);
                }

                plugin.runAsync(() -> {
                    plugin.getDatabase().updateUser(user, preferences);
                    plugin.getLocales().getLocale(type == Type.IGNORE_CLAIMS ?
                                    "ignoring_claims_" : "town_chat_spy_" + (newValue ? "enabled" : "disabled"))
                            .ifPresent(user::sendMessage);
                });
            }
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

        protected ManageTownCommand(@NotNull Command parent, @NotNull HuskTowns plugin, @NotNull Type manageCommandType) {
            super(manageCommandType.name, manageCommandType.aliases, parent, "<town>", plugin);
            setOperatorCommand(true);
            this.manageCommandType = manageCommandType;
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

        @Override
        @NotNull
        public List<Town> getTowns() {
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

    private static class TownBonusCommand extends ChildCommand implements TownTabProvider {

        protected TownBonusCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("bonus", List.of(), parent, "<town> [<set|add|remove|clear> <bonus> <amount>]", plugin);
            setOperatorCommand(true);
            setConsoleExecutable(true);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final Optional<String> townName = parseStringArg(args, 0);
            final Optional<Operation> operation = parseStringArg(args, 1).flatMap(Operation::parse);
            final Optional<Town.Bonus> type = parseStringArg(args, 2).flatMap(Town.Bonus::parse);
            final Optional<Integer> amount = parseIntArg(args, 3).map(num -> Math.max(0, num));
            if (townName.isEmpty()) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
                return;
            }

            if (operation.isEmpty() || type.isEmpty()) {
                plugin.getManager().admin().viewTownBonus(executor, townName.get());
                return;
            }
            final Town.Bonus bonus = type.get();
            final int value = amount.orElse(0);
            switch (operation.get()) {
                case ADD -> plugin.getManager().admin().addTownBonus(executor, townName.get(), bonus, value);
                case REMOVE -> plugin.getManager().admin().removeTownBonus(executor, townName.get(), bonus, value);
                case SET -> plugin.getManager().admin().setTownBonus(executor, townName.get(), bonus, value);
                case CLEAR -> plugin.getManager().admin().clearTownBonus(executor, townName.get(), bonus);
            }
        }

        @Override
        @NotNull
        public List<Town> getTowns() {
            return plugin.getTowns();
        }

        @Override
        @NotNull
        public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
            return switch (args.length) {
                case 0, 1 -> TownTabProvider.super.suggest(user, args);
                case 2 -> filter(List.of("set", "add", "remove", "clear"), args);
                case 3 -> filter(Arrays.stream(Town.Bonus.values())
                        .map(Town.Bonus::name)
                        .map(String::toLowerCase).toList(), args);
                default -> List.of();
            };
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

}
