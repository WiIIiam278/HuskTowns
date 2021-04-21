package me.william278.bungeetowny.command;

import me.william278.bungeetowny.MessageManager;
import me.william278.bungeetowny.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.Locale;

public class TownCommand extends CommandBase {

    private static void showTownMenu() {

    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length >= 1) {
            switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "create":
                    if (args.length == 2) {
                        String townName = args[1];
                        DataManager.createTown(player, townName);
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town create <name>");
                    }
                    break;
                case "claim":
                    DataManager.claimChunk(player);
                    break;
                case "leave":
                    DataManager.leaveTown(player);
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
            MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
        }
    }

}
