package me.william278.husktowns.command;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.town.TownBonus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.time.Instant;
import java.util.*;

public class TownBonusCommand extends CommandBase implements TabCompleter {

    // /townbonus <add|clear> <town> [bonus claims] [bonus members]

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 2) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "add":
                    if (args.length != 4) {
                        MessageManager.sendMessage(sender, "error_invalid_syntax", "/townbonus add [bonus claims] [bonus members]");
                        return true;
                    }
                    try {
                        String townName = args[1];
                        int extraClaims = Integer.parseInt(args[2]);
                        int extraMembers = Integer.parseInt(args[3]);
                        UUID applierID;
                        if (sender instanceof Player) {
                            applierID = ((Player) sender).getUniqueId();
                        } else {
                            applierID = null;
                        }
                        TownBonus bonus = new TownBonus(applierID, extraClaims,
                                extraMembers, Instant.now().getEpochSecond());
                        DataManager.addTownBonus(sender, townName, bonus);
                    } catch (NumberFormatException exception) {
                        MessageManager.sendMessage(sender, "error_invalid_syntax", "/townbonus add <town> <bonus claims> <bonus members>");
                    }
                    return true;
                case "clear":
                    DataManager.clearTownBonuses(sender, args[1]);
                    return true;
                default:
                    MessageManager.sendMessage(sender, "error_invalid_syntax", command.getUsage());
                    return true;
            }
        } else {
            MessageManager.sendMessage(sender, "error_invalid_syntax", command.getUsage());
        }
        return true;
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) { }

    final static String[] COMMAND_TAB_ARGS = {"add", "clear"};

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
        } else if (args.length == 2) {
            if (HuskTowns.getPlayerCache().getTown(p.getUniqueId()) == null) {
                return Collections.emptyList();
            }
            final List<String> arg1TabComp = new ArrayList<>();
            StringUtil.copyPartialMatches(args[1], HuskTowns.getPlayerCache().getTowns(), arg1TabComp);
            Collections.sort(arg1TabComp);
            return arg1TabComp;
        } else {
            return Collections.emptyList();
        }
    }
}
