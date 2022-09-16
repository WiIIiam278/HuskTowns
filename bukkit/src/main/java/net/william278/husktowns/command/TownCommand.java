package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.MessageManager;
import net.william278.husktowns.command.subcommands.ShortcutTownSubCommand;
import net.william278.husktowns.command.subcommands.SubCommand;
import net.william278.husktowns.command.subcommands.TownSubCommand;
import net.william278.husktowns.command.subcommands.town.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TownCommand extends CommandBase {

    public static final ArrayList<TownSubCommand> subCommands = new ArrayList<>();

    private static ArrayList<TownSubCommand> getUsableSubCommands(Player player) {
        ArrayList<TownSubCommand> tabCompletions = new ArrayList<>();
        for (TownSubCommand subCommand : subCommands) {
            if (subCommand.permissionNode != null) {
                if (!player.hasPermission(subCommand.permissionNode)) {
                    continue;
                }
            }
            tabCompletions.add(subCommand);
        }
        return tabCompletions;
    }

    private static ArrayList<String> getUsableSubCommandStrings(Player player) {
        final ArrayList<String> subCommands = new ArrayList<>();
        for (SubCommand subCommand : getUsableSubCommands(player)) {
            subCommands.add(subCommand.subCommand);
        }
        return subCommands;
    }

    public TownCommand() {
        subCommands.addAll(Arrays.asList(
                // Sub commands
                new TownBioSubCommand(),
                new TownCreateSubCommand(),
                new TownDepositSubCommand(),
                new TownDisbandSubCommand(),
                new TownFarewellSubCommand(),
                new TownFlagSubCommand(),
                new TownGreetingSubCommand(),
                new TownHelpSubCommand(),
                new TownInfoSubCommand(),
                new TownLeaveSubCommand(),
                new TownPlayerSubCommand(),
                new TownPublicSpawnSubCommand(),
                new TownRenameSubCommand(),
                new TownSetSpawnSubCommand(),
                new TownSettingsSubCommand(),
                new TownSpawnSubCommand(),

                // Shortcut commands
                new ShortcutTownSubCommand("autoclaim", "autoclaim", "", "ac"),
                new ShortcutTownSubCommand("claim", "claim", "info", "c"),
                new ShortcutTownSubCommand("claimlist", "claimlist", "[town]", "claims"),
                new ShortcutTownSubCommand("demote", "demote", "<town_member>"),
                new ShortcutTownSubCommand("promote", "promote", "<town_member>"),
                new ShortcutTownSubCommand("farm", "farm", ""),
                new ShortcutTownSubCommand("plot", "plot", "<assign/claim/remove/set/trust/unclaim/untrust>"),
                new ShortcutTownSubCommand("list", "townlist", "[sort_by]"),
                new ShortcutTownSubCommand("map", "map", "", "m"),
                new ShortcutTownSubCommand("transfer", "transfer", "<town_member>", "setowner"),
                new ShortcutTownSubCommand("chat", "townchat", "[message]", "c", "townchat"),
                new ShortcutTownSubCommand("kick", "evict", "<town_member>", "evict")));

    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length >= 1) {
            for (SubCommand subCommand : subCommands) {
                if (subCommand.matchesInput(args[0])) {
                    subCommand.onCommand(player, args);
                    return;
                }
            }
            MessageManager.sendMessage(player, "error_invalid_subcommand", label);
        } else {
            new TownHelpSubCommand().onCommand(player, args);
        }
    }

    public static class TownTab implements TabCompleter {

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
            if (args.length == 0 || args.length == 1) {
                final List<String> tabCompletions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[0], getUsableSubCommandStrings((Player) sender), tabCompletions);
                Collections.sort(tabCompletions);
                return tabCompletions;
            } else {
                SubCommand currentCommand = null;
                for (TownSubCommand subCommand : getUsableSubCommands((Player) sender)) {
                    if (subCommand.matchesInput(args[0])) {
                        currentCommand = subCommand;
                    }
                }
                if (currentCommand != null) {
                    int currentSubArgIndex = args.length - 2;
                    if (!currentCommand.getUsage().isEmpty()) {
                        String[] subCommandArgs = currentCommand.getUsage().split(" ");
                        if (currentSubArgIndex < subCommandArgs.length - 2) {
                            String currentSubArg = subCommandArgs[currentSubArgIndex + 2];
                            final List<String> completionOptions = switch (currentSubArg) {
                                case "<town>", "[town]", "<town_name>", "[town_name]" ->
                                        new ArrayList<>(HuskTowns.getTownDataCache().getPublicSpawnTowns());
                                case "<town_member>, [town_member]" ->
                                        new ArrayList<>(HuskTowns.getPlayerCache().getPlayersInTown(HuskTowns.getPlayerCache().getPlayerTown(((Player) sender).getUniqueId())));
                                case "<player>", "[player]", "<player/accept/decline>" ->
                                        new ArrayList<>(HuskTowns.getPlayerList().getPlayers());
                                case "[sort_by]" -> getSortByTypes();
                                default -> Collections.singletonList(currentSubArg);
                            };
                            final List<String> tabCompletions = new ArrayList<>();
                            StringUtil.copyPartialMatches(args[args.length - 1], completionOptions, tabCompletions);
                            Collections.sort(tabCompletions);
                            return tabCompletions;
                        }
                    }
                }
                return Collections.emptyList();
            }
        }

        private List<String> getSortByTypes() {
            final ArrayList<String> orderTypes = new ArrayList<>();
            for (TownListCommand.TownListOrderType orderType : TownListCommand.TownListOrderType.values()) {
                orderTypes.add(orderType.name().toLowerCase());
            }
            return orderTypes;
        }

    }
}