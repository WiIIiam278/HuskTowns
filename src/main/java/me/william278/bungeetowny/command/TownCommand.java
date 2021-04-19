package me.william278.bungeetowny.command;

import me.william278.bungeetowny.MessageManager;
import me.william278.bungeetowny.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.Locale;

public class TownCommand extends CommandBase {

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
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
                    break;
            }
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
        }
    }

}
