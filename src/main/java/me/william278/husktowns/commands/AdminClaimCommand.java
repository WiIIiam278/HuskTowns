package me.william278.husktowns.commands;

import me.william278.husktowns.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class AdminClaimCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        DataManager.createAdminClaim(player, player.getLocation(), false);
    }
}
