package me.william278.husktowns.commands;

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
import java.util.HashSet;
import java.util.List;

public class PromoteCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length == 1) {
            String playerName = args[0];
            DataManager.promotePlayer(player, playerName);
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
        }
    }

    public static class TownMemberTab implements TabCompleter {

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
                final List<String> playerListTabCom = new ArrayList<>();
                HashSet<String> playersInTown = HuskTowns.getPlayerCache().getPlayersInTown(HuskTowns.getPlayerCache().getTown(p.getUniqueId()));
                if (playersInTown.isEmpty()) {
                    return Collections.emptyList();
                }
                StringUtil.copyPartialMatches(args[0], playersInTown, playerListTabCom);
                Collections.sort(playerListTabCom);
                return playerListTabCom;
            } else {
                return Collections.emptyList();
            }
        }
    }
}