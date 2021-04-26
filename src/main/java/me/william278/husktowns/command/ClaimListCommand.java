package me.william278.husktowns.command;

import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class ClaimListCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        switch (args.length) {
            case 1:
                DataManager.showClaimList(player, args[0], 1);
                break;
            case 2:
                int pageNo;
                try {
                    pageNo = Integer.parseInt(args[1]);
                    DataManager.showClaimList(player, args[0], pageNo);
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
