package me.william278.husktowns.command;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class PlotCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        Location playerLocation = player.getLocation();
        if (args.length >= 1) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "set":
                case "unset":
                case "remove":
                case "delete":
                    DataManager.makePlot(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                            playerLocation.getChunk().getZ(), playerLocation.getWorld().getName()));
                    return;
                case "claim":
                    DataManager.claimPlot(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                            playerLocation.getChunk().getZ(), playerLocation.getWorld().getName()));
                    return;
                case "unclaim":
                case "abandon":
                case "evict":
                case "clear":
                case "unassign":
                    DataManager.unClaimPlot(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                            playerLocation.getChunk().getZ(), playerLocation.getWorld().getName()));
                    return;
                case "assign":
                    if (args.length == 2) {
                        DataManager.assignPlotPlayer(player, args[1], HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                                playerLocation.getChunk().getZ(), playerLocation.getWorld().getName()));
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/plot assign <player>");
                    }
                    return;
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
            }
        } else {
            DataManager.makePlot(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                    playerLocation.getChunk().getZ(), playerLocation.getWorld().getName()));
        }
    }

    public static class PlotTab implements TabCompleter {

        final static String[] COMMAND_TAB_ARGS = {"set", "remove", "claim", "unclaim", "assign"};

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            Player p = (Player) sender;
            if (command.getPermission() != null) {
                if (!p.hasPermission(command.getPermission())) {
                    return Collections.emptyList();
                }
            }
            switch (args.length) {
                case 1:
                    final List<String> tabCompletions = new ArrayList<>();
                    StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMAND_TAB_ARGS), tabCompletions);
                    Collections.sort(tabCompletions);
                    return tabCompletions;
                case 2:
                    if (HuskTowns.getPlayerCache().getTown(p.getUniqueId()) == null) {
                        return Collections.emptyList();
                    }
                    if ("assign".equals(args[0].toLowerCase(Locale.ENGLISH))) {
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
                default:
                    return Collections.emptyList();
            }

        }
    }
}