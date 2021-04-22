package me.william278.bungeetowny.command;

import me.william278.bungeetowny.HuskTowns;
import me.william278.bungeetowny.data.DataManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class UnclaimCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        Location playerLocation = player.getLocation();
        DataManager.removeClaim(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                playerLocation.getChunk().getZ(), playerLocation.getWorld().getName()));
    }
}
