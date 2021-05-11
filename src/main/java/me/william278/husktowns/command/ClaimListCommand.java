package me.william278.husktowns.command;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static class TownListTab implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            Player p = (Player) sender;
            if (command.getPermission() != null) {
                if (!p.hasPermission(command.getPermission())) {
                    return Collections.emptyList();
                }
            }
            if (args.length == 1) {
                if (HuskTowns.getPlayerCache().getTown(p.getUniqueId()) == null) {
                    return Collections.emptyList();
                }
                final List<String> arg1TabComp = new ArrayList<>();
                StringUtil.copyPartialMatches(args[0], HuskTowns.getPlayerCache().getTowns(), arg1TabComp);
                Collections.sort(arg1TabComp);
                return arg1TabComp;
            } else {
                return Collections.emptyList();
            }
        }

    }
}
