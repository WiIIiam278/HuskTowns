package me.william278.husktowns.commands;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class IgnoreClaimsCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (HuskTowns.ignoreClaimPlayers.contains(player.getUniqueId())) {
            HuskTowns.ignoreClaimPlayers.remove(player.getUniqueId());
            MessageManager.sendMessage(player, "ignore_claim_toggled_off");
        } else {
            HuskTowns.ignoreClaimPlayers.add(player.getUniqueId());
            MessageManager.sendMessage(player, "ignore_claim_toggled_on");
        }
    }

}
