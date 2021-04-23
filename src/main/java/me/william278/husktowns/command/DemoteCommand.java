package me.william278.husktowns.command;

import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class DemoteCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length == 1) {
            String playerName = args[0];
            DataManager.demotePlayer(player, playerName);
        } else {
            MessageManager.sendMessage(player,"error_invalid_syntax", command.getUsage());
        }
    }
}
