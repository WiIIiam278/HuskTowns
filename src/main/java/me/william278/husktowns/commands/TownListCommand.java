package me.william278.husktowns.commands;

import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.TownListOrderType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class TownListCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length == 1) {
            switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "oldest":
                case "by_oldest":
                    DataManager.sendTownList(player, TownListOrderType.BY_OLDEST, 1);
                    return;
                case "newest":
                case "by_newest":
                    DataManager.sendTownList(player, TownListOrderType.BY_NEWEST, 1);
                    return;
                case "level":
                case "by_level":
                    DataManager.sendTownList(player, TownListOrderType.BY_LEVEL, 1);
                    return;
                case "name":
                case "by_name":
                    DataManager.sendTownList(player, TownListOrderType.BY_NAME, 1);
                    return;
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
            }
        } else if (args.length == 2) {
            int pageNumber;
            try {
                pageNumber = Integer.parseInt(args[1]);
                if (pageNumber < 0) {
                    MessageManager.sendMessage(player, "error_invalid_page_number");
                    return;
                }
            } catch (NumberFormatException exception) {
                MessageManager.sendMessage(player, "error_invalid_page_number");
                return;
            }
            switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "oldest":
                case "by_oldest":
                    DataManager.sendTownList(player, TownListOrderType.BY_OLDEST, pageNumber);
                    return;
                case "newest":
                case "by_newest":
                    DataManager.sendTownList(player, TownListOrderType.BY_NEWEST, pageNumber);
                    return;
                case "level":
                case "by_level":
                    DataManager.sendTownList(player, TownListOrderType.BY_LEVEL, pageNumber);
                    return;
                case "name":
                case "by_name":
                    DataManager.sendTownList(player, TownListOrderType.BY_NAME, pageNumber);
                    return;
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
            }
        } else {
            DataManager.sendTownList(player, TownListOrderType.BY_NAME, 1);
        }
    }

    public static class TownListCommandTab implements TabCompleter {

        final static String[] COMMAND_TAB_ARGS = {"oldest", "newest", "name", "level"};

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
