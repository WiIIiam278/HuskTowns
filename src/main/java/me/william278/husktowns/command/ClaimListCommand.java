package me.william278.husktowns.command;

import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class ClaimListCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        switch (args.length) {
            case 2:
                DataManager.showClaimList(player, args[1], 1);
                break;
            case 3:
                int pageNo;
                try {
                    pageNo = Integer.parseInt(args[2]);
                    DataManager.showClaimList(player, args[1], pageNo);
                } catch (NumberFormatException ex) {
                    MessageManager.sendMessage(player, "error_invalid_page_number");
                }
                break;
            default:
                DataManager.showClaimList(player, 1);
                break;
        }
    }
}
