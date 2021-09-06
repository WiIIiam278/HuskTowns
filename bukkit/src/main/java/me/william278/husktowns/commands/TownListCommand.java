package me.william278.husktowns.commands;

import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.town.Town;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class TownListCommand extends CommandBase {

    private static final HashMap<UUID, ArrayList<Town>> cachedTownLists = new HashMap<>();

    public static ArrayList<Town> getTownList(UUID uuid) {
        return cachedTownLists.get(uuid);
    }

    public static boolean townListsContains(UUID uuid) {
        return cachedTownLists.containsKey(uuid);
    }

    public static void addTownList(UUID uuid, ArrayList<Town> arrayList) {
        cachedTownLists.put(uuid, arrayList);
    }

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        int pageNumber = 1;
        TownListOrderType type = TownListOrderType.BY_NAME;
        boolean useCache = false;
        if (args.length < 1) {
            DataManager.sendTownList(player, type, pageNumber, false);
        } else {
            switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "oldest", "by_oldest" -> type = TownListOrderType.BY_OLDEST;
                case "newest", "by_newest" -> type = TownListOrderType.BY_NEWEST;
                case "level", "by_level" -> type = TownListOrderType.BY_LEVEL;
                case "wealth", "by_wealth" -> type = TownListOrderType.BY_WEALTH;
                case "name", "by_name" -> {
                }
                default -> {
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
                    return;
                }
            }
            int argIndex = 1;
            if (args.length >= 2) {
                if (args[argIndex].equalsIgnoreCase("-c")) {
                    useCache = true;
                    argIndex++;
                }
                if (args.length >= (argIndex + 1)) {
                    try {
                        pageNumber = Integer.parseInt(args[argIndex]);
                        if (pageNumber < 0) {
                            MessageManager.sendMessage(player, "error_invalid_page_number");
                            return;
                        }
                    } catch (NumberFormatException exception) {
                        MessageManager.sendMessage(player, "error_invalid_page_number");
                        return;
                    }
                }
            }
            if (useCache) {
                if (!townListsContains(player.getUniqueId())) {
                    useCache = false;
                }
            }
            DataManager.sendTownList(player, type, pageNumber, useCache);
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
