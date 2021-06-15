package me.william278.husktowns.commands;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.data.DataManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class UnClaimCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        Location playerLocation = player.getLocation();
        DataManager.removeClaim(player, HuskTowns.getClaimCache().getChunkAt(playerLocation.getChunk().getX(),
                playerLocation.getChunk().getZ(), playerLocation.getWorld().getName()));
    }
}
