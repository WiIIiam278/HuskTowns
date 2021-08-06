package me.william278.husktowns.commands;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class UnClaimCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("all")) {
                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("confirm")) {
                        DataManager.deleteAllTownClaims(player);
                        return;
                    }
                }
                MessageManager.sendMessage(player, "unclaim_all_confirm");
            } else {
                MessageManager.sendMessage(player, "error_invalid_usage", command.getUsage());
            }
        } else {
            Location playerLocation = player.getLocation();
            DataManager.removeClaim(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                    playerLocation.getChunk().getZ(), playerLocation.getWorld().getName()));
        }
    }

    public static class UnClaimCommandTab extends SimpleTab {
        public UnClaimCommandTab() {
            commandTabArgs = new String[]{"all"};
        }
    }

}
