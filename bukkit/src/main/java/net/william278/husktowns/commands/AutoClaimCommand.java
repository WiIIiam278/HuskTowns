package net.william278.husktowns.commands;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.MessageManager;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.util.AutoClaimUtil;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class AutoClaimCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (!HuskTowns.getPlayerCache().hasLoaded()) {
            MessageManager.sendMessage(player, "error_cache_updating", "Player Data");
            return;
        }
        if (AutoClaimUtil.isAutoClaiming(player)) {
            AutoClaimUtil.removeAutoClaimer(player);
            MessageManager.sendMessage(player, "auto_claim_toggle_off");
        } else {
            if (!HuskTowns.getPlayerCache().isPlayerInTown(player.getUniqueId())) {
                MessageManager.sendMessage(player, "error_not_in_town");
                return;
            }
            AutoClaimUtil.addAutoClaimer(player, AutoClaimUtil.AutoClaimMode.REGULAR);
            MessageManager.sendMessage(player, "auto_claim_toggle_on");
            DataManager.claimChunk(player, player.getLocation(), false);
        }
    }
}
