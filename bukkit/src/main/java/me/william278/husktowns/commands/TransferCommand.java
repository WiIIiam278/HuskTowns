package me.william278.husktowns.commands;

import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class TransferCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].length() > 16) {
                MessageManager.sendMessage(player, "error_invalid_player");
                return;
            }
            MessageManager.sendMessage(player, "transfer_town_confirm", args[0]);
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("confirm")) {
                DataManager.transferTownOwnership(player, args[0]);
            } else {
                MessageManager.sendMessage(player, "error_invalid_syntax",
                        command.getUsage());
            }
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax",
                    command.getUsage());
        }
    }

}
