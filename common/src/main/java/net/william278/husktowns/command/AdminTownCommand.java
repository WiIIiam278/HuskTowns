package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import org.jetbrains.annotations.NotNull;

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
                new IgnoreClaimsCommand(this, plugin),
                new ManageTownCommand(this, plugin, ManageTownCommand.Type.DELETE),
                new ManageTownCommand(this, plugin, ManageTownCommand.Type.TAKE_OVER),
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

    private static class AdminClaimCommand extends ChildCommand {
        private final boolean creatingClaim;

        protected AdminClaimCommand(@NotNull Command parent, @NotNull HuskTowns plugin, boolean creatingClaim) {
            super(creatingClaim ? "claim" : "unclaim", List.of(), parent, "[<x> <z>] [-m]", plugin);
            setOperatorCommand(true);
            this.creatingClaim = creatingClaim;
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final Chunk chunk = Chunk.at(parseIntArg(args, 0).orElse(user.getChunk().getX()),
                    parseIntArg(args, 1).orElse(user.getChunk().getZ()));
            final boolean showMap = parseStringArg(args, 2).map(arg -> arg.equals("-m")).orElse(false);
            if (creatingClaim) {
                plugin.getManager().admin().createAdminClaim(user, user.getWorld(), chunk, showMap);
            } else {
                plugin.getManager().admin().deleteClaim(user, user.getWorld(), chunk, showMap);
            }
        }
    }

    private static class IgnoreClaimsCommand extends ChildCommand {
        protected IgnoreClaimsCommand(@NotNull Command parent, @NotNull HuskTowns plugin) {
            super("ignoreclaims", List.of("ignore"), parent, "", plugin);
            setOperatorCommand(true);
        }

        @Override
        public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
            final OnlineUser user = (OnlineUser) executor;
            final Optional<Preferences> optionalPreferences = plugin.getUserPreferences(user.getUuid());
            if (optionalPreferences.isPresent()) {
                final Preferences preferences = optionalPreferences.get();
                preferences.setIgnoringClaims(!preferences.isIgnoringClaims());
                plugin.runAsync(() -> plugin.getDatabase().updateUser(user, preferences));
                plugin.getLocales().getLocale("ignoring_claims_" + (preferences.isIgnoringClaims() ? "enabled" : "disabled"))
                        .ifPresent(user::sendMessage);
            }
        }
    }

    private static class ManageTownCommand extends ChildCommand {
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
                case TAKE_OVER -> plugin.getManager().admin().assumeTownOwnership(user, townName);
            }
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

}
