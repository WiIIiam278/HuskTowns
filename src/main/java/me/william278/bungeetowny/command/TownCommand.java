package me.william278.bungeetowny.command;

import me.william278.bungeetowny.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class TownCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length >= 1) {

        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
        }
    }

}
