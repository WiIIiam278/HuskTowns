package me.william278.husktowns.command;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.Locale;

public class TownCommand extends CommandBase {

    private static final HuskTowns plugin = HuskTowns.getInstance();

    //todo Commands: change town name, update farewell & welcome messages, set spawn,
    // return to spawn, deposit and view menu, set, claim and evict plot chunk, set farm chunk, town leaderboard list
    // also todo set limits for home names

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
                case "claim":
                    player.performCommand("claim");
                    break;
                case "leave":
                    DataManager.leaveTown(player);
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
                    StringBuilder claimsCmdArgs = new StringBuilder();
                    for (String arg : args) {
                        claimsCmdArgs.append(arg).append(" ");
                    }
                    player.performCommand(claimsCmdArgs.toString());
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
