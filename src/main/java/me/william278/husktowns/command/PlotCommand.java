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
                case "remove":
                case "delete":
                    DataManager.makePlot(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                            playerLocation.getChunk().getZ(), playerLocation.getWorld().getName()));
                    return;
                case "claim":
                    return;
                case "unclaim":
                case "abandon":
                    return;
                case "assign":
                    return;
                case "evict":
                case "clear":
                case "unassign":
                    return;
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
                    return;
            }
        } else {
            DataManager.makePlot(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                    playerLocation.getChunk().getZ(), playerLocation.getWorld().getName()));
        }
    }
}
