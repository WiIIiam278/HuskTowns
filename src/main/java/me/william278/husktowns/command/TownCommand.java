package me.william278.husktowns.command;

import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.Locale;

public class TownCommand extends CommandBase {

    //todo Set spawn, return to spawn,
    // view a menu, costs for subcommands
    // town leaderboard list, farm chunk functionality
    // then Tab completion for every bloody command
    // Vault integration

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length >= 1) {
            switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "create":
                case "found":
                    if (args.length == 2) {
                        String townName = args[1];
                        DataManager.createTown(player, townName);
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town create <name>");
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
                    if (args.length == 2) {
                        String townName = args[1];
                        DataManager.renameTown(player, townName);
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town rename <new name>");
                    }
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
                            description.append(args[i-1]).append(" ");
                        }

                        DataManager.updateTownGreeting(player, description.toString().trim());
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town greeting <new message>");
                    }
                    break;
                case "farewell":
                    if (args.length == 2) {
                        StringBuilder description = new StringBuilder();
                        for (int i = 2; i <= args.length; i++) {
                            description.append(args[i-1]).append(" ");
                        }

                        DataManager.updateTownFarewell(player, description.toString().trim());
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town farewell <new message>");
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
                case "claimlist":
                case "claimslist":
                case "invite":
                case "add":
                case "map":
                case "evict":
                case "claim":
                case "plot":
                case "transfer":
                    StringBuilder commandArgs = new StringBuilder();
                    for (String arg : args) {
                        commandArgs.append(arg).append(" ");
                    }
                    player.performCommand(commandArgs.toString());
                    break;
                case "disband":
                    if (args.length == 1) {
                        MessageManager.sendMessage(player, "disband_town_confirm");
                    } else if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("confirm")) {
                            DataManager.disbandTown(player);
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town disband [confirm]");
                        }
                    }
                    break;
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
                    break;
            }
        } else {
            DataManager.showTownMenu(player);
        }
    }

}
