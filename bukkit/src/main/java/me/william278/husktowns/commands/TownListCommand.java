package me.william278.husktowns.commands;

import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.Locale;

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
                    case "name", "by_name" -> {}
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

    public static class TownListCommandTab extends SimpleTab {
        public TownListCommandTab() {
            commandTabArgs = new String[]{"oldest", "newest", "name", "level", "wealth"};
        }
    }
}
