package me.william278.husktowns.commands;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.util.AutoClaimUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
                case "claimlist" -> player.performCommand("claimlist " + HuskTowns.getSettings().getAdminTownName());
            }
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
        }
    }

    public static class AdminTownCommandTab implements TabCompleter {

        final static String[] COMMAND_TAB_ARGS = {"claim", "unclaim", "autoclaim", "ignoreclaims", "townbonus", "claimlist"};

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            Player p = (Player) sender;
            if (command.getPermission() != null) {
                if (!p.hasPermission(command.getPermission())) {
                    return Collections.emptyList();
                }
            }
            if (args.length == 1) {
                final List<String> tabCompletions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMAND_TAB_ARGS), tabCompletions);
                Collections.sort(tabCompletions);
                return tabCompletions;
            } else {
                return Collections.emptyList();
            }
        }
    }

}
