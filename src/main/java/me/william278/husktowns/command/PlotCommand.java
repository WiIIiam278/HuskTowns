package me.william278.husktowns.command;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.Locale;

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
}