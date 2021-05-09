package me.william278.husktowns.command;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.util.AutoClaimUtil;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class AutoClaimCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (AutoClaimUtil.isAutoClaiming(player)) {
            AutoClaimUtil.removeAutoClaimer(player);
            MessageManager.sendMessage(player, "auto_claim_toggle_off");
        } else {
            if (HuskTowns.getPlayerCache().getTown(player.getUniqueId()) == null) {
                MessageManager.sendMessage(player, "error_not_in_town");
                return;
            }
            AutoClaimUtil.addAutoClaimer(player);
            MessageManager.sendMessage(player, "auto_claim_toggle_on");
            DataManager.claimChunk(player);
        }
    }
}
