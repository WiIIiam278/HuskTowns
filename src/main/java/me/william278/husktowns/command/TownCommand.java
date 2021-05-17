package me.william278.husktowns.command;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.town.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class TownCommand extends CommandBase {

    //todo better organise town menus,

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length >= 1) {
            switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "create":
                case "found":
                    if (player.hasPermission("husktowns.create_town")) {
                        if (args.length == 2) {
                            String townName = args[1];
                            DataManager.createTown(player, townName);
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town create <name>");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "deposit":
                    if (args.length == 2) {
                        try {
                            double amountToDeposit = Double.parseDouble(args[1]);
                            DataManager.depositMoney(player, amountToDeposit);
                        } catch (NumberFormatException ex) {
                            MessageManager.sendMessage(player, "error_invalid_amount");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town deposit <amount>");
                    }
                    break;
                case "leave":
                    DataManager.leaveTown(player);
                    break;
                case "rename":
                    if (!player.hasPermission("husktowns.command.town.rename")) {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    if (args.length == 2) {
                        String townName = args[1];
                        DataManager.renameTown(player, townName);
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town rename <new name>");
                    }
                    break;
                case "setspawn":
                    DataManager.updateTownSpawn(player);
                    break;
                case "spawn":
                    DataManager.teleportPlayerToSpawn(player);
                    break;
                case "info":
                case "view":
                case "about":
                case "check":
                    if (args.length == 2) {
                        DataManager.showTownMenu(player, args[1]);
                    } else {
                        DataManager.showTownMenu(player);
                    }
                    break;
                case "greeting":
                    if (args.length >= 2) {
                        StringBuilder description = new StringBuilder();
                        for (int i = 2; i <= args.length; i++) {
                            description.append(args[i - 1]).append(" ");
                        }

                        DataManager.updateTownGreeting(player, description.toString().trim());
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town greeting <new message>");
                    }
                    break;
                case "farewell":
                    if (args.length >= 2) {
                        StringBuilder description = new StringBuilder();
                        for (int i = 2; i <= args.length; i++) {
                            description.append(args[i - 1]).append(" ");
                        }

                        DataManager.updateTownFarewell(player, description.toString().trim());
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town farewell <new message>");
                    }
                    break;
                case "chat":
                    if (args.length >= 2) {
                        StringBuilder description = new StringBuilder();
                        for (int i = 2; i <= args.length; i++) {
                            description.append(args[i - 1]).append(" ");
                        }

                        player.performCommand("townchat " + description.toString().trim());
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town chat <message>");
                    }
                    break;
                case "list":
                    if (args.length == 2) {
                        player.performCommand("townlist " + args[1]);
                    } else {
                        player.performCommand("townlist");
                    }
                    break;
                case "kick":
                    if (args.length == 2) {
                        player.performCommand("evict " + args[1]);
                    } else {
                        player.performCommand("evict");
                    }
                    break;
                case "claims":
                case "autoclaim":
                case "claimlist":
                case "claimslist":
                case "invite":
                case "map":
                case "evict":
                case "promote":
                case "demote":
                case "trust":
                case "untrust":
                case "claim":
                case "unclaim":
                case "abandonclaim":
                case "plot":
                case "farm":
                case "transfer":
                    StringBuilder commandArgs = new StringBuilder();
                    for (String arg : args) {
                        commandArgs.append(arg).append(" ");
                    }
                    player.performCommand(commandArgs.toString());
                    break;
                case "disband":
                case "delete":
                    if (args.length == 1) {
                        if (HuskTowns.getPlayerCache().getTown(player.getUniqueId()) != null) {
                            if (HuskTowns.getPlayerCache().getRole(player.getUniqueId()) == TownRole.MAYOR) {
                                MessageManager.sendMessage(player, "disband_town_confirm");
                            } else {
                                MessageManager.sendMessage(player, "error_insufficient_disband_privileges");
                            }
                        } else {
                            MessageManager.sendMessage(player, "error_not_in_town");
                        }
                    } else if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("confirm")) {
                            DataManager.disbandTown(player);
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town disband [confirm]");
                        }
                    }
                    break;
                case "help":
                    if (args.length == 2) {
                        try {
                            int pageNo = Integer.parseInt(args[1]);
                            HuskTownsCommand.showHelpMenu(player, pageNo);
                        } catch (NumberFormatException ex) {
                            MessageManager.sendMessage(player, "error_invalid_page_number");
                        }
                    } else {
                        HuskTownsCommand.showHelpMenu(player, 1);
                    }
                    return;
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
                    break;
            }
        } else {
            DataManager.showTownMenu(player);
        }
    }

    public static class TownTab implements TabCompleter {

        final static String[] COMMAND_TAB_ARGS = {"create", "deposit", "leave", "rename", "setspawn",
                "spawn", "info", "greeting", "farewell", "kick", "claims", "promote", "demote",
                "claim", "unclaim", "plot", "farm", "map", "transfer", "disband", "list", "help"};


        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            Player p = (Player) sender;
            if (command.getPermission() != null) {
                if (!p.hasPermission(command.getPermission())) {
                    return Collections.emptyList();
                }
            }
            switch (args.length) {
                case 1:
                    final List<String> tabCompletions = new ArrayList<>();
                    StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMAND_TAB_ARGS), tabCompletions);
                    Collections.sort(tabCompletions);
                    return tabCompletions;
                case 2:
                    if (HuskTowns.getPlayerCache().getTown(p.getUniqueId()) == null) {
                        return Collections.emptyList();
                    }
                    switch (args[0].toLowerCase(Locale.ENGLISH)) {
                        case "kick":
                        case "evict":
                        case "promote":
                        case "demote":
                        case "trust":
                        case "untrust":
                        case "transfer":
                            final List<String> playerListTabCom = new ArrayList<>();
                            StringUtil.copyPartialMatches(args[1], HuskTowns.getPlayerCache().getPlayersInTown(HuskTowns.getPlayerCache().getTown(p.getUniqueId())), playerListTabCom);
                            Collections.sort(playerListTabCom);
                            return playerListTabCom;
                        case "info":
                        case "about":
                        case "view":
                        case "check":
                            final List<String> townListTabCom = new ArrayList<>();
                            StringUtil.copyPartialMatches(args[1], HuskTowns.getPlayerCache().getTowns(), townListTabCom);
                            Collections.sort(townListTabCom);
                            return townListTabCom;
                        case "invite":
                            final List<String> inviteTabCom = new ArrayList<>();
                            final ArrayList<String> players = new ArrayList<>();
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                players.add(player.getName());
                            }
                            StringUtil.copyPartialMatches(args[1], players, inviteTabCom);
                            Collections.sort(inviteTabCom);
                            return inviteTabCom;
                        default:
                            return Collections.emptyList();
                    }
                default:
                    return Collections.emptyList();
            }

        }

    }
}