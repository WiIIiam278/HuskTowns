package net.william278.husktowns.commands;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.MessageManager;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.cache.ClaimCache;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class FarmCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        Location playerLocation = player.getLocation();
        final ClaimCache claimCache = HuskTowns.getClaimCache();
        if (!claimCache.hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", claimCache.getName());
            return;
        }
        DataManager.changeToFarm(player, claimCache.getChunkAt(playerLocation.getChunk().getX(),
                playerLocation.getChunk().getZ(), playerLocation.getWorld().getName()));
    }
}
