package me.william278.husktowns.commands;

import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class TownListCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        int pageNumber = 1;
        TownListOrderType type = TownListOrderType.BY_NAME;
        switch (args.length) {
            case 2:
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
            case 1:
                switch (args[0].toLowerCase(Locale.ENGLISH)) {
                    case "oldest", "by_oldest" -> type = TownListOrderType.BY_OLDEST;
                    case "newest", "by_newest" -> type = TownListOrderType.BY_NEWEST;
                    case "level", "by_level" -> type = TownListOrderType.BY_LEVEL;
                    case "wealth", "by_wealth" -> type = TownListOrderType.BY_WEALTH;
                    case "name", "by_name" -> type = TownListOrderType.BY_NAME;
                    default -> {
                        MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
                        return;
                    }
                }
            default:
                DataManager.sendTownList(player, type, pageNumber);
                break;
        }
    }

    public enum TownListOrderType {
        BY_NAME,
        BY_NEWEST,
        BY_OLDEST,
        BY_LEVEL,
        BY_WEALTH,
    }

    public static class TownListCommandTab implements TabCompleter {

        final static String[] COMMAND_TAB_ARGS = {"oldest", "newest", "name", "level", "wealth"};

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
