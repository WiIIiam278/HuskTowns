package net.william278.husktowns.command;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.MessageManager;
import net.william278.husktowns.data.DataManager;
import net.william278.husktowns.util.AutoClaimUtil;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class AdminTownCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "claim" -> player.performCommand("adminclaim");
                case "adminclaim", "unclaim", "delclaim", "abandonclaim", "townbonus", "ignoreclaims", "respectclaims" -> {
                    StringBuilder commandArgs = new StringBuilder();
                    for (String arg : args) {
                        commandArgs.append(arg).append(" ");
                    }
                    player.performCommand(commandArgs.toString());
                }
                case "autoclaim" -> {
                    if (!player.hasPermission("husktowns.administrator.claim")) {
                        MessageManager.sendMessage(player, "error_no_permission");
                        return;
                    }
                    if (AutoClaimUtil.isAutoClaiming(player)) {
                        AutoClaimUtil.removeAutoClaimer(player);
                        MessageManager.sendMessage(player, "auto_claim_toggle_off_admin");
                    } else {
                        AutoClaimUtil.addAutoClaimer(player, AutoClaimUtil.AutoClaimMode.ADMIN);
                        MessageManager.sendMessage(player, "auto_claim_toggle_on_admin");
                        DataManager.createAdminClaim(player, player.getLocation(), false);
                    }
                }
                case "claimlist" -> player.performCommand("claimlist " + HuskTowns.getSettings().adminTownName);
            }
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
        }
    }

    public static class AdminTownCommandTab extends SimpleTab {
        public AdminTownCommandTab() {
            commandTabArgs = new String[]{"claim", "unclaim", "autoclaim", "ignoreclaims", "townbonus", "claimlist"};
        }
    }

}
